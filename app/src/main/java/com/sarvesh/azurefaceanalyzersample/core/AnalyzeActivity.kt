package com.sarvesh.azurefaceanalyzersample.core

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import android.view.SurfaceView
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import com.azure.ai.vision.common.internal.implementation.EventListener
import com.azure.android.ai.vision.common.VisionServiceOptions
import com.azure.android.ai.vision.common.VisionSource
import com.azure.android.ai.vision.common.VisionSourceOptions
import com.azure.android.ai.vision.faceanalyzer.ActionRequiredFromApplication
import com.azure.android.ai.vision.faceanalyzer.FaceAnalysisOptions
import com.azure.android.ai.vision.faceanalyzer.FaceAnalysisStoppedEventArgs
import com.azure.android.ai.vision.faceanalyzer.FaceAnalysisStoppedReason
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzedEventArgs
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzer
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzerBuilder
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzerCreateOptions
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzerMode
import com.azure.android.ai.vision.faceanalyzer.FaceAnalyzingEventArgs
import com.azure.android.ai.vision.faceanalyzer.FaceSelectionMode
import com.azure.android.ai.vision.faceanalyzer.FeedbackForFace
import com.azure.android.ai.vision.faceanalyzer.LivenessFailureReason
import com.azure.android.ai.vision.faceanalyzer.LivenessStatus
import com.azure.android.ai.vision.faceanalyzer.RecognitionStatus
import com.azure.android.core.credential.AccessToken
import com.azure.android.core.credential.TokenCredential
import com.azure.android.core.credential.TokenRequestContext
import com.sarvesh.azurefaceanalyzersample.R
import com.sarvesh.azurefaceanalyzersample.constants.MODEL
import com.sarvesh.azurefaceanalyzersample.constants.RESULT
import com.sarvesh.azurefaceanalyzersample.databinding.ActivityAnalyzeBinding
import org.threeten.bp.OffsetDateTime
import kotlin.math.sqrt

open class AnalyzeActivity : AppCompatActivity() {
    class StringTokenCredential(token: String) : TokenCredential {
        override fun getToken(
            request: TokenRequestContext, callback: TokenCredential.TokenCredentialCallback
        ) {
            callback.onSuccess(_token)
        }

        private var _token: AccessToken? = null

        init {
            _token = AccessToken(token, OffsetDateTime.MAX)
        }
    }

