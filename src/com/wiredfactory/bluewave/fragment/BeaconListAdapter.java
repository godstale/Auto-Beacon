package com.wiredfactory.bluewave.fragment;

import java.util.ArrayList;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.interfaces.IAdapterListener;
import com.wiredfactory.bluewave.interfaces.IDialogListener;
import com.wiredfactory.bluewave.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class BeaconListAdapter extends ArrayAdapter<Beacon> implements IDialogListener {
	public static final String TAG = "BeaconListAdapter";
	
	private Context mContext = null;
	private ArrayList<Beacon> mBeaconList = null;
	private IAdapterListener mAdapterListener = null;
	
	public BeaconListAdapter(Context c, int resId, ArrayList<Beacon> beaconList) {
		super(c, resId, beaconList);
		mContext = c;
		if(beaconList == null)
			mBeaconList = new ArrayList<Beacon>();
		else
			mBeaconList = beaconList;
	}
	
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	
	@Override
	public int getCount() {
		return mBeaconList.size();
	}
	@Override
	public Beacon getItem(int position) { 
		return mBeaconList.get(position); 
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;
		Beacon beacon = getItem(position);
		
		if(v == null) {
			LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.list_item_beacon, null);
			holder = new ViewHolder();
			
			holder.mItemContainer = (LinearLayout) v.findViewById(R.id.item_container);
			holder.mItemContainer.setOnTouchListener(mListItemTouchListener);
			holder.mTextName = (TextView) v.findViewById(R.id.item_name);
			holder.mTextInfo1 = (TextView) v.findViewById(R.id.item_info1);
			holder.mTextInfo2 = (TextView) v.findViewById(R.id.item_info2);
			holder.mTextInfo3 = (TextView) v.findViewById(R.id.item_info3);
			
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		holder.mBeacon = beacon;
		
		if (beacon != null && holder != null) {
			if(!beacon.getIsRemembered()) {	// Found by scanning
				holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.lightblue1));
			} else { 	// Remembered beacon
				holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.graye));
			}
			if(beacon.getBeaconName() != null && beacon.getBeaconName().length() > 0) {
				holder.mTextName.setText(beacon.getBeaconName());
				holder.mTextName.setVisibility(View.VISIBLE);
			} else {
				holder.mTextName.setVisibility(View.GONE);
			}
				
			holder.mTextInfo1.setText("UUID: "+beacon.getProximityUuid());
			holder.mTextInfo2.setText("Major: "+beacon.getMajor()+", Minor: "+beacon.getMinor()
					+", RSSI: "+beacon.getRssi()+", TX PW: "+beacon.getTxPower());
			if(beacon.getBeaconName() != null && beacon.getBeaconName().length() > 0 
					&& beacon.getId() > -1
					&& beacon.getIsRemembered()) {
				// This beacon is a remembered beacon
				holder.mTextInfo3.setVisibility(View.GONE);
			} else {
				holder.mTextInfo3.setVisibility(View.VISIBLE);
				holder.mTextInfo3.setText(mContext.getString(R.string.title_macro_distance)+": "+Utils.getDistanceString(beacon.getProximity()+1));
			}
		}
		
		return v;
	}	// End of getView()
	
	@Override
	public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IDialogListener.CALLBACK_BEACON_MAKE_UUID:
		case IDialogListener.CALLBACK_BEACON_MAKE_MAJOR:
		case IDialogListener.CALLBACK_BEACON_MAKE_MACRO:
			if(arg4 != null && arg4 instanceof Beacon) {
				// call fragment
				if(mAdapterListener != null)
					mAdapterListener.OnAdapterCallback(msgType, arg0, arg1, arg2, arg3, arg4);
			}
			break;
			
		case IDialogListener.CALLBACK_BEACON_FORM_DIALOG:
			if(arg4 != null && arg4 instanceof Beacon) {
				// Show insert/edit dialog
				BeaconListDialog dialog = new BeaconListDialog(mContext);
				dialog.setDialogParams(this, null, (Beacon)arg4, BeaconListDialog.TYPE_REMEMBER_BEACON);
				dialog.show();
			}
			break;
			
		case IDialogListener.CALLBACK_INSERT_EDIT_BEACON:
		case IDialogListener.CALLBACK_DELETE_BEACON:
			if(arg4 != null && arg4 instanceof Beacon) {
				// call fragment
				if(mAdapterListener != null)
					mAdapterListener.OnAdapterCallback(msgType, arg0, arg1, arg2, arg3, arg4);
			}
			break;
			
		case IDialogListener.CALLBACK_CLOSE:
			break;
		}
	}
	
	
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	
	/**
	 * Sometimes onClick listener misses event.
	 * Uses touch listener instead.
	 */
	private OnTouchListener mListItemTouchListener = new OnTouchListener() {
		private float startx = 0;
		private float starty = 0;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				startx = event.getX();
				starty = event.getY();
			}
			if(event.getAction()==MotionEvent.ACTION_UP){
				// if action-up occurred within 30px from start, process as click event. 
				if( (startx - event.getX())*(startx - event.getX()) + (starty - event.getY())*(starty - event.getY()) < 900 ) {
					processOnClickEvent(v);
				}
			}
			return true;
		}
	};	// End of new OnTouchListener
	
	private void processOnClickEvent(View v) {
		switch(v.getId())
		{
			case R.id.item_container:
				if(v.getTag() == null)
					break;
				Beacon beacon = ((ViewHolder)v.getTag()).mBeacon;
				if(beacon != null) {
					BeaconListDialog dialog = new BeaconListDialog(mContext);
					dialog.setDialogParams(this, null, beacon, BeaconListDialog.TYPE_BEACON_MENU);
					dialog.show();
				}
				break;
		}	// End of switch()
	}
	
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public void setAdapterParams(IAdapterListener l) {
		mAdapterListener = l;
	}
	
	public void addObject(Beacon beacon) {
		mBeaconList.add(beacon);
	}
	
	public void addObjectOnTop(Beacon beacon) {
		mBeaconList.add(0, beacon);
	}
	
	public void addObjectAll(ArrayList<Beacon> itemList) {
		if(itemList == null)
			return;
		for(int i=0; i<itemList.size(); i++)
			addObject(itemList.get(i));
	}
	
	public void deleteObject(String uuid) {
		for(int i = mBeaconList.size() - 1; -1 < i; i--) {
			Beacon beacon = mBeaconList.get(i);
			if(beacon.getProximityUuid().equalsIgnoreCase(uuid)) {
				mBeaconList.remove(beacon);
			}
		}
	}
	
	public void deleteObject(int id, String uuid, int major, int minor) {
		for(int i = mBeaconList.size() - 1; -1 < i; i--) {
			Beacon beacon = mBeaconList.get(i);
			if(beacon.getId() == id
					&& beacon.getProximityUuid().equalsIgnoreCase(uuid)
					&& beacon.getMajor() == major
					&& beacon.getMinor() == minor) {
				mBeaconList.remove(beacon);
			}
		}
	}
	
	public void deleteRememberedObject(int id, String uuid, int major, int minor) {
		for(int i = mBeaconList.size() - 1; -1 < i; i--) {
			Beacon beacon = mBeaconList.get(i);
			if(beacon.getIsRemembered()
					&& beacon.getId() == id
					&& beacon.getProximityUuid().equalsIgnoreCase(uuid)
					&& beacon.getMajor() == major
					&& beacon.getMinor() == minor) {
				mBeaconList.remove(beacon);
			}
		}
	}
	
	public void deleteObjectAll() {
		mBeaconList.clear();
	}
	
	
	
	/*****************************************************
	 *	Listener, Handler, Sub classes
	 ******************************************************/
	
	public class ViewHolder {
		public LinearLayout mItemContainer = null;
		public TextView mTextName = null;
		public TextView mTextInfo1 = null;
		public TextView mTextInfo2 = null;
		public TextView mTextInfo3 = null;
		
		public Beacon mBeacon = null;
	}
}
