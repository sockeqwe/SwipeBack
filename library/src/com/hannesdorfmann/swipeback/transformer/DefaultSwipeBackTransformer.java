package com.hannesdorfmann.swipeback.transformer;

import android.app.Activity;
import android.view.View;

import com.hannesdorfmann.swipeback.SwipeBackTransformer;

/**
 * The default SwipeBackTransformator
 * Created by Hannes Dorfmann on 03.01.14.
 */
public class DefaultSwipeBackTransformer implements SwipeBackTransformer{

    public void onSwipeBackViewCreated(Activity activity, View swipeBackView){

    }

    public void onSwipeBackCompleted(Activity activity){
        activity.onBackPressed();
    }


    public void onSwipeBackReseted(Activity activity){

    }



    public void onSwiping(float openRatio, int pixelOffset, int state){

    }
}
