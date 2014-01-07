package com.hannesdorfmann.swipeback.example.viewpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hannesdorfmann.swipeback.example.R;

public class SimpleFragment extends Fragment {

	private String text;
	private int backgroundColor;

	private TextView textView;

	public static SimpleFragment newInstance(String text, int backgroundColor) {
		SimpleFragment f = new SimpleFragment();
		f.text = text;
		f.backgroundColor = backgroundColor;
		return f;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_simple, container, false);

		textView = (TextView) v.findViewById(R.id.textView);

		textView.setText(text);

		v.setBackgroundColor(backgroundColor);

		return v;

	}

}
