package com.getcapacitor.community.admob.nativeads

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.getcapacitor.JSObject
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Builds the plugin-owned layout around the final Next-Gen SDK [NativeAdView]. */
internal class PluginNativeAdView private constructor(
    private val context: Context,
    nativeAd: NativeAd,
    template: String,
    private val style: JSObject,
) {
    private val density = context.resources.displayMetrics.density
    private val view = NativeAdView(context)

    init {
        view.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        applyBackground()
        bindAssets(nativeAd, template == "small")
        view.setNativeAd(nativeAd)
    }

    private fun bindAssets(nativeAd: NativeAd, isSmall: Boolean) {
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pixels(12), pixels(12), pixels(12), pixels(12))
        }
        view.addView(
            content,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )

        if (!isSmall) {
            val media = MediaView(context).apply { minimumHeight = pixels(120) }
            content.addView(media, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            view.mediaView = media
        }

        val details = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, pixels(if (isSmall) 0 else 8), 0, 0)
        }
        content.addView(
            details,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (isSmall) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )

        addAttribution(details)
        bindTextAssets(details, nativeAd, isSmall)
        bindFooter(details, nativeAd, isSmall)

        val adChoices = AdChoicesView(context)
        view.addView(adChoices, FrameLayout.LayoutParams(pixels(24), pixels(24), Gravity.TOP or Gravity.END))
        view.adChoicesView = adChoices
    }

    private fun addAttribution(parent: LinearLayout) {
        val attribution = TextView(context).apply {
            text = "Ad"
            textSize = 10f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        parent.addView(attribution, LinearLayout.LayoutParams(pixels(28), pixels(20)))
    }

    private fun bindTextAssets(parent: LinearLayout, nativeAd: NativeAd, isSmall: Boolean) {
        val headline = TextView(context).apply {
            text = nativeAd.headline
            textSize = fontStyle("headlineFontSize", 16f, 12f, 24f)
            setTextColor(colorStyle("headlineColor", Color.BLACK))
            maxLines = 2
        }
        parent.addView(headline)
        view.headlineView = headline

        val body = TextView(context).apply {
            text = nativeAd.body
            textSize = fontStyle("bodyFontSize", 13f, 10f, 18f)
            setTextColor(colorStyle("bodyColor", Color.DKGRAY))
            maxLines = if (isSmall) 1 else 2
            visibility = if (isSmall || nativeAd.body == null) View.GONE else View.VISIBLE
        }
        parent.addView(body)
        view.bodyView = body
    }

    private fun bindFooter(parent: LinearLayout, nativeAd: NativeAd, isSmall: Boolean) {
        val footer = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        parent.addView(
            footer,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        val icon = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            nativeAd.icon?.drawable?.let(::setImageDrawable) ?: run { visibility = View.GONE }
        }
        val iconSize = pixels(if (isSmall) 28 else 32)
        footer.addView(icon, LinearLayout.LayoutParams(iconSize, iconSize))
        view.iconView = icon

        val advertiser = TextView(context).apply {
            text = nativeAd.advertiser
            textSize = 11f
            maxLines = 1
            visibility = if (nativeAd.advertiser == null) View.GONE else View.VISIBLE
        }
        footer.addView(advertiser, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        view.advertiserView = advertiser

        val callToAction = Button(context).apply {
            text = nativeAd.callToAction
            textSize = fontStyle("callToActionFontSize", 13f, 12f, 18f)
            setTextColor(colorStyle("callToActionTextColor", Color.WHITE))
            setBackgroundColor(colorStyle("callToActionBackgroundColor", Color.rgb(33, 150, 243)))
            visibility = if (nativeAd.callToAction == null) View.GONE else View.VISIBLE
        }
        footer.addView(
            callToAction,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, pixels(if (isSmall) 32 else 40)),
        )
        view.callToActionView = callToAction
    }

    private fun applyBackground() {
        val drawable = GradientDrawable().apply {
            setColor(colorStyle("backgroundColor", Color.WHITE))
            cornerRadius = pixels(floatStyle("cornerRadius", 0f)).toFloat()
            val borderWidth = pixels(floatStyle("borderWidth", 0f))
            if (borderWidth > 0) setStroke(borderWidth, colorStyle("borderColor", Color.TRANSPARENT))
        }
        view.background = drawable
    }

    private fun pixels(logicalPixels: Number): Int = (logicalPixels.toDouble() * density).roundToInt()

    private fun colorStyle(key: String, fallback: Int): Int {
        val hex = style.getString(key)?.trim()?.removePrefix("#") ?: return fallback
        val rgba = hex.toLongOrNull(16) ?: return fallback
        return when (hex.length) {
            6 -> Color.rgb((rgba shr 16).toInt() and 0xff, (rgba shr 8).toInt() and 0xff, rgba.toInt() and 0xff)
            8 -> Color.argb(
                rgba.toInt() and 0xff,
                (rgba shr 24).toInt() and 0xff,
                (rgba shr 16).toInt() and 0xff,
                (rgba shr 8).toInt() and 0xff,
            )
            else -> fallback
        }
    }

    private fun floatStyle(key: String, fallback: Float): Float =
        if (style.has(key)) max(0f, style.optDouble(key, fallback.toDouble()).toFloat()) else fallback

    private fun fontStyle(key: String, fallback: Float, minimum: Float, maximum: Float): Float =
        min(maximum, max(minimum, floatStyle(key, fallback)))

    companion object {
        fun create(context: Context, nativeAd: NativeAd, template: String, style: JSObject): NativeAdView =
            PluginNativeAdView(context, nativeAd, template, style).view
    }
}
