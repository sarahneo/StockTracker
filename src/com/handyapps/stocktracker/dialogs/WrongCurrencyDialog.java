package com.handyapps.stocktracker.dialogs;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;

import com.handyapps.stocktracker.R;

@SuppressLint("ValidFragment")
public class WrongCurrencyDialog extends DialogFragment {
	
	private Resources resources;
	private AlertDialog aDialog;
	private FragmentActivity activity;
	
	public WrongCurrencyDialog() {
	}		
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		activity = getActivity();
		resources = activity.getResources();
		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		String dialogTitle = resources.getString(R.string.wrong_currency_dialog_title);
		String dialogMsg = resources.getString(R.string.wrong_currency_dialog_msg);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(dialogTitle);
		builder.setMessage(dialogMsg);
		builder.setIcon(typedValue.resourceId);
		builder.setPositiveButton(resources.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();			
			}
		});

		aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return aDialog;
    }
	

}
