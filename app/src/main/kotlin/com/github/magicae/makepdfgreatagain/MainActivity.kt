package com.github.magicae.makepdfgreatagain

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.webkit.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val webview = findViewById(R.id.webview) as WebView
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.useWideViewPort = true
        webview.settings.loadWithOverviewMode = true
        webview.settings.builtInZoomControls = true
        webview.setWebChromeClient(object :WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("WebViewConsole", consoleMessage?.message() + " -- From line "
                        + consoleMessage?.lineNumber() + " of "
                        + consoleMessage?.sourceId())
                return true
            }
        })
        webview.loadUrl("https://make-pdf-great-again.magica.io/web/viewer.html")
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
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
