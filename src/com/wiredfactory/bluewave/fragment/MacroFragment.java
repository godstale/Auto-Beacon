package com.wiredfactory.bluewave.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Beacon;
import com.wiredfactory.bluewave.contents.MacroManager;
import com.wiredfactory.bluewave.contents.Macro;
import com.wiredfactory.bluewave.contents.MacroWork;
import com.wiredfactory.bluewave.interfaces.IAdapterListener;
import com.wiredfactory.bluewave.interfaces.IFragmentListener;
import com.wiredfactory.bluewave.utils.Logs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class MacroFragment extends Fragment implements IAdapterListener, View.OnClickListener {

	// Constants
	private static final int LAYOUT_MACRO_LIST = 1;
	private static final int LAYOUT_NEW_MACRO = 2;
	
	public static final int MAKE_MACRO_WITH_UUID = 101;
	public static final int MAKE_MACRO_WITH_MAJOR = 102;
	public static final int MAKE_MACRO_WITH_ALL = 103;
	
	public static final int EDIT_MODE_NEW = 1;
	public static final int EDIT_MODE_EDIT = 2;
	
	// System
	private Context mContext;
	private IFragmentListener mFragmentListener;
	private Handler mActivityHandler;

	// UI
	private LinearLayout mMacroListContainer;
	private ListView mListMacro;
	private MacroListAdapter mMacroListAdapter;
	private Button mButtonNew;
	
	private LinearLayout mMakeMacroContainer;
	private LinearLayout mConditionContainer;
	private EditText mEditMajor;
	private EditText mEditMinor;
	private EditText mEditUuid;
	private Spinner mSpinnerDistance;
	
	private LinearLayout mWorkContainer;
	private Spinner mSpinnerWork;
	private LinearLayout mWorkSubContainer;
	private EditText mEditWorkTarget;
	
	private LinearLayout mMessageContainer;
	private EditText mEditMessage;
	
	private LinearLayout mTitleContainer;
	private EditText mEditTitle;
	private CheckBox mCheckEnabled;
	
	private Button mButtonBackToList;
	private Button mButtonMake;
	
	
	// Contents
	private MacroManager mMacroManager;
	private Macro mCurrentMacro;
	
	// Parameters
	private int mEditMode = EDIT_MODE_NEW;
	
	// Refresh timer
	private Timer mRefreshTimer = null;
	
	
	
	public MacroFragment(Context c, IFragmentListener l, Handler h) {
		mContext = c;
		mFragmentListener = l;
		mActivityHandler = h;
	}
	
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_macro, container, false);

		mMacroListContainer = (LinearLayout) rootView.findViewById(R.id.container_list);
		mListMacro = (ListView) rootView.findViewById(R.id.list_macro);
		if(mMacroListAdapter == null) {
			mMacroListAdapter = new MacroListAdapter(mContext, R.layout.list_item_macro, null);
			mMacroListAdapter.setAdapterParams(this);
		}
		mListMacro.setAdapter(mMacroListAdapter);
		mButtonNew = (Button) rootView.findViewById(R.id.button_new_macro);
		mButtonNew.setOnClickListener(this);
		
		mMakeMacroContainer = (LinearLayout) rootView.findViewById(R.id.container_make_macro);
		mMakeMacroContainer.setOnClickListener(this);
		mConditionContainer = (LinearLayout) rootView.findViewById(R.id.container_condition);
		mEditMajor = (EditText) rootView.findViewById(R.id.edit_macro_major);
		mEditMinor = (EditText) rootView.findViewById(R.id.edit_macro_minor);
		mEditUuid = (EditText) rootView.findViewById(R.id.edit_macro_uuid);
		mSpinnerDistance = (Spinner) rootView.findViewById(R.id.spinner_macro_distance);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, 
				R.array.array_macro_distance, 
				R.layout.spinner_simple_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_simple_item);
		mSpinnerDistance.setPrompt(mContext.getString(R.string.title_spin_title_distance));
		mSpinnerDistance.setAdapter(adapter);
		mSpinnerDistance.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCurrentMacro.distance = position - 1;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		mWorkContainer = (LinearLayout) rootView.findViewById(R.id.container_works);		
		mSpinnerWork = (Spinner) rootView.findViewById(R.id.spinner_macro_work_type);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(mContext, 
				R.array.array_macro_works, 
				R.layout.spinner_simple_item);
		adapter2.setDropDownViewResource(R.layout.spinner_dropdown_simple_item);
		mSpinnerWork.setAdapter(adapter2);
		mSpinnerWork.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCurrentMacro.works = position;
				showWorkSubContainer(true, position);
				showMessageSubContainer(mCurrentMacro.works);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		mSpinnerWork.setPrompt(mContext.getString(R.string.title_spin_title_work));
		
		mWorkSubContainer = (LinearLayout) rootView.findViewById(R.id.container_work_details);
		mWorkSubContainer.setVisibility(View.GONE);
		mEditWorkTarget = (EditText) rootView.findViewById(R.id.edit_macro_target);
		
		mMessageContainer = (LinearLayout) rootView.findViewById(R.id.container_message);
		mMessageContainer.setVisibility(View.GONE);
		mEditMessage = (EditText) rootView.findViewById(R.id.edit_macro_message);
		
		mTitleContainer = (LinearLayout) rootView.findViewById(R.id.container_macro_title);
		mEditTitle = (EditText) rootView.findViewById(R.id.edit_macro_title);
		mCheckEnabled = (CheckBox) rootView.findViewById(R.id.check_macro_enabled);
		mCheckEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCurrentMacro.isEnabled = isChecked;
			}
		});
		mCheckEnabled.setOnClickListener(this);
		
		mButtonBackToList = (Button) rootView.findViewById(R.id.button_back_to_list);
		mButtonBackToList.setOnClickListener(this);
		mButtonMake = (Button) rootView.findViewById(R.id.button_make_macro);
		mButtonMake.setOnClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void OnAdapterCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IAdapterListener.CALLBACK_MACRO_TOGGLE:
			if(arg4 != null 
					&& arg4 instanceof Macro 
					&& mMacroManager != null) {
				Macro macro = (Macro) arg4;
				mMacroManager.updateMacro(macro);
				refreshMacroList();
			}
			break;
		case IAdapterListener.CALLBACK_MACRO_LAYOUT_EDIT:
			if(arg4 != null && arg4 instanceof Macro) {
				Macro macro = (Macro) arg4;
				mCurrentMacro.copyFromMacro(macro);
				mEditMode = EDIT_MODE_EDIT;
				changeLayout(LAYOUT_NEW_MACRO);
			}
			break;
		case IAdapterListener.CALLBACK_MACRO_DELETE:
			if(arg4 != null 
					&& arg4 instanceof Macro 
					&& mMacroManager != null) {
				Macro macro = (Macro) arg4;
				mMacroManager.deleteMacro(macro.id);
			}
			break;
		}
	}
	
	@Override
	public void onResume() {
		if(mCurrentMacro == null)
			mCurrentMacro = new Macro();
		setInputFieldValue(mCurrentMacro);
		
		if(mMacroManager == null)
			mMacroManager = MacroManager.getInstance(mContext);
		
		if(mMacroManager != null && mMacroManager.isInitialized()) {
			refreshMacroList();
		} else {
			// Stop the timer
			if(mRefreshTimer != null) {
				mRefreshTimer.cancel();
				mRefreshTimer = null;
			}
			mRefreshTimer = new Timer();
			mRefreshTimer.schedule(new RefreshTimerTask(), 2*1000);
		}
		
		changeLayout(LAYOUT_MACRO_LIST);
		
		super.onResume();
	}
	
	@Override
	public void onStop() {
		// Stop the timer
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
			mRefreshTimer = null;
		}
		super.onStop();
	}
	
	@Override 
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_new_macro:
			//mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SCAN_CLASSIC, 0, 0, null, null,null);
			hideKeyboard();
			mEditMode = EDIT_MODE_NEW;
			mCurrentMacro = new Macro();
			changeLayout(LAYOUT_NEW_MACRO);
			break;
		case R.id.button_back_to_list:
			hideKeyboard();
			changeLayout(LAYOUT_MACRO_LIST);
			break;
		case R.id.button_make_macro:
			hideKeyboard();
			boolean isValid = getInputFieldValue(mCurrentMacro);
			if(isValid) {
				if(mEditMode == EDIT_MODE_EDIT) {
					if(updateMacro(mCurrentMacro) == false) {
						Toast.makeText(mContext, "Cannot edit macro!!!", Toast.LENGTH_SHORT).show();
						Logs.e("##### Cannot edit macro!!!");
					}
				} else {
					if(insertMacroToDB(mCurrentMacro) == false) {
						Toast.makeText(mContext, "Cannot make macro!!!", Toast.LENGTH_SHORT).show();
						Logs.e("##### Cannot insert macro!!!");
					}
				}
				changeLayout(LAYOUT_MACRO_LIST);
				if(mMacroManager != null)
					clearAndAddObjectAll(mMacroManager.getMacroList());
			}
			break;
		case R.id.container_make_macro:
		case R.id.check_macro_enabled:
			hideKeyboard();
			break;
		default:
			hideKeyboard();
			break;
				
		}
	}
	
	
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	private boolean insertMacroToDB(Macro macro) {
		if(macro == null) return false;
		if(mMacroManager == null) {
			mMacroManager = MacroManager.getInstance(mContext);
			if(mMacroManager == null)
				return false;
		}
		return mMacroManager.addMacro(macro);
	}
	
	private boolean updateMacro(Macro macro) {
		if(macro == null) return false;
		if(mMacroManager == null) {
			mMacroManager = MacroManager.getInstance(mContext);
			if(mMacroManager == null)
				return false;
		}
		mMacroManager.updateMacro(macro);
		return true;
	}
	
	private void changeLayout(int layout) {
		switch(layout) {
		case LAYOUT_MACRO_LIST:
			mMacroListContainer.setVisibility(View.VISIBLE);
			mMakeMacroContainer.setVisibility(View.GONE);
			break;
		case LAYOUT_NEW_MACRO:
			mMacroListContainer.setVisibility(View.GONE);
			setInputFieldValue(mCurrentMacro);
			mMakeMacroContainer.setVisibility(View.VISIBLE);
			if(mEditMode == EDIT_MODE_EDIT) {
				mButtonMake.setText(mContext.getText(R.string.title_macro_edit));
			} else if(mEditMode == EDIT_MODE_NEW) {
				mButtonMake.setText(mContext.getText(R.string.title_macro_make));
			}
			break;
		}
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditUuid.getWindowToken(),0);
		imm.hideSoftInputFromWindow(mEditWorkTarget.getWindowToken(),0);
		imm.hideSoftInputFromWindow(mEditMessage.getWindowToken(),0);
		imm.hideSoftInputFromWindow(mEditTitle.getWindowToken(),0);
	}
	
	private void setInputFieldValue(Macro macro) {
		if(macro.major < 0x01 || macro.major > 0xffff)
			mEditMajor.setText("");
		else
			mEditMajor.setText(Integer.toString(macro.major));
		
		if(macro.major < 0x01 || macro.major > 0xffff)
			mEditMinor.setText("");
		else
			mEditMinor.setText(Integer.toString(macro.minor));
		
		if(macro.uuid != null)
			mEditUuid.setText(macro.uuid);
		else
			mEditUuid.setText("");
		
		if(macro.distance < Macro.MACRO_DISTANCE_NONE || macro.distance > Macro.MACRO_DISTANCE_FAR)
			mSpinnerDistance.setSelection(0);
		else
			mSpinnerDistance.setSelection(macro.distance+1);
		
		if(macro.distance < Macro.MACRO_WORKS_NONE || macro.distance > Macro.MACRO_WORKS_LAST)
			mSpinnerWork.setSelection(0);
		else
			mSpinnerWork.setSelection(macro.works);

		showWorkSubContainer(true, macro.works);
		showMessageSubContainer(macro.works);
		
		if(macro.destination == null || macro.destination.length() < 8)
			mEditWorkTarget.setText(mContext.getText(R.string.uri_prefix));
		else
			mEditWorkTarget.setText(macro.destination);
		
		mEditMessage.setText(macro.userMessage);
		mEditTitle.setText(macro.title);
		mCheckEnabled.setChecked(macro.isEnabled);
	}
	
	private void showWorkSubContainer(boolean isShow, int workType) {
		if(!isShow) {
			mWorkSubContainer.setVisibility(View.GONE);
			return;
		}
		
		switch(workType) {
		case Macro.MACRO_WORKS_SEND_EMAIL:
			mWorkSubContainer.setVisibility(View.VISIBLE);
			mEditWorkTarget.setHint(mContext.getText(R.string.hint_type_email));
			break;
		case Macro.MACRO_WORKS_SEND_SMS:
			mWorkSubContainer.setVisibility(View.VISIBLE);
			mEditWorkTarget.setHint(mContext.getText(R.string.hint_type_number));
			break;
		case Macro.MACRO_WORKS_SEND_MESSAGE:
			mWorkSubContainer.setVisibility(View.VISIBLE);
			mEditWorkTarget.setHint(mContext.getText(R.string.hint_type_msg_id));
			break;
		case Macro.MACRO_WORKS_LAUNCH_BROWSER:
			mWorkSubContainer.setVisibility(View.VISIBLE);
			mEditWorkTarget.setHint(mContext.getText(R.string.hint_type_uri));
			break;
		default:
			mWorkSubContainer.setVisibility(View.GONE);
			break;
		}
	}
	
	private void showMessageSubContainer(int workType) {
		switch(workType) {
		case Macro.MACRO_WORKS_SEND_EMAIL:
		case Macro.MACRO_WORKS_SEND_SMS:
		case Macro.MACRO_WORKS_SEND_MESSAGE:
		case Macro.MACRO_WORKS_NOTIFICATION:
			mMessageContainer.setVisibility(View.VISIBLE);
			break;
		default:
			mMessageContainer.setVisibility(View.GONE);
			break;
		}
	}
	
	private boolean getInputFieldValue(Macro macro) {
		macro.title = mEditTitle.getText().toString();
		
		String majorCode = mEditMajor.getText().toString();
		if(majorCode != null && majorCode.length() > 0)
			majorCode.trim();
		if(majorCode != null && majorCode.length() > 0) {
			macro.major = Integer.parseInt(majorCode);
		} else {
			macro.major = -1;
		}
		
		String minorCode = mEditMinor.getText().toString();
		if(minorCode != null && minorCode.length() > 0)
			minorCode.trim();
		if(minorCode != null && minorCode.length() > 0) {
			macro.minor = Integer.parseInt(minorCode);
		} else {
			macro.minor = -1;
		}
		
		macro.uuid = mEditUuid.getText().toString();
		if(macro.uuid != null && macro.uuid.length() > 0)
			macro.uuid.trim();
		if( (macro.uuid == null || macro.uuid.length() < 1) 
				&& (macro.major < 0 || macro.minor < 0 || macro.major > 0xffff || macro.minor > 0xffff)) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_type_device), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		macro.distance = mSpinnerDistance.getSelectedItemPosition() - 1;
		if(macro.distance < Macro.MACRO_DISTANCE_NONE || macro.distance > Macro.MACRO_DISTANCE_FAR) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_select_distance), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		macro.works = mSpinnerWork.getSelectedItemPosition();
		if(macro.works <= Macro.MACRO_WORKS_NONE || macro.works > Macro.MACRO_WORKS_LAST) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_select_what_todo), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		macro.destination = mEditWorkTarget.getText().toString();
		if( (macro.destination == null || macro.destination.length() < 1) 
				&& (macro.works == Macro.MACRO_WORKS_SEND_EMAIL 
					|| macro.works == Macro.MACRO_WORKS_SEND_SMS
					|| macro.works == Macro.MACRO_WORKS_SEND_MESSAGE) ) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_type_destination), 
					Toast.LENGTH_SHORT).show();
			return false;
		}

		macro.userMessage = mEditMessage.getText().toString();
		if( (macro.userMessage == null || macro.userMessage.length() < 1) 
				&& (macro.works == Macro.MACRO_WORKS_SEND_EMAIL 
				|| macro.works == Macro.MACRO_WORKS_SEND_SMS
				|| macro.works == Macro.MACRO_WORKS_SEND_MESSAGE) ) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_type_message), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		macro.title = mEditTitle.getText().toString();
		if( (macro.title == null || macro.title.length() < 1) 
				&& (macro.userMessage == null || macro.userMessage.length() < 1) ) {
			Toast.makeText(mContext, 
					mContext.getString(R.string.error_type_name), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if(macro.title == null || macro.title.length() < 1) {
			macro.title = macro.userMessage;
		}
		mCurrentMacro.isEnabled = mCheckEnabled.isChecked();
		
		return true;
	}
	
	private void refreshMacroList() {
		if(mMacroManager != null) {
			ArrayList<Macro> macroList = mMacroManager.getMacroList(); 
			clearAndAddObjectAll(macroList);
		}
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	public void addObject(Macro macro) {
		if(macro != null) {
			mMacroListAdapter.addObject(macro);
			mMacroListAdapter.notifyDataSetChanged();
		}
	}
	
	public void addObjectAll(ArrayList<Macro> objList) {
		if(objList != null) {
			mMacroListAdapter.addObjectAll(objList);
			mMacroListAdapter.notifyDataSetChanged();
		}
	}
	
	public void clearAndAddObjectAll(ArrayList<Macro> objList) {
		mMacroListAdapter.deleteObjectAll();
		addObjectAll(objList);
	}
	
	public void deleteObject(int id) {
		mMacroListAdapter.deleteObject(id);
		mMacroListAdapter.notifyDataSetChanged();
	}
	
	public void deleteObjectAll() {
		mMacroListAdapter.deleteObjectAll();
		mMacroListAdapter.notifyDataSetChanged();
	}
	
	public void addNewMacro(int type, Beacon beacon) {
		// WARNING: You must define edit mode before changing layout!!!
		mEditMode = EDIT_MODE_NEW;
		
		changeLayout(LAYOUT_NEW_MACRO);
		mCurrentMacro = new Macro();
		
		if(beacon != null) {
			switch(type) {
			case MAKE_MACRO_WITH_UUID:
				mCurrentMacro.uuid = beacon.getProximityUuid();
				break;
			case MAKE_MACRO_WITH_MAJOR:
				mCurrentMacro.major = beacon.getMajor();
				mCurrentMacro.minor = beacon.getMinor();
				break;
			case MAKE_MACRO_WITH_ALL:
				mCurrentMacro.uuid = beacon.getProximityUuid();
				mCurrentMacro.major = beacon.getMajor();
				mCurrentMacro.minor = beacon.getMinor();
				break;
			}
		}
		setInputFieldValue(mCurrentMacro);
	}
	
	
	
	/*****************************************************
	 *	Handler, Listener, Sub classes
	 ******************************************************/
    /**
     * Auto-refresh Timer
     */
	private class RefreshTimerTask extends TimerTask {
		public RefreshTimerTask() {}
		
		public void run() {
			if(mActivityHandler == null) return;
			
			mActivityHandler.post(new Runnable() {
				public void run() {
					if(mMacroManager == null) {
						mMacroManager = MacroManager.getInstance();
					}
					if(mMacroManager == null) {
						mRefreshTimer = new Timer();
						mRefreshTimer.schedule(new RefreshTimerTask(), 2*1000);
					} else {
						mRefreshTimer = null;
						refreshMacroList();
					}
				}
			});
		}
	}
	
}
