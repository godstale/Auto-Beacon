package com.wiredfactory.bluewave.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Macro;
import com.wiredfactory.bluewave.interfaces.IAdapterListener;
import com.wiredfactory.bluewave.interfaces.IDialogListener;
import com.wiredfactory.bluewave.utils.Utils;

public class MacroListAdapter extends ArrayAdapter<Macro> implements IDialogListener  {
	public static final String TAG = "MacroListAdapter";
	
	private Context mContext = null;
	private ArrayList<Macro> mMacroList = null;
	private IAdapterListener mAdapterListener = null;
	
	public MacroListAdapter(Context c, int resId, ArrayList<Macro> macroList) {
		super(c, resId, macroList);
		mContext = c;
		if(macroList == null)
			mMacroList = new ArrayList<Macro>();
		else
			mMacroList = macroList;
	}
	
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public void setAdapterParams(IAdapterListener l) {
		mAdapterListener = l;
	}
	
	public void addObject(Macro macro) {
		mMacroList.add(macro);
	}
	
	public void addObjectOnTop(Macro macro) {
		mMacroList.add(0, macro);
	}
	
	public void addObjectAll(ArrayList<Macro> itemList) {
		if(itemList == null)
			return;
		for(int i=0; i<itemList.size(); i++)
			addObject(itemList.get(i));
	}
	
	public void deleteObject(int id) {
		for(int i = mMacroList.size() - 1; -1 < i; i--) {
			Macro macro = mMacroList.get(i);
			if(macro.id == id) {
				mMacroList.remove(macro);
			}
		}
	}
	
	public void deleteObjectAll() {
		mMacroList.clear();
	}
	
	@Override
	public int getCount() {
		return mMacroList.size();
	}
	@Override
	public Macro getItem(int position) { 
		return mMacroList.get(position); 
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;
		Macro macro = getItem(position);
		
		if(v == null) {
			LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.list_item_macro, null);
			holder = new ViewHolder();
			
			holder.mItemContainer = (LinearLayout) v.findViewById(R.id.item_container);
			holder.mItemContainer.setOnTouchListener(mListItemTouchListener);
			holder.mTextInfo1 = (TextView) v.findViewById(R.id.item_info1);
			holder.mTextInfo2 = (TextView) v.findViewById(R.id.item_info2);
			holder.mTextInfo3 = (TextView) v.findViewById(R.id.item_info3);
			
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		holder.mMacro = macro;
		
		if (macro != null && holder != null) {
			if(macro.isEnabled)
				holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.lightblue1));
			else
				holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.graye));
			holder.mTextInfo1.setText(macro.title);

			// Make condition text
			StringBuilder sb = new StringBuilder();
			sb.append(mContext.getString(R.string.title_macro_conditions)).append("\n");
			if(macro.major >= 0)
				sb.append("    ").append(mContext.getString(R.string.title_macro_major)).append(" : ").append(macro.major).append("\n");
			if(macro.minor >= 0)
				sb.append("    ").append(mContext.getString(R.string.title_macro_minor)).append(" : ").append(macro.minor).append("\n");
			if(macro.uuid != null && macro.uuid.length() > 0)
				sb.append("    ").append(mContext.getString(R.string.title_macro_uuid)).append(" : ").append(macro.uuid).append("\n");
			sb.append("    ").append(mContext.getString(R.string.title_macro_distance)).append(" : ").append(Utils.getDistanceString(macro.distance + 1));
			holder.mTextInfo2.setText(sb.toString());
			
			// Make work type text
			StringBuilder sb2 = new StringBuilder();
			sb2.append(mContext.getString(R.string.title_macro_works)).append("\n");
			sb2.append("    ").append(Utils.getWorkTypeString(macro.works)).append("\n");
			if(macro.destination != null && macro.destination.length() > 0)
				sb2.append("    ").append(mContext.getString(R.string.title_macro_target)).append(" : ").append(macro.destination).append("\n");
			sb2.append("    ").append(mContext.getString(R.string.title_macro_message)).append(" : ").append(macro.userMessage);
			holder.mTextInfo3.setText(sb2.toString());
		}
		
		return v;
	}	// End of getView()
	
	@Override
	public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IDialogListener.CALLBACK_MACRO_TOGGLE:
			if(arg4 != null && arg4 instanceof Macro) {
				Macro macro = (Macro) arg4;
				for(Macro item : mMacroList) {
					if(item.id == macro.id) {
						item.isEnabled = !item.isEnabled; 
					}
				}
				notifyDataSetChanged();
				// call fragment
				if(mAdapterListener != null)
					mAdapterListener.OnAdapterCallback(msgType, arg0, arg1, arg2, arg3, arg4);
			}
			break;
		case IDialogListener.CALLBACK_MACRO_LAYOUT_EDIT:
			if(arg4 != null && arg4 instanceof Macro) {
				// call fragment
				if(mAdapterListener != null)
					mAdapterListener.OnAdapterCallback(msgType, arg0, arg1, arg2, arg3, arg4);
			}
			break;
		case IDialogListener.CALLBACK_MACRO_DELETE:
			if(arg4 != null && arg4 instanceof Macro) {
				Macro macro = (Macro) arg4;
				deleteObject(macro.id);
				notifyDataSetChanged();
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
				Macro macro = ((ViewHolder)v.getTag()).mMacro;
				if(macro != null) {
					MacroListDialog dialog = new MacroListDialog(mContext);
					dialog.setDialogParams(this, null, macro);
					dialog.show();
				}
				break;
		}	// End of switch()
	}
	
	
	
	/*****************************************************
	 *	Listener, Handler, Sub classes
	 ******************************************************/
	
	public class ViewHolder {
		public LinearLayout mItemContainer = null;
		public TextView mTextInfo1 = null;
		public TextView mTextInfo2 = null;
		public TextView mTextInfo3 = null;
		
		public Macro mMacro = null;
	}
}
