package com.wiredfactory.bluewave.activity;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.bluetooth.BleManager;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.fragment.BeaconFragment;
import com.wiredfactory.bluewave.fragment.MacroFragment;
import com.wiredfactory.bluewave.interfaces.IFragmentListener;
import com.wiredfactory.bluewave.service.BlueWaveService;
import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.Logs;
import com.wiredfactory.bluewave.utils.RecycleUtils;
import com.wiredfactory.bluewave.utils.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, IFragmentListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
	
    // Constants
    private static final int EXIT_RETRY_TIME_THRESHOLD = 3*1000; 
    
	// Context, System
	private Context mContext;
	
	// Listener, Handler
	private ActivityHandler mActivityHandler;
	
	// Management
	private Utils mUtils;
	
	// Service
	private BlueWaveService mService;
	
	// UI stuff
	private FragmentManager mFragmentManager;
	private FragmentAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	private ImageView mImageBT = null;
	private TextView mTextStatus = null;
	private Button mButtonStopAlarm = null;
	
	// Global parameters
	private int mCurrentPage;
	private long mLastExitTry = 0; 
	private int mScanState = BleManager.STATE_NONE;
	
	

	/*****************************************************
	 *	 Overrided methods
	 ******************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//----- System, Context
		mContext = this;//.getApplicationContext();
		mActivityHandler = new ActivityHandler();
		
		AppSettings.initializeAppSettings(mContext);
		
		setContentView(R.layout.activity_main);

		// Load static utilities
		mUtils = new Utils();
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setIcon(R.drawable.actionbar_icon);

		// Create the adapter that will return a fragment for each of the primary sections of the app.
		mFragmentManager = getSupportFragmentManager();
		mSectionsPagerAdapter = new FragmentAdapter(mFragmentManager, mContext, this, mActivityHandler);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mCurrentPage = FragmentAdapter.FRAGMENT_POS_BEACON;

		// When swiping between different sections, select the corresponding tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				mCurrentPage = position;
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by the adapter.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		// Setup views
		mImageBT = (ImageView) findViewById(R.id.status_title);
		mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
		mTextStatus = (TextView) findViewById(R.id.status_text);
		mTextStatus.setText(getResources().getString(R.string.bt_state_init));
		mTextStatus.setOnClickListener(this);
		mButtonStopAlarm = (Button) findViewById(R.id.button_stop_alarm);
		mButtonStopAlarm.setOnClickListener(this);
		
		// Do data initialization after service started and binded
		doStartService();
		Utils.initialize(mContext);
		AppSettings.initializeAppSettings(mContext);
	}

	@Override
	public synchronized void onStart() {
		super.onStart();
	}
	
	@Override
	public synchronized void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		// Stop the timer
		/*
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
			mRefreshTimer = null;
		}
		*/
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		finalizeActivity();
	}
	
	@Override
	public void onLowMemory (){
		super.onLowMemory();
		// onDestroy is not always called when applications are finished by Android system.
		finalizeActivity();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_scan:
			scanBLE();
			return true;
		case R.id.action_discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();		// TODO: Disable this line to run below code
		
		if(mCurrentPage != FragmentAdapter.FRAGMENT_POS_BEACON) {
			mViewPager.setCurrentItem(FragmentAdapter.FRAGMENT_POS_BEACON);
			mLastExitTry = 0;
		} else {
			long currentTime = System.currentTimeMillis();
			if(currentTime - mLastExitTry < EXIT_RETRY_TIME_THRESHOLD) {
				finish();
			} else {
				mLastExitTry = currentTime;
				Toast.makeText(this, mContext.getString(R.string.error_exit_app), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		// This prevents reload after configuration changes
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * Implements TabListener
	 */
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	@Override
	public void OnFragmentCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IFragmentListener.CALLBACK_REQUEST_SCAN_CLASSIC:
			scanClassic();
			break;
		
		case IFragmentListener.CALLBACK_REQUEST_SCAN_BLE:
			scanBLE();
			break;
			
		case IFragmentListener.CALLBACK_BEACON_MAKE_UUID:
		case IFragmentListener.CALLBACK_BEACON_MAKE_MAJOR:
		case IFragmentListener.CALLBACK_BEACON_MAKE_MACRO:
			if(arg4 != null && arg4 instanceof Beacon) {
				mViewPager.setCurrentItem(FragmentAdapter.FRAGMENT_POS_MACRO);
				MacroFragment frg = (MacroFragment) mSectionsPagerAdapter.getItem(FragmentAdapter.FRAGMENT_POS_MACRO);
				frg.addNewMacro(msgType, (Beacon)arg4);
			}
			break;
		case IFragmentListener.CALLBACK_RUN_IN_BACKGROUND:
			if(mService != null)
				mService.startServiceMonitoring();
			break;
		case IFragmentListener.CALLBACK_SCAN_INTERVAL:
			int interval = arg0;
			if(mService != null)
				mService.setScanInterval(interval);
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_stop_alarm:
			if(mService != null)
				mService.stopWorkingMacro();
			break;
		case R.id.status_text:
			if(mScanState == BleManager.STATE_NONE 
				|| mScanState == BleManager.STATE_IDLE
				|| mScanState == BleManager.STATE_ERROR) {
				scanBLE();
			}
		}
	}
	
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	
	/**
	 * Service connection
	 */
	private ServiceConnection mServiceConn = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "Activity - Service connected");
			
			mService = ((BlueWaveService.LLServiceBinder) binder).getService();
			
			// Activity couldn't work with mService until connections are made
			// So initialize parameters and settings here, not while running onCreate()
			initialize();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
	
	private void doStartService() {
		Log.d(TAG, "# Activity - doStartService()");
		bindService(new Intent(this, BlueWaveService.class), mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	private void doStopService() {
		Log.d(TAG, "# Activity - doStopService()");
		mService.finalizeService();
		stopService(new Intent(this, BlueWaveService.class));
	}
	
	/**
	 * Initialization / Finalization
	 */
	private void initialize() {
		Logs.d(TAG, "# Activity - initialize()");
		mService.setupService(mActivityHandler);
		
		// If BT is not on, request that it be enabled.
		// BlueWaveService.setupBT() will then be called during onActivityResult
		if(!mService.isBluetoothEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
		}
	}
	
	private void finalizeActivity() {
		Logs.d(TAG, "# Activity - finalizeActivity()");
		
		if(!AppSettings.getBgService()) {
			doStopService();
		} else {
			if(mService != null)
				mService.setActivityFinished();
		}
		
		unbindService(mServiceConn);

		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
	}
	
	/**
	 * Scan Bluetooth Classic devices
	 */
	private void scanClassic() {
		if(mService != null)
			mService.discoverClassic();
	}
	
	/**
	 * Scan Bluetooth LE devices
	 */
	private void scanBLE() {
		boolean isScanStarted = false;
		
		clearListAndAddMemorizedBeacons();
		
		if(mService != null)
			isScanStarted = mService.discoverBLE();
		
		if(isScanStarted) {
			mScanState = BleManager.STATE_SCANNING;
		}
	}
	
	/**
	 * Ensure this device is discoverable by others
	 */
	private void ensureDiscoverable() {
		if (mService.getBluetoothScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(intent);
		}
	}
	
	private void refreshBeaconList() {
		BeaconFragment frgBeacon = (BeaconFragment) mSectionsPagerAdapter.getItem(FragmentAdapter.FRAGMENT_POS_BEACON);
		frgBeacon.deleteObjectAll();
		frgBeacon.addObjectAll(mService.getScannedBeacons());
		frgBeacon.addObjectAll(mService.getMemorizedBeacons());
	}
	
	private void clearListAndAddMemorizedBeacons() {
		BeaconFragment frgBeacon = (BeaconFragment) mSectionsPagerAdapter.getItem(FragmentAdapter.FRAGMENT_POS_BEACON);
		frgBeacon.deleteObjectAll();
		frgBeacon.addObjectAll(mService.getMemorizedBeacons());
	}
	
	
	/*****************************************************
	 *	Public classes
	 ******************************************************/
	
	/**
	 * Receives result from external activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logs.d(TAG, "onActivityResult " + resultCode);
		
		switch(requestCode) {
		case Constants.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up BT
				//mService.setupBT();
				mService.setupBLE();
				mService.setupService(mActivityHandler);
			} else {
				// User did not enable Bluetooth or an error occured
				Logs.e(TAG, "BT is not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			}
			break;
		}	// End of switch(requestCode)
	}
	
	
	
	/*****************************************************
	 *	Handler, Callback, Sub-classes
	 ******************************************************/
	
	public class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what) {
			case Constants.MESSAGE_BT_NEW_BEACON:
				mScanState = BleManager.STATE_SCANNING;
				if(msg.obj != null && (msg.obj instanceof Beacon)) {
					BeaconFragment frg = (BeaconFragment) mSectionsPagerAdapter.getItem(FragmentAdapter.FRAGMENT_POS_BEACON);
					frg.addObjectOnTop((Beacon)msg.obj);
				}
				break;
			
			case Constants.MESSAGE_BT_SCAN_STARTED:
				// Update status bar
				mScanState = BleManager.STATE_SCANNING;
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
				mTextStatus.setText(Utils.getScanString(Utils.SCAN_STRING_SCANNING));
				clearListAndAddMemorizedBeacons();
				break;
				
			case Constants.MESSAGE_BT_SCAN_FINISHED:
				// Update status bar
				mScanState = BleManager.STATE_IDLE;
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
				// mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));		// Clock image
				mTextStatus.setText(Utils.getScanString(Utils.SCAN_STRING_SCAN_FINISHED));
				break;
				
			case Constants.MESSAGE_BT_NOT_AVAILABLE:
				// Update status bar
				mScanState = BleManager.STATE_ERROR;
				mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
				mTextStatus.setText(Utils.getScanString(Utils.SCAN_STRING_BT_OFF));
				break;
				
			case Constants.MESSAGE_UPDATE_BEACON:
				if(mService != null) {
					mService.updateBeacon(msg.arg1, (Beacon)msg.obj);
					refreshBeaconList();
				}
				break;
				
			case Constants.MESSAGE_REFRESH_BEACON_LIST:
				refreshBeaconList();
				break;
				
			default:
				break;
			}
			
			super.handleMessage(msg);
		}
	}	// End of class ActivityHandler


}
