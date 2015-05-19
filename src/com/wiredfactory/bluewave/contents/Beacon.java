package com.wiredfactory.bluewave.contents;

import android.bluetooth.BluetoothDevice;

public class Beacon {

	public static final int PROXIMITY_IMMEDIATE = 1;	// Less than half a meter away
	public static final int PROXIMITY_NEAR = 2;			// More than half a meter away, but less than four meters away
	public static final int PROXIMITY_FAR = 3;			// More than four meters away
	public static final int PROXIMITY_UNKNOWN = 0;		// No distance estimate was possible due to a bad RSSI value or measured TX power

	
	protected int id = -1;
	protected String beaconName;
	protected boolean isRemembered = false;
	
	/**
	* A 16 byte UUID that typically represents the company owning a number of iBeacons
	* Example: E2C56DB5-DFFB-48D2-B060-D0F5A71096E0 
	*/
	protected String proximityUuid;
	
	protected byte[] proximityUuidBytes;		// UUID in bytes
	
	protected int major;		// A 16 bit integer typically used to represent a group of iBeacons
	protected int minor;		// A 16 bit integer that identifies a specific iBeacon within a group
	
	/**
	* An integer with four possible values representing a general idea of how far the iBeacon is away
	* @see #PROXIMITY_IMMEDIATE
	* @see #PROXIMITY_NEAR
	* @see #PROXIMITY_FAR
	* @see #PROXIMITY_UNKNOWN
	*/
	protected Integer proximity;
	
	/**
	* A double that is an estimate of how far the iBeacon is away in meters.  This name is confusing, but is copied from
	* the iOS7 SDK terminology.   Note that this number fluctuates quite a bit with RSSI, so despite the name, it is not
	* super accurate.   It is recommended to instead use the proximity field, or your own bucketization of this value. 
	*/
	protected Double accuracy;
	
	/**
	* The measured signal strength of the Bluetooth packet that led do this iBeacon detection.
	*/
	protected int rssi;
	
	/**
	* The calibrated measured Tx power of the iBeacon in RSSI
	* This value is baked into an iBeacon when it is manufactured, and
	* it is transmitted with each packet to aid in the distance estimate
	*/
	protected int txPower;

	/**
	* The bluetooth mac address
	*/
	protected String bluetoothAddress;

	/**
	* If multiple RSSI samples were available, this is the running average
	*/
	protected Double runningAverageRssi = null;

	
	
	public Beacon(Beacon otherIBeacon) {
		this.id = otherIBeacon.id;
		this.beaconName = otherIBeacon.beaconName;
		this.isRemembered = otherIBeacon.isRemembered;
		this.major = otherIBeacon.major;
		this.minor = otherIBeacon.minor;
		this.accuracy = otherIBeacon.accuracy;
		this.proximity = otherIBeacon.proximity;
		this.proximityUuidBytes = otherIBeacon.proximityUuidBytes;
		this.runningAverageRssi = otherIBeacon.runningAverageRssi;
		this.rssi = otherIBeacon.rssi;
		this.proximityUuid = otherIBeacon.proximityUuid;
		this.txPower = otherIBeacon.txPower;
		this.bluetoothAddress = otherIBeacon.bluetoothAddress;
	}

	protected Beacon() {
	}

	protected Beacon(String proximityUuid, int major, int minor, int txPower, int rssi) {
		this.proximityUuid = proximityUuid.toLowerCase();
		// TODO: convert String to byte[]
		this.proximityUuidBytes = null;
		this.major = major;
		this.minor = minor;
		this.rssi = rssi;
		this.txPower = txPower;
	}

