package com.nj.cantus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by FinnYu on 2018. 2. 24..
 */

public class SettingActivity extends AppCompatActivity
{
	private ActionBar actionBar;

	private int speed_drawable;
	private int start_drawable;

	private ArrayList<Button> speeds = new ArrayList<>();
	private ArrayList<Button> starts = new ArrayList<>();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting);

		Button speed_pink = (Button)findViewById(R.id.color_speed_pink);
		speed_pink.setTransitionName("speed_pink");
		speeds.add(speed_pink);
		Button speed_yellow = (Button)findViewById(R.id.color_speed_yellow);
		speed_yellow.setTransitionName("speed_yellow");
		speeds.add(speed_yellow);
		Button speed_skyblue = (Button)findViewById(R.id.color_speed_skyblue);
		speed_skyblue.setTransitionName("speed_skyblue");
		speeds.add(speed_skyblue);
		Button speed_green = (Button)findViewById(R.id.color_speed_green);
		speed_green.setTransitionName("speed_green");
		speeds.add(speed_green);
		Button speed_red = (Button)findViewById(R.id.color_speed_red);
		speed_red.setTransitionName("speed_red");
		speeds.add(speed_red);
		Button speed_purple = (Button)findViewById(R.id.color_speed_purple);
		speed_purple.setTransitionName("speed_purple");
		speeds.add(speed_purple);

		Button start_pink = (Button)findViewById(R.id.color_start_pink);
		start_pink.setTransitionName("start_pink");
		starts.add(start_pink);
		Button start_yellow = (Button)findViewById(R.id.color_start_yellow);
		start_yellow.setTransitionName("start_yellow");
		starts.add(start_yellow);
		Button start_skyblue = (Button)findViewById(R.id.color_start_skyblue);
		start_skyblue.setTransitionName("start_skyblue");
		starts.add(start_skyblue);
		Button start_green = (Button)findViewById(R.id.color_start_green);
		start_green.setTransitionName("start_green");
		starts.add(start_green);
		Button start_red = (Button)findViewById(R.id.color_start_red);
		start_red.setTransitionName("start_red");
		starts.add(start_red);
		Button start_purple = (Button)findViewById(R.id.color_start_purple);
		start_purple.setTransitionName("start_purple");
		starts.add(start_purple);

		for (Button button : speeds)
			button.setOnClickListener(new OnSpeedClicked());
		for (Button button : starts)
			button.setOnClickListener(new OnStartClicked());
	}

	private int getButtonDrawableId(String name)
	{
		switch(name) {
		case "speed_pink": return R.drawable.speed_button_pink;
		case "speed_yellow": return R.drawable.speed_button_yellow;
		case "speed_skyblue": return R.drawable.speed_button_skyblue;
		case "speed_green": return R.drawable.speed_button_green;
		case "speed_red": return R.drawable.speed_button_red;
		case "speed_purple": return R.drawable.speed_button_purple;
		case "start_pink": return R.drawable.start_button_pink;
		case "start_yellow": return R.drawable.start_button_yellow;
		case "start_skyblue": return R.drawable.start_button_skyblue;
		case "start_green": return R.drawable.start_button_green;
		case "start_red": return R.drawable.start_button_red;
		case "start_purple": return R.drawable.start_button_purple;
		}

		return 0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		actionBar = getSupportActionBar();

		if (actionBar != null)
		{
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
		}

		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View actionBarView = inflater.inflate(R.layout.layout_actionbar_setting, null);
		actionBar.setCustomView(actionBarView);


		TextView backButton = (TextView) actionBarView.findViewById(R.id.btn_back);
		backButton.setText("< " + getString(R.string.back));

		backButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Bundle conData = new Bundle();
				conData.putInt("speed_color", speed_drawable);
				conData.putInt("start_color", start_drawable);
				Intent intent = new Intent();
				intent.putExtras(conData);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.activity_slide_enter, R.anim.activity_slide_exit);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private class OnSpeedClicked implements View.OnClickListener {

		@Override
		public void onClick(View v)
		{
			speed_drawable = getButtonDrawableId(v.getTransitionName());
		}
	}


	private class OnStartClicked implements View.OnClickListener {

		@Override
		public void onClick(View v)
		{
			start_drawable = getButtonDrawableId(v.getTransitionName());
		}
	}
}
