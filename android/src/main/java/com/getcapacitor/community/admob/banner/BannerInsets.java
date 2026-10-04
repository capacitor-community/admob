package com.getcapacitor.community.admob.banner;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class BannerInsets {

    private BannerInsets() {}

    static void observe(View banner, String position, int margin, boolean systemBarsHandlesInsets) {
        banner.addOnAttachStateChangeListener(
            new View.OnAttachStateChangeListener() {
                private ViewTreeObserver observer;
                private final int[] rootLocation = new int[2];
                private final int[] parentLocation = new int[2];
                private final ViewTreeObserver.OnGlobalLayoutListener listener = () -> {
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(banner);
                    if (insets == null || !(banner.getParent() instanceof View)) {
                        return;
                    }
                    View parent = (View) banner.getParent();
                    View root = banner.getRootView();
                    root.getLocationInWindow(rootLocation);
                    parent.getLocationInWindow(parentLocation);
                    Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                    boolean keyboardVisible =
                        insets.isVisible(WindowInsetsCompat.Type.ime()) && insets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0;

                    // Older Capacitor versions consume insets before they reach the banner.
                    // Read window insets and subtract the space already provided by the parent.
                    int top = margin;
                    int bottom = margin;
                    if ("TOP_CENTER".equals(position)) {
                        top += Math.max(0, rootLocation[1] + safe.top - parentLocation[1] - parent.getPaddingTop());
                    } else if (!keyboardVisible || !systemBarsHandlesInsets) {
                        bottom += Math.max(
                            0,
                            parentLocation[1] +
                                parent.getHeight() -
                                parent.getPaddingBottom() -
                                (rootLocation[1] + root.getHeight() - safe.bottom)
                        );
                    }
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) banner.getLayoutParams();
                    if (params.topMargin != top || params.bottomMargin != bottom) {
                        params.topMargin = top;
                        params.bottomMargin = bottom;
                        banner.setLayoutParams(params);
                    }
                };

                @Override
                public void onViewAttachedToWindow(View view) {
                    observer = view.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(listener);
                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(listener);
                    }
                }
            }
        );
    }
}
