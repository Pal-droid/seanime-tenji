package expo.modules.seanimeserver

import android.content.Context
import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.promise.Promise
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class ExpoSeanimeServerModule : Module() {
    private val TAG = "ExpoSeanimeServer"
    private var binaryProcess: Process? = null
    private val isRunning = AtomicBoolean(false)
    private val context: Context
        get() = appContext.reactContext ?: throw Exception("React context not available")

    override fun definition() = ModuleDefinition {
        Name("ExpoSeanimeServer")

        AsyncFunction("startServer") { promise: Promise ->
            try {
                if (isRunning.get()) {
                    promise.resolve(mapOf("success" to true, "message" to "Server already running"))
                    return@AsyncFunction
                }

                val binaryFile = prepareBinary()
                if (binaryFile == null) {
                    promise.reject("BINARY_PREP_FAILED", "Failed to prepare binary")
                    return@AsyncFunction
                }

                startBinary(binaryFile)
                isRunning.set(true)
                promise.resolve(mapOf("success" to true, "message" to "Server started"))
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server", e)
                promise.reject("START_ERROR", e.message)
            }
        }

        AsyncFunction("stopServer") { promise: Promise ->
            try {
                stopBinary()
                isRunning.set(false)
                promise.resolve(mapOf("success" to true, "message" to "Server stopped"))
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server", e)
                promise.reject("STOP_ERROR", e.message)
            }
        }

        AsyncFunction("isServerRunning") {
            isRunning.get()
        }

        AsyncFunction("getServerStatus") {
            mapOf(
                "running" to isRunning.get(),
                "processId" to (binaryProcess?.pid() ?: -1)
            )
        }
    }

    private fun prepareBinary(): File? {
        return try {
            val filesDir = context.filesDir
            val targetBinary = File(filesDir, "libseanime.so")

            // Copy from native lib directory
            val nativeLibDir = context.applicationInfo.nativeLibraryDir
            val sourceBinary = File(nativeLibDir, "libseanime.so")

            if (!sourceBinary.exists()) {
                Log.e(TAG, "Source binary not found in native lib dir")
                return null
            }

            // Only copy if it doesn't exist or size differs
            if (!targetBinary.exists() || sourceBinary.length() != targetBinary.length()) {
                Log.d(TAG, "Copying binary from $sourceBinary to $targetBinary")
                sourceBinary.copyTo(targetBinary, overwrite = true)
                targetBinary.setExecutable(true)
            }

            // Also copy libc++_shared.so if it exists
            val sourceCpp = File(nativeLibDir, "libc++_shared.so")
            if (sourceCpp.exists()) {
                val targetCpp = File(filesDir, "libc++_shared.so")
                if (!targetCpp.exists() || sourceCpp.length() != targetCpp.length()) {
                    sourceCpp.copyTo(targetCpp, overwrite = true)
                }
            }

            targetBinary
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing binary", e)
            null
        }
    }

    private fun startBinary(binaryFile: File) {
        try {
            val filesDir = context.filesDir
            val linker = "/system/bin/linker64"

            val pb = ProcessBuilder(linker, binaryFile.absolutePath)
                .directory(filesDir)
                .redirectErrorStream(true)

            pb.environment().apply {
                put("LD_LIBRARY_PATH", filesDir.absolutePath)
            }

            binaryProcess = pb.start()

            // Log output in background
            Thread {
                binaryProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    Log.d(TAG, line)
                }
            }.start()

            Log.d(TAG, "Binary process started with PID: ${binaryProcess?.pid()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting binary", e)
            throw e
        }
    }

    private fun stopBinary() {
        try {
            binaryProcess?.destroy()
            binaryProcess?.waitFor()
            binaryProcess = null
            Log.d(TAG, "Binary process stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping binary", e)
        }
    }
}
