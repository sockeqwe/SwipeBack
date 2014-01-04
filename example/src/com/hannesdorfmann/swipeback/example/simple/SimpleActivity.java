package com.hannesdorfmann.swipeback.example.simple;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;

/**
 * Created by hannes on 03.01.14.
 */
public class SimpleActivity  extends SwipeBackActivity {

    public void onCreate(Bundle saved){
        super.onCreate(saved);

        // Init the swipe back mechanism
        SwipeBack.attach(this, Position.LEFT)
                .setDrawOverlay(false)
                .setDividerEnabled(false)
                .setContentView(R.layout.activity_simple)
                .setMenuView(R.layout.swipeback_default);
    }
}
