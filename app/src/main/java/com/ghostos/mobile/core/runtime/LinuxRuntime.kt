package com.ghostos.mobile.core.runtime

/** Runtime boundary. A concrete engine must be integrated and verified before Linux is marked ready. */
interface LinuxRuntime {
    suspend fun installDistribution(distribution: Distribution, onProgress: (Int, String) -> Unit)
    suspend fun startSession(): RuntimeSession
    suspend fun stop()
    fun status(): RuntimeStatus
}

enum class Distribution { DEBIAN_STABLE, ALPINE }
enum class RuntimeStatus { NOT_INSTALLED, INSTALLING, READY, RUNNING, FAILED }
data class RuntimeSession(val id: String, val workingDirectory: String)
