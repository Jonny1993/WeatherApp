package com.example.weatherapplication

import java.io.Serializable

class WeatherItem (val time: String, val icon: Int, val temp: Double, val cloud: Int) : Serializable{
    override fun toString(): String {
        return "WeatherItem(time='$time', icon=$icon, temp='$temp', cloud=$cloud)"
    }
}