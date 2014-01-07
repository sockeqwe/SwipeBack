package com.hannesdorfmann.swipeback.example.overlay;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.SwipeBack.Type;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;

public class OverlayActivity extends SwipeBackActivity {

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		// Init the swipe back mechanism
		SwipeBack.attach(this, Type.OVERLAY, Position.LEFT)
		.setContentView(R.layout.activity_overlay)
		.setSwipeBackView(R.layout.swipeback_default);

	}
}
