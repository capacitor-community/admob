package com.getcapacitor.community.admob.nativeads;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.getcapacitor.JSObject;
import com.google.android.gms.ads.nativead.AdChoicesView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

/** Builds the plugin-owned layout around the final Next-Gen SDK {@link NativeAdView}. */
final class PluginNativeAdView {

    private final Context context;
    private final float density;
    private final NativeAdView view;

    private PluginNativeAdView(Context context, NativeAd nativeAd, String template, JSObject style) {
        this.context = context;
        density = context.getResources().getDisplayMetrics().density;
        view = new NativeAdView(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        applyBackground(style);
        bindAssets(nativeAd, "small".equals(template), style);
        view.setNativeAd(nativeAd);
    }

    static NativeAdView create(Context context, NativeAd nativeAd, String template, JSObject style) {
        return new PluginNativeAdView(context, nativeAd, template, style).view;
    }

    private void bindAssets(NativeAd nativeAd, boolean isSmall, JSObject style) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(toPixels(12), toPixels(12), toPixels(12), toPixels(12));
        view.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (!isSmall) {
            MediaView mediaView = new MediaView(context);
            mediaView.setMinimumHeight(toPixels(120));
            content.addView(mediaView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            view.setMediaView(mediaView);
        }

        LinearLayout details = new LinearLayout(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(0, toPixels(isSmall ? 0 : 8), 0, 0);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            isSmall ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT
        );
        content.addView(details, detailsParams);

        addAttribution(details);
        bindTextAssets(details, nativeAd, isSmall, style);
        bindFooter(details, nativeAd, isSmall, style);

        AdChoicesView adChoicesView = new AdChoicesView(context);
        FrameLayout.LayoutParams adChoicesParams = new FrameLayout.LayoutParams(toPixels(24), toPixels(24), Gravity.TOP | Gravity.END);
        view.addView(adChoicesView, adChoicesParams);
        view.setAdChoicesView(adChoicesView);
    }

    private void addAttribution(LinearLayout parent) {
        TextView attribution = new TextView(context);
        attribution.setText("Ad");
        attribution.setTextSize(10);
        attribution.setTextColor(Color.WHITE);
        attribution.setBackgroundColor(Color.DKGRAY);
        attribution.setGravity(Gravity.CENTER);
        parent.addView(attribution, new LinearLayout.LayoutParams(toPixels(28), toPixels(20)));
    }

    private void bindTextAssets(LinearLayout parent, NativeAd nativeAd, boolean isSmall, JSObject style) {
        TextView headline = new TextView(context);
        headline.setText(nativeAd.getHeadline());
        headline.setTextSize(fontStyle(style, "headlineFontSize", 16f, 12f, 24f));
        headline.setTextColor(colorStyle(style, "headlineColor", Color.BLACK));
        headline.setMaxLines(2);
        parent.addView(headline);
        view.setHeadlineView(headline);

        TextView body = new TextView(context);
        body.setText(nativeAd.getBody());
        body.setTextSize(fontStyle(style, "bodyFontSize", 13f, 10f, 18f));
        body.setTextColor(colorStyle(style, "bodyColor", Color.DKGRAY));
        body.setMaxLines(isSmall ? 1 : 2);
        body.setVisibility(isSmall || nativeAd.getBody() == null ? View.GONE : View.VISIBLE);
        parent.addView(body);
        view.setBodyView(body);
    }

    private void bindFooter(LinearLayout parent, NativeAd nativeAd, boolean isSmall, JSObject style) {
        LinearLayout footer = new LinearLayout(context);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        parent.addView(footer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (nativeAd.getIcon() == null) {
            icon.setVisibility(View.GONE);
        } else {
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());
        }
        int iconSize = toPixels(isSmall ? 28 : 32);
        footer.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));
        view.setIconView(icon);

        TextView advertiser = new TextView(context);
        advertiser.setText(nativeAd.getAdvertiser());
        advertiser.setTextSize(11);
        advertiser.setMaxLines(1);
        advertiser.setVisibility(nativeAd.getAdvertiser() == null ? View.GONE : View.VISIBLE);
        footer.addView(advertiser, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        view.setAdvertiserView(advertiser);

        Button callToAction = new Button(context);
        callToAction.setText(nativeAd.getCallToAction());
        callToAction.setTextSize(fontStyle(style, "callToActionFontSize", 13f, 12f, 18f));
        callToAction.setTextColor(colorStyle(style, "callToActionTextColor", Color.WHITE));
        callToAction.setBackgroundColor(colorStyle(style, "callToActionBackgroundColor", Color.rgb(33, 150, 243)));
        callToAction.setVisibility(nativeAd.getCallToAction() == null ? View.GONE : View.VISIBLE);
        footer.addView(callToAction, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, toPixels(isSmall ? 32 : 40)));
        view.setCallToActionView(callToAction);
    }

    private void applyBackground(JSObject style) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(colorStyle(style, "backgroundColor", Color.WHITE));
        background.setCornerRadius(toPixels(floatStyle(style, "cornerRadius", 0f)));
        int borderWidth = toPixels(floatStyle(style, "borderWidth", 0f));
        if (borderWidth > 0) {
            background.setStroke(borderWidth, colorStyle(style, "borderColor", Color.TRANSPARENT));
        }
        view.setBackground(background);
    }

    private int toPixels(double logicalPixels) {
        return Math.round((float) logicalPixels * density);
    }

    private int colorStyle(JSObject style, String key, int fallback) {
        String value = style.getString(key);
        if (value == null) return fallback;
        try {
            String normalized = value.trim();
            String hex = normalized.startsWith("#") ? normalized.substring(1) : normalized;
            long rgba = Long.parseLong(hex, 16);
            if (hex.length() == 6) {
                return Color.rgb((int) (rgba >> 16) & 0xff, (int) (rgba >> 8) & 0xff, (int) rgba & 0xff);
            }
            if (hex.length() == 8) {
                return Color.argb((int) rgba & 0xff, (int) (rgba >> 24) & 0xff, (int) (rgba >> 16) & 0xff, (int) (rgba >> 8) & 0xff);
            }
            return fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private float floatStyle(JSObject style, String key, float fallback) {
        return style.has(key) ? Math.max(0, (float) style.optDouble(key, fallback)) : fallback;
    }

    private float fontStyle(JSObject style, String key, float fallback, float minimum, float maximum) {
        return Math.min(maximum, Math.max(minimum, floatStyle(style, key, fallback)));
    }
}
