package com.github.magicae.makepdfgreatagain

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import com.google.gson.Gson
import java.io.IOException

class NoteActivity : AppCompatActivity() {

  val noteService = NoteService()
  val gson = Gson()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_note)
    // Setup toolbar of note activity.
    val toolbar = findViewById(R.id.note_toolbar) as Toolbar
    toolbar.setTitle(R.string.notes)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener {
      onBackPressed()
    }

    // Use recycler view to list note cards.
    val recyclerView = findViewById(R.id.recycler_view) as RecyclerView
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(this)
    val adapter = NoteListAdapter(Array(0, {
      Note()
    }))
    recyclerView.adapter = adapter

    val server = intent.extras.getString("server")
    val page = intent.extras.getInt("page")
    val filename = intent.extras.getString("filename")

    // Get notes from server and only show notes of this page.
    noteService.getNote(server, filename, object : Callback {
      override fun onResponse(call: Call?, response: Response?) {
        val body = response!!.body()!!.string()
        val notes = gson.fromJson(body, Array<Note>::class.java)
        adapter.notes = notes.filter {
          note -> note.page == page
        }.reversed().toTypedArray()
        Log.d("SIZE", adapter.notes.size.toString())
        runOnUiThread {
          adapter.notifyDataSetChanged()
        }
      }
      override fun onFailure(call: Call?, e: IOException?) { }
    })
  }
}
