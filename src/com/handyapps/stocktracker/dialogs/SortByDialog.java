package com.handyapps.stocktracker.dialogs;

import java.util.Arrays;
import java.util.List;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;

@SuppressLint("ValidFragment")
public class SortByDialog extends DialogFragment {
	
	private static final String EXTRA_INT = "extra_int";
	
	private Resources resources;
	private AlertDialog aDialog;
	private FragmentActivity activity;
	
	public SortByDialog() {
	}		
	
	private void sendResult(int sortByIndex, int resultCode) {    
		if (getTargetFragment() == null)        
			return;
    
		Intent i = new Intent();    
		i.putExtra(EXTRA_INT, sortByIndex);
   
		getTargetFragment()        
		.onActivityResult(getTargetRequestCode(), resultCode, i); 
	}
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		activity = getActivity();
		resources = activity.getResources();
		int index = 0;
		
		if (getArguments() != null)
			index = getArguments().getInt(Constants.KEY_CASH_TRADES_ALL, 0);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(resources.getString(R.string.title_sort_by));

		final List<String> list = Arrays.asList(resources.getStringArray(R.array.spinner_sort_port_by));
		final String[] stringArr = resources.getStringArray(R.array.spinner_sort_port_by);

		if (list.size() > 0) {
			// Specify the list array, the items to be selected by default (null for none),
		    // and the listener through which to receive callbacks when items are selected
			builder.setSingleChoiceItems(stringArr, index,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							int sortByIndex = which;
							sendResult(sortByIndex, Activity.RESULT_OK);
							dialog.dismiss();
						}
					});
		               
		}
		 

		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
		aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		
        return aDialog;
    }	

}
