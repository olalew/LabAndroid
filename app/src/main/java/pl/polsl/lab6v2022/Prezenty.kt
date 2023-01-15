package pl.polsl.lab6v2022

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.GsonBuilder
import java.util.*
import kotlin.collections.ArrayList

class Prezenty : AppCompatActivity() {

    private var presentList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = WebView(this)
        page.settings.javaScriptEnabled=true

        page.addJavascriptInterface(this, "activity")

        page.loadUrl("file:///android_asset/Prezenty.html")

        setContentView(page)
    }

    @JavascriptInterface
    fun appendGift(giftName: String): Unit {
        presentList.add(giftName)
    }

    @JavascriptInterface
    fun getPresentList(): String {
        return GsonBuilder().create().toJson(this.presentList)
    }

    @JavascriptInterface
    fun generate() : Unit {
        val showCardIntent: Intent = Intent(this, Kartka::class.java)
        showCardIntent.putStringArrayListExtra("gifts", presentList)

        startActivity(showCardIntent)
    }

    @JavascriptInterface
    fun sayHello(name: String) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getDate(): String {
        return Date().toString()
    }
}