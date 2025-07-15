package com.firman.capstone.urvoice.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.scale

object GalleryUtils {

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile("temp_image", ".jpg, .png, jpeg, JPG, .PNG, .JPEG", storageDir)
    }

    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return bitmap.scale(finalWidth, finalHeight)
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 80): Boolean {
        return try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.flush()
            stream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun processImageFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        return try {
            val bitmap = getBitmapFromUri(context, uri) ?: return null
            val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
            val file = createCustomTempFile(context)

            if (saveBitmapToFile(resizedBitmap, file, quality)) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createImageMultipart(file: File, partName: String = "file"): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun getFileSizeInMB(file: File): Double {
        val fileSizeInBytes = file.length()
        return fileSizeInBytes / (1024.0 * 1024.0)
    }

    fun isImageFile(uri: Uri, context: Context): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            Log.d("GalleryUtils", "MIME type: $mimeType")

            if (mimeType?.startsWith("image/") == true) {
                return true
            }

            val uriString = uri.toString()
            val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
            val hasImageExtension = imageExtensions.any {
                uriString.lowercase().contains(it)
            }

            if (hasImageExtension) {
                Log.d("GalleryUtils", "Detected image by extension")
                return true
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val isValidBitmap = options.outWidth != -1 && options.outHeight != -1
            Log.d("GalleryUtils", "Bitmap validation: $isValidBitmap")

            return isValidBitmap

        } catch (e: Exception) {
            Log.e("GalleryUtils", "Error validating image file", e)
            false
        }
    }
}