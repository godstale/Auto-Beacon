package com.wiredfactory.bluewave.service;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.bluetooth.*;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.contents.BeaconManager;
import com.wiredfactory.bluewave.contents.MacroExec;
import com.wiredfactory.bluewave.contents.MacroManager;
import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Constants;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class BlueWaveService extends Service {
	private static final String TAG = "LLService";
	
	// Context, System
	private Context mContext = null;
	private static Handler mActivityHandler = null;
	private ServiceHandler mServiceHandler = new ServiceHandler();
	private final IBinder mBinder = new LLServiceBinder();
	
	// Bluetooth
	private BluetoothAdapter mBluetoothAdapter = null;
	private BtManager mBtManager = null;
	private BleManager mBleManager = null;
	private boolean mIsBleSupported = true;
	
	// Contents
	private static MacroManager mMacroManager;
	private static MacroExec mMacroExec;
	private static BeaconManager mBeaconManager;
	
	// Parameters
	private boolean isActivityLaunched = false;
	
	// Auto-refresh timer
	private static Timer mScanTimer = null;
    
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	@Override
	public void onCreate() {
		Log.d(TAG, "# Service - onCreate() starts here");
		
		mContext = getApplicationContext();
		initialize();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "# Service - onStartCommand() starts here");
		
		// If service returns START_STICKY, android restarts service automatically after forced close.
		// At this time, onStartCommand() method in service must handle null intent.
		
		// After forced closed, start the scan timer
		if(mScanTimer == null)
			setScanInterval(AppSettings.getScanInterval(), AppSettings.getScanInterval());
		
		return Service.START_STICKY;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		// This prevents reload after configuration changes
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "# Service - onBind()");
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "# Service - onUnbind()");
		return true;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "# Service - onDestroy()");
		finalizeService();
	}
	
	@Override
	public void onLowMemory (){
		Log.d(TAG, "# Service - onLowMemory()");
		// onDestroy is not always called when applications are finished by Android system.
		finalizeService();
	}

	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	private void initialize() {
		Log.d(TAG, "# Service : initialize ---");
		
		AppSettings.initializeAppSettings(mContext);
		startServiceMonitoring();
		
		// Use this check to determine whether BLE is supported on the device. Then
		// you can selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(this, R.string.bt_ble_not_supported, Toast.LENGTH_SHORT).show();
		    mIsBleSupported = false;
		}
		
		// Get local Bluetooth adapter
		if(mBluetoothAdapter == null)
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// If the adapter is null, then Bluetooth is not supported
		if(mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(mBtManager == null) {
			setupBT();
		}
		if(mBleManager == null && mIsBleSupported) {
			setupBLE();
		}
		
		if(mMacroExec == null) {
			mMacroExec = new MacroExec(mContext, mServiceHandler);
		}
		if(mMacroManager == null) {
			mMacroManager = MacroManager.getInstance(mContext);
			mMacroManager.setHandler(mServiceHandler);
			mMacroManager.setMacroExec(mMacroExec);
		}
		if(mBeaconManager == null) {
			mBeaconManager = BeaconManager.getInstance(mContext);
			mBeaconManager.setHandler(mServiceHandler);
		}
		if(!mMacroExec.isAlive())
			mMacroExec.start();
		
		stopWorkingMacro();
	}
	
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void finalizeService() {
		Log.d(TAG, "# Service : finalize ---");
		
		setActivityFinished();
		
		// Stop the bluetooth session
		mBluetoothAdapter = null;
		if(mBtManager != null)
			mBtManager.finalize();
		mBtManager = null;
		
		if(mBleManager != null)
			mBleManager.finalize();
		mBleManager = null;
		
		if(mMacroManager != null)
			mMacroManager.finalize();
		mMacroManager = null;
		
		if(mBeaconManager != null)
			mBeaconManager.finalize();
		mBeaconManager = null;
		
		if(mMacroExec != null) {
			mMacroExec.finalize();
			mMacroExec.interrupt();
			mMacroExec = null;
		}
		
		if(mScanTimer != null) {
			mScanTimer.cancel();
			mScanTimer = null;
		}
	}
	
	public void setActivityFinished() {
		isActivityLaunched = false;
		if(mMacroExec != null)
			mMacroExec.setActivityAlive(isActivityLaunched);
	}
	
	public void setupService(Handler h) {
		mActivityHandler = h;
		isActivityLaunched = true;
		if(mMacroExec != null) {
			mMacroExec.setActivityAlive(isActivityLaunched);
			mMacroExec.cancelNotifications();
		}
		
		if(!mBluetoothAdapter.isEnabled()) {
			// BT is not on, need to turn on manually.
			// Activity will do this.
			mActivityHandler.obtainMessage(Constants.MESSAGE_BT_NOT_AVAILABLE).sendToTarget();
		} else {
			setScanInterval(AppSettings.getScanInterval());
		}
	}
	
	public void startServiceMonitoring() {
		if(AppSettings.getBgService()) {
			ServiceMonitoring.startMonitoring(mContext);
		} else {
			ServiceMonitoring.stopMonitoring(mContext);
		}
	}
	
    /**
     * Setup and initialize BT manager
     */
	public void setupBT() {
        Log.d(TAG, "Service - setupBT()");

        // Initialize the BluetoothManager to perform bluetooth connections
        if(mBtManager == null)
        	mBtManager = BtManager.getInstance(mContext, mServiceHandler);
    }
	
    /**
     * Setup and initialize BLE manager
     */
	public void setupBLE() {
        Log.d(TAG, "Service - setupBLE()");

        // Initialize the BluetoothManager to perform bluetooth le scanning
        if(mBleManager == null)
        	mBleManager = BleManager.getInstance(mContext, mServiceHandler);
    }
	
    /**
     * Check bluetooth is enabled or not.
     */
	public boolean isBluetoothEnabled() {
		if(mBluetoothAdapter==null) {
			Log.e(TAG, "# Service - cannot find bluetooth adapter. Restart app.");
			return false;
		}
		return mBluetoothAdapter.isEnabled();
	}
	
	/**
	 * Get scan mode
	 */
	public int getBluetoothScanMode() {
		int scanMode = -1;
		if(mBluetoothAdapter != null)
			scanMode = mBluetoothAdapter.getScanMode();
		
		return scanMode;
	}
	
	public boolean discoverClassic() {
		return mBtManager.discover();
	}
	
	public boolean discoverBLE() {
		if(mIsBleSupported)
			return mBleManager.scanLeDevice(true); 	// true: start scan, false: stop scan
		else
			return false;
	}
	
	public void setScanInterval(long interval) {
		if(mScanTimer != null) {
			mScanTimer.cancel();
		}
		mScanTimer = new Timer();
		mScanTimer.schedule(new ScanTimerTask(), 0, interval);
	}
	
	public void setScanInterval(long delay, long interval) {
		if(mScanTimer != null) {
			mScanTimer.cancel();
		}
		mScanTimer = new Timer();
		mScanTimer.schedule(new ScanTimerTask(), delay, interval);
	}
	
	public void stopWorkingMacro() {
		if(mMacroExec != null)
			mMacroExec.stopWorkingMacro();
	}
	
	public void cancelNotifications() {
		if(mMacroExec != null)
			mMacroExec.cancelNotifications();
	}
	
	public ArrayList<Beacon> getMemorizedBeacons() {
		ArrayList<Beacon> beaconList = null;
		if(mBeaconManager != null) {
			beaconList = mBeaconManager.getBeaconList();
		}
		return beaconList;
	}
	
	public ArrayList<Beacon> getScannedBeacons() {
		return mBleManager.getBeaconList();
	}
	
	public void updateBeacon(int type, Beacon beacon) {
		if(type == Constants.UPDATE_TYPE_INSERT_OR_EDIT) {
			int id = mBeaconManager.insertOrUpdateBeacon(beacon);
			if(beacon.getIsRemembered()) {
				// updated beacon info
				mBleManager.updateBeaconName(beacon.getProximityUuid(), beacon.getMajor(), beacon.getMinor(), 
						mContext.getString(R.string.title_beacon_already_remembered)+beacon.getBeaconName());
			} else {
				Beacon newBeacon = new Beacon(beacon);
				newBeacon.setId(id);
				mBleManager.updateBeaconName(beacon.getProximityUuid(), beacon.getMajor(), beacon.getMinor(), 
						mContext.getString(R.string.title_beacon_already_remembered)+beacon.getBeaconName());
			}
		} else if(type == Constants.UPDATE_TYPE_DELETE) {
			mBeaconManager.deleteBeacon(beacon.getId());
			mBleManager.deleteBeaconName(beacon.getProximityUuid(), beacon.getMajor(), beacon.getMinor());
		}
	}
	
	
	/*****************************************************
	 *	Handler, Listener, Timer, Sub classes
	 ******************************************************/
	public class LLServiceBinder extends Binder {
		public BlueWaveService getService() {
			return BlueWaveService.this;
		}
	}
	
    /**
     * Receives messages from bluetooth manager
     */
	class ServiceHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case Constants.MESSAGE_BT_NEW_BEACON:
				if(msg.obj != null && msg.obj instanceof Beacon) {
					Beacon beacon = (Beacon)msg.obj;
					// Check it's same with the remembered beacon
					if(mBeaconManager != null) {
						if(!mBeaconManager.checkWithRememberedBeacon(beacon)) {
							// If beacon is not match with remebered beacons, set the beacon name as null.
							beacon.setBeaconName(null);
						}
					}
					// Send to activity
					if(mActivityHandler != null)
						mActivityHandler.obtainMessage(msg.what, beacon).sendToTarget();
					// Do the reserved macro on this beacon
					if(mMacroManager != null)
						mMacroManager.checkMacroWithBeacon(beacon);
				}
				break;
			
			case Constants.MESSAGE_BT_SCAN_STARTED:
			case Constants.MESSAGE_BT_SCAN_FINISHED:
				if(mActivityHandler != null)
					mActivityHandler.obtainMessage(msg.what).sendToTarget();
				if(msg.what == Constants.MESSAGE_BT_SCAN_FINISHED) {
					// After scanning, check not-found condition macro
					if(mMacroManager != null && mBleManager != null)
						mMacroManager.checkNotFoundMacro(mBleManager.getBeaconList());
				}
				break;
			}	// End of switch(msg.what)
			
			super.handleMessage(msg);
		}
	}	// End of class MainHandler
	
    /**
     * Auto-scanning Timer
     */
	private class ScanTimerTask extends TimerTask {
		public ScanTimerTask() {}
		
		public void run() {
			mServiceHandler.post(new Runnable() {
				public void run() {
					if(mBluetoothAdapter.isEnabled()) {
						discoverBLE();
					}
				}
			});
		}
	}
	
}
