package com.wiredfactory.bluewave.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Logs;

public class ServiceMonitoring {
	
	private static long SERVICE_RESTART_INTERVAL = 60*1000;
	

	
	private static boolean isRunningService(Context context, Class<?> cls) {
		boolean isRunning = false;

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);

		if (info != null) {
			for(ActivityManager.RunningServiceInfo serviceInfo : info) {
				ComponentName compName = serviceInfo.service;
				String className = compName.getClassName();

				if(className.equals(cls.getName())) {
					isRunning = true;
					break;
				}
			}
		}
		return isRunning;
	}
	
	public static void startMonitoring(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, ServiceMonitoringBR.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), SERVICE_RESTART_INTERVAL, pi);
	}

	public static void stopMonitoring(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, ServiceMonitoringBR.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		am.cancel(pi);
	}
	
	
	/**
	 *	Broadcast receiver
	 */
	public static class ServiceMonitoringBR extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Logs.d("# Monitoring service");
			
			// Check settings value
			AppSettings.initializeAppSettings(context);
			if(!AppSettings.getBgService()) {
				stopMonitoring(context);
				return;
			}
			// If service is running, start service.
			if(isRunningService(context, BlueWaveService.class) == false) {
				context.startService(new Intent(context, BlueWaveService.class));
			}
		}
	}
	
}
