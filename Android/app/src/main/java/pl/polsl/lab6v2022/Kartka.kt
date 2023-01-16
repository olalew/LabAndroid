package pl.polsl.lab6v2022

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Base64.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class Kartka : AppCompatActivity() {

    private val launcher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        parseImageResult(result)
    }

    private lateinit var page: WebView

    private var presentList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presentList = if (savedInstanceState != null) {
            savedInstanceState.getStringArrayList("gifts") as ArrayList<String>
        } else {
            intent.getStringArrayListExtra("gifts") as ArrayList<String>
        }

        page = WebView(this)

        page.settings.javaScriptEnabled=true
        page.addJavascriptInterface(this, "activity")

        page.loadUrl("file:///android_asset/card/Kartka.html")

        setContentView(page)

        launchTakeImage();
    }

    private fun parseImageResult(result: ActivityResult?): Unit {
        if (result?.resultCode != RESULT_OK)
            return

        val imageBitmap = result.data?.extras?.get("data")

        checkNotNull(imageBitmap) { return }

        val byteArrayOutputStream = ByteArrayOutputStream()
        (imageBitmap as Bitmap).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        val encoded: String = "data:image/png;base64," + Base64.getEncoder().encodeToString(byteArray)
        val callback =
            java.lang.String.format("javascript:window.activity.onReceiveImage('%s')", encoded)

        runOnUiThread {
            page.loadUrl(callback)
            page.resumeTimers()
        }
    }

    private fun launchTakeImage(): Unit {
        val intAparat = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcher.launch(intAparat)
    }

    @JavascriptInterface
    fun receiveList(): String {
        return GsonBuilder().create().toJson(this.presentList)
    }

    @JavascriptInterface
    fun done(): Unit {
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putStringArrayList("gifts", presentList)
    }
}