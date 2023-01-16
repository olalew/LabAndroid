package pl.polsl.lab6v2022

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import java.util.*


class Prezenty : AppCompatActivity() {

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        parseGoogleNowResult(result)
    }

    var googleNowGift: String = "Default Gift"

    private var presentList: ArrayList<String> = ArrayList()
    private lateinit var page: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            googleNowGift = savedInstanceState!!.getString("googleNowGift")!!
        }

        page = WebView(this)
        page.settings.javaScriptEnabled=true

        page.addJavascriptInterface(this, "activity")

        page.loadUrl("file:///android_asset/gifts/Prezenty.html")

        setContentView(page)
    }

    fun askGoogleNow() {
        val wywolanie = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        wywolanie.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        wywolanie.putExtra(RecognizerIntent.EXTRA_PROMPT, "Powiedz cos do mnie...")
        try {
            launcher.launch(wywolanie)
        } catch (wyjatek: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext, "Nie slucham Ciebie!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun parseGoogleNowResult(result: ActivityResult?) {
        if (result?.resultCode != RESULT_OK)
            return

        val list = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val match = result.data!!.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES)

        Toast.makeText(this,"Rozpoznalem \""+ list?.get(0), Toast.LENGTH_SHORT).show();

        googleNowGift = list?.get(0).toString()

        val callback =
            java.lang.String.format("javascript:window.activity.onDefaultSet('%s')", googleNowGift)

        runOnUiThread {
            page.loadUrl(callback)
            page.resumeTimers()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("googleNowGift", googleNowGift)
    }

    @JavascriptInterface
    fun askGoogle() {
        askGoogleNow()
    }

    @JavascriptInterface
    fun defaultGift(): String {
        return googleNowGift
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