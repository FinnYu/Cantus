package com.nj.cantus;

/**
 * Created by FinnYu on 2018. 1. 28..
 */

public class Constants
{
	// Message types sent from the BluetoothLeService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int REQUEST_CONNECT_DEVICE = 6;
	public static final int REQUEST_ENABLE_BT = 7;

	public static final int PERMISSION_REQUEST_CODE = 9292;
	// Key names received from the BluetoothLeService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

}
