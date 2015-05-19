package com.wiredfactory.bluewave.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.utils.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;



public class BleManager {

	// Debugging
	private static final String TAG = "BleManager";
	
	// Constants that indicate the current connection state
	public static final int STATE_ERROR = -1;
	public static final int STATE_NONE = 0;
	public static final int STATE_IDLE = 1;
	public static final int STATE_SCANNING = 2;
	
	public static final long SCAN_PERIOD = 5*1000;	// Stops scanning after a pre-defined scan period.
	public static final long SCAN_INTERVAL = 5*60*1000;
	
	// System, Management
	private static Context mContext = null;
	private static BleManager mBleManager = null;		// Singleton pattern
	
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	private ArrayList<Beacon> mBeaconList = new ArrayList<Beacon>();
	
	// Parameters
	private int mState;
	
	
	/**
	 * Constructor. Prepares a new Bluetooth session.
	 * @param context  The UI Activity Context
	 * @param handler  A Listener to receive messages back to the UI Activity
	 */
	private BleManager(Context context, Handler h) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = h;
		mContext = context;
		
		if(mContext == null)
			return;
		
		// Register for broadcasts when a device is discovered
//		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		filter.addAction(BluetoothDevice.ACTION_UUID);
//		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//		mContext.registerReceiver(mReceiver, filter);
	}
	
	public synchronized static BleManager getInstance(Context c, Handler h) {
		if(mBleManager == null)
			mBleManager = new BleManager(c, h);
		
		return mBleManager;
	}

	public synchronized void finalize() {
		// Make sure we're not doing discovery anymore
		if (mAdapter != null) {
			mState = STATE_IDLE;
			mAdapter.stopLeScan(mLeScanCallback);
		}
		
		if(mContext == null)
			return;
        
		// Don't forget this!!
		// Unregister broadcast listeners
//		mContext.unregisterReceiver(mReceiver);
	}
	
	
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	
	/**
	 * This method extracts UUIDs from advertised data
	 * Because Android native code has bugs in parsing 128bit UUID
	 * use this method instead.
	 */
