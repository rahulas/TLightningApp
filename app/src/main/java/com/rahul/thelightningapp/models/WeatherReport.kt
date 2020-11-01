package com.rahul.thelightningapp.models

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
class WeatherReport : Parcelable {
    var windSpeed: Double? = null
    var cloudColor: String? = null
    var temperature: Double? = null
    var reportID: String? = null
    var successResult: Int? = null

    companion object {
        fun DocumentSnapshot.toWeatherReport(): WeatherReport? {
            return try {
                val windSpeed = getDouble("windSpeed")!!
                val cloudColor = getString("cloudColor")!!
                val temperature = getDouble("temperature")!!
                val report = WeatherReport()
                report.reportID = id
                report.windSpeed = windSpeed
                report.cloudColor = cloudColor
                report.temperature = temperature
                report
            } catch (e: Exception) {
                Log.e(TAG, "Error converting weather report", e)
                null
            }
        }

        private const val TAG = "WeatherReport"
    }

}