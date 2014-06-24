package com.hannesdorfmann.swipeback;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
final class ViewHelper {

    private ViewHelper() {
    }

    public static int getLeft(View v) {
        if (SwipeBack.USE_TRANSLATIONS) {
            return (int) (v.getLeft() + v.getTranslationX());
        }

        return v.getLeft();
    }

    public static int getTop(View v) {
        if (SwipeBack.USE_TRANSLATIONS) {
            return (int) (v.getTop() + v.getTranslationY());
        }

        return v.getTop();
    }

    public static int getRight(View v) {
        if (SwipeBack.USE_TRANSLATIONS) {
            return (int) (v.getRight() + v.getTranslationX());
        }

        return v.getRight();
    }

    public static int getBottom(View v) {
        if (SwipeBack.USE_TRANSLATIONS) {
            return (int) (v.getBottom() + v.getTranslationY());
        }

        return v.getBottom();
    }

    @SuppressWarnings("")
    public static int getLayoutDirection(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return v.getLayoutDirection();
        }

        return View.LAYOUT_DIRECTION_LTR;
    }
}
