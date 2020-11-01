package com.rahul.thelightningapp.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.rahul.thelightningapp.models.WeatherReport


val items =
    listOf(
        "Grey",
        "Dark Grey",
        "White",
        "Violet",
        "Indigo",
        "Blue",
        "Green",
        "Yellow",
        "Orange",
        "Red",
        "Deep Red"
    )

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}


fun clearETErrors(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText) {
    textInputEditText.addTextChangedListener {
        textInputLayout.error = null
    }
}

private val gson = GsonBuilder()
    .setPrettyPrinting().create()

fun print(o: Any?): String? {
    var str = gson.toJson(o)
    str = str.replace("{", "")
    str = str.replace("}", "")
    str = str.replace(",", "\n")
    return str
}

fun formatReport(report: WeatherReport): String {
    val s = buildString {
        append("The Lightning App Report\n\n")
        append("WindSpeed : ${report.windSpeed} m/s\n")
        append("Cloud color : ${report.cloudColor}\n")
        append("Temperature color : ${report.temperature}\u00B0 C")
    }
    return s
}

fun closeKeyBoard(activity: AppCompatActivity) {
    val view = activity.currentFocus
    if (view != null) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}