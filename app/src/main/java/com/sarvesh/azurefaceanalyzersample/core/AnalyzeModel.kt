package com.sarvesh.azurefaceanalyzersample.core

import android.os.Parcelable
import android.os.ResultReceiver
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalyzeModel(
    val endpoint: String,
    val token: String,
    val resultReceiver: ResultReceiver?
) : Parcelable