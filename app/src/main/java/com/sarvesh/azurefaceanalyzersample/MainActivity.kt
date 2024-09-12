package com.sarvesh.azurefaceanalyzersample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sarvesh.azurefaceanalyzersample.constants.ERROR
import com.sarvesh.azurefaceanalyzersample.constants.FACE_API_ENDPOINT
import com.sarvesh.azurefaceanalyzersample.constants.LIVENESS_FAILURE_REASON
import com.sarvesh.azurefaceanalyzersample.constants.LIVENESS_STATUS
import com.sarvesh.azurefaceanalyzersample.constants.MODEL
import com.sarvesh.azurefaceanalyzersample.constants.RESULT
import com.sarvesh.azurefaceanalyzersample.constants.VERIFICATION_CONFIDENCE
import com.sarvesh.azurefaceanalyzersample.constants.VERIFICATION_STATUS
import com.sarvesh.azurefaceanalyzersample.core.AnalyzeActivity
import com.sarvesh.azurefaceanalyzersample.core.AnalyzeModel
import com.sarvesh.azurefaceanalyzersample.core.AnalyzedResult
import com.sarvesh.azurefaceanalyzersample.core.AnalyzedResultType
import com.sarvesh.azurefaceanalyzersample.core.ResultActivity
import com.sarvesh.azurefaceanalyzersample.databinding.ActivityMainBinding
import com.sarvesh.azurefaceanalyzersample.utils.Utils
import com.sarvesh.azurefaceanalyzersample.utils.Utils.mSessionToken

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val appRequestCode = 1
    private var appPermissionGranted = false
    private var appPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnStart.setOnClickListener {
            startAnalyzeActivity()
        }
    }

    private fun startAnalyzeActivity() {
        Utils.getFaceAPISessionToken(this, null)
        val token = mSessionToken
        val intent = Intent(this, AnalyzeActivity::class.java)
        val ctx = this.applicationContext
        val resultReceiver = object : ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                when (resultCode) {
                    AnalyzedResultType.RESULT -> {
                        if (resultData == null) {
                            return
                        }
                        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            resultData.getParcelable(RESULT, AnalyzedResult::class.java)
                        } else {
                            resultData.getParcelable(RESULT)
                        }

                        val livenessStatus = result?.livenessStatus.toString()
                        val livenessFailureReason = result?.failureReason.toString()
                        val verificationStatus = result?.verificationStatus.toString()
                        val verificationConfidence = result?.confidence.toString()
                        val resultIntent = Intent(ctx, ResultActivity::class.java).apply {
                            putExtra(LIVENESS_STATUS, livenessStatus)
                            putExtra(LIVENESS_FAILURE_REASON, livenessFailureReason)
                            putExtra(VERIFICATION_STATUS, verificationStatus)
                            putExtra(VERIFICATION_CONFIDENCE, verificationConfidence)
                        }
                        startActivity(resultIntent)
                    }

                    AnalyzedResultType.BACKPRESSED -> {
                        val backIntent = Intent(ctx, MainActivity::class.java)
                        backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(backIntent)
                    }

                    AnalyzedResultType.ERROR -> {
                        val resultIntent = Intent(ctx, ResultActivity::class.java).apply {
                            putExtra(ERROR, R.string.missing_incorrect_settings)
                        }
                        startActivity(resultIntent)
                    }
                }
            }
        }

        val analyzeModel = AnalyzeModel(FACE_API_ENDPOINT, token, resultReceiver)
        intent.putExtra(MODEL, analyzeModel)
        this.startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            appRequestCode -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        appPermissionGranted = false
                        return
                    }
                }
                appPermissionGranted = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions: ArrayList<String> = ArrayList()

        if (!appPermissionGranted && !appPermissionRequested) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            val perms = permissions.toTypedArray()

            if (permissions.size > 1) {
                ActivityCompat.requestPermissions(
                    this, perms, appRequestCode
                )
            }
        }
    }
}