package com.wiredfactory.bluewave.interfaces;

public interface IFragmentListener {
	public static final int CALLBACK_MACRO_TOGGLE = 1;
	public static final int CALLBACK_MACRO_EDIT = 2;
	public static final int CALLBACK_MACRO_DELETE = 3;
	
	public static final int CALLBACK_BEACON_MAKE_UUID = 101;
	public static final int CALLBACK_BEACON_MAKE_MAJOR = 102;
	public static final int CALLBACK_BEACON_MAKE_MACRO = 103;
	
	public static final int CALLBACK_RUN_IN_BACKGROUND = 201;
	public static final int CALLBACK_SCAN_INTERVAL = 202;
	
	public static final int CALLBACK_CLOSE = 1000;
	
	public static final int CALLBACK_REQUEST_SCAN_CLASSIC = 10001;
	public static final int CALLBACK_REQUEST_SCAN_BLE = 10002;
	
	public void OnFragmentCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
