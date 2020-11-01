package com.rahul.thelightningapp.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.rahul.thelightningapp.databinding.ActivityRetrieveBinding
import com.rahul.thelightningapp.models.WeatherReport
import com.rahul.thelightningapp.utils.*
import com.rahul.thelightningapp.viewmodels.UserInputViewModel
import java.io.File


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
            .check()

        addReportObserver(reportId)

        binding.openFolder.setOnClickListener {
            openSamsungFolder(this@RetrieveActivity)
        }

        binding.mailPrediction.setOnClickListener {
            report?.let { it1 -> mailPredictions(it1, this@RetrieveActivity) }
        }
    }

    private fun addReportObserver(reportId: String?) {
        if (reportId != null) {
            userInputViewModel.getReport(reportId).observe(this, {
                if (it != null)
                    binding.reportTv.apply {
                        report = it
                        text = formatReport(it)

                        val filePdf = File(
                            createAppDir(), "filename" + System.currentTimeMillis() + ".pdf"
                        )
                        createPdf(text.toString(), filePdf)
                        openPDF(filePdf, this@RetrieveActivity)
                    }
            })
        }
    }
}