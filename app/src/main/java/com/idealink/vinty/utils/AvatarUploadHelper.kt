package com.idealink.vinty.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

/**
 * Utility class for handling avatar image uploads
 */
object AvatarUploadHelper {

    private const val TAG = "AvatarUploadHelper"
    private const val JPEG_QUALITY = 80
    private const val MAX_IMAGE_SIZE = 2048 // Max width/height in pixels

    /**
     * Prepare a MultipartBody.Part from a Bitmap for avatar upload
     * Compresses the image to JPEG format with quality optimization
     */
    fun prepareAvatarPart(context: Context, bitmap: Bitmap): MultipartBody.Part? {
        return try {
            // Compress bitmap to JPEG
            val outputStream = ByteArrayOutputStream()
            
            // Scale down if image is too large
            val scaledBitmap = if (bitmap.width > MAX_IMAGE_SIZE || bitmap.height > MAX_IMAGE_SIZE) {
                val scale = MAX_IMAGE_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                bitmap.scale(newWidth, newHeight)
            } else {
                bitmap
            }
            
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val imageBytes = outputStream.toByteArray()

            // Create temporary file
            val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { fos ->
                fos.write(imageBytes)
            }

            // Create RequestBody and MultipartBody.Part
            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestBody)

            Log.d(TAG, "Avatar part prepared successfully (${imageBytes.size} bytes)")
            part
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare avatar part: ${e.message}", e)
            null
        }
    }

}
