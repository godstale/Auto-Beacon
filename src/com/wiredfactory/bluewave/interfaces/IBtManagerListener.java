package com.wiredfactory.bluewave.interfaces;

public interface IBtManagerListener {
	public static final int CALLBACK_BT_XXX = 1;
	
	public void OnBtManagerCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
