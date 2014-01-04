package com.hannesdorfmann.swipeback.example;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Created by Hannes Dorfmann on 03.01.14.
 */
public class SwipeBackActivity extends ActionBarActivity{

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Init the swipe back mechanism
        SwipeBack.attach(this, Position.LEFT)
                .setDrawOverlay(false)
                .setDropShadowEnabled(false)
                .peekSwipeBack(1000, 500);




    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.swipeback_slide_left_in, R.anim.swipeback_slide_right_out);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
