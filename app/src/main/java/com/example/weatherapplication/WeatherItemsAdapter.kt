package com.example.weatherapplication

import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// Create the basic adapter extending from RecyclerView.Adapter
class WeatherItemsAdapter (private val mItems: List<WeatherItem>) : RecyclerView.Adapter<WeatherItemsAdapter.ViewHolder>() {
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val timeTextView = itemView.findViewById<TextView>(R.id.time_text)
        val tempTextView = itemView.findViewById<TextView>(R.id.temp_text)
        val iconImageView = itemView.findViewById<ImageView>(R.id.weather_icon)
        val resources = itemView.resources
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherItemsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemsView = inflater.inflate(R.layout.items_weather, parent, false)
        return ViewHolder(itemsView)
    }
    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: WeatherItemsAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val weatherItem = mItems[position]
        // Set item views based on views and data model
        val timeTextView = viewHolder.timeTextView
        timeTextView.text = weatherItem.time
        val iconImageView = viewHolder.iconImageView
        val currentHour = weatherItem.time.substring(11,13).toInt()
        var iconId: Int
        if(currentHour >= 16 || currentHour < 7){
            iconId = viewHolder.resources.getIdentifier("night" + weatherItem.icon.toString(), "drawable"
                    , "com.example.weatherapplication")
        }else{
            iconId = viewHolder.resources.getIdentifier("day" + weatherItem.icon.toString(), "drawable"
                    , "com.example.weatherapplication")
        }
        iconImageView.setImageResource(iconId)
        val tempTextView = viewHolder.tempTextView
        tempTextView.text = weatherItem.temp.toString() + " °C"
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return mItems.size
    }
}
