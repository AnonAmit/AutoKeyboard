package com.autokeyboard.data.provider.local

import android.content.Context
import com.autokeyboard.data.model.DownloadProgress
import com.autokeyboard.data.model.LocalModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages download, deletion, and storage of on-device AI models.
 * Models are stored in app-private external files directory.
 */
@Singleton
class LocalModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelsDir: File
        get() = File(context.getExternalFilesDir(null), "models").also { it.mkdirs() }

    /**
     * Downloads a model file with progress reporting.
     * Supports resume on failure via Range header.
     */
    fun downloadModel(model: LocalModel): Flow<DownloadProgress> = flow {
        val targetFile = File(modelsDir, "${model.id}.bin")
        var downloaded = if (targetFile.exists()) targetFile.length() else 0L

        try {
            val connection = URL(model.downloadUrl).openConnection() as HttpURLConnection
            if (downloaded > 0) {
                connection.setRequestProperty("Range", "bytes=$downloaded-")
            }
            connection.connect()

            val totalBytes = if (downloaded > 0) {
                downloaded + connection.contentLength
            } else {
                connection.contentLength.toLong()
            }

            emit(DownloadProgress(model.id, downloaded, totalBytes))

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(targetFile, downloaded > 0)

            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloaded += bytesRead
                emit(DownloadProgress(model.id, downloaded, totalBytes))
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()

            emit(DownloadProgress(model.id, downloaded, totalBytes, isComplete = true))
        } catch (e: Exception) {
            emit(DownloadProgress(model.id, downloaded, 0, error = e.message))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Deletes a downloaded model.
     */
    suspend fun deleteModel(model: LocalModel) = withContext(Dispatchers.IO) {
        File(modelsDir, "${model.id}.bin").delete()
    }

    /**
     * Checks if a model has been fully downloaded.
     */
    fun isModelDownloaded(model: LocalModel): Boolean {
        val file = File(modelsDir, "${model.id}.bin")
        return file.exists() && file.length() > 0
    }

    /**
     * Returns the absolute path to a downloaded model file.
     */
    fun getModelPath(model: LocalModel): String {
        return File(modelsDir, "${model.id}.bin").absolutePath
    }

    /**
     * Returns available storage in GB.
     */
    fun getAvailableStorageGB(): Float {
        val stat = android.os.StatFs(modelsDir.absolutePath)
        return stat.availableBytes / (1024f * 1024f * 1024f)
    }

    /**
     * Returns the RAM required for a model in MB.
     */
    fun estimateRAMRequired(model: LocalModel): Int {
        return model.ramRequiredMB
    }

    /**
     * Returns total storage used by downloaded models in GB.
     */
    fun getUsedStorageGB(): Float {
        val totalBytes = modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
        return totalBytes / (1024f * 1024f * 1024f)
    }

    /**
     * Lists all downloaded model files.
     */
    fun getDownloadedModelIds(): List<String> {
        return modelsDir.listFiles()
            ?.filter { it.extension == "bin" && it.length() > 0 }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
}
