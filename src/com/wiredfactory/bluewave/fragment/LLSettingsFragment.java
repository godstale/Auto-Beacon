package com.wiredfactory.bluewave.fragment;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.interfaces.IFragmentListener;
import com.wiredfactory.bluewave.utils.AppSettings;
import com.wiredfactory.bluewave.utils.Constants;
import com.wiredfactory.bluewave.utils.Security;
import com.wiredfactory.bluewave.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class LLSettingsFragment extends Fragment implements View.OnClickListener {

	// System
	private Context mContext = null;
	private IFragmentListener mFragmentListener = null;
	private Handler mActivityHandler;
	
	// UI
	private CheckBox mCheckUseNoti;
	private CheckBox mCheckBackground;
	private Spinner mSpinnerScanInterval;
	private EditText mEditEmailAddr;
	private EditText mEditEmailPw;
	private TextView mTextFindMore;
	
	
	// Parameters
	private int[] mScanIntervalValues = {60*1000, 5*60*1000, 10*60*1000, 20*60*1000, 30*60*1000, 40*60*1000, 50*60*1000, 60*60*1000}; 
	
	

	public LLSettingsFragment(Context c, IFragmentListener l, Handler h) {
		mContext = c;
		mFragmentListener = l;
		mActivityHandler = h;
	}
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		
		AppSettings.initializeAppSettings(mContext);
		
		mCheckUseNoti = (CheckBox) rootView.findViewById(R.id.check_use_noti);
		mCheckUseNoti.setChecked(AppSettings.getUseNoti());
		mCheckUseNoti.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AppSettings.setSettingsValue(AppSettings.SETTINGS_USE_NOTIFICATION, isChecked, 0, null);
			}
		});
		
		mCheckBackground = (CheckBox) rootView.findViewById(R.id.check_background_service);
		mCheckBackground.setChecked(AppSettings.getBgService());
		mCheckBackground.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AppSettings.setSettingsValue(AppSettings.SETTINGS_BACKGROUND_SERVICE, isChecked, 0, null);
				mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_RUN_IN_BACKGROUND, 0, 0, null, null,null);
			}
		});
		
		mSpinnerScanInterval = (Spinner) rootView.findViewById(R.id.spinner_scan_interval);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, 
				R.array.array_scan_interval, 
				R.layout.spinner_simple_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_simple_item);
		mSpinnerScanInterval.setPrompt(mContext.getString(R.string.title_spin_title_distance));
		mSpinnerScanInterval.setAdapter(adapter);
		mSpinnerScanInterval.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int scanInterval = 5*60*1000;
				if(position > -1 && position < mScanIntervalValues.length) {
					scanInterval = mScanIntervalValues[position];
				}
				
				int prevScanInterval = AppSettings.getScanInterval();
				if(prevScanInterval != scanInterval) {
					AppSettings.setSettingsValue(AppSettings.SETTINGS_SCAN_INTERVAL, false, scanInterval, null);
					mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_SCAN_INTERVAL, scanInterval, 0, null, null,null);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		int scanInterval = AppSettings.getScanInterval();
		for(int i=0; i<mScanIntervalValues.length; i++) {
			if(scanInterval <= mScanIntervalValues[i]) {
				mSpinnerScanInterval.setSelection(i);
				break;
			}
		}
		
		mEditEmailAddr = (EditText) rootView.findViewById(R.id.edit_email_addr);
		String defaultAddr = AppSettings.getEmailAddr();
		if(defaultAddr != null && !defaultAddr.isEmpty())
			mEditEmailAddr.setText(defaultAddr);
		mEditEmailAddr.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String addr = s.toString();
				if(addr != null && !addr.isEmpty()
						&& addr.contains("@")
						&& addr.contains(".")) {
					AppSettings.setSettingsValue(AppSettings.SETTINGS_EMAIL_ADDR, false, 0, addr);
				}
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		
		mEditEmailPw = (EditText) rootView.findViewById(R.id.edit_email_pw);
		String emailPw = AppSettings.getEmailPw();
		if(emailPw != null && !emailPw.isEmpty())
			mEditEmailPw.setText(emailPw);
		mEditEmailPw.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String password = s.toString();
				if(password != null && !password.isEmpty()) {
					String enc_password = Security.encrypt(password, Constants.PREFERENCE_ENC_DEC_KEY);
					if(enc_password != null && !enc_password.isEmpty())
						AppSettings.setSettingsValue(AppSettings.SETTINGS_EMAIL_PW, false, 0, enc_password);
				}
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		
		mTextFindMore = (TextView) rootView.findViewById(R.id.copyright);
		mTextFindMore.setOnClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override 
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.copyright:
			Utils.launchBrowser("http://www.hardcopyworld.com/ngine/android/index.php/archives/227");
			break;
		default:
			break;
		}
	}
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
