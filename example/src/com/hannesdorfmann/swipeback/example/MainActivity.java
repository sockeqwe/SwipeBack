package com.hannesdorfmann.swipeback.example;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hannesdorfmann.swipeback.example.simple.SimpleActivity;
import com.hannesdorfmann.swipeback.example.stack.StackActivity;

public class MainActivity extends ActionBarActivity{


	private ListView menuListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String [] menuItems = {
				"Simple",
				"StackAnimated",
				"ViewPager"
		};


		menuListView = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		menuListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_menu_item, R.id.menuText, menuItems));

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			@Override
			public void onDrawerClosed(View view) {

			}

			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {

			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);


		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		initButtons();

		menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position){
				case 0:
					showSimple();
					break;

				case 1:
					showStackAnimated();
					break;

				case 2:
					// TODO implement
					break;
				}

			}
		});
	}

	private void showStackAnimated() {
		Intent i = new Intent(this, StackActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.swipeback_stack_right_in,
				R.anim.swipeback_stack_to_back);
	}


	private void startActivityAnimated(Class<?> clazz){

		Intent i = new Intent(this, clazz);
		startActivity(i);

		overridePendingTransition(R.anim.swipeback_slide_right_in, R.anim.swipeback_slide_left_out);
	}

	private void initButtons(){
		findViewById(R.id.simple).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				showSimple();
			}
		});

		findViewById(R.id.stack).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showStackAnimated();
			}
		});
	}


	private void showSimple(){
		startActivityAnimated(SimpleActivity.class);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}





}
