package com.wiredfactory.bluewave.fragment;

import com.wiredfactory.bluewave.R;
import com.wiredfactory.bluewave.contents.Macro;
import com.wiredfactory.bluewave.interfaces.IDialogListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MacroListDialog extends Dialog {
	// Global
	public static final String tag = "MacroListDialog";

	private String mDialogTitle;

	// Context, system
	private Context mContext;
	private IDialogListener mDialogListener;
	private OnClickListener mClickListener;

	// Layout
	private Button mBtnToggle;
	private Button mBtnEdit;
	private Button mBtnDelete;

	// Params
	private Macro mMacro;

	// Constructor
    public MacroListDialog(Context context) {
        super(context);
        mContext = context;
    }
    public MacroListDialog(Context context, int theme) {
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

        setContentView(R.layout.dialog_macro_menu);
        mClickListener = new OnClickListener(this);
        
        mBtnToggle = (Button) findViewById(R.id.button_toggle_enable);
        mBtnToggle.setOnClickListener(mClickListener);
        mBtnEdit = (Button) findViewById(R.id.button_edit);
        mBtnEdit.setOnClickListener(mClickListener);
        mBtnDelete = (Button) findViewById(R.id.button_delete);
        mBtnDelete.setOnClickListener(mClickListener);
    }
    
    @Override
    protected  void onStop() {
    	super.onStop();
    }


	/*****************************************************
	 *		Public methods
	 ******************************************************/
    public void setDialogParams(IDialogListener listener, String title, Macro macro) {
    	mDialogListener = listener;
    	mDialogTitle = title;
    	mMacro = macro;
    }
    
	/*****************************************************
	 *		Private methods
	 ******************************************************/

    
	/*****************************************************
	 *		Sub classes
	 ******************************************************/
	private class OnClickListener implements View.OnClickListener 
	{
		MacroListDialog mDialogContext;

		public OnClickListener(MacroListDialog context) {
			mDialogContext = context;
		}

		@Override
		public void onClick(View v) 
		{
			switch(v.getId())
			{
				case R.id.button_toggle_enable:
					mDialogContext.dismiss();
					if(mDialogListener != null)
						mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_MACRO_TOGGLE, 0, 0, null, null, mMacro);
					break;

				case R.id.button_edit:
					mDialogContext.dismiss();
					if(mDialogListener != null)
						mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_MACRO_LAYOUT_EDIT, 0, 0, null, null, mMacro);
					break;

				case R.id.button_delete:
					mDialogContext.dismiss();
					if(mDialogListener != null)
						mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_MACRO_DELETE, 0, 0, null, null, mMacro);
					break;
			}
		}
	}	// End of class OnClickListener
}
