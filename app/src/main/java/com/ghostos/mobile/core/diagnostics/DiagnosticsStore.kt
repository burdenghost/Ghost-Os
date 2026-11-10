package com.ghostos.mobile.core.diagnostics

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** App-private persistent diagnostics. Never writes outside Ghost OS storage. */
object DiagnosticsStore {
    private const val FILE_NAME = "ghost-diagnostics.log"
    private val lock = Any()
    private val formatter = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    }

    fun append(context: Context, message: String) {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        val entry = "${formatter.get().format(Date())}  $message\n"
        synchronized(lock) {
            runCatching {
                file.parentFile?.mkdirs()
                file.appendText(entry, Charsets.UTF_8)
            }
        }
    }

    fun read(context: Context): String {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        return synchronized(lock) {
            runCatching { if (file.isFile) file.readText(Charsets.UTF_8) else "" }.getOrDefault("")
        }
    }

    fun clear(context: Context) {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        synchronized(lock) { runCatching { file.writeText("", Charsets.UTF_8) } }
    }
}
