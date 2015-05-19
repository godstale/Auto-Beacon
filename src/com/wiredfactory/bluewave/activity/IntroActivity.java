package com.wiredfactory.bluewave.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.MacroManager;
import com.wiredfactory.bluewave.service.BlueWaveService;
import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.Logs;
import com.wiredfactory.bluewave.utils.RecycleUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class IntroActivity extends Activity {

	private Context mContext;
	private Timer mRefreshTimer;
	
	
	
	/*****************************************************
	 *	 Overrided methods
	 ******************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//----- System, Context
		mContext = this;//.getApplicationContext();
		
		//----- UI
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_intro);
		
		initialize();
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
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		// Stop the timer
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
			mRefreshTimer = null;
		}
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
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//case R.id.action_discoverable:
		//	return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		//super.onBackPressed();		// TODO: Disable this line to run below code
		finalizeActivity();
		finishActivity();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		// This prevents reload after configuration changes
		super.onConfigurationChanged(newConfig);
	}
	
	
	/**
	 * Initialization / Finalization
	 */
	private void initialize() {
		
		// Check bluetooth
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// If the adapter is null, then Bluetooth is not supported
		if (bluetoothAdapter == null) {
			Toast.makeText(this, getString(R.string.bt_ble_not_supported), Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
			return;
		}
		
		startServiceAndManager();
		reserveActivityChange(2*1000);
	}
	
	private void finalizeActivity() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
	}
	
	public void finishActivity() {
		finish();
	}
	
	private void startServiceAndManager() {
		startService(new Intent(this, BlueWaveService.class));
		MacroManager.getInstance(getApplicationContext());		// To make macro manager instance
	}
	
	private void reserveActivityChange(long delay) {
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
		}
		mRefreshTimer = new Timer();
		mRefreshTimer.schedule(new RefreshTimerTask(), delay);
	}
	
	/*****************************************************
	 *	Public classes
	 ******************************************************/
	/**
	 * Receives result from external activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case Constants.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				startServiceAndManager();
				reserveActivityChange(1*1000);
			} else {
				// User did not enable Bluetooth or an error occured
				Logs.e("BT is not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			}
			break;
		}	// End of switch(requestCode)
	}
	
	
	
	/*****************************************************
	 *	Sub classes
	 ******************************************************/
	private class RefreshTimerTask extends TimerTask {
		public RefreshTimerTask() {}
		
		public void run() {
			startActivity(new Intent(mContext, MainActivity.class));
			finishActivity();
		}
	}
	

	
}
