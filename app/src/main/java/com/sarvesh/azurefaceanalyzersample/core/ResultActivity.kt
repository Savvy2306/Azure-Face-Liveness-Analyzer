package com.sarvesh.azurefaceanalyzersample.core

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sarvesh.azurefaceanalyzersample.MainActivity
import com.sarvesh.azurefaceanalyzersample.R
import com.sarvesh.azurefaceanalyzersample.constants.ERROR
import com.sarvesh.azurefaceanalyzersample.constants.LIVENESS_FAILURE_REASON
import com.sarvesh.azurefaceanalyzersample.constants.LIVENESS_STATUS
import com.sarvesh.azurefaceanalyzersample.constants.VERIFICATION_CONFIDENCE
import com.sarvesh.azurefaceanalyzersample.constants.VERIFICATION_STATUS

class ResultActivity : AppCompatActivity() {

    private val viewMap: LinkedHashMap<TextView, TextView> = LinkedHashMap()
    private lateinit var mRetryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_result)

        viewMap[findViewById(R.id.resultLabel1)] = findViewById(R.id.resultValue1)
        viewMap[findViewById(R.id.resultLabel2)] = findViewById(R.id.resultValue2)
        viewMap[findViewById(R.id.resultLabel3)] = findViewById(R.id.resultValue3)
        viewMap[findViewById(R.id.resultLabel4)] = findViewById(R.id.resultValue4)

        val livenessStatus = intent.getStringExtra(LIVENESS_STATUS)
        val livenessFailureReason = intent.getStringExtra(LIVENESS_FAILURE_REASON)
        val verificationStatus = intent.getStringExtra(VERIFICATION_STATUS)
        val verificationConfidence = intent.getStringExtra(VERIFICATION_CONFIDENCE)
        val errorMessage = intent.getStringExtra(ERROR)

        val itr = viewMap.entries.iterator()
        var mapEntry = itr.next()

        if(!errorMessage.isNullOrBlank()){
            mapEntry.key.text = "Error:"
            mapEntry.value.text = errorMessage

        } else {
            if (!livenessStatus.isNullOrBlank()) {
                mapEntry.key.text = "Liveness status:"
                mapEntry.value.text = livenessStatus
                mapEntry = itr.next()
                mapEntry.key.text = "Liveness Failure Reason:"
                mapEntry.value.text = livenessFailureReason
                mapEntry = itr.next()
            }

            if (!verificationStatus.isNullOrBlank()) {
                mapEntry.key.text = "Verification status:"
                mapEntry.value.text = verificationStatus
                mapEntry = itr.next()
                mapEntry.key.text = "Verification confidence:"
                mapEntry.value.text = verificationConfidence
            }
        }

        mRetryButton = findViewById(R.id.retryButton)
        mRetryButton.setOnClickListener { @Suppress("DEPRECATION") super.onBackPressed() }
    }

@Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        @Suppress("DEPRECATION") super.onBackPressed()
        for (entry in viewMap.entries) {
            entry.key.text = ""
            entry.value.text = ""
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
