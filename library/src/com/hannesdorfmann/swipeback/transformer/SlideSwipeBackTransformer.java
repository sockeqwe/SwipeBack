package com.hannesdorfmann.swipeback.transformer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.hannesdorfmann.swipeback.R;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.util.MathUtils;

/**
 * The default SwipeBackTransformator
 * Created by Hannes Dorfmann on 03.01.14.
 */
public class SlideSwipeBackTransformer implements SwipeBackTransformer {

	protected View arrowTop;
	protected View arrowBottom;
	protected TextView textView;

	@Override
	public void onSwipeBackViewCreated(SwipeBack swipeBack, Activity activity,
			final View swipeBackView) {

		arrowTop = swipeBackView.findViewById(R.id.arrowTop);
		arrowBottom = swipeBackView.findViewById(R.id.arrowBottom);
		textView = (TextView) swipeBackView.findViewById(R.id.text);

		onSwipeBackReseted(swipeBack, activity);

	}

	@Override
	public void onSwipeBackCompleted(SwipeBack swipeBack, Activity activity){
		activity.finish();
		activity.overridePendingTransition(R.anim.swipeback_slide_left_in, R.anim.swipeback_slide_right_out);
	}


	@SuppressLint("NewApi")
	@Override
	public void onSwipeBackReseted(SwipeBack swipeBack, Activity activity){

		// Reset the values to the initial state
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			textView.setAlpha(0);
		} else {
			// Pre Honeycomb
		}
	}



	@SuppressLint("NewApi")
	@Override
	public void onSwiping(SwipeBack swipeBack, float openRatio, int pixelOffset){

		// Do step by step animations
		float startAlphaAt = 0.5f;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Android 3 and above

			// Animate the textview
			textView.setAlpha(MathUtils.mapPoint(openRatio, startAlphaAt, 1f,
					0f, 1f));

		} else {
			// Pre Honeycomb (Android 2.x)

			// No good idea how to animate without nineold androids ( I will not
			// bring dependencies to nineold android into the library)
		}

	}



}
