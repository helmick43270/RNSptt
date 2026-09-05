package com.example.rnsptt

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.File
import java.io.FileOutputStream

class RustBinaryService : Service() {

    private var rustProcess: Process? = null

    override fun onCreate() {
        super.onCreate()
        prepareBinary()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun prepareBinary() {
        val destFile = File(filesDir, "tcp_server")
        if (!destFile.exists()) {
            assets.open("tcp_server").use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.setExecutable(true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runBinary()
        return START_STICKY
    }
    private fun runBinary() {
        Thread {
            try {
                rustProcess = ProcessBuilder(File(filesDir, "tcp_server").absolutePath)
                    .directory(filesDir)
                    .start()
                rustProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    android.util.Log.d("RustBinary", line)
                }
                rustProcess?.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    override fun onDestroy() {
        rustProcess?.destroy()
        super.onDestroy()
    }
}
