package pl.polsl.lab6v2022

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Base64.*
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
        page.loadUrl("file:///android_asset/Kartka.html")

        setContentView(page)

        launchTakeImage();
    }

    private fun parseImageResult(result: ActivityResult?): Unit {
        if (result?.resultCode != RESULT_OK)
            return

        val imageBitmap = result.data?.extras?.get("data") as Bitmap ?: return

        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        val encoded: String = Base64.getEncoder().encodeToString(byteArray)
        val callback =
            java.lang.String.format("javascript:window.imageManager(%s)", encoded)

        runOnUiThread {
            page.loadUrl(callback)
            page.resumeTimers()
        }
    }

    private fun launchTakeImage(): Unit {
        val plik = File(
            applicationContext
                .getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
                )!!.path + "/zdjecie.jpg"
        )

        val sciezkaDoPliku: Uri = FileProvider.getUriForFile(this,
            applicationContext.packageName + ".provider", plik);

        val intAparat = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intAparat.putExtra(MediaStore.EXTRA_OUTPUT, sciezkaDoPliku)
        intAparat.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        launcher.launch(intAparat)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putStringArrayList("gifts", presentList)
    }
}