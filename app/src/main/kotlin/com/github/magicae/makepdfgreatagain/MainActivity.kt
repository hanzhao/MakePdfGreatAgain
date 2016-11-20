package com.github.magicae.makepdfgreatagain

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import org.xwalk.core.XWalkResourceClient
import org.xwalk.core.XWalkView
import java.io.IOException

class MainActivity : AppCompatActivity() {

  private val FILE_SELECT_CODE = 40001
  // If you need to deploy a new server, please change the server hostname to yours.
  private val SERVER = "http://make-pdf-great-again.magica.io"

  private var mActionMode: ActionMode? = null
  private var progress: ProgressDialog? = null

  private val fileService = FileService()
  private val noteService = NoteService()

  // Name of current open file.
  var filename = ""

  // ActionMode setup.
  override fun onActionModeStarted(mode: ActionMode?) {
    if (mActionMode == null) {
      mActionMode = mode
      val menu = mode?.menu!!
      var i = 0
      // Only show "COPY", "SELECT ALL", "HIGHLIGHT" in menu.
      while (i < menu.size()) {
        if (menu.getItem(i).title != "Copy" && menu.getItem(i).title != "Select all") {
          menu.getItem(i).isVisible = false
        }
        ++i
      }
      // Programmatically add "HIGHLIGHT" button to menu.
      val item = menu.add(0, 0, 0, R.string.action_highlight)
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
      item.setOnMenuItemClickListener {
        val webView = findViewById(R.id.webview) as XWalkView
        webView.evaluateJavascript("window.makePdfGreatAgain.highlightSelection()", { })
        true
      }
    }
    super.onActionModeStarted(mode)
  }

  override fun onActionModeFinished(mode: ActionMode?) {
    mActionMode = null
    super.onActionModeFinished(mode)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)
    // Add note button listener.
    val fab = findViewById(R.id.fab) as FloatingActionButton
    fab.setOnClickListener {
      onFabClicked()
    }
    // Setup webview.
    val webView = findViewById(R.id.webview) as XWalkView
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true
    webView.settings.useWideViewPort = true
    webView.settings.loadWithOverviewMode = true
    webView.settings.builtInZoomControls = true
    webView.setResourceClient(object : XWalkResourceClient(webView) {
      // Dismiss loading dialog once PDF file is opened.
      override fun onLoadFinished(view: XWalkView?, url: String?) {
        progress?.dismiss()
        super.onLoadFinished(view, url)
      }
    })
    webView.load("file:///android_asset/index.html", null)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks.
    val id = item.itemId

    if (id == R.id.action_open_file) {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
      intent.type = "application/pdf"
      intent.addCategory(Intent.CATEGORY_OPENABLE)
      startActivityForResult(intent, FILE_SELECT_CODE)
      return true
    } else if (id == R.id.action_show_notes) {
      val intent = Intent(this, NoteActivity::class.java)
      getCurrentPage { page ->
        intent.putExtra("server", SERVER)
        intent.putExtra("filename", filename)
        intent.putExtra("page", page)
        // Start note activity.
        startActivity(intent)
      }
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
      val context = this
      progress = ProgressDialog.show(context, getString(R.string.uploading_dialog_title),
          getString(R.string.uploading_dialog_message), true)
      fileService.uploadFile(context, SERVER, data?.data, object : Callback {
        override fun onResponse(call: Call?, response: Response?) {
          val url = JSONObject(response!!.body()?.string()).getString("url")
          val webView = findViewById(R.id.webview) as XWalkView
          val toolbar = findViewById(R.id.toolbar) as Toolbar
          val fab = findViewById(R.id.fab) as FloatingActionButton
          filename = url.split("/")[2]
          context.runOnUiThread {
            webView.load("$SERVER/web/viewer.html?file=$SERVER$url", null)
            toolbar.menu.setGroupVisible(R.id.group_document_action, true)
            fab.show()
          }
        }

        override fun onFailure(call: Call?, e: IOException?) {
          Log.e("Response error", e.toString())
          context.runOnUiThread {
            progress?.dismiss()
            Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show()
          }
        }
      })
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  fun getCurrentPage(callback: (Int) -> Unit) {
    val webView = findViewById(R.id.webview) as XWalkView
    webView.evaluateJavascript("window.pdfViewerApplication.page", { str ->
      callback(str.toInt())
    })
  }

  fun onFabClicked() {
    val context = this
    val contentView = LayoutInflater.from(this).inflate(R.layout.note_dialog, null)
    val noteDialog = AlertDialog.Builder(this)
        .setTitle(R.string.note_dialog_title)
        .setView(contentView)
        .setPositiveButton("OK", { dialog, which ->
          val input = contentView.findViewById(R.id.note_input) as EditText
          getCurrentPage { page ->
            noteService.addNote(SERVER, filename, page, input.text.toString(), object : Callback {
              override fun onResponse(call: Call?, response: Response?) {
                runOnUiThread {
                  Toast.makeText(context, R.string.send_ok, Toast.LENGTH_LONG).show()
                }
              }
              override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                  Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show()
                }
              }
            })
          }
        })
        .setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
    noteDialog.show()
  }
}
