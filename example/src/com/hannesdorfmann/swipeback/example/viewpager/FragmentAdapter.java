package com.hannesdorfmann.swipeback.example.viewpager;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentAdapter extends FragmentPagerAdapter {

	private final int colors[] = { Color.parseColor("#FA5F67"),
			Color.parseColor("#D973D5"), Color.parseColor("#6D64CC"),
			Color.parseColor("#64CC9D"), Color.parseColor("#E6DD7A") };

	public FragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		return SimpleFragment.newInstance("Fragment " + i, colors[i]);
	}

	@Override
	public int getCount() {
		return 5;
	}

}
