package com.example.weathermoscow

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var cityNameTextView: TextView
    private lateinit var temptext: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    private lateinit var feelsLikeTextView: TextView
    private lateinit var currentUrl: String
    private var mRequestQueue: RequestQueue? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация виджетов
        cityNameTextView = findViewById(R.id.cityname)
        temptext = findViewById(R.id.temptext)
        windSpeedTextView = findViewById(R.id.windSpeed)
        humidityTextView = findViewById(R.id.humidity)
        weatherDescriptionTextView = findViewById(R.id.weatherDescription)
        feelsLikeTextView = findViewById(R.id.feelsLike)
        val citySelectorButton: Button = findViewById(R.id.citySelectorButton)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        mRequestQueue = Volley.newRequestQueue(this)

        // Загрузка последнего выбранного города
        val lastCity = loadCityFromPreferences()
        cityNameTextView.text = "$lastCity сегодня"
        currentUrl = "https://api.openweathermap.org/data/2.5/weather?q=$lastCity&units=metric&appid=75cda820a8902118f5d797edc6784239&lang=ru"

        swipeRefreshLayout.setOnRefreshListener {
            getWeather(currentUrl)
        }

        citySelectorButton.setOnClickListener {
            showCitySelectorDialog()
        }

        getWeather(currentUrl)
    }

    private fun getWeather(url: String) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
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
                swipeRefreshLayout.isRefreshing = false
            },
            { error ->
                Toast.makeText(this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            })

        mRequestQueue?.add(jsonObjectRequest)
    }

    private fun showCitySelectorDialog() {
        val cities = arrayOf("Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Нижний Новгород")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите город")
        builder.setItems(cities) { _, which ->
            val city = cities[which]
            cityNameTextView.text = "$city сегодня"
            saveCityToPreferences(city)
            currentUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=75cda820a8902118f5d797edc6784239&lang=ru"
            getWeather(currentUrl)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun saveCityToPreferences(city: String) {
        val sharedPreferences = getSharedPreferences("weather_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_selected_city", city)
        editor.apply()
    }

    private fun loadCityFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("weather_preferences", MODE_PRIVATE)
        return sharedPreferences.getString("last_selected_city", "Moscow") ?: "Moscow"
    }
}
