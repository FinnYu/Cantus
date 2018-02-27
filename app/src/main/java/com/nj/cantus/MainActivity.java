package com.nj.cantus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
	private String speedValue = "1";
	private String lValue = "a";
	private boolean connected = false;
	private static MainActivity instance;

	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_CAMERA = 2;

	public static MainActivity i() {
		return instance;
	}

	private ActionBar actionBar;
	private ArrayList<RadioButton> speedButtons = new ArrayList<>();
	private ArrayList<RadioButton> lButtons = new ArrayList<>();
	private RadioButton startButton;
	private static boolean isSpeedButtonYellow = false;
	private static boolean isStartButtonYellow = false;

	private TextView timerView;
	private TextView bluetoothTextView;
	private String connectionStatus = "CONNECT";
	private int connectionColor = Color.WHITE;
	private int elapsedTime = 0;
	private boolean isTimerRunning = false;

	private Handler timerHandler = new Handler();

	Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			setTime(elapsedTime);
			elapsedTime++;
			timerHandler.postDelayed(timerRunnable, 1000);
		}
	};


	private BluetoothGattCharacteristic characteristic;
	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;
	private Handler mHandler;
	private static final long SCAN_PERIOD = 10000;
	private BluetoothLeScanner mLEScanner;
	private ScanSettings settings;
	private List<ScanFilter> filters;
	private BluetoothGatt mGatt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "BLE Not Supported",
				Toast.LENGTH_SHORT).show();
			finish();
		}
		final BluetoothManager bluetoothManager =
			(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();


		RadioButton button_50 = (RadioButton)findViewById(R.id.button_50);
		button_50.setTransitionName("1");
		RadioButton button_60 = (RadioButton)findViewById(R.id.button_60);
		button_60.setTransitionName("2");
		RadioButton button_70 = (RadioButton)findViewById(R.id.button_70);
		button_70.setTransitionName("3");
		RadioButton button_80 = (RadioButton)findViewById(R.id.button_80);
		button_80.setTransitionName("4");
		RadioButton button_90 = (RadioButton)findViewById(R.id.button_90);
		button_90.setTransitionName("5");
		RadioButton button_100 = (RadioButton)findViewById(R.id.button_100);
		button_100.setTransitionName("6");
		RadioButton button_110 = (RadioButton)findViewById(R.id.button_110);
		button_110.setTransitionName("7");
		RadioButton button_120 = (RadioButton)findViewById(R.id.button_120);
		button_120.setTransitionName("8");


		RadioButton l1Button = (RadioButton)findViewById(R.id.button_L1);
		l1Button.setTransitionName("a");
		RadioButton l2Button = (RadioButton)findViewById(R.id.button_L2);
		l2Button.setTransitionName("b");

		speedButtons.add(button_50);
		speedButtons.add(button_60);
		speedButtons.add(button_70);
		speedButtons.add(button_80);
		speedButtons.add(button_90);
		speedButtons.add(button_100);
		speedButtons.add(button_110);
		speedButtons.add(button_120);

		lButtons.add(l1Button);
		lButtons.add(l2Button);

		for (RadioButton button : speedButtons)
			button.setOnClickListener(new SpeedButtonClickListener());

		for (RadioButton button : lButtons)
			button.setOnClickListener(new LButtonClickListener());

		button_50.callOnClick();
		l1Button.callOnClick();

		timerView = (TextView)findViewById(R.id.timer);

		startButton = (RadioButton)findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				sendCharacter("s");


				if (isTimerRunning)
				{
					startButton.setText("START");
					cancelTimer();
				}
				else
				{
					sendCharacter(speedValue);
					startButton.setText("STOP");
					startTimer();
				}
				updateStartButtonState();
			}
		});
		updateStartButtonState();

		// Make sure we have access coarse location enabled, if not, prompt the user to enable it
		if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("This app needs location access");
			builder.setMessage("Please grant location access so this app can detect peripherals.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
				}
			});
			builder.show();
		}


		// Make sure we have access camera enabled, if not, prompt the user to enable it
		if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("This app needs location access");
			builder.setMessage("Please grant location access so this app can detect peripherals.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
				}
			});
			builder.show();
		}

		ImageButton cameraBtn = (ImageButton) findViewById(R.id.camera_btn);
		cameraBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
				startActivity(intent);
			}
		});
	}

	private void updateStartButtonState()
	{
		if (startButton != null)
		{
			boolean checked = connected && !isTimerRunning;
			startButton.setChecked(checked);
			if (checked && isStartButtonYellow)
				startButton.setTextColor(Color.rgb(0, 0, 0));
			else
				startButton.setTextColor(Color.rgb(255, 255, 255));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode)
		{
		case PERMISSION_REQUEST_COARSE_LOCATION:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				System.out.println("coarse location permission granted");
				scanLeDevice(true);
			}
			else
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Functionality limited");
				builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener()
				{

					@Override
					public void onDismiss(DialogInterface dialog)
					{
					}

				});
				builder.show();
			}

		case PERMISSION_REQUEST_CAMERA:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				System.out.println("camera permission granted");
			}
			else
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Functionality limited");
				builder.setMessage("Since camera has not been granted, this app will not be able to take a picture.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener()
				{

					@Override
					public void onDismiss(DialogInterface dialog)
					{
					}

				});
				builder.show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			if (Build.VERSION.SDK_INT >= 21) {
				mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
				settings = new ScanSettings.Builder()
					.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
					.build();
				filters = new ArrayList<ScanFilter>();
			}
			scanLeDevice(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			scanLeDevice(false);
		}
	}

	@Override
	protected void onDestroy() {
		if (mGatt == null) {
			return;
		}
		mGatt.close();
		mGatt = null;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_CANCELED) {
				//Bluetooth not enabled.
				finish();
				return;
			}
		}
		else if (requestCode == 222) {
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();
				int speed_drawable = res.getInt("speed_color");
				int start_drawable = res.getInt("start_color");

				if (speed_drawable != 0)
				{
					int speed_text_color = Color.rgb(255, 255, 255);

					if (speed_drawable == R.drawable.speed_button_yellow)
						isSpeedButtonYellow = true;

					for (RadioButton button : speedButtons)
					{
						button.setBackground(getDrawable(speed_drawable));
						button.setTextColor(speed_text_color);

						if (isSpeedButtonYellow && button.isChecked())
							button.setTextColor(Color.rgb(0, 0, 0));
						else
							button.setTextColor(Color.rgb(255, 255, 255));
					}
				}

				if (start_drawable != 0)
				{
					int start_text_color = Color.rgb(255, 255, 255);

					if (start_drawable == R.drawable.start_button_yellow)
						isStartButtonYellow = true;

					startButton.setBackground(getDrawable(start_drawable));
					startButton.setTextColor(start_text_color);
					updateStartButtonState();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void scanLeDevice(final boolean enable) {
		setConnectedStatus(enable);

		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (enable) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (Build.VERSION.SDK_INT < 21) {
								mBluetoothAdapter.stopLeScan(mLeScanCallback);
							} else {
								mLEScanner.stopScan(mScanCallback);

							}
						}
					}, SCAN_PERIOD);
					if (Build.VERSION.SDK_INT < 21) {
						mBluetoothAdapter.startLeScan(mLeScanCallback);
					} else {
						mLEScanner.startScan(filters, settings, mScanCallback);
					}
				} else {
					if (Build.VERSION.SDK_INT < 21) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					} else {
						mLEScanner.stopScan(mScanCallback);
					}
				}
			}
		});
	}


	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.i("callbackType", String.valueOf(callbackType));
			Log.i("result", result.toString());
			BluetoothDevice btDevice = result.getDevice();
			if (btDevice != null && btDevice.getName() != null && btDevice.getName().contains("HM"))
				connectToDevice(btDevice);
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult sr : results) {
				Log.i("ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.e("Scan Failed", "Error Code: " + errorCode);
		}
	};

	private BluetoothAdapter.LeScanCallback mLeScanCallback =
		new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(final BluetoothDevice device, int rssi,
								 byte[] scanRecord) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.i("onLeScan", device.toString());
						connectToDevice(device);
					}
				});
			}
		};

	public void connectToDevice(BluetoothDevice device) {
		if (mGatt == null) {
			mGatt = device.connectGatt(this, false, gattCallback);
			scanLeDevice(false);// will stop after first device detection
		}
	}

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.i("onConnectionStateChange", "Status: " + status);
			switch (newState) {
			case BluetoothProfile.STATE_CONNECTED:
				Log.i("gattCallback", "STATE_CONNECTED");
				gatt.discoverServices();
				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				setConnectedStatus(false);
				mGatt.close();
				mGatt = null;
				Log.e("gattCallback", "STATE_DISCONNECTED");
				break;
			default:
				Log.e("gattCallback", "STATE_OTHER");
			}

		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			characteristic = gatt.getService(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB"))
				.getCharacteristic(UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));
			if (characteristic == null)
			{
				Toast.makeText(MainActivity.this, "Characteristic Not Found!", Toast.LENGTH_SHORT).show();
				finish();
			}
			setConnectedStatus(true);
			Log.i("onServicesDiscovered", services.toString());
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic
											 characteristic, int status) {
			Log.i("onCharacteristicRead", characteristic.toString());
		}
	};
	List<BluetoothGattService> services;

	@UiThread
	public void setTime(int time)
	{
		int r = time;
		int hour = time / (60 * 60);
		r -= hour * (60 * 60);
		int minute = r / 60;
		r -= minute * 60;
		int sec = r;
		timerView.setText(String.format(Locale.KOREA, "TIME\t\t%02d:%02d:%02d", hour, minute, sec));

		if (time >= 60 * 30)
			timerView.setTextColor(Color.YELLOW);
		else
			timerView.setTextColor(Color.WHITE);
	}

	private void applyConnectionStatus()
	{
		if (bluetoothTextView != null)
		{
			bluetoothTextView.setText(connectionStatus);
			bluetoothTextView.setTextColor(connectionColor);
		}
		updateStartButtonState();
	}

	private void setConnectedStatus(boolean connected)
	{
		updateStartButtonState();
		this.connected = connected;

		if (connected) {
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					connectionStatus = "DISCONNECT";
					connectionColor = Color.RED;
					applyConnectionStatus();
				}
			});
		}
		else
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					connectionStatus = "CONNECT";
					connectionColor = Color.WHITE;
					applyConnectionStatus();
				}
			});
		}
	}

	public void resetTimer()
	{
		elapsedTime = 0;
	}

	public void startTimer() {
		isTimerRunning = true;
		timerHandler.postDelayed(timerRunnable, 1000);
	}

	public void cancelTimer() {
		resetTimer();
		runOnUiThread(timerRunnable);
		isTimerRunning = false;
		timerHandler.removeCallbacks(timerRunnable);
	}

	@Override
	protected void onStop() {
		super.onStop();
		isTimerRunning = false;
		resetTimer();
		runOnUiThread(timerRunnable);
		timerHandler.removeCallbacks(timerRunnable);
	}
	static final int REQUEST_TAKE_PHOTO = 1;

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
		View actionBarView = inflater.inflate(R.layout.layout_actionbar, null);
		actionBar.setCustomView(actionBarView);

		View bluetoothBtn = actionBarView.findViewById(R.id.btn_bluetooth);
		View settingBtn = actionBarView.findViewById(R.id.btn_setting);

		bluetoothBtn.setOnTouchListener(new ViewTouchListener());
		bluetoothBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (mBluetoothAdapter != null && mGatt != null)
				{
					if (mBluetoothAdapter.isEnabled())
					{
						setConnectedStatus(false);
						mBluetoothAdapter.disable();
						mGatt.close();
						mGatt = null;
					}
					else
					{
						mBluetoothAdapter.enable();
					}
				}
			}
		});

		settingBtn.setOnTouchListener(new ViewTouchListener());
		settingBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent i = new Intent(MainActivity.this, SettingActivity.class);
				startActivityForResult(i, 222);
				overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out);
			}
		});

		bluetoothTextView = (TextView)actionBarView.findViewById(R.id.text_bluetooth);

		applyConnectionStatus();

		return super.onCreateOptionsMenu(menu);
	}

	private void sendCharacter(String c)
	{
		if (mGatt != null)
		{
			characteristic.setValue(c);
			mGatt.writeCharacteristic(characteristic);
		}
	}




	private class SpeedButtonClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v)
		{
			speedValue = v.getTransitionName();
			sendCharacter(speedValue);
			for (RadioButton button : speedButtons)
			{
				button.setChecked(false);

				if (isSpeedButtonYellow && button.isChecked())
					button.setTextColor(Color.rgb(0, 0, 0));
				else
					button.setTextColor(Color.rgb(255, 255, 255));
			}

			RadioButton b = (RadioButton) v;
			b.setChecked(true);

			if (isSpeedButtonYellow && b.isChecked())
				b.setTextColor(Color.rgb(0, 0, 0));
			else
				b.setTextColor(Color.rgb(255, 255, 255));
		}
	}

	private class LButtonClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v)
		{
			lValue = v.getTransitionName();
			sendCharacter(lValue);
			for (RadioButton button : lButtons)
			{
				button.setChecked(false);
			}

			((RadioButton)v).setChecked(true);
		}
	}

	private class ViewTouchListener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				v.setAlpha(0.5f);
				break;
			case MotionEvent.ACTION_UP:
				v.setAlpha(1f);
				break;
			}
			return false;
		}
	}
}
