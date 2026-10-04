package com.getcapacitor.community.admob.banner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

class BannerInsetsTest {

    @ParameterizedTest
    @CsvSource({
        "BOTTOM_CENTER,false,true,24,0,0,1000,0,0,7,31",
        "BOTTOM_CENTER,true,true,24,300,0,1000,0,0,7,7",
        "BOTTOM_CENTER,true,true,24,0,0,1000,0,0,7,31",
        "BOTTOM_CENTER,false,true,0,0,0,1000,0,0,7,7",
        "BOTTOM_CENTER,true,true,0,300,0,1000,0,0,7,7",
        "BOTTOM_CENTER,false,false,24,0,0,1000,0,0,7,31",
        "BOTTOM_CENTER,true,false,24,300,0,1000,0,0,7,31",
        "TOP_CENTER,false,true,24,0,0,1000,0,0,39,7",
        "TOP_CENTER,true,true,24,300,0,1000,0,0,39,7",
        // Parent padding (modern Capacitor) and parent margins (legacy/custom hosts).
        "BOTTOM_CENTER,false,true,24,0,0,1000,32,24,7,7",
        "TOP_CENTER,false,true,24,0,0,1000,32,24,7,7",
        "BOTTOM_CENTER,false,true,24,0,32,944,0,0,7,7",
        "TOP_CENTER,false,true,24,0,32,944,0,0,7,7",
        "BOTTOM_CENTER,false,true,24,0,0,1000,0,10,7,21",
        "TOP_CENTER,false,true,24,0,10,990,10,0,19,7",
        "BOTTOM_CENTER,true,true,24,300,0,700,0,0,7,7",
        "BOTTOM_CENTER,true,false,24,300,0,700,0,0,7,7"
    })
    void respectsWindowInsetsAndParentSpace(
        String position,
        boolean imeVisible,
        boolean handlesInsets,
        int safeBottom,
        int imeBottom,
        int parentTop,
        int parentHeight,
        int paddingTop,
        int paddingBottom,
        int expectedTop,
        int expectedBottom
    ) {
        Fixture f = new Fixture();
        when(f.insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
            Insets.of(0, 32, 0, safeBottom)
        );
        when(f.insets.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(imeVisible);
        when(f.insets.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, imeBottom));
        when(f.parent.getHeight()).thenReturn(parentHeight);
        when(f.parent.getPaddingTop()).thenReturn(paddingTop);
        when(f.parent.getPaddingBottom()).thenReturn(paddingBottom);
        // Nonzero window origin must cancel out of the geometry calculation.
        doAnswer((i) -> {
            ((int[]) i.getArgument(0))[1] = 100;
            return null;
        })
            .when(f.root)
            .getLocationInWindow(any());
        doAnswer((i) -> {
            ((int[]) i.getArgument(0))[1] = 100 + parentTop;
            return null;
        })
            .when(f.parent)
            .getLocationInWindow(any());
        try (var compat = mockStatic(ViewCompat.class)) {
            compat.when(() -> ViewCompat.getRootWindowInsets(f.banner)).thenReturn(f.insets);
            var listener = f.attach(position, handlesInsets);
            listener.onGlobalLayout();
            assertEquals(expectedTop, f.params.topMargin);
            assertEquals(expectedBottom, f.params.bottomMargin);
            assertEquals(11, f.params.leftMargin);
            assertEquals(13, f.params.rightMargin);
            clearInvocations(f.banner);
            listener.onGlobalLayout();
            verify(f.banner, never()).setLayoutParams(any());
            verify(f.parent, never()).setOnApplyWindowInsetsListener(any());
        }
    }

    @Test
    void keyboardCloseRestoresMarginsAndDetachRemovesObserver() {
        Fixture f = new Fixture();
        when(f.insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
            Insets.of(0, 32, 0, 24)
        );
        when(f.insets.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(false, true, false, false);
        when(f.insets.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, 300));
        try (var compat = mockStatic(ViewCompat.class)) {
            compat.when(() -> ViewCompat.getRootWindowInsets(f.banner)).thenReturn(f.insets);
            var listener = f.attach("BOTTOM_CENTER", true);
            for (int bottom : new int[] { 31, 7, 31, 31 }) {
                listener.onGlobalLayout();
                assertEquals(bottom, f.params.bottomMargin);
            }
            verify(f.banner, times(3)).setLayoutParams(f.params);
            f.attachListener.onViewDetachedFromWindow(f.banner);
            verify(f.observer).removeOnGlobalLayoutListener(listener);
            ViewTreeObserver next = mock(ViewTreeObserver.class);
            when(f.banner.getViewTreeObserver()).thenReturn(next);
            f.attachListener.onViewAttachedToWindow(f.banner);
            verify(next).addOnGlobalLayoutListener(listener);
        }
    }

    @Test
    void missingInsetsLeaveMarginsUntouched() {
        Fixture f = new Fixture();
        try (var compat = mockStatic(ViewCompat.class)) {
            f.attach("BOTTOM_CENTER", true).onGlobalLayout();
            verify(f.banner, never()).setLayoutParams(any());
        }
    }

    static class Fixture {

        final View banner = mock(View.class);
        final ViewGroup parent = mock(ViewGroup.class);
        final View root = mock(View.class);
        final ViewTreeObserver observer = mock(ViewTreeObserver.class);
        final ViewGroup.MarginLayoutParams params = mock(ViewGroup.MarginLayoutParams.class);
        final WindowInsetsCompat insets = mock(WindowInsetsCompat.class);
        View.OnAttachStateChangeListener attachListener;

        Fixture() {
            params.topMargin = 7;
            params.bottomMargin = 7;
            params.leftMargin = 11;
            params.rightMargin = 13;
            when(banner.getParent()).thenReturn(parent);
            when(banner.getRootView()).thenReturn(root);
            when(banner.getLayoutParams()).thenReturn(params);
            when(banner.getViewTreeObserver()).thenReturn(observer);
            when(observer.isAlive()).thenReturn(true);
            when(root.getHeight()).thenReturn(1000);
            when(parent.getHeight()).thenReturn(1000);
        }

        ViewTreeObserver.OnGlobalLayoutListener attach(String position, boolean handlesInsets) {
            BannerInsets.observe(banner, position, 7, handlesInsets);
            var attach = ArgumentCaptor.forClass(View.OnAttachStateChangeListener.class);
            verify(banner).addOnAttachStateChangeListener(attach.capture());
            attachListener = attach.getValue();
            attachListener.onViewAttachedToWindow(banner);
            var layout = ArgumentCaptor.forClass(ViewTreeObserver.OnGlobalLayoutListener.class);
            verify(observer).addOnGlobalLayoutListener(layout.capture());
            return layout.getValue();
        }
    }
}
