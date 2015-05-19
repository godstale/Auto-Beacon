package com.wiredfactory.bluewave.utils;

public class Constants {

	// Service handler message key
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_NAME = "device_name";
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS = "device_address";
	public static final String SERVICE_HANDLER_MSG_KEY_TOAST = "toast";
    
    // Message types sent from Service to Activity
    public static final int MESSAGE_CMD_ERROR_NOT_CONNECTED = -50;
    public static final int MESSAGE_BT_STATE_INITIALIZED = 1;
    public static final int MESSAGE_BT_UUID_INFO = 101;
    
    public static final int MESSAGE_BT_SCAN_STARTED = 111;
    public static final int MESSAGE_BT_NEW_BEACON = 112;
    public static final int MESSAGE_BT_SCAN_FINISHED = 113;
    
    public static final int MESSAGE_BT_NOT_AVAILABLE = 121;
    
    public static final int MESSAGE_UPDATE_BEACON = 201;
    public static final int MESSAGE_REFRESH_BEACON_LIST = 211;
    
    
    public static final int UPDATE_TYPE_INSERT_OR_EDIT = 1;
    public static final int UPDATE_TYPE_DELETE = 2;
    
	
	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	
	// Preference
	public static final String PREFERENCE_NAME = "BlueWavePref";
	
	public static final String PREFERENCE_KEY_USE_NOTI = "UseNotification";
	public static final String PREFERENCE_KEY_BG_SERVICE = "BacgroundService";
	public static final String PREFERENCE_KEY_SCAN_INTERVAL = "ScanInterval";
	public static final String PREFERENCE_KEY_EMAIL_ADDR = "EmailAddress";
	public static final String PREFERENCE_KEY_EMAIL_PW = "EmailPass";
	
	// Preference encryption/decryption key
	// Key must be 16byte long.
	public static final String PREFERENCE_ENC_DEC_KEY = "wiredfactory@har";
	
}
