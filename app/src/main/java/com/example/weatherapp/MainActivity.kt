package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    var weather_url = ""
    var api_id = "963d821267b34fb493d72753a72981bc"

    private lateinit var cityName: TextView
    private lateinit var date: TextView
    private lateinit var temperature: TextView
    private lateinit var condition: TextView
    private lateinit var feelsLike: TextView
    private lateinit var highLow0: TextView
    private lateinit var highLow1: TextView
    private lateinit var highLow2: TextView
    private lateinit var dayOfWeek1: TextView
    private lateinit var dayOfWeek2: TextView

    private lateinit var home: RelativeLayout

    private lateinit var progressBar: ProgressBar

    private lateinit var day0Icon: ImageView
    private lateinit var day1Icon: ImageView
    private lateinit var day2Icon: ImageView

    private lateinit var cityInput: TextInputEditText
    private lateinit var magGlass: ImageView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        setContentView(R.layout.activity_main)


        cityName = findViewById(R.id.cityName)
        date = findViewById(R.id.date)
        temperature = findViewById(R.id.temperature)
        condition = findViewById(R.id.condition)
        feelsLike = findViewById(R.id.feelsLike)
        highLow0 = findViewById(R.id.highLow0)
        highLow1 = findViewById(R.id.highLow1)
        highLow2 = findViewById(R.id.highLow2)
        dayOfWeek2 = findViewById(R.id.dayOfWeek2)

        home = findViewById(R.id.home)

        progressBar = findViewById(R.id.pBar1)

        day0Icon = findViewById(R.id.day0Icon)
        day1Icon = findViewById(R.id.day1Icon)
        day2Icon = findViewById(R.id.day2Icon)

        cityInput = findViewById(R.id.inputCity)
        magGlass = findViewById(R.id.magGlass)
        magGlass.bringToFront()

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        magGlass.setOnClickListener{
            var temp = cityInput.text.toString()
            weather_url = "&city=" + temp + "&country=US" + "&key=" + api_id
            getWeather()
        }
        obtainLocation()
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation()
    {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                //weather_url = "https://api.weatherbit.io/v2.0/current?" + "lat=" + location?.latitude + "&lon=" + location?.longitude + "&key=" + api_id
                weather_url = "lat=" + location?.latitude + "&lon=" + location?.longitude + "&key=" + api_id
                getWeather()
            }
    }

    private fun getWeather()
    {
        // Grab current day of the week for display
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)

        // New request queue to use Volley to get the API
        val queue = Volley.newRequestQueue(this)

        // Pull the weather url constructed from calling func
        var url: String = "https://api.weatherbit.io/v2.0/current?" + weather_url

        // Request the string from the API
        val currWeatherString = StringRequest(url, Response.Listener<String> { response ->
            // If it failed, do nothing
            if(response.isNullOrEmpty())
                return@Listener
            // Otherwise made JSONObject
            val objResponse = JSONObject(response)
            // Grab the array named data
            val arr = objResponse.getJSONArray("data")
            // Index it to today
            val objDay0 = arr.getJSONObject(0)
            // Grab weather object
            val objDay0Weather = objDay0.getJSONObject("weather")

            // Set temperature and city name on UI
            temperature.text = objDay0.getString("temp") + "\u2103"
            cityName.text = objDay0.getString("city_name") + ", " + objDay0.getString("state_code")

            // Get the date in format and output
            val rawDate = objDay0.getString("ob_time")
            var dateStr = ""
            var currDay = ""
            var currMonth = ""
            when (day) {
                Calendar.SUNDAY -> currDay = "Sunday"
                Calendar.MONDAY -> currDay = "Monday"
                Calendar.TUESDAY -> currDay = "Tuesday"
                Calendar.WEDNESDAY -> currDay = "Wednesday"
                Calendar.THURSDAY -> currDay = "Thursday"
                Calendar.FRIDAY -> currDay = "Friday"
                Calendar.SATURDAY -> currDay = "Saturday"
            }
            currMonth = rawDate.subSequence(5, 7) as String
            when (currMonth) {
                "01" -> currMonth = "January"
                "02" -> currMonth = "February"
                "03" -> currMonth = "March"
                "04" -> currMonth = "April"
                "05" -> currMonth = "May"
                "06" -> currMonth = "June"
                "07" -> currMonth = "July"
                "08" -> currMonth = "August"
                "09" -> currMonth = "September"
                "10" -> currMonth = "October"
                "11" -> currMonth = "November"
                "12" -> currMonth = "December"
                else -> currMonth = "Error"
            }
            dateStr = currDay + ", " + currMonth + " " + rawDate.subSequence(8, 10) + ", " + rawDate.subSequence(0, 4)
            date.text = dateStr
            condition.text = objDay0Weather.getString("description")
            feelsLike.text = "Feels like: " + objDay0.getString("app_temp") + "\u2103" },
            Response.ErrorListener { temperature!!.text = "Error!" })
        queue.add(currWeatherString)

        val url2 = "https://api.weatherbit.io/v2.0/forecast/daily?" + weather_url
        // Request the string from the API
        val forecastWeatherString = StringRequest(url2, Response.Listener<String> { response2 ->
            // If it failed, do nothing
            if(response2.isNullOrEmpty())
                return@Listener
            // Otherwise made JSONObject
            val objResponse = JSONObject(response2)
            // Grab the array named data
            val arr = objResponse.getJSONArray("data")
            // Index it for each day used in forecast
            val objDay0 = arr.getJSONObject(0)
            val objDay1 = arr.getJSONObject(1)
            val objDay2 = arr.getJSONObject(2)
            // Grab weather objects
            val objDay0Weather = objDay0.getJSONObject("weather")
            val objDay1Weather = objDay1.getJSONObject("weather")
            val objDay2Weather = objDay2.getJSONObject("weather")

            // Set high and low text
            highLow0.text = objDay0.getString("low_temp") + "\u2103" + "/" + objDay0.getString("high_temp") + "\u2103"
            highLow1.text = objDay1.getString("low_temp") + "\u2103" + "/" + objDay1.getString("high_temp") + "\u2103"
            highLow2.text = objDay2.getString("low_temp") + "\u2103" + "/" + objDay2.getString("high_temp") + "\u2103"

            // Find day of week in two days
            var twoDay = ""
            when (day) {
                Calendar.SUNDAY -> twoDay = "Tuesday"
                Calendar.MONDAY -> twoDay = "Wednesday"
                Calendar.TUESDAY -> twoDay = "Thursday"
                Calendar.WEDNESDAY -> twoDay = "Friday"
                Calendar.THURSDAY -> twoDay = "Saturday"
                Calendar.FRIDAY -> twoDay = "Sunday"
                Calendar.SATURDAY -> twoDay = "Monday"
                else -> twoDay = "Error"
            }

            // Set it
            dayOfWeek2.text = twoDay

            var resourceId0 = resources.getIdentifier(objDay0Weather.getString("icon"), "png", this.packageName)
            var resourceId1 = resources.getIdentifier(objDay1Weather.getString("icon"), "png", this.packageName)
            var resourceId2 = resources.getIdentifier(objDay2Weather.getString("icon"), "png", this.packageName)

            day0Icon.bringToFront()
            day1Icon.bringToFront()
            day2Icon.bringToFront()
            day0Icon.setImageResource(resourceId0)
            day1Icon.setImageResource(resourceId1)
            day2Icon.setImageResource(resourceId2)
                                                                                 },
            Response.ErrorListener { dayOfWeek2!!.text = "Error1!" })
        queue.add(forecastWeatherString)
    }
}