/*
	private List<UUID> parseUuids(byte[] advertisedData) {
		List<UUID> uuids = new ArrayList<UUID>();

		ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
		while (buffer.remaining() > 2) {
			byte length = buffer.get();
			if (length == 0) break;

			byte type = buffer.get();
			switch (type) {
			case 0x02: // Partial list of 16-bit UUIDs
			case 0x03: // Complete list of 16-bit UUIDs
				while (length >= 2) {
					uuids.add(UUID.fromString(String.format(
							"%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
					length -= 2;
				}
				break;

			case 0x06: // Partial list of 128-bit UUIDs
			case 0x07: // Complete list of 128-bit UUIDs
				while (length >= 16) {
					long lsb = buffer.getLong();
					long msb = buffer.getLong();
					uuids.add(new UUID(msb, lsb));
					length -= 16;
				}
				break;

			default:
				buffer.position(buffer.position() + length - 1);
				break;
			}
		}	// End of while()

		return uuids;
	}	// End of parseUuids()
	
	private List<UUID> parseUUIDs2(final byte[] advertisedData) {
	    List<UUID> uuids = new ArrayList<UUID>();

	    int offset = 0;
	    while (offset < (advertisedData.length - 2)) {
	        int len = advertisedData[offset++];
	        if (len == 0)
	            break;

	        int type = advertisedData[offset++];
	        switch (type) {
	        case 0x02: // Partial list of 16-bit UUIDs
	        case 0x03: // Complete list of 16-bit UUIDs
	            while (len > 1) {
	                int uuid16 = advertisedData[offset++];
	                uuid16 += (advertisedData[offset++] << 8);
	                len -= 2;
	                uuids.add(UUID.fromString(String.format(
	                        "%08x-0000-1000-8000-00805f9b34fb", uuid16)));
	            }
	            break;
	        case 0x06:// Partial list of 128-bit UUIDs
	        case 0x07:// Complete list of 128-bit UUIDs
	            // Loop through the advertised 128-bit UUID's.
	            while (len >= 16) {
	                try {
	                    // Wrap the advertised bits and order them.
	                    ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
	                            offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
	                    long mostSignificantBit = buffer.getLong();
	                    long leastSignificantBit = buffer.getLong();
	                    uuids.add(new UUID(leastSignificantBit,
	                            mostSignificantBit));
	                } catch (IndexOutOfBoundsException e) {
	                    // Defensive programming.
	                    Logs.e(e.toString());
	                    continue;
	                } finally {
	                    // Move the offset to read the next uuid.
	                    offset += 15;
	                    len -= 16;
	                }
	            }
	            break;
	        default:
	            offset += (len - 1);
	            break;
	        }
	    }

	    return uuids;
	}
	
	private static List<UUID> parseUuids3(final byte[] adv_data) {
	    final List<UUID> uuids = new ArrayList<UUID>();
	    int ptr = 0;

	    while (ptr < adv_data.length - 2) {
	        byte length = adv_data[ptr];
	        final byte ad_type = adv_data[ptr + 1];

	        length -= 1;
	        int offset = 2;
	        // deliberate fall through
	        switch (ad_type) {
	        case 0x02:
	        case 0x03:
	            while (length > 1) {
	                int uuid16 = adv_data[ptr + offset++];
	                uuid16 |= (adv_data[ptr + offset++] << 8);
	                length -= 2;
	                uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
	            }
	            break;
	        case 0x06:
	        case 0x07:
	            while (length > 15) {
	                long msb = (adv_data[ptr + offset++] << 56);
	                msb |= (adv_data[ptr + offset++] << 48);
	                msb |= (adv_data[ptr + offset++] << 40);
	                msb |= (adv_data[ptr + offset++] << 32);
	                msb |= (adv_data[ptr + offset++] << 24);
	                msb |= (adv_data[ptr + offset++] << 16);
	                msb |= (adv_data[ptr + offset++] << 8);
	                msb |= (adv_data[ptr + offset++] << 0);
	                long lsb = (adv_data[ptr + offset++] << 56);
	                lsb |= (adv_data[ptr + offset++] << 48);
	                lsb |= (adv_data[ptr + offset++] << 40);
	                lsb |= (adv_data[ptr + offset++] << 32);
	                lsb |= (adv_data[ptr + offset++] << 24);
	                lsb |= (adv_data[ptr + offset++] << 16);
	                lsb |= (adv_data[ptr + offset++] << 8);
	                lsb |= (adv_data[ptr + offset++] << 0);
	                length -= 16;
	                uuids.add(new UUID(msb, lsb));
	            }
	            break;
	        default:
	            break;
	        }
	        // length byte isn't included in length, hence the +1
	        ptr += length + 1;
	    }
	    return uuids;
	}
*/
	
	private void stopScanning() {
		mState = STATE_IDLE;
		mAdapter.stopLeScan(mLeScanCallback);
		mHandler.obtainMessage(Constants.MESSAGE_BT_SCAN_FINISHED).sendToTarget();
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public boolean scanLeDevice(final boolean enable) {
		boolean isScanStarted = false;
		if (enable) {
			if(mState == STATE_SCANNING)
				return false;
			
			if(mAdapter.startLeScan(mLeScanCallback)) {
				mState = STATE_SCANNING;
				mDeviceList.clear();
				mBeaconList.clear();

				// If you want to scan for only specific types of peripherals
				// call below function instead
				//startLeScan(UUID[], BluetoothAdapter.LeScanCallback);
				
				// Stops scanning after a pre-defined scan period.
				mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							stopScanning();
						}
					}, SCAN_PERIOD);
				
				mHandler.obtainMessage(Constants.MESSAGE_BT_SCAN_STARTED).sendToTarget();
				isScanStarted = true;
			}
		} else {
			stopScanning();
		}
		
		return isScanStarted;
	}
	
	public ArrayList<Beacon> getBeaconList() {
		return mBeaconList;
	}
	
	public void updateBeaconName(String uuid, int major, int minor, String beaconName) {
		synchronized(mBeaconList) {
			for(int i = mBeaconList.size() - 1; -1 < i; i--) {
				Beacon beacon = mBeaconList.get(i);
				if(beacon.getProximityUuid().equalsIgnoreCase(uuid)
						&& beacon.getMajor() == major
						&& beacon.getMinor() == minor) {
					beacon.setBeaconName(beaconName);
				}
			}
		}
	}
	
	public void deleteBeaconName(String uuid, int major, int minor) {
		synchronized(mBeaconList) {
			for(int i = mBeaconList.size() - 1; -1 < i; i--) {
				Beacon beacon = mBeaconList.get(i);
				if(beacon.getProximityUuid().equalsIgnoreCase(uuid)
						&& beacon.getMajor() == major
						&& beacon.getMinor() == minor) {
					beacon.setBeaconName(null);
				}
			}
		}
	}
	
	
	
	
	/*****************************************************
	 *	Handler, Listener, Timer, Sub classes
	 ******************************************************/
	
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if(device != null && scanRecord != null) {
		    	mDeviceList.add(device);
		    	//List<UUID> uuids = parseUuids(scanRecord);
		    	List<UUID> uuids = new ArrayList<UUID>();
		    	Beacon ibeacon = Beacon.fromScanData(scanRecord, rssi, device);
		    	if(ibeacon != null) {
		    		uuids.add(UUID.fromString(ibeacon.getProximityUuid()));
		    	}
		    	synchronized(mBeaconList) {
			    	mBeaconList.add(ibeacon);
		    	}
		    	
		    	// Make report string
		    	/*
		    	StringBuilder sb = new StringBuilder();
		    	sb.append("\n  Device: ").append(device.getName())
		    		.append(", RSSI: ").append(rssi)
		    		.append(", UUID array size: ").append(uuids.size());
		    	
	    		if(ibeacon != null) {
		    		sb.append("\n Accuracy: ").append(ibeacon.getAccuracy())
		    			.append(", Major: ").append(ibeacon.getMajor())
		    			.append(", Minor: ").append(ibeacon.getMinor())
		    			.append(", Proximity: ").append(ibeacon.getProximity())
		    			.append(", TxPower: ").append(ibeacon.getTxPower())
		    			.append("\n UUID: ").append(ibeacon.getProximityUuid());
	    		}
	    		*/
		    	
				mHandler.obtainMessage(Constants.MESSAGE_BT_NEW_BEACON, ibeacon).sendToTarget();
			}
		}
	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