	public Beacon(String proximityUuid, int major, int minor) {
		this.proximityUuid = proximityUuid.toLowerCase();
		// TODO: convert String to byte[]
		this.proximityUuidBytes = null;
		this.major = major;
		this.minor = minor;
		this.rssi = rssi;
		this.txPower = -59;
		this.rssi = 0;
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public int getId() {
		return id;
	}
	
	public void setId(int _id) {
		this.id = _id;
	}
	
	public void setIsRemembered(boolean found) {
		this.isRemembered = found;
	} 
	
	public boolean getIsRemembered() {
		return this.isRemembered;
	}
	
	/**
	* @see #accuracy
	* @return accuracy
	*/
	public double getAccuracy() {
		if (accuracy == null) {
			double bestRssiAvailable = rssi;
			if (runningAverageRssi != null) {
				bestRssiAvailable = runningAverageRssi;
			} else {
				//Logs.d(TAG, "Not using running average RSSI because it is null");
			}
			accuracy = calculateAccuracy(txPower, bestRssiAvailable );
		}
		return accuracy;
	}
	
	/**
	* @see #major
	* @return major
	*/
	public int getMajor() {
		return major;
	}
	/**
	* @see #minor
	* @return minor
	*/
	public int getMinor() {
		return minor;
	}
	/**
	* @see #proximity
	* @return proximity
	*/
	public int getProximity() {
		if (proximity == null) {
			proximity = calculateProximity(getAccuracy());		
		}
		return proximity;		
	}
	/**
	* @see #rssi
	* @return rssi
	*/
	public int getRssi() {
		return rssi;
	}
	/**
	* @see #txPower
	* @return txPowwer
	*/
	public int getTxPower() {
		return txPower;
	}

	/**
	* @see #proximityUuid
	* @return proximityUuid
	*/
	public String getProximityUuid() {
		return proximityUuid;
	}
	
	/**
	* @see #proximityUuidBytes
	* @return proximityUuidBytes
	*/
	public byte[] getProximityUuidBytes() {
		return proximityUuidBytes;
	}

	/**
	* @see #bluetoothAddress
	* @return bluetoothAddress
	*/
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	
	public String getBeaconName() {
		return beaconName;
	}
	
	public void setBeaconName(String name) {
		beaconName = name;
	}

	public int hashCode() {
		return minor;
	}

	/**
	* Two detected iBeacons are considered equal if they share the same three identifiers, regardless of their distance or RSSI.
	*/
	public boolean equals(Object that) {
		if (!(that instanceof Beacon)) {
			return false;
		}
		Beacon thatIBeacon = (Beacon) that;		
		return (thatIBeacon.getMajor() == this.getMajor() && thatIBeacon.getMinor() == this.getMinor() && thatIBeacon.getProximityUuid().equals(this.getProximityUuid()));
	}

	/**
	* Construct an iBeacon from a Bluetooth LE packet collected by Android's Bluetooth APIs
	*
	* @param scanData The actual packet bytes
	* @param rssi The measured signal strength of the packet
	* @return An instance of an <code>Beacon</code>
	*/
	public static Beacon fromScanData(byte[] scanData, int rssi) {
		return fromScanData(scanData, rssi, null);
	}

	/**
	* Construct an iBeacon from a Bluetooth LE packet collected by Android's Bluetooth APIs,
	* including the raw bluetooth device info
	* 
	* @param scanData The actual packet bytes
	* @param rssi The measured signal strength of the packet
	* @param device The bluetooth device that was detected
	* @return An instance of an <code>Beacon</code>
	*/
	public static Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
		int startByte = 2;
		boolean patternFound = false;
		while (startByte <= 5) {
			if (((int)scanData[startByte+2] & 0xff) == 0x02 &&
					((int)scanData[startByte+3] & 0xff) == 0x15) {			
				// yes!  This is an iBeacon	
				patternFound = true;
				break;
			}
			else if (((int)scanData[startByte] & 0xff) == 0x2d &&
					((int)scanData[startByte+1] & 0xff) == 0x24 &&
					((int)scanData[startByte+2] & 0xff) == 0xbf &&
					((int)scanData[startByte+3] & 0xff) == 0x16) {
				// Logs.d(TAG, "This is a proprietary Estimote beacon advertisement that does not meet the iBeacon standard.  Identifiers cannot be read.");
				//Beacon iBeacon = new Beacon();
				//iBeacon.major = 0;
				//iBeacon.minor = 0;
				//iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				//iBeacon.txPower = -55;
				//return iBeacon;
				return null;
			}
			else if (((int)scanData[startByte] & 0xff) == 0xad &&
					((int)scanData[startByte+1] & 0xff) == 0x77 &&
					((int)scanData[startByte+2] & 0xff) == 0x00 &&
					((int)scanData[startByte+3] & 0xff) == 0xc6) {
				//Logs.d(TAG, "This is a proprietary Gimbal beacon advertisement that does not meet the iBeacon standard.  Identifiers cannot be read.");
				//Beacon iBeacon = new Beacon();
				//iBeacon.major = 0;
				//iBeacon.minor = 0;
				//iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				//iBeacon.txPower = -55;
				//return iBeacon;
				return null;
			}
			startByte++;
		}

		if (patternFound == false) {
			// This is not an iBeacon
			// Logs.d(TAG, "This is not an iBeacon advertisment (no 0215 seen in bytes 4-7).  The bytes I see are: "+bytesToHex(scanData));
			return null;
		}

		Beacon beacon = new Beacon();

		beacon.major = (scanData[startByte+20] & 0xff) * 0x100 + (scanData[startByte+21] & 0xff);
		beacon.minor = (scanData[startByte+22] & 0xff) * 0x100 + (scanData[startByte+23] & 0xff);
		beacon.txPower = (int)scanData[startByte+24]; // this one is signed
		beacon.rssi = rssi;
		beacon.accuracy = Beacon.calculateAccuracy(beacon.txPower, beacon.rssi);
		beacon.getProximity();

		// AirLocate:
		// 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
		// e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
		// 00 00 # major 
		// 00 00 # minor 
		// c5 # The 2's complement of the calibrated Tx Power

		// Estimote:		
		// 02 01 1a 11 07 2d 24 bf 16 
		// 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16); 
		beacon.proximityUuidBytes = proximityUuidBytes;
		
		String hexString = bytesToHex(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0,8));
		sb.append("-");
		sb.append(hexString.substring(8,12));
		sb.append("-");
		sb.append(hexString.substring(12,16));
		sb.append("-");
		sb.append(hexString.substring(16,20));
		sb.append("-");
		sb.append(hexString.substring(20,32));
		beacon.proximityUuid = sb.toString();

