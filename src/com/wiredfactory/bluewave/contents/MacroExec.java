package com.wiredfactory.bluewave.contents;

import java.io.IOException;
import java.util.ArrayList;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.activity.IntroActivity;
import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.GmailSender;
import com.wiredfactory.bluewave.utils.Logs;
import com.wiredfactory.bluewave.utils.Security;
import com.wiredfactory.bluewave.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

public class MacroExec extends Thread {
	// Debugging
	private static final String TAG = "MacroExec";

	// Constants
	private static final int RINGER_MODE_BELL = 1;
	private static final int RINGER_MODE_VIBRATE = 2;
	private static final int RINGER_MODE_SILENT = 3;
	
	private static final int NOTIFICATION_ID = 1284; 
	private static final long SLEEP_TIME_MAX = 5*60*1000;
	private static final long SLEEP_TIME_MIN = 1000;
	
	private static final int THREAD_STATUS_INITIALIZING = 1;
	private static final int THREAD_STATUS_RUNNING = 2;
	private static final int THREAD_STATUS_IDLE = 3;
	
	
	// System, Management
	private static Context mContext;
	private final Handler mHandler;
	
	private NotificationManager mNM;
	
	private ArrayList<MacroWork> mMacroWorkList = new ArrayList<MacroWork>();
	
	// Parameters
	private boolean mIsActivityAlive = false;
	private boolean mKillSign = false;
	private long mSleepTime = SLEEP_TIME_MIN;
	private int mThreadStatus = THREAD_STATUS_INITIALIZING;
	

