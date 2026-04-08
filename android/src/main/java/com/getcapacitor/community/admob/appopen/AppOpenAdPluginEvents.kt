package com.getcapacitor.community.admob.appopen

import com.getcapacitor.community.admob.models.LoadPluginEventNames

object AppOpenAdPluginEvents : LoadPluginEventNames {
    const val Loaded = "appOpenAdLoaded"
    const val FailedToLoad = "appOpenAdFailedToLoad"
    const val Opened = "appOpenAdOpened"
    override val Showed = "appOpenAdOpened"
    override val FailedToShow = "appOpenAdFailedToShow"
    override val Dismissed = "appOpenAdClosed"
}
