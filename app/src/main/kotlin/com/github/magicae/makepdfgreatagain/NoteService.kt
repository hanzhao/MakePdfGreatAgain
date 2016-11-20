package com.github.magicae.makepdfgreatagain

import okhttp3.*

class NoteService {
  val client = OkHttpClient()

  // Add a note of current page.
  fun addNote(host: String, filename: String, pageNumber: Int, note: String, callback: Callback) {
    val body = FormBody.Builder()
        .add("content" , note)
        .build()
    val request = Request.Builder()
        .url("$host/note/$filename?page=$pageNumber")
        .post(body)
        .build()
    client.newCall(request).enqueue(callback)
  }

  // Get notes of current PDF file.
  fun getNote(host: String, filename: String, callback: Callback) {
    val request = Request.Builder()
        .url("$host/note/$filename")
        .get()
        .build()
    client.newCall(request).enqueue(callback)
  }
}
