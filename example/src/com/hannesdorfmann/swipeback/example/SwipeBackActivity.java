package com.hannesdorfmann.swipeback.example;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Hannes Dorfmann on 03.01.14.
 */
public class SwipeBackActivity extends ActionBarActivity{

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);

        SwipeBack.attach(this, Position.LEFT)
                .setDrawOverlay(false)
                .peekSwipeBack(2000);



    }


    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.swipeback_slide_left_in, R.anim.swipeback_slide_right_out);
    }
}
