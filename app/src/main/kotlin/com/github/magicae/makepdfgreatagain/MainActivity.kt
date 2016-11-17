package com.github.magicae.makepdfgreatagain

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import org.xwalk.core.XWalkView
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val FILE_SELECT_CODE = 40001

    private var mActionMode: ActionMode? = null

    private val fileService = FileService()

    private val SERVER = "https://make-pdf-great-again.magica.io"

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
                webView.evaluateJavascript("window.makePdfGreatAgain.highlightSelection()", {
                    Log.d("MakePDFGreatAgain", "Evaluate OK.")
                })
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
        val webView = findViewById(R.id.webview) as XWalkView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.builtInZoomControls = true
        webView.load(SERVER, null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_open_file) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, FILE_SELECT_CODE)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            fileService.uploadFile(this, SERVER, data?.data, object : Callback {
                override fun onResponse(call: Call?, response: Response?) {
                    val url = JSONObject(response!!.body()?.string()).getString("url")
                    val webview = findViewById(R.id.webview) as XWalkView
                    webview.post {
                        webview.load("$SERVER/web/viewer.html?file=$SERVER$url", null)
                    }
                }
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.e("Response error", e.toString())
                }
            })
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
