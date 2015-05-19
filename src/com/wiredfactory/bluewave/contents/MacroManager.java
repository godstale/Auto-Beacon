package com.wiredfactory.bluewave.contents;

import java.util.ArrayList;


import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

public class MacroManager {
	// Debugging
	private static final String TAG = "MacroManager";

	// Constants
	
	// System, Management
	private static Context mContext;
	private static Handler mHandler;
	private static MacroManager mMacroManager = null;		// Singleton pattern
	private static MacroExec mMacroExec;
	
	private static ArrayList<Macro> mMacroList = new ArrayList<Macro>();
	
	// DB
	private DBHelper mDB;
	
	// Parameters
	private static boolean mIsInitialized = false;


	/**
	 * Constructor
	 * @param context  The UI Activity Context
	 * @param handler  A Listener to receive messages back to the UI Activity
	 */
	private MacroManager(Context context) {
		mContext = context;
		
		if(mContext == null)
			return;
		
		if(mDB == null) {
			mDB = new DBHelper(mContext).openWritable();
		}
		
		getAllMacroFromDB();
		mIsInitialized = true;
	}

	public synchronized static MacroManager getInstance(Context c) {
		if(mMacroManager == null)
			mMacroManager = new MacroManager(c);
		
		return mMacroManager;
	}
	
	public synchronized static MacroManager getInstance() {
		return mMacroManager;
	}
	
	public void setMacroExec(MacroExec exec) {
		mMacroExec = exec;
	}

	public synchronized void finalize() {
		if(mContext == null)
			return;
        
		mMacroList.clear();
		
		if(mDB != null) {
			mDB.close();
			mDB = null;
		}
		
		mIsInitialized = false;
		mMacroManager = null;
	}


