package com.wiredfactory.bluewave.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

	// Constants
	public static final int SETTINGS_USE_NOTIFICATION = 1;
	public static final int SETTINGS_BACKGROUND_SERVICE = 2;
	public static final int SETTINGS_SCAN_INTERVAL = 3;
	public static final int SETTINGS_EMAIL_ADDR = 4;
	public static final int SETTINGS_EMAIL_PW = 5;
	
	
	private static boolean mIsInitialized = false;
	private static Context mContext;
	
	// Setting values
	private static boolean mUseNotification;
	private static boolean mUseBackgroundService;
	private static int mScanInterval;
	private static String mEmailAddr;
	private static String mEmailPw;
	
	
	public static void initializeAppSettings(Context c) {
		if(mIsInitialized)
			return;
		
		mContext = c;
		
		// Load setting values from preference
		mUseNotification = loadUseNoti();
		mUseBackgroundService = loadBgService();
		mScanInterval = loadScanInterval();
		mEmailAddr = loadEmailAddr();
		mEmailPw = loadEmailPw();
		
		mIsInitialized = true;
	} 
	
	
	////////////////////////////////////////////////////////////////
	//	Set preference value
	////////////////////////////////////////////////////////////////
	
	public static void setSettingsValue(int type, boolean boolValue, int intValue, String stringValue) {
		if(mContext == null)
			return;
		
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		switch(type) {
		case SETTINGS_USE_NOTIFICATION:
			editor.putBoolean(Constants.PREFERENCE_KEY_USE_NOTI, boolValue);
			editor.commit();
			mUseNotification = boolValue;
			break;
		case SETTINGS_BACKGROUND_SERVICE:
			editor.putBoolean(Constants.PREFERENCE_KEY_BG_SERVICE, boolValue);
			editor.commit();
			mUseBackgroundService = boolValue;
			break;
		case SETTINGS_SCAN_INTERVAL:
			editor.putInt(Constants.PREFERENCE_KEY_SCAN_INTERVAL, intValue);
			editor.commit();
			mScanInterval = intValue;
			break;
		case SETTINGS_EMAIL_ADDR:
			editor.putString(Constants.PREFERENCE_KEY_EMAIL_ADDR, stringValue);
			editor.commit();
			mEmailAddr = stringValue;
			break;
		case SETTINGS_EMAIL_PW:
			editor.putString(Constants.PREFERENCE_KEY_EMAIL_PW, stringValue);
			editor.commit();
			mEmailPw = stringValue;
			break;
		default:
			editor.commit();
			break;
		}
	}
	
	
	////////////////////////////////////////////////////////////////
	//	Load preference value from file
	////////////////////////////////////////////////////////////////
	
	public static boolean loadUseNoti() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PREFERENCE_KEY_USE_NOTI, true);
	}
	
	public static boolean loadBgService() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PREFERENCE_KEY_BG_SERVICE, true);
	}
	
	public static int loadScanInterval() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(Constants.PREFERENCE_KEY_SCAN_INTERVAL, 10*60*1000);
	}
	
	public static String loadEmailAddr() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREFERENCE_KEY_EMAIL_ADDR, "");
	}
	
	public static String loadEmailPw() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PREFERENCE_KEY_EMAIL_PW, "");
	}
	
	
	////////////////////////////////////////////////////////////////
	//	Returns cached preference value
	////////////////////////////////////////////////////////////////
	
	public static boolean getUseNoti() {
		return mUseNotification;
	}
	
	public static boolean getBgService() {
		return mUseBackgroundService;
	}
	
	public static int getScanInterval() {
		return mScanInterval;
	}
	
	public static String getEmailAddr() {
		return mEmailAddr;
	}
	
	public static String getEmailPw() {
		return mEmailPw;
	}
	
}