    private lateinit var binding: ActivityAnalyzeBinding
    private lateinit var surfaceView: SurfaceView
    private val previewAreaRatio = 0.12
    private var lastTextUpdateTime = 0L
    private val delayMillis = 200L
    private var visionSource: VisionSource? = null
    private var faceAnalyzer: FaceAnalyzer? = null
    private var faceAnalysisOptions: FaceAnalysisOptions? = null
    private var serviceOptions: VisionServiceOptions? = null
    private var faceApiEndpoint: String? = null
    private var sessionToken: String? = null
    private var resultReceiver: ResultReceiver? = null
    private var backPressed: Boolean = false
    private var handler = Handler(Looper.getMainLooper())
    private var doneAnalyzing: Boolean = false
    private var tag = AnalyzeActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        surfaceView = SurfaceView(this)
        binding.cameraPreview.removeAllViews()
        binding.cameraPreview.addView(surfaceView)
        binding.cameraPreview.visibility = View.INVISIBLE
        val analyzeModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(MODEL, AnalyzeModel::class.java)
        } else {
            intent.getParcelableExtra(MODEL)
        }
        faceApiEndpoint = analyzeModel?.endpoint
        sessionToken = analyzeModel?.token
        resultReceiver = analyzeModel?.resultReceiver
        if (faceApiEndpoint.isNullOrBlank() || sessionToken.isNullOrBlank()) {
            resultReceiver?.send(AnalyzedResultType.ERROR, null)
            return
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                synchronized(this) {
                    backPressed = true
                }
                faceAnalyzer?.stopAnalyzeOnce()
                val bd = Bundle()
                resultReceiver?.send(AnalyzedResultType.BACKPRESSED, bd)
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (faceAnalyzer == null) {
            initializeConfig()
            val visionSourceOptions = VisionSourceOptions(this, this as LifecycleOwner)
            visionSourceOptions.setPreview(surfaceView)
            visionSource = VisionSource.fromDefaultCamera(visionSourceOptions)
            displayCameraOnLayout()
            createFaceAnalyzer()
        }
        startAnalyzeOnce()
    }


    override fun onDestroy() {
        super.onDestroy()
        visionSource?.close()
        visionSource = null
        serviceOptions?.close()
        serviceOptions = null
        faceAnalysisOptions?.close()
        faceAnalysisOptions = null
        try {
            faceAnalyzer?.close()
            faceAnalyzer = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun initializeConfig() {
        if (!faceApiEndpoint.isNullOrBlank()) {
            serviceOptions = VisionServiceOptions(StringTokenCredential(sessionToken.toString()))
        }
    }

    private fun createFaceAnalyzer() {
        if (serviceOptions == null) {
            Log.e(tag, getString(R.string.service_options_are_not_initialize))
            return
        }

        if (visionSource == null) {
            Log.e(tag, getString(R.string.vision_source_is_not_initialize))
            return
        }

        try {
            FaceAnalyzerCreateOptions().use { createOptions ->
                createOptions.setFaceAnalyzerMode(FaceAnalyzerMode.TRACK_FACES_ACROSS_IMAGE_STREAM)

                faceAnalyzer =
                    FaceAnalyzerBuilder().serviceOptions(serviceOptions).source(visionSource)
                        .createOptions(createOptions).build().get()

                faceAnalyzer?.apply {
                    this.analyzed.addEventListener(analyzedListener)
                    this.analyzing.addEventListener(analyzingListener)
                    this.stopped.addEventListener(stoppedListener)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, getString(R.string.error_initializing_faceAnalyzer), e)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected var analyzingListener = EventListener<FaceAnalyzingEventArgs> { _, e ->

        e.result.use { result ->
            if (result.faces.isNotEmpty()) {
                val face = result.faces.iterator().next()
                val requiredAction = face.actionRequiredFromApplicationTask?.action
                when (requiredAction) {
                    ActionRequiredFromApplication.BRIGHTEN_DISPLAY -> {
                        binding.activityMainLayout.setBackgroundColor(Color.WHITE)
                        face.actionRequiredFromApplicationTask.setAsCompleted()
                    }

                    ActionRequiredFromApplication.DARKEN_DISPLAY -> {
                        binding.activityMainLayout.setBackgroundColor(Color.BLACK)
                        face.actionRequiredFromApplicationTask.setAsCompleted()
                    }

                    ActionRequiredFromApplication.STOP_CAMERA -> {
                        binding.cameraPreview.visibility = View.INVISIBLE
                        face.actionRequiredFromApplicationTask.setAsCompleted()
                    }

                    else -> {
                        //Add Any other action required from application
                    }
                }

                if (!doneAnalyzing) {
                    var feedbackMessage = mapFeedbackToMessage(FeedbackForFace.NONE)
                    if (face.feedbackForFace != null) {
                        feedbackMessage = mapFeedbackToMessage(face.feedbackForFace)
                    }

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTextUpdateTime >= delayMillis) {
                        updateTextView(feedbackMessage)
                        lastTextUpdateTime = currentTime
                    }
                }
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected var analyzedListener = EventListener<FaceAnalyzedEventArgs> { _, e ->
        val bd = Bundle()
        e.result.use { result ->
            if (result.faces.isNotEmpty()) {
                val face = result.faces.iterator().next()
                val livenessStatus: LivenessStatus =
                    face.livenessResult?.livenessStatus ?: LivenessStatus.FAILED
                val livenessFailureReason =
                    face.livenessResult?.livenessFailureReason ?: LivenessFailureReason.NONE
                val verifyStatus =
                    face.recognitionResult?.recognitionStatus ?: RecognitionStatus.NOT_COMPUTED
                val verifyConfidence = face.recognitionResult?.confidence ?: Float.NaN
                val resultIdsList: ArrayList<String> = ArrayList()
                if (face.livenessResult.resultId != null) {
                    resultIdsList.add(face.livenessResult.resultId.toString())
                }
                val digest = result.details?.digest ?: ""
                val resultIds = resultIdsList.joinToString(",")
                val analyzedResult = AnalyzedResult(
                    livenessStatus,
                    livenessFailureReason,
                    verifyStatus,
                    verifyConfidence,
                    resultIds,
                    digest
                )
                bd.putParcelable(RESULT, analyzedResult)
            } else {
                val analyzedResult = AnalyzedResult(
                    LivenessStatus.NOT_COMPUTED,
                    LivenessFailureReason.NONE,
                    RecognitionStatus.NOT_COMPUTED,
                    Float.NaN,
                    "",
                    ""
                )
                bd.putParcelable(RESULT, analyzedResult)
            }
        }

        synchronized(this) {
            if (!backPressed) {
                resultReceiver?.send(AnalyzedResultType.RESULT, bd)
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected var stoppedListener = EventListener<FaceAnalysisStoppedEventArgs> { _, e ->
        if (e.reason == FaceAnalysisStoppedReason.ERROR) {
            resultReceiver?.send(AnalyzedResultType.ERROR, null)
        }
    }

    private fun startAnalyzeOnce() {

        binding.cameraPreview.visibility = View.VISIBLE
        if (serviceOptions == null) {
            resultReceiver?.send(AnalyzedResultType.ERROR, null)
            return
        }

        faceAnalysisOptions = FaceAnalysisOptions()

        faceAnalysisOptions?.setFaceSelectionMode(FaceSelectionMode.LARGEST)

        try {
            faceAnalyzer?.analyzeOnceAsync(faceAnalysisOptions)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        doneAnalyzing = false
    }

    private fun updateTextView(newText: String) {
        handler.post {
            binding.instructionString.text = newText
        }
    }

    private fun displayCameraOnLayout() {
        val previewSize = visionSource?.cameraPreviewFormat
        val params = binding.cameraPreview.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = previewSize?.height.toString() + ":" + previewSize?.width
        params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        params.matchConstraintDefaultWidth = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
        params.matchConstraintPercentWidth =
            sqrt(4 * previewAreaRatio * Resources.getSystem().displayMetrics.heightPixels / Math.PI / Resources.getSystem().displayMetrics.widthPixels).toFloat()
        binding.cameraPreview.layoutParams = params
    }

    private fun mapFeedbackToMessage(feedback: FeedbackForFace): String {
        return when (feedback) {
            FeedbackForFace.NONE -> getString(R.string.feedback_none)
            FeedbackForFace.LOOK_AT_CAMERA -> getString(R.string.feedback_look_at_camera)
            FeedbackForFace.FACE_NOT_CENTERED -> getString(R.string.feedback_face_not_centered)
            FeedbackForFace.MOVE_CLOSER -> getString(R.string.feedback_move_closer)
            FeedbackForFace.CONTINUE_TO_MOVE_CLOSER -> getString(R.string.feedback_continue_to_move_closer)
            FeedbackForFace.MOVE_BACK -> getString(R.string.feedback_move_back)
            FeedbackForFace.REDUCE_MOVEMENT -> getString(R.string.feedback_reduce_movement)
            FeedbackForFace.SMILE -> getString(R.string.feedback_smile)
            FeedbackForFace.ATTENTION_NOT_NEEDED -> {
                doneAnalyzing = true
                getString(R.string.feedback_attention_not_needed)
            }
        }
    }
}