	/*****************************************************
	 *	Private methods
	 ******************************************************/
	/**
	 * Extract macro info from cursor and insert macro object to array list
	 * WARNING: This method doesn't close cursor. Caller must close cursor after use.
	 * @param c		Cursor
	 */
	private void fetchMacroFromCursor(Cursor c) {
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				Macro macro = new Macro();
				macro.id = c.getInt(DBHelper.INDEX_MACRO_ID);
				macro.title = c.getString(DBHelper.INDEX_MACRO_TITLE);
				int isEnabledInt = c.getInt(DBHelper.INDEX_MACRO_ENABLED);
				macro.isEnabled = (isEnabledInt == 0 ? false : true);
				macro.count = c.getInt(DBHelper.INDEX_MACRO_COUNT);
				macro.major = c.getInt(DBHelper.INDEX_MACRO_MAJOR);
				macro.minor = c.getInt(DBHelper.INDEX_MACRO_MINOR);
				macro.uuid = c.getString(DBHelper.INDEX_MACRO_UUID);
				macro.distance = c.getInt(DBHelper.INDEX_MACRO_DISTANCE);
				macro.works = c.getInt(DBHelper.INDEX_MACRO_WORK);
				macro.repeats = c.getInt(DBHelper.INDEX_MACRO_REPEAT);
				macro.destination = c.getString(DBHelper.INDEX_MACRO_DESTINATION);
				macro.duration = c.getInt(DBHelper.INDEX_MACRO_DURATION);
				macro.userMessage = c.getString(DBHelper.INDEX_MACRO_MESSAGE);
				
				mMacroList.add(macro);
				c.moveToNext();
			}
		}
	}
	
	@Deprecated
	private boolean isDuplicatedMacro(Macro macro) {
		boolean isDuplicated = false;
		for(Macro _macro : mMacroList) {
			if(macro.uuid.equalsIgnoreCase(_macro.uuid)) {
				isDuplicated = true;
				break;
			}
		}
		return isDuplicated;
	}
	
	private void updateMacroInList(Macro macro) {
		for(Macro _macro : mMacroList) {
			if(macro.id == _macro.id) {
				_macro.copyFromMacro(macro);
			}
		}
	}
	
	private void deleteMacroInList(int id) {
		for(int i=mMacroList.size()-1; i>-1; i--) {
			Macro _macro = mMacroList.get(i);
			if(_macro.id == id) {
				mMacroList.remove(i);
			}
		}
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void setHandler(Handler h) {
		mHandler = h;
	}
	
	public ArrayList<Macro> getMacroList() {
		return mMacroList;
	}
	
	public void getAllMacroFromDB() {
		if(mDB == null)
			return;
		Cursor c = mDB.selectMacroAll();
		if(c == null)
			return;

		mMacroList.clear();
		fetchMacroFromCursor(c);
		c.close();
	}
	
	public synchronized boolean addMacro(Macro macro) {
		if(macro == null || mDB == null) return false;
		
		int _id = (int)mDB.insertMacro(macro);
		if(_id < 0)
			return false;
		
		macro.id = _id;
		//mMacroList.add(macro);
		getAllMacroFromDB();
		
		return true;
	}
	
	public synchronized void updateMacro(Macro macro) {
		if(macro == null || mDB == null) return;
		
		int count = mDB.updateMacro(macro);
		if(count > 0) {
			// updateMacroInList(macro);
			getAllMacroFromDB();
		}
	}
	
	public synchronized void deleteMacro(int id) {
		if(id < 0 || mDB == null) return;
		
		int count = mDB.deleteMacroWithID(id);
		if(count > 0) {
			//deleteMacroInList(id);
			getAllMacroFromDB();
		}
	}
	
	public boolean isInitialized() {
		return mIsInitialized;
	}
	
	public synchronized void checkMacroWithBeacon(Beacon beacon) {
		if(beacon != null && beacon.getProximityUuid() != null) {
			for(Macro macro : mMacroList) {
				// Check if this macro is enabled
				if(!macro.isEnabled || macro.distance == Macro.MACRO_DISTANCE_NONE)
					continue;
				
				// Check UUID, Major and Minor code
				boolean isFound = true;
				if(macro.uuid != null && macro.uuid.length() > 1) {
					isFound = macro.uuid.equalsIgnoreCase(beacon.getProximityUuid())
							&& beacon.getProximity() != Beacon.PROXIMITY_UNKNOWN
							&& macro.distance != Beacon.PROXIMITY_UNKNOWN
							&& beacon.getProximity() <= macro.distance;
				}
				if(isFound && macro.major > -1) {
					isFound = isFound && macro.major == beacon.getMajor() 
							&& beacon.getProximity() != Beacon.PROXIMITY_UNKNOWN
							&& macro.distance != Beacon.PROXIMITY_UNKNOWN
							&& beacon.getProximity() <= macro.distance;
				}
				if(isFound && macro.minor > -1) {
					isFound = isFound && macro.minor == beacon.getMinor() 
							&& beacon.getProximity() != Beacon.PROXIMITY_UNKNOWN
							&& macro.distance != Beacon.PROXIMITY_UNKNOWN
							&& beacon.getProximity() <= macro.distance;
				}
				
				if(isFound) {
					// Reserve what to do
					MacroWork macro_work = new MacroWork(macro.works);
					switch(macro.works) {
					// Messaging group
					case Macro.MACRO_WORKS_SEND_SMS:
					case Macro.MACRO_WORKS_SEND_EMAIL:
					case Macro.MACRO_WORKS_SEND_MESSAGE:
						macro_work.count = 1;
						macro_work.interval = 0;
						macro_work.duration = 0;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
						
					// Alarm group
					case Macro.MACRO_WORKS_VIBRATION:
						macro_work.count = 5;
						macro_work.interval = 1500;
						macro_work.duration = 750;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
					case Macro.MACRO_WORKS_SOUND:
						macro_work.count = 5;
						macro_work.interval = 31*1000;
						macro_work.duration = 30*1000;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
					case Macro.MACRO_WORKS_NOTIFICATION:
						macro_work.count = 1;
						macro_work.interval = 0;
						macro_work.duration = 0;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
						
					// Change settings group
					case Macro.MACRO_WORKS_SET_BELL_MODE:
					case Macro.MACRO_WORKS_SET_VIBRATION_MODE:
					case Macro.MACRO_WORKS_SET_SILENT_MODE:
					case Macro.MACRO_WORKS_TURN_ON_WIFI:
					case Macro.MACRO_WORKS_TURN_OFF_WIFI:
						macro_work.count = 1;
						macro_work.interval = 0;
						macro_work.duration = 0;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
						
					// Launch app group
					case Macro.MACRO_WORKS_LAUNCH_BROWSER:
						macro_work.count = 1;
						macro_work.interval = 0;
						macro_work.duration = 0;
						macro_work.destination = macro.destination;
						macro_work.message = macro.userMessage;
						macro_work.title = macro.title;
						break;
					}	// End of switch
					
					mMacroExec.addMacroWork(macro_work);
					mMacroExec.interrupt();		// Wake up thread
				}
			}	// End of for loop
		}
	}	// End of checkMacroWithBeacon()
	
	public synchronized void checkNotFoundMacro(ArrayList<Beacon> beaconList) {
		for(Macro macro : mMacroList) {
			// Check if this macro is enabled and work in not-found condition 
			if(!macro.isEnabled || macro.distance != Macro.MACRO_DISTANCE_NONE)
				continue;
			
			boolean doWork = true;
			if(beaconList == null || beaconList.size() < 1) {
				// No beacon exists. Do macro work which has not-found condition 
			} else {
				for(Beacon beacon : beaconList) {
					// Sometimes below code makes NullPointerException but I dont know why
					// Double check null pointer
					if(beacon == null) continue;
					
					// Check selected UUID is found or not.
					boolean isFound = true;
					if(beacon != null && macro != null && macro.uuid != null && macro.uuid.length() > 1) {
						isFound = ( macro.uuid.equalsIgnoreCase(beacon.getProximityUuid()) 
								&& beacon.getProximity() > Beacon.PROXIMITY_UNKNOWN );
					}
					if(beacon != null && macro != null && isFound && macro.major > -1) {
						isFound = isFound && macro.major == beacon.getMajor() 
								&& beacon.getProximity() > Beacon.PROXIMITY_UNKNOWN;
					}
					if(beacon != null && macro != null && isFound && macro.minor > -1) {
						isFound = isFound && macro.minor == beacon.getMinor() 
								&& beacon.getProximity() > Beacon.PROXIMITY_UNKNOWN;
					}
					
					if(isFound) {
						// Specified beacon is found. Do not execute macro work.
						doWork = false;
						break;
					}
				}
			}
			
			if(doWork) {
				// Reserve what to do
				MacroWork macro_work = new MacroWork(macro.works);
				switch(macro.works) {
				// Messaging group
				case Macro.MACRO_WORKS_SEND_SMS:
				case Macro.MACRO_WORKS_SEND_EMAIL:
				case Macro.MACRO_WORKS_SEND_MESSAGE:
					macro_work.count = 1;
					macro_work.interval = 0;
					macro_work.duration = 0;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
					
				// Alarm group
				case Macro.MACRO_WORKS_VIBRATION:
					macro_work.count = 7;
					macro_work.interval = 1500;
					macro_work.duration = 750;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
				case Macro.MACRO_WORKS_SOUND:
					macro_work.count = 5;
					macro_work.interval = 31*1000;
					macro_work.duration = 30*1000;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
				case Macro.MACRO_WORKS_NOTIFICATION:
					macro_work.count = 1;
					macro_work.interval = 0;
					macro_work.duration = 0;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
					
				// Change settings group
				case Macro.MACRO_WORKS_SET_BELL_MODE:
				case Macro.MACRO_WORKS_SET_VIBRATION_MODE:
				case Macro.MACRO_WORKS_SET_SILENT_MODE:
				case Macro.MACRO_WORKS_TURN_ON_WIFI:
				case Macro.MACRO_WORKS_TURN_OFF_WIFI:
					macro_work.count = 1;
					macro_work.interval = 0;
					macro_work.duration = 0;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
					
				// Launch app group
				case Macro.MACRO_WORKS_LAUNCH_BROWSER:
					macro_work.count = 1;
					macro_work.interval = 0;
					macro_work.duration = 0;
					macro_work.destination = macro.destination;
					macro_work.message = macro.userMessage;
					macro_work.title = macro.title;
					break;
				}	// End of switch
				
				mMacroExec.addMacroWork(macro_work);
				mMacroExec.interrupt();		// Wake up the macro executor thread
			}
		}	// End of for loop
	}	// End of checkNotFoundMacro()
	
	
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
}
