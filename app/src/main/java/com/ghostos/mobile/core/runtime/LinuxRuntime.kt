package com.ghostos.mobile.core.runtime

import android.content.Context
import id.or.oo.pr.engine.ProotLauncher
import id.or.oo.pr.session.PrCliCommand
import com.ghostos.mobile.GhostApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

interface LinuxRuntime {
    suspend fun installDistribution(distribution: Distribution, onProgress: (Int, String) -> Unit)
    suspend fun startSession(): RuntimeSession
    suspend fun stop()
    fun status(): RuntimeStatus
}

enum class Distribution { DEBIAN_STABLE }
enum class RuntimeStatus { NOT_INSTALLED, INSTALLING, READY, RUNNING, FAILED }

/** PRoot-backed Debian runtime. All guest execution stays inside the app-private prefix. */
class GhostProotRuntime(context: Context) : LinuxRuntime {
    private val host = context.applicationContext as GhostApplication
    private val launcher = ProotLauncher(host)
    private val cli = PrCliCommand(host)
    private val activeSession = AtomicReference<ProotLauncher.Session?>(null)

    // pr-cli's current OCI installer stores images under containers/<name>/rootfs.
    // Keep the legacy path too so installations made by older pr-cli layouts remain usable.
    private val legacyRootfs = File(host.prefixDir, "var/lib/proot/installed-roots/debian")
    private val ociRootfs = File(host.prefixDir, "var/lib/proot/containers/debian/rootfs")

    private fun hasInstalledRootfs(): Boolean =
        legacyRootfs.isDirectory || ociRootfs.isDirectory

    override suspend fun installDistribution(distribution: Distribution, onProgress: (Int, String) -> Unit) {
        require(distribution == Distribution.DEBIAN_STABLE) { "Only Debian stable is enabled in this alpha." }
        withContext(Dispatchers.IO) {
            onProgress(0, "Preparing private Linux environment")
            val exit = cli.run(listOf("install", "debian"), onLine = { line -> onProgress(5, line) })
            onProgress(95, "Validating Debian root filesystem")
            val legacyExists = legacyRootfs.isDirectory
            val ociExists = ociRootfs.isDirectory
            check(exit == 0 && (legacyExists || ociExists)) {
                "Debian installation validation failed: pr-cli exit=$exit; " +
                    "legacy rootfs exists=$legacyExists at ${legacyRootfs.absolutePath}; " +
                    "OCI rootfs exists=$ociExists at ${ociRootfs.absolutePath}"
            }
            onProgress(100, "Debian stable installed")
        }
    }

    override suspend fun startSession(): RuntimeSession = withContext(Dispatchers.IO) {
        check(hasInstalledRootfs()) { "Debian is not installed. Install it first." }
        check(activeSession.get() == null) { "A Linux terminal session is already active." }
        val session = launcher.startSession("debian") ?: error("Could not start the Debian PTY session")
        check(activeSession.compareAndSet(null, session)) {
            session.close()
            "Another Linux session started concurrently."
        }
        RuntimeSession(UUID.randomUUID().toString(), "/", session) { activeSession.compareAndSet(session, null) }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            activeSession.getAndSet(null)?.close()
            Unit
        }
    }

    override fun status(): RuntimeStatus = when {
        activeSession.get() != null -> RuntimeStatus.RUNNING
        hasInstalledRootfs() -> RuntimeStatus.READY
        else -> RuntimeStatus.NOT_INSTALLED
    }
}

class RuntimeSession internal constructor(
    val id: String,
    val workingDirectory: String,
    private val delegate: ProotLauncher.Session,
    private val onClose: () -> Unit,
) : AutoCloseable {
    fun read(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): Int {
        val count = delegate.read(buffer, offset, length)
        if (count < 0) close()
        return count
    }

    fun write(bytes: ByteArray): Int = delegate.write(bytes)
    fun write(text: String): Int = write(text.toByteArray(Charsets.UTF_8))
    fun resize(rows: Int, columns: Int): Int = delegate.resize(rows, columns)
    val exitCode: Int? get() = delegate.exitCode

    override fun close() {
        delegate.close()
        onClose()
    }
}
