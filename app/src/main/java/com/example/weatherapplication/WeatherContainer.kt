package com.example.weatherapplication

import java.io.Serializable

class WeatherContainer (val approvedTime: String, val long: String, val lat: String, val weatherItems: ArrayList<WeatherItem>): Serializable {
    override fun toString(): String {
        return "WeatherContainer(approvedTime='$approvedTime', long='$long', lat='$lat')"
    }
}