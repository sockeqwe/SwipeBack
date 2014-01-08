package com.hannesdorfmann.swipeback.example.dragcontent;

import android.os.Bundle;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.SwipeBack.Type;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;

public class DragContentActivity extends SwipeBackActivity {

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		// Init the swipe back mechanism
		SwipeBack
				.attach(this, Type.BEHIND, Position.LEFT,
				SwipeBack.DRAG_CONTENT)
				.setContentView(R.layout.activity_drag_content)
				.setSwipeBackView(R.layout.swipeback_default);

	}
}
