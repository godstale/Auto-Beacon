package com.wiredfactory.bluewave.contents;

import java.util.ArrayList;

import com.wiredfactory.bluewave.R;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;


public class BeaconManager {
	// Debugging
	private static final String TAG = "BeaconManager";

	// Constants
	
	// System, Management
	private static Context mContext;
	private static Handler mHandler;
	private static BeaconManager mBeaconManager = null;		// Singleton pattern
	
	private static ArrayList<Beacon> mBeaconList = new ArrayList<Beacon>();
	
	// DB
	private DBHelper mDB;
	
	// Parameters
	private static boolean mIsInitialized = false;


	/**
	 * Constructor
	 * @param context  The UI Activity Context
	 * @param handler  A Listener to receive messages back to the UI Activity
	 */
	private BeaconManager(Context context) {
		mContext = context;
		
		if(mContext == null)
			return;
		
		if(mDB == null) {
			mDB = new DBHelper(mContext).openWritable();
		}
		
		getAllBeaconFromDB();
		mIsInitialized = true;
	}

	public synchronized static BeaconManager getInstance(Context c) {
		if(mBeaconManager == null)
			mBeaconManager = new BeaconManager(c);
		
		return mBeaconManager;
	}
	
	public synchronized static BeaconManager getInstance() {
		return mBeaconManager;
	}

	public synchronized void finalize() {
		if(mContext == null)
			return;
        
		mBeaconList.clear();
		
		if(mDB != null) {
			mDB.close();
			mDB = null;
		}
		
		mIsInitialized = false;
		mBeaconManager = null;
	}


	/*****************************************************
	 *	Private methods
	 ******************************************************/
	/**
	 * Extract beacon info from cursor and insert beacon object to array list
	 * WARNING: This method doesn't close cursor. Caller must close cursor after use.
	 * @param c		Cursor
	 */
	private void fetchBeaconFromCursor(Cursor c) {
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				Beacon beacon = new Beacon();
				beacon.id = c.getInt(DBHelper.INDEX_BEACON_ID);
				beacon.beaconName = c.getString(DBHelper.INDEX_BEACON_NAME);
				beacon.proximityUuid = c.getString(DBHelper.INDEX_BEACON_UUID);
				beacon.major = c.getInt(DBHelper.INDEX_BEACON_MAJOR);
				beacon.minor = c.getInt(DBHelper.INDEX_BEACON_MINOR);
				beacon.proximity = c.getInt(DBHelper.INDEX_BEACON_PROXIMITY);
				beacon.accuracy = c.getDouble(DBHelper.INDEX_BEACON_ACCURACY);
				beacon.rssi = c.getInt(DBHelper.INDEX_BEACON_RSSI);
				beacon.txPower = c.getInt(DBHelper.INDEX_BEACON_TXPOWER);
				beacon.bluetoothAddress = c.getString(DBHelper.INDEX_BEACON_BT_ADDRESS);
				
				beacon.isRemembered = true;
				
				mBeaconList.add(beacon);
				c.moveToNext();
			}
		}
	}
	
	private boolean isDuplicatedBeacon(Beacon beacon) {
		boolean isDuplicated = false;
		for(Beacon _beacon : mBeaconList) {
			if(beacon.getProximityUuid().equalsIgnoreCase(_beacon.getProximityUuid())) {
				isDuplicated = true;
				break;
			}
		}
		return isDuplicated;
	}
	
	private void updateBeaconInList(Beacon beacon) {
		for(Beacon _beacon : mBeaconList) {
			if(beacon.id == _beacon.id) {
				_beacon.copyFromBeacon(beacon);
			}
		}
	}
	
	private void deleteMacroInList(int id) {
		for(int i=mBeaconList.size()-1; i>-1; i--) {
			Beacon _beacon = mBeaconList.get(i);
			if(_beacon.id == id) {
				mBeaconList.remove(i);
			}
		}
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void setHandler(Handler h) {
		mHandler = h;
	}
	
	public ArrayList<Beacon> getBeaconList() {
		return mBeaconList;
	}
	
	public void getAllBeaconFromDB() {
		if(mDB == null)
			return;
		Cursor c = mDB.selectBeaconAll();
		if(c == null)
			return;

		mBeaconList.clear();
		fetchBeaconFromCursor(c);
		c.close();
	}
	
	/**
	 * 
	 * @param beacon
	 * @return	int		ID of new beacon
	 */
	public synchronized int addBeacon(Beacon beacon) {
		if(beacon == null || mDB == null) return -1;
		
		int _id = (int)mDB.insertBeacon(beacon);
		if(_id < 0)
			return -1;
		
		beacon.id = _id;
		//mBeaconList.add(macro);
		getAllBeaconFromDB();
		
		return _id;
	}
	
	public synchronized void updateBeacon(Beacon beacon) {
		if(beacon == null || mDB == null) return;
		
		int count = mDB.updateBeacon(beacon);
		if(count > 0) {
			// updateMacroInList(macro);
			getAllBeaconFromDB();
		}
	}
	
	public synchronized void deleteBeacon(int id) {
		if(id < 0 || mDB == null) return;
		
		int count = mDB.deleteBeaconWithID(id);
		if(count > 0) {
			//deleteMacroInList(id);
			getAllBeaconFromDB();
		}
	}
	
	public boolean isInitialized() {
		return mIsInitialized;
	}
	
	public synchronized int insertOrUpdateBeacon(Beacon beacon) {
		int _id = -1;
		if(beacon.getId() < 0) {
			_id = addBeacon(beacon);
		} else {
			if(isDuplicatedBeacon(beacon)) {
				updateBeacon(beacon);
				_id = beacon.id;
			} else {
				_id = addBeacon(beacon);
			}
		}
		
		return _id;
	}
	
	public boolean checkWithRememberedBeacon(Beacon beacon) {
		boolean isFound = false;
		if(beacon != null) {
			for(Beacon _beacon : mBeaconList) {
				if(beacon.major == _beacon.major
						&& beacon.minor == _beacon.minor
						&& beacon.getProximityUuid().equalsIgnoreCase(_beacon.getProximityUuid())) {
					beacon.setBeaconName(mContext.getString(R.string.title_beacon_already_remembered) + _beacon.getBeaconName());
					isFound = true;
				}
			}
		}
		return isFound;
	}
	
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
}
