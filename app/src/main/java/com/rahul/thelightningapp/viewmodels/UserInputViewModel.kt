package com.rahul.thelightningapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahul.thelightningapp.models.WeatherReport
import com.rahul.thelightningapp.repos.WeatherRepo

class UserInputViewModel : ViewModel() {

    var saveResult = MutableLiveData<WeatherReport>()
    private val repo: WeatherRepo = WeatherRepo

    init {
        repo.saveResult.observeForever {
            saveResult.value = it
        }
    }

    fun saveWeatherReport(report: WeatherReport) {
        repo.saveWeatherReport(report)
    }

    fun getReport(reportId: String): MutableLiveData<WeatherReport> {
        return repo.getReport(reportId)
    }
}