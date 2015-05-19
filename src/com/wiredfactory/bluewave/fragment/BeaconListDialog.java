package com.wiredfactory.bluewave.fragment;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.contents.Macro;
import com.wiredfactory.bluewave.interfaces.IDialogListener;
import com.wiredfactory.bluewave.utils.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BeaconListDialog extends Dialog {
	// Global
	public static final String tag = "BeaconListDialog";
	
	public static final int TYPE_BEACON_MENU = 1;
	public static final int TYPE_REMEMBER_BEACON = 2;

	private String mDialogTitle;

	// Context, system
	private Context mContext;
	private IDialogListener mDialogListener;
	private OnClickListener mClickListener;

	// Layout
	private LinearLayout mLayoutBeacon;
	private Button mBtnMakeUuid;
	private Button mBtnMakeMajor;
	private Button mBtnMakeAll;
	private Button mBtnRemember;
	
	private LinearLayout mLayoutRemember;
	private Button mBtnRememberOk;
	private Button mBtnDelete;
	private EditText mEditBeaconName;
	private TextView mTextInfo1;
	private TextView mTextInfo2;
	private TextView mTextInfo3;

	// Params
	private Beacon mBeacon;
	private int mType = TYPE_BEACON_MENU;
	
	

	// Constructor
    public BeaconListDialog(Context context) {
        super(context);
        mContext = context;
    }
    public BeaconListDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

	/*****************************************************
	 *		Overrided methods
	 ******************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
    	//----- Set title
    	if(mDialogTitle != null) {
    		setTitle(mDialogTitle);
    	} else {
    		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	}
        
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_beacon_menu);
        mClickListener = new OnClickListener(this);
        
        // Beacon menu dialog
        mLayoutBeacon = (LinearLayout) findViewById(R.id.layout_beacon_menu);
        mBtnMakeAll = (Button) findViewById(R.id.button_make_macro_with_this);
        mBtnMakeAll.setOnClickListener(mClickListener);
        mBtnRemember = (Button) findViewById(R.id.button_remember_beacon);
        mBtnRemember.setOnClickListener(mClickListener);
        if(mBeacon.getIsRemembered()) {
        	mBtnRemember.setVisibility(View.VISIBLE);
        	mBtnRemember.setText(mContext.getString(R.string.menu_edit_beacon));
        } else {
        	if(mBeacon.getBeaconName() == null || mBeacon.getBeaconName().length() < 1) {
        		// Not remembered beacon. Add 'Remember' button
            	mBtnRemember.setVisibility(View.VISIBLE);
        	} else {
        		// Already remembered beacon. Add 'Remember' button
        		mBtnRemember.setVisibility(View.GONE);
        	}
        }
        mBtnDelete = (Button) findViewById(R.id.button_delete_beacon);
        mBtnDelete.setOnClickListener(mClickListener);
        if(mBeacon.getIsRemembered()) {
        	mBtnDelete.setVisibility(View.VISIBLE);
        } else {
        	mBtnDelete.setVisibility(View.GONE);
        }

        // Beacon info edit dialog
        mLayoutRemember = (LinearLayout) findViewById(R.id.layout_remember);
        mTextInfo1 = (TextView) findViewById(R.id.item_info1);
        mTextInfo2 = (TextView) findViewById(R.id.item_info2);
        mTextInfo3 = (TextView) findViewById(R.id.item_info3);
        mEditBeaconName = (EditText) findViewById(R.id.edit_beacon_name);
        mBtnRememberOk = (Button) findViewById(R.id.button_remember_ok);
        mBtnRememberOk.setOnClickListener(mClickListener);
        
        if(mBeacon.getIsRemembered()) {
        	// It's remembered beacon fetched from DB. Show edit button.
        	mBtnRememberOk.setText(mContext.getString(R.string.menu_edit_beacon));
        }
        
        if(mBeacon != null) {
			mTextInfo1.setText("UUID: "+mBeacon.getProximityUuid());
			mTextInfo2.setText("Major: "+mBeacon.getMajor()+", Minor: "+mBeacon.getMinor()
					+", RSSI: "+mBeacon.getRssi()+", TX PW: "+mBeacon.getTxPower());
			mTextInfo3.setText(mContext.getString(R.string.title_macro_distance)+": "+Utils.getDistanceString(mBeacon.getProximity()+1));
			mEditBeaconName.setText(mBeacon.getBeaconName());
        }
        
/*        mBtnMakeUuid = (Button) findViewById(R.id.button_make_macro_with_uuid);
        mBtnMakeUuid.setOnClickListener(mClickListener);
        mBtnMakeMajor = (Button) findViewById(R.id.button_make_macro_with_major);
        mBtnMakeMajor.setOnClickListener(mClickListener);
*/
        
        if(mType == TYPE_BEACON_MENU) {
        	mLayoutBeacon.setVisibility(View.VISIBLE);
        	mLayoutRemember.setVisibility(View.GONE);
        } else {
        	mLayoutBeacon.setVisibility(View.GONE);
        	mLayoutRemember.setVisibility(View.VISIBLE);
        }
        
    }
    
    @Override
    protected  void onStop() {
    	super.onStop();
    }


	/*****************************************************
	 *		Public methods
	 ******************************************************/
    public void setDialogParams(IDialogListener listener, String title, Beacon beacon, int type) {
    	mDialogListener = listener;
    	mDialogTitle = title;
    	mBeacon = beacon;
    	mType = type;
    }
    
	/*****************************************************
	 *		Private methods
	 ******************************************************/

    
	/*****************************************************
	 *		Sub classes
	 ******************************************************/
	private class OnClickListener implements View.OnClickListener {
		BeaconListDialog mDialogContext;

		public OnClickListener(BeaconListDialog context) {
			mDialogContext = context;
		}

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.button_make_macro_with_this:
				mDialogContext.dismiss();
				if(mDialogListener != null)
					mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_BEACON_MAKE_MACRO, 0, 0, null, null, mBeacon);
				break;
				
			case R.id.button_remember_beacon:
				mDialogContext.dismiss();
				if(mDialogListener != null)
					mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_BEACON_FORM_DIALOG, 0, 0, null, null, mBeacon);
				break;
				
			case R.id.button_remember_ok:
				String beaconName = mEditBeaconName.getEditableText().toString();
				if(beaconName != null && beaconName.length() > 0) {
					mBeacon.setBeaconName(beaconName);
					if(mDialogListener != null)
						mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_INSERT_EDIT_BEACON, 0, 0, beaconName, null, mBeacon);
					
					mDialogContext.dismiss();
				}
				break;
				
			case R.id.button_delete_beacon:
				if(mDialogListener != null)
					mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_DELETE_BEACON, 0, 0, null, null, mBeacon);
				mDialogContext.dismiss();
				break;
				
/*			case R.id.button_make_macro_with_uuid:
				mDialogContext.dismiss();
				if(mDialogListener != null)
					mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_BEACON_MAKE_UUID, 0, 0, null, null, mBeacon);
				break;

			case R.id.button_make_macro_with_major:
				mDialogContext.dismiss();
				if(mDialogListener != null)
					mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_BEACON_MAKE_MAJOR, 0, 0, null, null, mBeacon);
				break;
*/
			}
		}
	}	// End of class OnClickListener
}
