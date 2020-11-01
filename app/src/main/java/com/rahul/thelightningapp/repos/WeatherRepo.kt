package com.rahul.thelightningapp.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.rahul.thelightningapp.models.WeatherReport

object WeatherRepo {
    private const val TAG = "WeatherRepo"

    private val db = Firebase.firestore

    var saveResult = MutableLiveData<WeatherReport>()

    fun saveWeatherReport(report: WeatherReport) {
        db.collection("weather_reports")
            .add(report)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val report = WeatherReport()
                report.reportID = documentReference.id
                report.successResult = 1
                saveResult.postValue(report)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                val report = WeatherReport()
                report.successResult = 0
                saveResult.postValue(report)
            }
    }


    fun getReport(reportId: String): MutableLiveData<WeatherReport> {
        val reportM = MutableLiveData<WeatherReport>()
        db.collection("weather_reports")
            .document(reportId).get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "${result.id} => ${result.data}")
                val gson = Gson()
                val jsonElement = gson.toJsonTree(result.data)
                val report = gson.fromJson(jsonElement, WeatherReport::class.java)
                reportM.postValue(report)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                reportM.postValue(null)
            }
        return reportM
    }

}