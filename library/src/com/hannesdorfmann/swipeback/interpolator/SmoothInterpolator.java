package com.hannesdorfmann.swipeback.interpolator;

import android.view.animation.Interpolator;

public class SmoothInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float t) {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    }
}
