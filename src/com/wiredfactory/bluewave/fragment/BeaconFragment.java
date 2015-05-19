package com.wiredfactory.bluewave.fragment;

import java.util.ArrayList;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.contents.BeaconManager;
import com.wiredfactory.bluewave.interfaces.IAdapterListener;
import com.wiredfactory.bluewave.interfaces.IDialogListener;
import com.wiredfactory.bluewave.interfaces.IFragmentListener;
import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.Logs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BeaconFragment extends Fragment implements IAdapterListener, View.OnClickListener {
	private static final String TAG = "BeaconFragment";
	
	// Constants
	
	// System
	private Context mContext = null;
	private IFragmentListener mFragmentListener;
	private Handler mActivityHandler;
	
	// UI
	private ListView mListBeacon = null;
	private BeaconListAdapter mBeaconListAdapter = null;
	
	// Contents
	private BeaconManager mBeaconManager;
	
	
	
	public BeaconFragment(Context c, IFragmentListener l, Handler h) {
		mContext = c;
		mFragmentListener = l;
		mActivityHandler = h;
	}
	
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logs.d(TAG, "# MessageListFragment - onCreateView()");
		
		View rootView = inflater.inflate(R.layout.fragment_beacon, container, false);

		mListBeacon = (ListView) rootView.findViewById(R.id.list_beacon);
		if(mBeaconListAdapter == null)
			mBeaconListAdapter = new BeaconListAdapter(mContext, R.layout.list_item_beacon, null);
		mBeaconListAdapter.setAdapterParams(this);
		mListBeacon.setAdapter(mBeaconListAdapter);
		
		return rootView;
	}
	
	@Override
	public void OnAdapterCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IAdapterListener.CALLBACK_BEACON_MAKE_UUID:
		case IAdapterListener.CALLBACK_BEACON_MAKE_MAJOR:
		case IAdapterListener.CALLBACK_BEACON_MAKE_MACRO:
			if(arg4 != null && arg4 instanceof Beacon) {
				// call activity
				if(mFragmentListener != null)
					mFragmentListener.OnFragmentCallback(msgType, arg0, arg1, arg2, arg3, arg4);
			}
			break;
			
		case IAdapterListener.CALLBACK_BEACON_FORM_DIALOG:
			break;
			
		case IAdapterListener.CALLBACK_INSERT_EDIT_BEACON:
			if(arg4 != null && arg4 instanceof Beacon && mBeaconManager != null) {
				Beacon beacon = (Beacon)arg4;

				// In this fragment I had problems in refreshing list. 
				// So the MainActivity will update beacons and refresh list.
				Message msg = mActivityHandler.obtainMessage(Constants.MESSAGE_UPDATE_BEACON);
				msg.arg1 = Constants.UPDATE_TYPE_INSERT_OR_EDIT;
				msg.obj = beacon;
				msg.sendToTarget();
			}
			break;
			
		case IAdapterListener.CALLBACK_DELETE_BEACON:
			if(arg4 != null && arg4 instanceof Beacon && mBeaconManager != null) {
				Beacon beacon = (Beacon)arg4;
				
				// In this fragment I had problems in refreshing list. 
				// So the MainActivity will update beacons and refresh list.
				Message msg = mActivityHandler.obtainMessage(Constants.MESSAGE_UPDATE_BEACON);
				msg.arg1 = Constants.UPDATE_TYPE_DELETE;
				msg.obj = beacon;
				msg.sendToTarget();
			}
			break;
			
		case IDialogListener.CALLBACK_CLOSE:
			break;
		}
	}
	
	@Override
	public void onResume() {
		if(mBeaconManager == null)
			mBeaconManager = BeaconManager.getInstance(mContext);
		
		mActivityHandler.obtainMessage(Constants.MESSAGE_REFRESH_BEACON_LIST).sendToTarget();
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override 
	public void onClick(View v) {
		/*
		switch(v.getId()) {
		case R.id.text_scan_classic:
			mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SCAN_CLASSIC, 
					0, 0, 			// int, int
					null, null, 	// String, String
					null);			// Object
			break;
		}
		*/
	}
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void addObject(boolean updateList, Beacon beacon) {
		if(beacon != null) {
			mBeaconListAdapter.addObject(beacon);
			if(updateList)
				mBeaconListAdapter.notifyDataSetChanged();
		}
	}
	
	public void addObjectOnTop(Beacon beacon) {
		if(beacon != null) {
			mBeaconListAdapter.addObjectOnTop(beacon);
			mBeaconListAdapter.notifyDataSetChanged();
		}
	}
	
	public void addObjectAll(ArrayList<Beacon> objList) {
		if(objList != null) {
			mBeaconListAdapter.addObjectAll(objList);
			mBeaconListAdapter.notifyDataSetChanged();
		}
	}
	
	public void deleteObject(boolean updateList, String uuid) {
		mBeaconListAdapter.deleteObject(uuid);
		if(updateList)
			mBeaconListAdapter.notifyDataSetChanged();
	}
	
	public void deleteObjectAll() {
		mBeaconListAdapter.deleteObjectAll();
		mBeaconListAdapter.notifyDataSetChanged();
	}
	
	
	/*****************************************************
	 *	Handler, Listener, Timer, Sub classes
	 ******************************************************/

	
}
