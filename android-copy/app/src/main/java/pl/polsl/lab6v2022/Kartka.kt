package pl.polsl.lab6v2022

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
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

    private var presentList: java.util.ArrayList<String> = ArrayList()
    private lateinit var locationClient: FusedLocationProviderClient

    private var callback: String = ""
    private var encodedImage: String = ""
    private var location: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.locationClient = LocationServices.getFusedLocationProviderClient(this)
        page = WebView(this)

        val isSavedInstance: Boolean = savedInstanceState != null
        if (isSavedInstance) {
           presentList =
               savedInstanceState!!.getStringArrayList("gifts") as java.util.ArrayList<String>
            callback = savedInstanceState!!.getString("callback", "");
            encodedImage = savedInstanceState!!.getString("image", "")
            location = savedInstanceState!!.getString("location", "")
        } else {
            presentList = intent.getStringArrayListExtra("gifts") as ArrayList<String>
            launchTakeImage();
        }

        WebView.setWebContentsDebuggingEnabled(true)

        page.settings.javaScriptEnabled=true
        page.addJavascriptInterface(this, "activity")

        page.loadUrl("file:///android_asset/card/Kartka.html")

        setContentView(page)
    }

    override fun onResume() {
        super.onResume()

        displayLocation()

//        val handler = Handler()
//        handler.postDelayed(Runnable { // Do something after 5s = 5000ms
//           runOnUiThread {
//               if (callback.isNotEmpty()) {
//                   page.loadUrl(callback)
//                   page.resumeTimers()
//               }
//           }
//        }, 200)

    }

    private fun parseImageResult(result: ActivityResult?): Unit {
        if (result?.resultCode != RESULT_OK)
            return

        val imageBitmap = result.data?.extras?.get("data")

        checkNotNull(imageBitmap) { return }

        val byteArrayOutputStream = ByteArrayOutputStream()
        (imageBitmap as Bitmap).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        this.encodedImage = "data:image/png;base64," + Base64.getEncoder().encodeToString(byteArray)
        this.callback =
            java.lang.String.format("javascript:window.activity.onReceiveImage('%s')", encodedImage)

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

                this.location = contentText

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
        w[1] = (w[1] * 0.6).toInt()
        w[2] = (w[2] * 0.6).toInt()
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

    @JavascriptInterface
    fun getImageContent() : String {
        return encodedImage
    }

    @JavascriptInterface
    fun getLocation() : String {
        return location
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("gifts", presentList)
        outState.putString("callback", callback)
        outState.putString("image", encodedImage)
        outState.putString("location", location)
    }

}