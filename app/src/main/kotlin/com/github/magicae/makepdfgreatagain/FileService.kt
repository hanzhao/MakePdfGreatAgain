package com.github.magicae.makepdfgreatagain

import android.content.Context
import android.net.Uri
import com.google.common.io.ByteStreams
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest


class FileService {
    val MEDIA_TYPE_PDF: MediaType? = MediaType.parse("application/pdf")

    fun uploadFile(context: Context, server: String, uri: Uri?, callback: Callback) {
        if (uri == null) return
        val digest = MessageDigest.getInstance("SHA1")
        var sha1: String
        var inputStream: InputStream? = null

        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val buffer = ByteArray(8192)
            var read: Int
            while (true) {
                read = inputStream.read(buffer)
                if (read <= 0) {
                    break
                }
                digest.update(buffer, 0, read)
            }
            val sha1sum = digest.digest()
            val bigInt = BigInteger(1, sha1sum)
            // Get sha1 of the file
            sha1 = String.format("%40s", bigInt.toString(16)).replace(' ', '0')
        } catch (e: IOException) {
            throw RuntimeException("Unable to process SHA1", e)
        } finally {
            inputStream?.close()
        }

        try {
            val client = OkHttpClient()
            // Check SHA1
            val request = Request.Builder().url("$server/valid/$sha1.pdf").get().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call?, response: Response?) {
                    if (response?.code() == 200) {
                        callback.onResponse(call, response)
                        return
                    }
                    inputStream = context.contentResolver.openInputStream(uri)
                    // Create temporary file
                    val tempFile = File.createTempFile(sha1, ".pdf")
                    ByteStreams.copy(inputStream, FileOutputStream(tempFile))
                    // POST file
                    val multipart = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", "_.pdf", RequestBody.create(MEDIA_TYPE_PDF, tempFile))
                            .build()
                    val request = Request.Builder()
                            .url("$server/upload/$sha1.pdf")
                            .post(multipart)
                            .build()
                    client.newCall(request).enqueue(callback)
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    callback.onFailure(call, e)
                }
            })
        } finally {
            inputStream?.close()
        }
    }
}
