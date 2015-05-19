package com.wiredfactory.bluewave.bluetooth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.UUID;

import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.Logs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

public class BtManager {

	// Debugging
	private static final String TAG = "BtManager";

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;
	public static final int STATE_IDLE = 1;
	public static final int STATE_SCANNING = 2;
	public static final int STATE_FETCHING_UUID = 3;
	
	// Message types sent from the BluetoothManager to Listener
	public static final int MESSAGE_SCAN_IDLE = 1;
	public static final int MESSAGE_SCAN_START = 2;
	public static final int MESSAGE_SCAN_FINISHED = 3;
	
	// BT type
	public static final int TYPE_CLASSIC = 1;	// Classic Bluetooth type
	public static final int TYPE_BLE = 1;		// BLE(Bluetooth Smart) type
	public static final int TYPE_MIXED = 1;		// Classic+BLE type
	
	// Name for the SDP record when creating server socket
	private static final String NAME = "BlueWave";
	
	// Unique UUID for this application
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// System, Management
	private static Context mContext = null;
	private static BtManager mBtManager = null;		// Singleton pattern
	
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	
	// Parameters
	private int mState;
	
	private static final long RECONNECT_DELAY_MAX = 60*60*1000;
	
	private long mScanDelay = 30*1000;
	private Timer mScanTimer = null;
	private boolean mIsServiceStopped = false;



	/**
	 * Constructor. Prepares a new Bluetooth session.
	 * @param context  The UI Activity Context
	 * @param handler  A Listener to receive messages back to the UI Activity
	 */
	private BtManager(Context context, Handler h) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = h;
		mContext = context;
		
		if(mContext == null)
			return;
		
		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_UUID);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, filter);
	}

	public synchronized static BtManager getInstance(Context c, Handler h) {
		if(mBtManager == null)
			mBtManager = new BtManager(c, h);
		
		return mBtManager;
	}

	public synchronized void finalize() {
		// Make sure we're not doing discovery anymore
		if (mAdapter != null) {
			mAdapter.cancelDiscovery();
		}
		
		if(mContext == null)
			return;
        
		// Don't forget this!!
		// Unregister broadcast listeners
		try {
			mContext.unregisterReceiver(mReceiver);
		} catch(Exception e) {}
	}


	/*****************************************************
	 *	Private methods
	 ******************************************************/
	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		Logs.d(TAG, "doDiscovery()");
		if(mAdapter == null)
        	return;

		// If we're already discovering, stop it
		cancelDiscovery();

		// Request discover from BluetoothAdapter
		mAdapter.startDiscovery();
		mState = STATE_SCANNING;
	}
	
	private void cancelDiscovery() {
		if(mAdapter != null && mAdapter.isDiscovering()) {
			mAdapter.cancelDiscovery();
			mState = STATE_IDLE;
		}
	}
	
	
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public boolean discover() {
		doDiscovery();
		return true;
	}
	
	public void cancelDiscover() {
		cancelDiscovery();
	}
	
	
	
	
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
	// The BroadcastReceiver that listens for discovered devices
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Logs.d("\n  Device: " + device.getName() + ", " + device);
				mDeviceList.add(device);
                
			} else {
				// Start discovery
				if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					Logs.d("# BT discovery started");
					mDeviceList.clear();
				}
				// 
				else if(BluetoothDevice.ACTION_UUID.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
					if(uuidExtra != null) {
						for(int i=0; i<uuidExtra.length; i++) {
							if(uuidExtra[i] != null) {
								String tempStr = "\n  Device: " + device.getName() + ", " + device + ", UUID extra: " + uuidExtra[i].toString();
								mHandler.obtainMessage(Constants.MESSAGE_BT_UUID_INFO, tempStr).sendToTarget();
							} else {
								Logs.d("# Error: UUID Extra is null !!!!!!!!!!!!!");
							}
						}
					} else {
						Logs.d("# Fatal: UUID Extra array is null !!!!!!!!!!!!!");
					}
				}
				// When discovery is finished
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					mState = STATE_FETCHING_UUID;
					Iterator<BluetoothDevice> itr = mDeviceList.iterator();
					while (itr.hasNext()) {
						// Get Services for paired devices
						BluetoothDevice device = itr.next();
						Logs.d("Getting Services for " + device.getName() + ", " + device);
						if(!device.fetchUuidsWithSdp()) {
							Logs.d("SDP Failed for " + device.getName());
						}
					}
				}
			}
		}	// End of onReceive()
	};
	
	
	
	
}
