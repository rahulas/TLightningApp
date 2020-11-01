package com.rahul.thelightningapp.ui

import android.Manifest
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintAttributes.Margins
import android.print.PrintAttributes.Resolution
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.rahul.thelightningapp.R
import com.rahul.thelightningapp.databinding.ActivityRetrieveBinding
import com.rahul.thelightningapp.models.WeatherReport
import com.rahul.thelightningapp.utils.formatReport
import com.rahul.thelightningapp.viewmodels.UserInputViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class RetrieveActivity : AppCompatActivity() {
    companion object {
        const val SUCCESS_RESULT = "success_result"
        const val REPORT_ID = "report_id"
    }

    private val userInputViewModel: UserInputViewModel by viewModels()

    private lateinit var binding: ActivityRetrieveBinding
    private var permissionsGranted = false
    private var report: WeatherReport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRetrieveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val successResult = intent?.getIntExtra(SUCCESS_RESULT, 0)
        val reportId = intent?.getStringExtra(REPORT_ID)

        Log.v("hello", "" + successResult)

        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(this@RetrieveActivity, "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
                permissionsGranted = true
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@RetrieveActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check();

        if (reportId != null) {
            userInputViewModel.getReport(reportId).observe(this, {
                if (it != null)
                    binding.reportTv.apply {
                        report = it
                        text = formatReport(it)

                        if (permissionsGranted)
                            viewTreeObserver.addOnGlobalLayoutListener {
                                try {
                                    var filePdf: File? = null

                                    filePdf = File(
                                        createAppDir(),
                                        "filename" + System.currentTimeMillis() + ".pdf"
                                    )
                                    if (!filePdf.exists())
                                        filePdf.createNewFile()


                                    val document = getDocument()

                                    val fos = FileOutputStream(filePdf)
                                    document.writeTo(fos)
                                    document.close()
                                    fos.close()
                                    openPDF(filePdf)

                                } catch (e: IOException) {
                                    throw RuntimeException("Error generating file", e)
                                }

                            }
                    }
            })

        }

        binding.openFolder.setOnClickListener {
            openFolder()
        }

        binding.mailPrediction.setOnClickListener {
            mailPredictions()
        }

    }


    private fun getDocument(): PdfDocument {
        val printAttrs =
            PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(Resolution("zooey", PRINT_SERVICE, 450, 700))
                .setMinMargins(Margins.NO_MARGINS).build()
        val document: PdfDocument =
            PrintedPdfDocument(this@RetrieveActivity, printAttrs)
        val pageInfo = PageInfo.Builder(700, 700, 1).create()
        val page: PdfDocument.Page = document.startPage(pageInfo)
        if (page != null) {
            binding.reportTv.layout(
                0, 0, binding.reportTv.width,
                binding.reportTv.height
            )
            Log.i(
                "draw view",
                " content size: " + binding.reportTv.width
                    .toString() + " / " + binding.reportTv.height
            )
            binding.reportTv.draw(page.canvas)
            // Move the canvas for the next view.
            page.canvas.translate(0F, binding.reportTv.height.toFloat())
        }

        document.finishPage(page)

        return document
    }

    private fun openPDF(filePdf: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val pdfURI = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName.toString() + ".provider",
            filePdf
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(pdfURI, "application/pdf")
        startActivity(intent)
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_VIEW)
        val folderURI = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName.toString() + ".provider",
            createAppDir()
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(folderURI, "resource/folder")
        startActivity(intent)
    }

    private fun mailPredictions() {
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
        var body = getString(R.string.app_name)
        if (report != null)
            body = formatReport(report!!)

        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        val file = getLastModified(createAppDir())
        val attachmentURI = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName.toString() + ".provider",
            file!!
        )
        if (permissionsGranted)
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentURI)
        startActivity(Intent.createChooser(emailIntent, "Mail Predictions..."))
    }

    private fun getLastModified(directory: File): File? {
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

    private fun createAppDir(): File {
        val dir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/Download/The Lightning App/"
        )
        dir.mkdirs()
        return dir
    }
}