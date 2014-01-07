package com.hannesdorfmann.swipeback.example.stack;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;
import com.hannesdorfmann.swipeback.transformer.StackSwipeBackTransformer;

public class StackActivity extends SwipeBackActivity {

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		// Init the swipe back mechanism
		SwipeBack.attach(this, Position.LEFT).setDrawOverlay(false)
		.setDividerEnabled(false)
				.setSwipeBackTransformer(new StackSwipeBackTransformer())
		.setContentView(R.layout.activity_simple)
		.setSwipeBackView(R.layout.swipeback_default);

	}

}
