package com.github.magicae.makepdfgreatagain

import okhttp3.*

class NoteService {
  val client = OkHttpClient()

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

  fun getNote(host: String, filename: String, callback: Callback) {
    val request = Request.Builder()
        .url("$host/note/$filename")
        .get()
        .build()
    client.newCall(request).enqueue(callback)
  }
}