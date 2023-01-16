package pl.polsl.lab6v2022

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.util.*


class Kartka : AppCompatActivity() {

    private val launcher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        parseImageResult(result)
    }

    private lateinit var page: WebView

    private var presentList: ArrayList<String> = ArrayList()
    private lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presentList = if (savedInstanceState != null) {
            savedInstanceState.getStringArrayList("gifts") as ArrayList<String>
        } else {
            intent.getStringArrayListExtra("gifts") as ArrayList<String>
        }

        this.locationClient = LocationServices.getFusedLocationProviderClient(this)

        page = WebView(this)

        WebView.setWebContentsDebuggingEnabled(true)

        page.settings.javaScriptEnabled=true
        page.addJavascriptInterface(this, "activity")

        page.loadUrl("file:///android_asset/card/Kartka.html")

        setContentView(page)

        launchTakeImage();
    }

    override fun onResume() {
        super.onResume()

        displayLocation()
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

    private fun displayLocation(): Unit {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                requestLocationPermission()
            }

            return
        }
        locationClient.lastLocation.addOnSuccessListener {
            location ->

            if (location == null) {
                val callback =
                    java.lang.String.format("javascript:window.activity.onReceiveLocation('%s')", "test string")

                runOnUiThread {
                    page.loadUrl(callback)
                    page.resumeTimers()
                }
            } else {
                val lambda = wspolrzedna(location.longitude)
                val fi = wspolrzedna(location.latitude)
                val latitudeText: String =
                    "Dł: " + lambda!![0] + "° " + lambda!![1] + "' " + lambda!![2]
                val lontitudeText: String = "Sz: " + fi!![0] + "° " + fi!![1] + "' " + fi!![2]

                val contentText: String = String.format("%s; %s", latitudeText, lontitudeText)

                val callback =
                    java.lang.String.format("javascript:window.activity.onReceiveLocation(\"%s\")", contentText)

                runOnUiThread {
                    page.loadUrl(callback)
                    page.resumeTimers()
                }
            }
        }
    }

    fun wspolrzedna(pomiar: Double): IntArray? {
        val w = IntArray(3)
        w[0] = pomiar.toInt()
        w[1] = ((pomiar - w[0]) * 100).toInt()
        w[2] = ((pomiar - w[0] - w[1] * 0.01) * 10000).toInt()
        w[1] *= 0.6.toInt()
        w[2] *= 0.6.toInt()
        return w
    }

    private fun launchTakeImage(): Unit {
        val intAparat = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcher.launch(intAparat)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            99
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the
            // location-related task you need to do.
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
               displayLocation()
            }

        } else {
            Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", this.packageName, null),
                    ),
                )
            }
        }
        return
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