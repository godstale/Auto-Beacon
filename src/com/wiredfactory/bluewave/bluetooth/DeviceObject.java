package com.wiredfactory.bluewave.bluetooth;

import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

public class DeviceObject {

	public String mDeviceAddress = null;	// Target device's MAC address
	public String mDeviceName = null;		// Name of the connected device
	public int mDeviceClass = -1;			// BluetoothClass.Device
	public int mMajorDeviceClass = -1;		// BluetoothClass.Device.Major
	public int mBondState = -1;		// BOND_NONE, BOND_BONDING, BOND_BONDED.
	public int mBondType = -1;		// DEVICE_TYPE_CLASSIC, DEVICE_TYPE_LE, DEVICE_TYPE_DUAL, DEVICE_TYPE_UNKNOWN
	public ArrayList<UUID> mUUID = null;
	
	public DeviceObject() {
		mUUID = new ArrayList<UUID>(); 
	}
	
	public void fetchFromBtDevice(BluetoothDevice device) {
		mDeviceAddress = device.getAddress();
		mDeviceName = device.getName();
		mDeviceClass = device.getBluetoothClass().getDeviceClass();
		mMajorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
		ParcelUuid[] UuidArray = device.getUuids();
		for(int i=0; i<UuidArray.length; i++) {
			UUID uuid = UuidArray[i].getUuid();
			if(uuid != null && mUUID != null)
				mUUID.add(uuid);
		}
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
	}
	
}
