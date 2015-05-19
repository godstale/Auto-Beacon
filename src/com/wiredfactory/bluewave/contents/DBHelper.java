package com.wiredfactory.bluewave.contents;


import com.wiredfactory.bluewave.utils.Logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper {
	
	private static final String TAG  ="DBHelper";
	
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "bluewave";

	//----------- Macro data table parameters
	public static final String TABLE_NAME_MACRO = "t_macro";
	
	public static final String KEY_MACRO_ID = "_id";			// int		primary key, auto increment
	public static final String KEY_MACRO_TITLE = "title";
	public static final String KEY_MACRO_ENABLED = "is_enabled";
	public static final String KEY_MACRO_COUNT = "count";
	public static final String KEY_MACRO_MAJOR = "major";
	public static final String KEY_MACRO_MINOR = "minor";
	public static final String KEY_MACRO_UUID = "uuid";
	public static final String KEY_MACRO_DISTANCE = "distance";
	public static final String KEY_MACRO_WORK = "work_type";
	public static final String KEY_MACRO_REPEAT = "repeats";
	public static final String KEY_MACRO_DESTINATION = "destination";
	public static final String KEY_MACRO_DURATION = "duration";
	public static final String KEY_MACRO_MESSAGE = "user_msg";
	public static final String KEY_MACRO_ARG0 = "arg0";
	public static final String KEY_MACRO_ARG1 = "arg1";
	public static final String KEY_MACRO_ARG2 = "arg2";
	public static final String KEY_MACRO_ARG3 = "arg3";
	
	public static final int INDEX_MACRO_ID = 0;
	public static final int INDEX_MACRO_TITLE = 1;
	public static final int INDEX_MACRO_ENABLED = 2;
	public static final int INDEX_MACRO_COUNT = 3;
	public static final int INDEX_MACRO_MAJOR = 4;
	public static final int INDEX_MACRO_MINOR = 5;
	public static final int INDEX_MACRO_UUID = 6;
	public static final int INDEX_MACRO_DISTANCE = 7;
	public static final int INDEX_MACRO_WORK = 8;
	public static final int INDEX_MACRO_REPEAT = 9;
	public static final int INDEX_MACRO_DESTINATION = 10;
	public static final int INDEX_MACRO_DURATION = 11;
	public static final int INDEX_MACRO_MESSAGE = 12;
	public static final int INDEX_MACRO_ARG0 = 13;
	public static final int INDEX_MACRO_ARG1 = 14;
	public static final int INDEX_MACRO_ARG2 = 15;
	public static final int INDEX_MACRO_ARG3 = 16;
	
	private static final String DATABASE_CREATE_MACRO_TABLE = "CREATE TABLE " +TABLE_NAME_MACRO+ "("
													+ KEY_MACRO_ID +" Integer primary key autoincrement, "
													+ KEY_MACRO_TITLE + " Integer not null, "
													+ KEY_MACRO_ENABLED + " integer, "
													+ KEY_MACRO_COUNT + " integer, "
													+ KEY_MACRO_MAJOR + " integer, "
													+ KEY_MACRO_MINOR + " integer, "
													+ KEY_MACRO_UUID + " Text, "
													+ KEY_MACRO_DISTANCE + " integer, "
													+ KEY_MACRO_WORK + " integer, "
													+ KEY_MACRO_REPEAT + " integer, "
													+ KEY_MACRO_DESTINATION + " Text, "
													+ KEY_MACRO_DURATION + " integer, "
													+ KEY_MACRO_MESSAGE + " Text, "
													+ KEY_MACRO_ARG0 + " integer, "
													+ KEY_MACRO_ARG1 + " integer, "
													+ KEY_MACRO_ARG2 + " Text, "
													+ KEY_MACRO_ARG3 + " Text"
													+ ")";
	private static final String DATABASE_DROP_MACRO_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_MACRO;
	//----------- End of Macro data table parameters
	
	//----------- Beacon data table parameters
	public static final String TABLE_NAME_BEACON = "t_beacon";
	
	public static final String KEY_BEACON_ID = "_id";			// int		primary key, auto increment
	public static final String KEY_BEACON_NAME = "name";
	public static final String KEY_BEACON_UUID = "uuid";
	public static final String KEY_BEACON_UUID_HIGH = "uuid_high";
	public static final String KEY_BEACON_UUID_LOW = "uuid_low";
	public static final String KEY_BEACON_MAJOR = "major";
	public static final String KEY_BEACON_MINOR = "minor";
	public static final String KEY_BEACON_PROXIMITY = "proximity";
	public static final String KEY_BEACON_ACCURACY = "accuracy";
	public static final String KEY_BEACON_RSSI = "rssi";
	public static final String KEY_BEACON_TXPOWER = "txpower";
	public static final String KEY_BEACON_BT_ADDRESS = "address";
	public static final String KEY_BEACON_SPARE = "spare";
	public static final String KEY_BEACON_ARG0 = "arg0";
	public static final String KEY_BEACON_ARG1 = "arg1";
	public static final String KEY_BEACON_ARG2 = "arg2";
	public static final String KEY_BEACON_ARG3 = "arg3";
	
	public static final int INDEX_BEACON_ID = 0;
	public static final int INDEX_BEACON_NAME = 1;
	public static final int INDEX_BEACON_UUID = 2;
	public static final int INDEX_BEACON_UUID_HIGH = 3;
	public static final int INDEX_BEACON_UUID_LOW = 4;
	public static final int INDEX_BEACON_MAJOR = 5;
	public static final int INDEX_BEACON_MINOR = 6;
	public static final int INDEX_BEACON_PROXIMITY = 7;
	public static final int INDEX_BEACON_ACCURACY = 8;
	public static final int INDEX_BEACON_RSSI = 9;
	public static final int INDEX_BEACON_TXPOWER = 10;
	public static final int INDEX_BEACON_BT_ADDRESS = 11;
	public static final int INDEX_BEACON_SPARE = 12;
	public static final int INDEX_BEACON_ARG0 = 13;
	public static final int INDEX_BEACON_ARG1 = 14;
	public static final int INDEX_BEACON_ARG2 = 15;
	public static final int INDEX_BEACON_ARG3 = 16;
	
	private static final String DATABASE_CREATE_BEACON_TABLE = "CREATE TABLE " +TABLE_NAME_BEACON+ "("
													+ KEY_BEACON_ID +" Integer primary key autoincrement, "
													+ KEY_BEACON_NAME + " Integer not null, "
													+ KEY_BEACON_UUID + " Text, "
													+ KEY_BEACON_UUID_HIGH + " Text, "
													+ KEY_BEACON_UUID_LOW + " Text, "
													+ KEY_BEACON_MAJOR + " integer, "
													+ KEY_BEACON_MINOR + " integer, "
													+ KEY_BEACON_PROXIMITY + " integer, "
													+ KEY_BEACON_ACCURACY + " REAL, "
													+ KEY_BEACON_RSSI + " integer, "
													+ KEY_BEACON_TXPOWER + " integer, "
													+ KEY_BEACON_BT_ADDRESS + " Text, "
													+ KEY_BEACON_SPARE + " integer, "
													+ KEY_BEACON_ARG0 + " integer, "
													+ KEY_BEACON_ARG1 + " integer, "
													+ KEY_BEACON_ARG2 + " Text, "
													+ KEY_BEACON_ARG3 + " Text"
													+ ")";
	private static final String DATABASE_DROP_BEACON_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_BEACON;
	//----------- End of Beacon data table parameters
	
	
	
	// Context, System
	private final Context mContext;
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	// Constructor
	public DBHelper(Context context) {
		this.mContext = context;
	}
	
	
	//----------------------------------------------------------------------------------
	// Public classes
	//----------------------------------------------------------------------------------
	// DB open (Writable)
	public DBHelper openWritable() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	// DB open (Readable)
	public DBHelper openReadable() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}
	
	// Terminate DB
	public void close() {
		if(mDb != null) {
			mDb.close();
			mDb = null;
		}
		if(mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}
	
	//----------------------------------------------------------------------------------
	// INSERT
	//----------------------------------------------------------------------------------
	public long insertMacro(Macro macro) throws SQLiteConstraintException {
		if(macro == null)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_MACRO_TITLE, macro.title);
		insertValues.put(KEY_MACRO_ENABLED, (macro.isEnabled ? 1 : 0) );
		insertValues.put(KEY_MACRO_COUNT, macro.count);
		insertValues.put(KEY_MACRO_MAJOR, macro.major);
		insertValues.put(KEY_MACRO_MINOR, macro.minor);
		insertValues.put(KEY_MACRO_UUID, macro.uuid);
		insertValues.put(KEY_MACRO_DISTANCE, macro.distance);
		insertValues.put(KEY_MACRO_WORK, macro.works);
		insertValues.put(KEY_MACRO_REPEAT, macro.repeats);
		insertValues.put(KEY_MACRO_DESTINATION, macro.destination);
		insertValues.put(KEY_MACRO_DURATION, macro.duration);
		insertValues.put(KEY_MACRO_MESSAGE, macro.userMessage);
		//insertValues.put(KEY_MACRO_ARG0, 0);
		//insertValues.put(KEY_MACRO_ARG1, 0);
		//insertValues.put(KEY_MACRO_ARG2, subData);
		
		Logs.d(TAG, "+ Insert macro : UUID="+macro.uuid+", Major="+macro.major+", minor="+macro.minor+", work="+macro.works+", msg="+macro.userMessage);
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.insertOrThrow(TABLE_NAME_MACRO, null, insertValues);
		}
	}
	
	public long insertBeacon(Beacon beacon) throws SQLiteConstraintException {
		if(beacon == null)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_BEACON_NAME, beacon.getBeaconName());
		insertValues.put(KEY_BEACON_UUID, beacon.getProximityUuid() );
		//insertValues.put(KEY_BEACON_UUID_HIGH, beacon.getProximityUuidBytes());	// TODO
		//insertValues.put(KEY_BEACON_UUID_LOW, beacon.getProximityUuidBytes());	// TODO
		insertValues.put(KEY_BEACON_MAJOR, beacon.getMajor());
		insertValues.put(KEY_BEACON_MINOR, beacon.getMinor());
		//insertValues.put(KEY_BEACON_PROXIMITY, beacon.getProximity());
		insertValues.put(KEY_BEACON_ACCURACY, beacon.getAccuracy());
		insertValues.put(KEY_BEACON_RSSI, beacon.getRssi());
		insertValues.put(KEY_BEACON_TXPOWER, beacon.getTxPower());
		insertValues.put(KEY_BEACON_BT_ADDRESS, beacon.getBluetoothAddress());
		//insertValues.put(KEY_BEACON_SPARE, 0);
		//insertValues.put(KEY_BEACON_ARG0, 0);
		//insertValues.put(KEY_BEACON_ARG1, 0);
		//insertValues.put(KEY_BEACON_ARG3, subData);
		
		Logs.d(TAG, "+ Insert macro : Name="+beacon.getBeaconName()
				+", UUID="+beacon.getProximityUuid()
				+", Major="+beacon.getMajor()
				+", minor="+beacon.getMinor()
				+", BT_ADDRESS="+beacon.getBluetoothAddress());
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.insertOrThrow(TABLE_NAME_BEACON, null, insertValues);
		}
	}
	

	/*
	public boolean insertBulkItems(ArrayList<FeedObject> feedList) {
		long start = System.currentTimeMillis();
		int time = (int)(start / 1000);
		if(feedList==null || feedList.size()<1) 
			return false;
		
		Logs.d(TAG, "# Insert bulk : type="+feedList.get(0).mType+", item count = "+feedList.size());

		InsertHelper iHelp = new InsertHelper(mDb, TABLE_NAME_FEED_ITEM);
		int ktype 		= iHelp.getColumnIndex(KEY_FEED_TYPE);
		int kstatus 	= iHelp.getColumnIndex(KEY_FEED_STATUS);
		int kid 			= iHelp.getColumnIndex(KEY_FEED_IDSTRING);
		int kname 			= iHelp.getColumnIndex(KEY_FEED_NAME);
		int klink 		= iHelp.getColumnIndex(KEY_FEED_LINK);
		int kkeyword 	= iHelp.getColumnIndex(KEY_FEED_KEYWORD);
		int kcontent 	= iHelp.getColumnIndex(KEY_FEED_CONTENT);
		int kthumbnail 	= iHelp.getColumnIndex(KEY_FEED_THUMBNAILURL);
		int kdate 			= iHelp.getColumnIndex(KEY_FEED_DATE);
		int krank 			= iHelp.getColumnIndex(KEY_FEED_RANK);
		int kclick 			= iHelp.getColumnIndex(KEY_FEED_CLICK);
		int kranktype	= iHelp.getColumnIndex(KEY_FEED_ARG0);
		int kversion	= iHelp.getColumnIndex(KEY_FEED_ARG1);
		int kfullimage	= iHelp.getColumnIndex(KEY_FEED_ARG2);
		
		synchronized (mDb) {
			if(mDb == null) return false;
			try
			{
				mDb.beginTransaction();
				// First one is recent one. So insert oldest first.
				for(int i = feedList.size()-1 ; -1<i ; i--)
				{
					FeedObject feed = feedList.get(i);
					// need to tell the helper you are inserting (rather than replacing)
					iHelp.prepareForInsert();

					// do the equivalent of ContentValues.put("field","value") here
					iHelp.bind(ktype, feed.mType);
					iHelp.bind(kstatus, feed.mDownloadStatus);
					iHelp.bind(kid, feed.mId);
					if(feed.mName != null)
						iHelp.bind(kname, feed.mName);
					if(feed.mLink != null)
						iHelp.bind(klink, feed.mLink);
					if(feed.mKeyword != null)
						iHelp.bind(kkeyword, feed.mKeyword);
					if(feed.mContent != null)
						iHelp.bind(kcontent, feed.mContent);
					if(feed.mThumbnailUrl != null)
						iHelp.bind(kthumbnail, feed.mThumbnailUrl);
					if(feed.mDate != null)
						iHelp.bind(kdate, feed.mDate);
					else
						iHelp.bind(kdate, time);

					iHelp.bind(krank, feed.mRankUpAndDown);
					iHelp.bind(kclick, feed.mCommentCount);
					iHelp.bind(kranktype, feed.mRankType);
					iHelp.bind(kversion, feed.mVersion);

					if(feed.mFullSizeImageURL != null)
						iHelp.bind(kfullimage, feed.mFullSizeImageURL);
					
					//the db.insert() equilvalent
					iHelp.execute();
				}
				mDb.setTransactionSuccessful();
			}
			catch(Exception e) {
			}
			finally	{
				mDb.endTransaction();
			}
		}

		return true;
	}
*/
	
	//----------------------------------------------------------------------------------
	// SELECT
	//----------------------------------------------------------------------------------
	public Cursor selectMacroAll() {
		synchronized (mDb) {
			if(mDb == null) return null;
			return mDb.query(
					TABLE_NAME_MACRO,	// Table : String
					null,		// Columns : String[]
					null,		// Selection : String
					null,		// Selection arguments: String[]
					null,		// Group by : String
					null,		// Having : String
					null,		// Order by : String
					null );		// Limit : String
		}
	}
	
	public Cursor selectMacro(int _id, int count) {
		synchronized (mDb) {
			if(mDb == null) return null;
			
			String selection = null;
			if(_id > -1)
				selection = KEY_MACRO_ID + "=" + Integer.toString(_id);
			
			String countString = null;
			if(count > 0)
				countString = Integer.toString(count);
			
			return mDb.query(
					TABLE_NAME_MACRO,		// Table : String
					null,						// Columns : String[]
					selection,		// Selection 	: String
					null,			// Selection arguments: String[]
					null,			// Group by 	: String
					null,			// Having 		: String
					null,			// Order by 	: String
					countString );		// Limit		: String
		}
	}

	public Cursor selectBeaconAll() {
		synchronized (mDb) {
			if(mDb == null) return null;
			return mDb.query(
					TABLE_NAME_BEACON,	// Table : String
					null,		// Columns : String[]
					null,		// Selection : String
					null,		// Selection arguments: String[]
					null,		// Group by : String
					null,		// Having : String
					null,		// Order by : String
					null );		// Limit : String
		}
	}
	
	public Cursor selectBeacon(int _id, int count) {
		synchronized (mDb) {
			if(mDb == null) return null;
			
			String selection = null;
			if(_id > -1)
				selection = KEY_BEACON_ID + "=" + Integer.toString(_id);
			
			String countString = null;
			if(count > 0)
				countString = Integer.toString(count);
			
			return mDb.query(
					TABLE_NAME_BEACON,		// Table : String
					null,						// Columns : String[]
					selection,		// Selection 	: String
					null,			// Selection arguments: String[]
					null,			// Group by 	: String
					null,			// Having 		: String
					null,			// Order by 	: String
					countString );		// Limit		: String
		}
	}
	
	
	//----------------------------------------------------------------------------------
	// Update
	//----------------------------------------------------------------------------------
	public int updateMacro(Macro macro) {
		if(macro == null || macro.id < 0)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_MACRO_TITLE, macro.title);
		insertValues.put(KEY_MACRO_ENABLED, (macro.isEnabled ? 1 : 0) );
		insertValues.put(KEY_MACRO_COUNT, macro.count);
		insertValues.put(KEY_MACRO_MAJOR, macro.major);
		insertValues.put(KEY_MACRO_MINOR, macro.minor);
		insertValues.put(KEY_MACRO_UUID, macro.uuid);
		insertValues.put(KEY_MACRO_DISTANCE, macro.distance);
		insertValues.put(KEY_MACRO_WORK, macro.works);
		insertValues.put(KEY_MACRO_REPEAT, macro.repeats);
		insertValues.put(KEY_MACRO_DESTINATION, macro.destination);
		insertValues.put(KEY_MACRO_DURATION, macro.duration);
		insertValues.put(KEY_MACRO_MESSAGE, macro.userMessage);
		//insertValues.put(KEY_MACRO_ARG0, 0);
		//insertValues.put(KEY_MACRO_ARG1, 0);
		//insertValues.put(KEY_MACRO_ARG2, subData);
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.update( TABLE_NAME_MACRO,	// table
					insertValues, 					// values
					KEY_MACRO_ID + "='" + macro.id + "'", // whereClause
					null ); 						// whereArgs
		}
	}
	
	public int updateBeacon(Beacon beacon) {
		if(beacon == null || beacon.getId() < 0)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_BEACON_NAME, beacon.getBeaconName());
		insertValues.put(KEY_BEACON_UUID, beacon.getProximityUuid() );
		//insertValues.put(KEY_BEACON_UUID_HIGH, beacon.getProximityUuidBytes());	// TODO
		//insertValues.put(KEY_BEACON_UUID_LOW, beacon.getProximityUuidBytes());	// TODO
		insertValues.put(KEY_BEACON_MAJOR, beacon.getMajor());
		insertValues.put(KEY_BEACON_MINOR, beacon.getMinor());
		//insertValues.put(KEY_BEACON_PROXIMITY, beacon.getProximity());
		insertValues.put(KEY_BEACON_ACCURACY, beacon.getAccuracy());
		insertValues.put(KEY_BEACON_RSSI, beacon.getRssi());
		insertValues.put(KEY_BEACON_TXPOWER, beacon.getTxPower());
		insertValues.put(KEY_BEACON_BT_ADDRESS, beacon.getBluetoothAddress());
		//insertValues.put(KEY_BEACON_SPARE, 0);
		//insertValues.put(KEY_BEACON_ARG0, 0);
		//insertValues.put(KEY_BEACON_ARG1, 0);
		//insertValues.put(KEY_BEACON_ARG3, subData);
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.update( TABLE_NAME_BEACON,	// table
					insertValues, 					// values
					KEY_BEACON_ID + "='" + beacon.getId() + "'", // whereClause
					null ); 						// whereArgs
		}
	}

	
	//----------------------------------------------------------------------------------
	// Delete
	//----------------------------------------------------------------------------------

	public int deleteMacroWithID(int id) {
		if(mDb == null) return 0;
		
		int count = 0;
		synchronized (mDb) {
			count = mDb.delete(TABLE_NAME_MACRO, 
					KEY_MACRO_ID + "=" + id, // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete macro record : id="+id+", count="+count);
		}
		return count;
	}
	
	public int deleteBeaconWithID(int id) {
		if(mDb == null) return 0;
		
		int count = 0;
		synchronized (mDb) {
			count = mDb.delete(TABLE_NAME_BEACON, 
					KEY_BEACON_ID + "=" + id, // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete beacon record : id="+id+", Result : deleted count="+count);
		}
		return count;
	}
	
	/*
	public void deleteReportWithTime(int type, long timeBiggerThan, long timeSmallerThan) {
		if(mDb == null) return;
		
		synchronized (mDb) {
			int count = mDb.delete(TABLE_NAME_MACRO, 
					KEY_MACRO_TYPE + "=" + type 
					+ " AND " + KEY_MACRO_TIME + ">" + Long.toString(timeBiggerThan) 
					+ " AND " + KEY_MACRO_TIME + "<" + Long.toString(timeSmallerThan), // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete record : type="+type+", "+timeBiggerThan+" < time < "+timeSmallerThan+", deleted count="+count);
		}
	}*/

	
	//----------------------------------------------------------------------------------
	// Count
	//----------------------------------------------------------------------------------
	public int getMacroCount() {
		String query = "select count(*) from " + TABLE_NAME_MACRO;
		Cursor c = mDb.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getBeaconCount() {
		String query = "select count(*) from " + TABLE_NAME_BEACON;
		Cursor c = mDb.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}

	

	//----------------------------------------------------------------------------------
	// SQLiteOpenHelper
	//----------------------------------------------------------------------------------
	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		// Constructor
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		// Will be called one time at first access
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_MACRO_TABLE);
			db.execSQL(DATABASE_CREATE_BEACON_TABLE);
		}

		// Will be called when the version is increased
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO: Keep previous data
			db.execSQL(DATABASE_DROP_MACRO_TABLE);
			db.execSQL(DATABASE_DROP_BEACON_TABLE);
			
			db.execSQL(DATABASE_CREATE_MACRO_TABLE);
			db.execSQL(DATABASE_CREATE_BEACON_TABLE);
		}
		
	}	// End of class DatabaseHelper
	
}
