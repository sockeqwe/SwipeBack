package com.hannesdorfmann.swipeback.example.bottomxml;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.SwipeBack.Type;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;

public class BottomActivity extends SwipeBackActivity {

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		// Init the swipe back mechanism
		SwipeBack
		.attach(this, Type.BEHIND, Position.BOTTOM,
				SwipeBack.DRAG_WINDOW)
				.setContentView(R.layout.activity_bottom)
				.setSwipeBackView(R.layout.swipeback_bottom)
				.setSwipeBackTransformer(new BottomSwipeBackTransformer());
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.swipeback_bottom_alpha_in,
				R.anim.swipeback_bottom_out);
	}
}
