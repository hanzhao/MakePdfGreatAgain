package com.github.magicae.makepdfgreatagain

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class NoteListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val cardView: CardView = view.findViewById(R.id.card_view) as CardView
  val dateTextView: TextView = view.findViewById(R.id.card_date) as TextView
  val textView: TextView = view.findViewById(R.id.card_text) as TextView
}

class NoteListAdapter(notes: Array<Note>) : RecyclerView.Adapter<NoteListViewHolder>() {
  var notes: Array<Note> = notes;

  override fun getItemCount(): Int {
    return notes.size
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NoteListViewHolder {
    val v = LayoutInflater.from(parent!!.context)
        .inflate(R.layout.note_card_view, parent, false)

    val vh = NoteListViewHolder(v)
    return vh
  }

  override fun onBindViewHolder(holder: NoteListViewHolder?, position: Int) {
    holder!!.textView!!.text = notes[position].content
    holder!!.dateTextView!!.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        .format(Date(notes[position].time * 1000L))
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
    super.onAttachedToRecyclerView(recyclerView)
  }
}