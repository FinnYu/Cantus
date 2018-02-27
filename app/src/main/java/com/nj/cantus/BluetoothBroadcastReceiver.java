package com.nj.cantus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by FinnYu on 2018. 1. 28..
 */

public class BluetoothBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		int state;

		switch (action)
		{
		case BluetoothAdapter.ACTION_STATE_CHANGED:
			state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			if (state == BluetoothAdapter.STATE_ON)
			{
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (MainActivity.i() != null)
							MainActivity.i().scanLeDevice(true);
					}}, 500);
			}
			break;

		case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
			state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			Toast.makeText(context, "to "+bluetoothDevice.getName() + " : " + state,
				Toast.LENGTH_SHORT).show();
			break;
		}
	}
}
