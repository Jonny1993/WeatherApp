package com.example.weatherapplication

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koushikdutta.ion.Ion
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URLEncoder
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var locationManager: LocationManager
    var weatherItems = ArrayList<WeatherItem>()
    lateinit var weatherContainer : WeatherContainer
    private val VALUES_FILE_NAME = "saved_values"
    private lateinit var enteredLocation : String
    private val URL1 = "https://www.smhi.se/wpt-a/backend_solr/autocomplete/search/"
    private val URL2 = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/"
    private var timePaused: Long = -1
    private val ONE_HOUR = 3600000

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if(!isOnline(this)){
            val fileRead = readFromFile()
            if(fileRead == null){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Warning!")
                builder.setMessage("File is empty." +
                        "\nPlease try launching the app again when network is available.\nQuitting app.")
                builder.setPositiveButton("Quit"){_, _ ->
                    exitProcess(-1)
                }
                val dialog = builder.create()
                dialog.show()
            }
            else{
                showData(fileRead.approvedTime, fileRead.weatherItems)
            }
        }
        else{
            if(timePaused > -1 && (System.currentTimeMillis() - timePaused) > ONE_HOUR){
                if(enteredLocation.isNotEmpty())  processLocation()
            }
            else if(weatherItems.isNullOrEmpty()){
				val mainText = findViewById<TextView>(R.id.approved_time)
                mainText.text = ("Welcome to the Weather Forecast App" +
                        "\n\nPlease enter a Scandinavian location to get a 10 day forecast")
                mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0F)
            }
        }
    }

    private fun readFromFile(): WeatherContainer?{
        val fInputSteam = this.openFileInput(VALUES_FILE_NAME)
        val objInputStream = ObjectInputStream(fInputSteam)
        weatherContainer = objInputStream.readObject() as WeatherContainer
        Log.d("Items", "Weather item 1: ${weatherContainer.weatherItems[0]}")
        if(weatherContainer.approvedTime.isNotEmpty()){
            weatherItems.addAll(weatherContainer.weatherItems)
            Log.d("WeatherItems", "Weather item 1: ${weatherItems[0]}")
            return weatherContainer
        }
        objInputStream.close()
        fInputSteam.close()
        return null
    }

    //source: https://stackoverflow.com/questions/51141970/check-internet-connectivity-android-in-kotlin
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPause() {
        super.onPause()
        timePaused = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        if(!weatherItems.isNullOrEmpty()) saveToFile(weatherContainer)
    }

    private fun saveToFile(weatherContainer: WeatherContainer){
        if(weatherContainer != null) {
            val fOutputStream = this.openFileOutput(VALUES_FILE_NAME, MODE_PRIVATE)
            val objOutputStream = ObjectOutputStream(fOutputStream)
            objOutputStream.writeObject(weatherContainer)
            objOutputStream.close()
            fOutputStream.close()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onSubmitClick(view: View){
        if(!isOnline(this)){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Warning!")
            builder.setMessage("There is no network, please try again later")
            builder.setPositiveButton("OK"){_, _ ->
            }
            val dialog = builder.create()
            dialog.show()
        }else {
            enteredLocation = findViewById<EditText>(R.id.location_text).text.toString()
            val charsRegex = "[a-zA-ZåäöÅÄÖ]+".toRegex()
            Log.d("Input", "Location is $enteredLocation")
            if (enteredLocation.isNullOrBlank() || !enteredLocation.matches(charsRegex)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Warning!")
                builder.setMessage("Location field is empty or invalid data was entered" +
                        "\nEnter a correct location to show data")
                builder.setPositiveButton("OK") { _, _ ->
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                processLocation()
            }
        }

    }

    private fun processLocation(){
        val encodedURL = URLEncoder.encode(enteredLocation, "UTF-8")
        Log.d("Encoded", "Encoded url: $encodedURL")
        Ion.with(this).load(URL1 + encodedURL).asString().setCallback{ ex, result ->
            Log.d("FirstResult", "First result: $result & Location is $encodedURL")
            if(result.isNullOrEmpty() || !result.contains("lon")){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Warning!")
                builder.setMessage("The location entered is not supported.\nOnly locations in Scandinavian countries are covered.")
                builder.setPositiveButton("OK"){_, _ ->
                }
                val dialog = builder.create()
                dialog.show()
            }
            else{
                val longLat = JSONParser(result).getLongLat()
                Log.d("Coord", "The coordinates after conversion ${longLat[0]}, ${longLat[1]}")
                getWeatherItems(longLat[0], longLat[1])
            }
        }
    }

    private fun getWeatherItems(long: String, lat: String){
        Ion.with(this).load(URL2 + "lon/$long/lat/$lat/data.json")
                .asString()
                .setCallback{ ex, result ->
                    if(!result.startsWith("{\"a")){
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Warning!")
                        builder.setMessage("The entered location is outside Scandinavia" +
                                "\nPlease enter a Scandinavian location.")
                        builder.setPositiveButton("OK") { _, _ ->
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }else {
                        if(!weatherItems.isNullOrEmpty()) weatherItems.clear()
                        weatherItems.addAll(JSONParser(result).weatherItems())
                        Log.d("Result2", "The 2nd JSON String: $result")
                        val approvedTime = JSONParser(result).approvedTime()
                        weatherContainer = WeatherContainer(approvedTime, long, lat, weatherItems)
                        Log.d("Container", "approvedT: ${weatherContainer.approvedTime}")
                        showData(approvedTime, weatherItems)
                    }
                }
    }

    private fun showData(approvedTime: String, weatherItems: ArrayList<WeatherItem>){
        findViewById<TextView>(R.id.approved_time).text = "Approved Time: $approvedTime"
        findViewById<TextView>(R.id.approved_time).setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0F)
        val rvItems = findViewById<RecyclerView>(R.id.rvItems) as RecyclerView
        val adapter = WeatherItemsAdapter(weatherItems)
        rvItems.adapter = adapter
        rvItems.layoutManager = LinearLayoutManager(this)
    }
}