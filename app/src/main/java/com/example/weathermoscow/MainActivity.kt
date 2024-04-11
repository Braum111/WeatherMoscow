package com.example.weathermoscow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private val site = "https://api.openweathermap.org/data/2.5/weather?q=Moscow&units=metric&appid=75cda820a8902118f5d797edc6784239"
    private lateinit var temptext: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    private lateinit var feelsLikeTextView: TextView
    private var mRequestQueue: RequestQueue? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        mRequestQueue = Volley.newRequestQueue(this)
        temptext = findViewById(R.id.temptext)
        windSpeedTextView = findViewById(R.id.windSpeed)
        humidityTextView = findViewById(R.id.humidity)
        weatherDescriptionTextView = findViewById(R.id.weatherDescription)
        feelsLikeTextView = findViewById(R.id.feelsLike)

        swipeRefreshLayout.setOnRefreshListener {
            getWeather()
        }
        getWeather() // Получаем погоду
    }

    private fun getWeather() {
        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.GET, site, null,
            { response ->
                try {
                    val main = response.getJSONObject("main")
                    val weather = response.getJSONArray("weather").getJSONObject(0)
                    val wind = response.getJSONObject("wind")

                    val temperature = main.getDouble("temp")
                    val windSpeed = wind.getDouble("speed")
                    val humidityValue = main.getDouble("humidity")
                    val weatherDescription = weather.getString("description")
                    val feelsLike = main.getDouble("feels_like")

                    temptext.text = "$temperature°C"
                    windSpeedTextView.text = getString(R.string.wind_speed) + ": \n$windSpeed m/s"
                    humidityTextView.text = getString(R.string.humidity) + ": \n$humidityValue %"
                    weatherDescriptionTextView.text = getString(R.string.weather) + ": \n$weatherDescription"
                    feelsLikeTextView.text = getString(R.string.feels_like) + ": \n$feelsLike°C"
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Ошибка при обработке данных", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show()
            })

        mRequestQueue?.add(jsonObjectRequest)
        swipeRefreshLayout.isRefreshing = false
    }
}




