package com.example.weatherapplication

import android.util.Log
import androidx.core.text.trimmedLength
import org.json.JSONObject

class JSONParser (private val jsonString: String){

    fun approvedTime(): String{
        val jsonObj = JSONObject(jsonString)
        return jsonObj.getString("approvedTime").substring(0, 16).replace("T", " ")
    }

    fun weatherItems(): ArrayList<WeatherItem>{
        val jsonObj = JSONObject(jsonString)
        val timeSeriesArray = jsonObj.getJSONArray("timeSeries")
        val weatherItems = ArrayList<WeatherItem>()
        var icon = 0
        var temp = 0.0
        var cloud = 0
        for(i in 0 until timeSeriesArray.length()){
            val forecastItem = timeSeriesArray.getJSONObject(i)
            val time = forecastItem.getString("validTime").substring(0, 16).replace("T", " ")
            val params = forecastItem.getJSONArray("parameters")
            for(j in 0 until params.length()) {
                val param = params.getJSONObject(j)
                if(param.getString("name") == "Wsymb2"){
                    icon = param.getJSONArray("values").getInt(0)
                }
                if(param.getString("name") == "t"){
                    temp = param.getJSONArray("values").getDouble(0)
                }
                if(param.getString("name") == "tcc_mean"){
                    cloud = param.getJSONArray("values").getInt(0)
                }
            }
            Log.d("Item", "The items are: T: $time, Image: $icon, Temp: $temp, Cloud: $cloud")
            weatherItems.add(WeatherItem(time, icon, temp, cloud))
        }
        return weatherItems
    }

    fun getLongLat(): Array<String>{
        var long = jsonString.substringAfter("lon\":").substringBefore(",")
        var lat = jsonString.substringAfter("lat\":").substringBefore(",")
        if(long.substringAfter(".").length > 3) long = long.substring(0, long.indexOf(".")+4)
        if(lat.substringAfter(".").length > 3) lat = lat.substring(0, lat.indexOf(".")+4)
        return arrayOf(long, lat)
    }
}