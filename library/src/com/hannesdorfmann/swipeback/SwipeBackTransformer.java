package com.hannesdorfmann.swipeback;

import android.app.Activity;
import android.view.View;

/**
 * A {@link com.hannesdorfmann.swipeback.SwipeBackTransformer} is responsible to manipulate the
 * swipe back view according to its current state (opening or closing)
 * {@link #onSwiping(float, int, int)} with some nice animations.
 * Also {@link #onSwipeBackCompleted(android.app.Activity)} will be called
 * after the swipe back has been completed to let you finish the activity by setting your own animation tranistion.
 * Created by Hannes Dorfmann on 03.01.14.
 */
public interface SwipeBackTransformer {

    /**
     * The swipe back view is opening (not fully opened yet, but opening)
     */
    public static final int STATE_OPENING = 0;


    /**
     * The swipe back view is closing (not fully closed yet, but closing)
     */
    public  static final int STATE_CLOSING = 1;

    /**
     * This is called if the swipe back view has been created. As parameter you get the concrete
     * view and you can setup your internal things like getting references to the child view by using {@link android.view.View#findViewById(int)} etc.
     * @param swipeBackView
     */
    public void onSwipeBackViewCreated(Activity activity, View swipeBackView);

    /**
     * This method is called after the swipe back has completed.
     * This means that the swipe back view is fully shown and the use has released the finger (up) from the display.
     * Usually you use this method to finish the activity by calling {@link android.app.Activity#onBackPressed()}
     * or  {@link android.app.Activity#finish()}} and you may set the activity animations by using
     * {@link android.app.Activity#overridePendingTransition(int, int)}}
     * @param activity
     */
    public void onSwipeBackCompleted(Activity activity);

    /**
     * This method is called if the swipe back has been canceled / reseted. So the swipe back view is no longer
     * visible, only the main activity layout is visible. This is done if the user closes the swipe back view before it was completed {@link #onSwipeBackCompleted(android.app.Activity)}.
     * @param activity
     */
    public void onSwipeBackReseted(Activity activity);


    /**
     * Called while the swipe back view is opening or closing.
     * This is the point where you do your frame by frame (step by step) animation for your swipe back view
     *
     * @param openRatio a value between 0 and 1 that indicates how much open (visible) the swiping view is
     * @param pixelOffset The number of pixels visible of the swipe back view
     * @param state The state that indicates if it's {@link #STATE_OPENING} or {@link #STATE_CLOSING}
     */
    public void onSwiping(float openRatio, int pixelOffset, int state);



}
