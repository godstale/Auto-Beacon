package com.wiredfactory.bluewave.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class BootReceiver extends BroadcastReceiver{
	public BootReceiver() {
		//super();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//Intent i = new Intent(context, BlueWaveService.class);
			//context.startService(i);
			ComponentName cName = new ComponentName(context.getPackageName(), BlueWaveService.class.getName());
			context.startService(new Intent().setComponent(cName));
		}
	}
}