	/**
	 * Constructor
	 * @param context  The UI Activity Context
	 * @param handler  A Listener to receive messages back to the UI Activity
	 */
	public MacroExec(Context context, Handler h) {
		mHandler = h;
		mContext = context;
		mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public synchronized void finalize() {
		mKillSign = true;
		mMacroWorkList.clear();
	}
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	@Override
	public void run() {
		while(!mKillSign) {
			mThreadStatus = THREAD_STATUS_RUNNING;
			
			synchronized(mMacroWorkList) {
				for(int i=mMacroWorkList.size()-1; i>-1; i--) {
					long current_time = System.currentTimeMillis();
					
					MacroWork work = mMacroWorkList.get(i); 
					
					if(work.count < 1) {
						mMacroWorkList.remove(i);
						
					} else if(work.isFirst || current_time - work.lastExecTime > work.interval) {
						String noti_title = mContext.getString(R.string.noti_title);
						switch(work.workType) {
						// Messaging group
						case Macro.MACRO_WORKS_SEND_SMS:
							sendSMS(work.destination, work.message);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_SEND_EMAIL:
							sendEmail(work.destination, "Auto beacon", work.message);
							break;
						case Macro.MACRO_WORKS_SEND_MESSAGE:
							// TODO: not supported
							break;
							
						// Alarm group
						case Macro.MACRO_WORKS_VIBRATION:
							vibrate(work);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_SOUND:
							//playRingtone();
							playAlarmSound();
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_NOTIFICATION:
							makeNotification(work.workType, noti_title, 
									( (work.message==null || work.message.length() < 1) ? Utils.getWorkTypeString(work.workType) : work.message )
									);
							break;
							
						// Change settings group
						case Macro.MACRO_WORKS_SET_BELL_MODE:
							setRingerMode(RINGER_MODE_BELL);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_SET_VIBRATION_MODE:
							setRingerMode(RINGER_MODE_VIBRATE);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_SET_SILENT_MODE:
							setRingerMode(RINGER_MODE_SILENT);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_TURN_ON_WIFI:
							changeWifiStatus(true);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
						case Macro.MACRO_WORKS_TURN_OFF_WIFI:
							changeWifiStatus(false);
							makeNotification(work.workType, noti_title, Utils.getWorkTypeString(work.workType));
							break;
							
						// Launch app group
						case Macro.MACRO_WORKS_LAUNCH_BROWSER:
							Utils.launchBrowser(work.destination);
							break;
							
						default:
							break;
						}
						
						work.lastExecTime = current_time;
						work.count--;
						work.isFirst = false;
						
						if(work.count < 1) {
							mMacroWorkList.remove(i);
						}
					}
				}	// End of for loop
			}	// End of synchronized(mMacroWorkList)
			
			// Block this thread
			try {
				if(mMacroWorkList.size() < 1) {
					Thread.sleep(SLEEP_TIME_MAX);
				} else {
					Thread.sleep(SLEEP_TIME_MIN);
				}
			} catch(InterruptedException ie) {
			}
			
		}	// End of while()
		
		mThreadStatus = THREAD_STATUS_IDLE;
	}	// End of run()
	
	public void setActivityAlive(boolean alive) {
		mIsActivityAlive = alive;
	}


	/*****************************************************
	 *	Private methods
	 ******************************************************/
	private void setRingerMode(int mode) {
		if(mContext == null)
			return;
		
		AudioManager  mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		switch(mode) {
		case RINGER_MODE_BELL:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			break;
		case RINGER_MODE_VIBRATE:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			break;
		case RINGER_MODE_SILENT:
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		}
	}
	
	private void sendSMS(String number, String message) {
		SmsManager sms = SmsManager.getDefault();
		// WARNING: We don't use source phone-number, sent pending intent, receive pending intent.
		// If you want to check sending and receiving status, make your own broadcast receiver
		// and set two types of pending intent.
		sms.sendTextMessage(number, null, message, null, null);
	}
	
	private void vibrate(MacroWork macro) {
		Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(macro.duration);
	}
	
	private MediaPlayer mMP = null;
	private void playAlarmSound() {
		if(mMP == null)
			mMP = MediaPlayer.create(mContext, R.raw.alarm);
		if(mMP.isPlaying())
			stopAlarmSound();

		mMP.seekTo(0);
		mMP.start();
	}
	
	private void stopAlarmSound() {
		if(mMP == null)
			return;
		
		if(mMP.isPlaying())
			mMP.stop();
		
		try {                        
			mMP.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void changeWifiStatus(boolean isOn) {
		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		wm.setWifiEnabled(isOn);
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void addMacroWork(MacroWork work) {
		synchronized(mMacroWorkList) {
			mMacroWorkList.add(work);
		}
	}
	
	public void setKillSign(boolean kill) {
		mKillSign = kill;
	}
	
	public void stopWorkingMacro() {
		// stopRingtone();
		stopAlarmSound();
		cancelNotifications();
		synchronized(mMacroWorkList) {
			for(MacroWork work : mMacroWorkList) {
				switch(work.workType) {
				// Alarm group
				case Macro.MACRO_WORKS_VIBRATION:
				case Macro.MACRO_WORKS_SOUND:
				case Macro.MACRO_WORKS_NOTIFICATION:
					work.count = 0;
					break;
				}
			}
		}
	}
	
	public void makeNotification(int workType, String title, String message) {
		if(mIsActivityAlive)
			return;
		
		if(!AppSettings.getUseNoti() && workType != Macro.MACRO_WORKS_NOTIFICATION)
			return;
		
		String _title;
		if(title == null || title.length() < 1)
			_title = "Beacon macro is working!!";
		else
			_title = title;
		
		PendingIntent mPendingIntent = PendingIntent.getActivity(
				mContext, 0, new Intent(mContext, IntroActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(mContext)
				.setContentTitle(_title).setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher_small)
				.setTicker(_title)
				.setAutoCancel(true)
				.setContentIntent(mPendingIntent)
				.build();
		mNM.notify(NOTIFICATION_ID, notification);
	}
	
	public void sendEmail(String targetAddr, String subject, String text) {
		if(targetAddr == null || targetAddr.length() < 1) {
			Logs.d("Cannot send email. Illegal parameters");
			return;
		}
		
		GmailSender sender = new GmailSender(AppSettings.getEmailAddr(),	// Sender email
				Security.decrypt(AppSettings.getEmailPw(), Constants.PREFERENCE_ENC_DEC_KEY));	// Sender password
		try {
			sender.sendMail(subject,		// Title
					text,		// Message body
					AppSettings.getEmailAddr(),		// Sender
					targetAddr);	// recipient
			Logs.d("Sent an email");
		} catch (Exception e) {
			Logs.d("Failed to send an email");
		}
	}
	
	public void cancelNotifications() {
		mNM.cancelAll();
	}
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
	
	
	
	
	
}
