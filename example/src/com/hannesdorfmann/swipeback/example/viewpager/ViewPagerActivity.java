package com.hannesdorfmann.swipeback.example.viewpager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.hannesdorfmann.swipeback.SwipeBack.OnInterceptMoveEventListener;
import com.hannesdorfmann.swipeback.example.R;
import com.hannesdorfmann.swipeback.example.SwipeBackActivity;

public class ViewPagerActivity extends SwipeBackActivity {

	private ViewPager mViewPager;
	private int mPagerPosition;
	private int mPagerOffsetPixels;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SwipeBack.attach(this, Position.LEFT)
		.setContentView(R.layout.activity_view_pager)
		.setSwipeBackView(R.layout.swipeback_default)
		.setDividerAsSolidColor(Color.WHITE)
		.setDividerSize(2)
		.setOnInterceptMoveEventListener(
				new OnInterceptMoveEventListener() {
					@Override
					public boolean isViewDraggable(View v, int dx,
							int x, int y) {
						if (v == mViewPager) {
							return !(mPagerPosition == 0 && mPagerOffsetPixels == 0)
									|| dx < 0;
						}

						return false;
					}
				});


		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));

		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mPagerPosition = position;
				mPagerOffsetPixels = positionOffsetPixels;
			}

		});
	}


}
