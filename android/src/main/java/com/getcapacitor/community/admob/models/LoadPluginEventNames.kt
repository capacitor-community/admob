package com.getcapacitor.community.admob.models

interface LoadPluginEventNames {
    val Clicked: String? get() = null
    val Showed: String
    val FailedToShow: String
    val Dismissed: String
}