		if (device != null) {
			beacon.bluetoothAddress = device.getAddress();
		}

		return beacon;
	}
	
	public static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		//Logs.d(TAG, "calculating accuracy based on rssi of "+rssi);


		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;	
			//Logs.d(TAG, " avg rssi: "+rssi+" accuracy: "+accuracy);
			return accuracy;
		}
	}

	public static int calculateProximity(double accuracy) {
		if (accuracy < 0) {
			return PROXIMITY_UNKNOWN;	 
			// is this correct?  does proximity only show unknown when accuracy is negative?  
			// I have seen cases where it returns unknown when
			// accuracy is -1;
		}
		if (accuracy < 0.5 ) {
			return Beacon.PROXIMITY_IMMEDIATE;
		}
		// forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
		if (accuracy <= 4.0) { 
			return Beacon.PROXIMITY_NEAR;
		}
		// if it is > 4.0 meters, call it far
		return Beacon.PROXIMITY_FAR;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder hex = new StringBuilder();
		for (byte b : bytes)
			hex.append(String.format("%02X", b));

		return hex.toString();
	} 
	
	public void copyFromBeacon(Beacon beacon) {
		this.id = beacon.id;
		this.isRemembered = beacon.isRemembered;
		this.beaconName = new String(beacon.beaconName);
		this.proximityUuid = new String(beacon.getProximityUuid());
		this.proximityUuidBytes = null;
		this.major = beacon.major;
		this.minor = beacon.minor;
		this.proximity = beacon.proximity;
		this.accuracy = beacon.accuracy;
		this.rssi = beacon.rssi;
		this.txPower = beacon.txPower;
		this.bluetoothAddress = new String(beacon.bluetoothAddress);
	}


	/*****************************************************
	 *	Private methods
	 ******************************************************/

	
	
	
}
