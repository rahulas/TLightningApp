package com.rahul.thelightningapp.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.rahul.thelightningapp.R
import com.rahul.thelightningapp.databinding.ActivityUserInputBinding
import com.rahul.thelightningapp.models.WeatherReport
import com.rahul.thelightningapp.utils.clearETErrors
import com.rahul.thelightningapp.utils.closeKeyBoard
import com.rahul.thelightningapp.utils.items
import com.rahul.thelightningapp.utils.visible
import com.rahul.thelightningapp.viewmodels.UserInputViewModel
import kotlinx.android.synthetic.main.activity_user_input.*


class UserInputActivity : AppCompatActivity() {

    private val userInputViewModel: UserInputViewModel by viewModels()
    private lateinit var binding: ActivityUserInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.windSpeedTextInputLay.apply {
            setEndIconOnClickListener {
                binding.windSpeedEt.text?.clear()
            }
        }
        binding.windSpeedEt.setOnEditorActionListener { v, actionId, event ->
            closeKeyBoard(this@UserInputActivity)
            binding.cloudColorEt.showDropDown()
            true
        }

        val adapter = ArrayAdapter(this, R.layout.cloud_color_list_item, items)
        binding.cloudColorEt.apply {
            setAdapter(adapter)
            onItemClickListener =
                OnItemClickListener { parent, arg1, pos, id ->
                    binding.tempEt.requestFocus()
                }
        }

        binding.tempTextInputLay.apply {
            setEndIconOnClickListener {
                binding.tempEt.text?.clear()
            }
        }
        binding.tempEt.setOnEditorActionListener { v, actionId, event ->
            closeKeyBoard(this@UserInputActivity)
            if (isDataValid()) {
                saveReport()
            }
            true
        }

        binding.predictButton.setOnClickListener {
            if (isDataValid()) {
                saveReport()
            }
        }

        clearETErrors(binding.windSpeedTextInputLay, binding.windSpeedEt)
        clearETErrors(binding.tempTextInputLay, binding.tempEt)

        userInputViewModel.saveResult.observe(this, {
            binding.progress.hide()
            binding.progress.visible(false)
            val intent = Intent(this@UserInputActivity, RetrieveActivity::class.java)
            intent.putExtra(RetrieveActivity.SUCCESS_RESULT, it.successResult)
            if (it.reportID?.isNotBlank()!!)
                intent.putExtra(RetrieveActivity.REPORT_ID, it.reportID)
            startActivity(intent)
        })

    }

    private fun isDataValid(): Boolean {
        var valid = true

        if (binding.windSpeedEt.text?.isBlank()!!) {
            windSpeedTextInputLay.error = getString(R.string.pls_enter_wind_speed)
            valid = false
        } else if (binding.windSpeedEt.text?.toString()?.toDouble()!! < 2) {
            windSpeedTextInputLay.error = getString(R.string.min_speed_error)
            valid = false
        } else if (binding.cloudColorEt.text?.isBlank()!!) {
            cloudColorTextInputLay.error = getString(R.string.pls_select_cloud_color)
            valid = false
        } else if (binding.tempEt.text?.isBlank()!!) {
            tempTextInputLay.error = getString(R.string.pls_enter_temp)
            valid = false
        }

        return valid
    }

    private fun saveReport() {
        binding.progress.visible(true)
        binding.progress.show()
        val windSpeed = binding.windSpeedEt.text.toString().toDouble()
        val cloudColor = binding.cloudColorEt.text.toString()
        val temp = binding.tempEt.text.toString().toDouble()
        val report = WeatherReport()
        report.windSpeed = windSpeed
        report.cloudColor = cloudColor
        report.temperature = temp
        userInputViewModel.saveWeatherReport(report)
    }
}