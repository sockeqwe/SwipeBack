package com.hannesdorfmann.swipeback.transformer;

import android.app.Activity;
import android.view.View;

import com.hannesdorfmann.swipeback.R;
import com.hannesdorfmann.swipeback.SwipeBack;

/**
 * The default SwipeBackTransformator
 * Created by Hannes Dorfmann on 03.01.14.
 */
public class DefaultSwipeBackTransformer implements SwipeBackTransformer{

    public void onSwipeBackViewCreated(SwipeBack swipeBack, Activity activity, View swipeBackView){

    }

    public void onSwipeBackCompleted(SwipeBack swipeBack, Activity activity){
        activity.onBackPressed();
        activity.overridePendingTransition(R.anim.swipeback_slide_left_in, R.anim.swipeback_slide_right_out);
    }


    public void onSwipeBackReseted(SwipeBack swipeBack, Activity activity){

    }



    public void onSwiping(SwipeBack swipeBack, float openRatio, int pixelOffset){

    }
}
