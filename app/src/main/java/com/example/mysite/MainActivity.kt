package com.example.mysite

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*

class MainActivity() : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    Parcelable {

    var networkAvailbale = false
    lateinit var mWebView: WebView
    lateinit var drawerLayout_: DrawerLayout

    constructor(parcel: Parcel) : this() {
        networkAvailbale = parcel.readByte() != 0.toByte()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        var urlFeedback = getString(R.string.website_feedback)

        val drawerLayot_: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView_: NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayot_, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayot_.addDrawerListener(toggle)
        toggle.syncState()

        navView_.setNavigationItemSelectedListener(this)

        mWebView = findViewById(R.id.webView)
        val webSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        var url = getString(R.string.website_url)
        webSettings.setAppCacheEnabled(false)

        loadWebSite(mWebView, url, applicationContext)

        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed, R.color.colorBlue, R.color.colorGreen)
        swipeRefreshLayout.apply{
            setOnRefreshListener {
                if (mWebView != null) url = mWebView.url
                loadWebSite(mWebView, url, applicationContext)
            }
            setOnChildScrollUpCallback { parent, child -> mWebView.getScrollY() > 0 }
        }


        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            loadWebSite(mWebView, urlFeedback, applicationContext)
        }


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> {
                val url = getString(R.string.website_url)
                loadWebSite(webView, url, applicationContext)
            }

            R.id.nav_at -> {
                val url = getString(R.string.website_at)
                loadWebSite(webView, url, applicationContext)
            }

            R.id.nav_kotlin -> {
                val url = getString(R.string.website_kotlin)
                loadWebSite(webView, url, applicationContext)
            }

            R.id.nav_java -> {
                val url = getString(R.string.website_java)
                loadWebSite(webView, url, applicationContext)
            }

            R.id.nav_android -> {
                val url = getString(R.string.website_android)
                loadWebSite(webView, url, applicationContext)
            }

            R.id.nav_video -> {
                val url = getString(R.string.website_video)
                loadWebSite(webView, url, applicationContext)
            }
        }
        drawerLayout_.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadWebSite(mWebView: WebView, url: String, context: Context){
        progressBar.visibility = View.VISIBLE
        networkAvailbale = isNetworkAvailable(context)
        mWebView.clearCache(true)
        if (networkAvailbale){
            wvVisible(webView)
            mWebView.webViewClient = MyWebViewClient()
            mWebView.loadUrl(url)
        }else{
            wvGone(webView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun wvVisible(mWebView: WebView) {
        mWebView.visibility = View.VISIBLE
        tvCheckConnection.visibility = View.GONE
    }

    private fun wvGone(mWebView: WebView) {
        mWebView.visibility = View.GONE
        tvCheckConnection.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT > 22) {
                val an = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(an) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val a = cm.activeNetworkInfo ?: return false
                a.isConnected && (a.type == ConnectivityManager.TYPE_WIFI || a.type == ConnectivityManager.TYPE_MOBILE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun onLoadComplete(){
        swipeRefreshLayout.isRefreshing = false
        progressBar.visibility = View.GONE
    }

    private inner class MyWebViewClient : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            val url = request?.url.toString()
            return urlOverride(url)
        }


        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            return urlOverride(url)
        }

        private fun urlOverride(url: String): Boolean {
            progressBar.visibility = View.VISIBLE
            networkAvailbale = isNetworkAvailable(applicationContext)

            if (networkAvailbale) {
                if (Uri.parse(url).host == getString(R.string.website_domain)) return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                onLoadComplete()
                return true
            } else {
                wvGone(webView)
                return false
            }
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (errorCode == 0) {
                view?.visibility = View.GONE
                tvCheckConnection.visibility = View.VISIBLE
                onLoadComplete()
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
           onReceivedError(view, error!!.errorCode, error.description.toString(), request!!.url.toString())
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadComplete()
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (networkAvailbale) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }
}



