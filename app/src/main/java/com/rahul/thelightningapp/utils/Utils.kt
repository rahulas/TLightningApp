package com.rahul.thelightningapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.rahul.thelightningapp.R
import com.rahul.thelightningapp.models.WeatherReport
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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
const val DATE_FORMAT_MMM_dd_yyyy_HH_mm_am_pm = "MMM dd yyyy, HH:mm a"

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

fun getLastModified(directory: File): File? {
    val files = directory?.listFiles { obj: File -> obj.isFile }
    var lastModifiedTime = Long.MIN_VALUE
    var chosenFile: File? = null
    if (files != null) {
        for (file in files) {
            if (file.lastModified() > lastModifiedTime) {
                chosenFile = file
                lastModifiedTime = file.lastModified()
            }
        }
    }
    return chosenFile
}

fun createPdf(textToPdf: String, filePdf: File) {

    val document = PdfDocument()

    var pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(600, 600, 1).create()

    var page: PdfDocument.Page = document.startPage(pageInfo)
    var canvas = page.canvas
    var paint = Paint()
    paint.color = Color.BLACK
    paint.textSize = 20F
    val lines: List<String> = textToPdf.split("\n")
    val bounds = Rect()
    var yOff = 0
    val x = 80F
    val y = 80F
    for (line in lines) {
        canvas.drawText(line, x, y + yOff, paint)
        paint.getTextBounds(line, 0, line.length, bounds)
        yOff += bounds.height() + 20
    }
    val dateStr = "Recorded on ${getCurrentDateTime()}"
    canvas.drawText(dateStr, x, 560F, paint)

    document.finishPage(page)

    if (!filePdf.exists())
        filePdf.createNewFile()
    try {
        document.writeTo(FileOutputStream(filePdf))
    } catch (e: IOException) {
        Log.e("main", "error $e")
    }
    document.close()

}

fun getCurrentDateTime(): String {
    val now = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(DATE_FORMAT_MMM_dd_yyyy_HH_mm_am_pm)
    return dateFormat.format(Date(now.timeInMillis))
}


fun createAppDir(): File {
    val dir = File(
        Environment.getExternalStorageDirectory()
            .toString() + "/Download/The Lightning App/"
    )
    dir.mkdirs()
    return dir
}


fun openFolder(activity: AppCompatActivity) {
    val intent = Intent(Intent.ACTION_VIEW)

    val folderURI = FileProvider.getUriForFile(
        activity,
        activity.applicationContext.packageName.toString() + ".provider", createAppDir()
    )

    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(folderURI, "resource/folder")
    activity.startActivity(intent)
}

fun openFolder1(activity: AppCompatActivity) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    val folderURI = FileProvider.getUriForFile(
        activity,
        activity.applicationContext.packageName.toString() + ".provider", createAppDir()
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.setDataAndType(folderURI, "image/jpeg")
    activity.startActivity(Intent.createChooser(intent, "Open folder"))
}

fun openSamsungFolder(activity: AppCompatActivity) {
    val intent = activity.packageManager.getLaunchIntentForPackage("com.sec.android.app.myfiles")
    intent!!.action = "samsung.myfiles.intent.action.LAUNCH_MY_FILES"
    intent.putExtra("samsung.myfiles.intent.extra.START_PATH", createAppDir().absolutePath)
    activity.startActivity(intent)
}

fun openPDF(filePdf: File, activity: AppCompatActivity) {
    val intent = Intent(Intent.ACTION_VIEW)
    val pdfURI = FileProvider.getUriForFile(
        activity,
        activity.applicationContext.packageName.toString() + ".provider",
        filePdf
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(pdfURI, "application/pdf")
    activity.startActivity(intent)
}


fun mailPredictions(report: WeatherReport, activity: AppCompatActivity) {
    val TO = arrayOf("")
    val CC = arrayOf("")

    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.data = Uri.parse("mailto:")
    emailIntent.type = "vnd.android.cursor.dir/email";
    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    emailIntent.putExtra(
        Intent.EXTRA_SUBJECT,
        "Lightning App Predictions"
    )
    var body = activity.getString(R.string.app_name)
    if (report != null)
        body = formatReport(report!!)

    emailIntent.putExtra(Intent.EXTRA_TEXT, body)

    val file = getLastModified(createAppDir())
    val attachmentURI = FileProvider.getUriForFile(
        activity,
        activity.applicationContext.packageName.toString() + ".provider",
        file!!
    )
    emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentURI)
    activity.startActivity(Intent.createChooser(emailIntent, "Mail Predictions..."))
}