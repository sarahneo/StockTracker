package com.handyapps.stocktracker.dialogs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.datetimepicker.time.RadialPickerLayout;
import org.holoeverywhere.widget.datetimepicker.time.TimePickerDialog;
import org.holoeverywhere.widget.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment.CalculatorCallbacks;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.utils.MyTimeFormat;

@SuppressLint("ValidFragment")
public class SetAlertsWindowDialog extends DialogFragment implements 
									OnClickListener, CalculatorCallbacks {
	
	private Resources resources;
	private AlertDialog aDialog;
	private EditText etEnterStartTime;
	private EditText etEnterEndTime;	
	private FragmentActivity activity;
	private String startTime = "";
	private String endTime = "";
	private String alertsWindow;
	private String enterStartTime;
	private String enterEndTime;
	
	public SetAlertsWindowDialog() {
	}		
	
	private void sendResult(int resultCode) {    
		if (getTargetFragment() == null)        
			return;
    
		Intent i = new Intent();    
		//i.putExtra(EXTRA_DATE, mDate);
   
		getTargetFragment()        
		.onActivityResult(getTargetRequestCode(), resultCode, i); 
	}
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater()        
				.inflate(R.layout.set_alerts_window_dialog, null);
		
		activity = getActivity();
		resources = activity.getResources();
		PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		String dialogTitle = resources.getString(R.string.alerts_window);
		String defaultValue = resources.getString(R.string.default_alerts_window);
		final String errMsg = resources.getString(R.string.alerts_window_error_msg);
		
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		alertsWindow = sp.getString(Constants.SP_KEY_ALERTS_WINDOW, defaultValue);
		startTime = alertsWindow.substring(0, alertsWindow.indexOf("-")-1);
		endTime = alertsWindow.substring(alertsWindow.indexOf("-")+2, alertsWindow.length());
		
		enterStartTime = resources.getString(R.string.set_start_time);
		enterEndTime = resources.getString(R.string.set_end_time);
		etEnterStartTime = (EditText) v.findViewById(R.id.et_enter_start_time); 
		etEnterStartTime.setOnClickListener(this);	
		etEnterStartTime.setText(startTime);		
		etEnterEndTime = (EditText) v.findViewById(R.id.et_enter_end_time); 
		etEnterEndTime.setOnClickListener(this);	
		etEnterEndTime.setText(endTime);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(v);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(resources.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				boolean isOkToSave = preCheckForSave();

				if (isOkToSave) {
					sp.edit().putString(Constants.SP_KEY_ALERTS_WINDOW, alertsWindow).commit();
					sendResult(Activity.RESULT_OK);
				} else
					showAlertDialog(errMsg);
				
			}
		});

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
		
        return aDialog;
    }
	
	
	protected void addToPortfolio(final CashPosObject cashPosObj) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(resources.getString(R.string.title_add_to));

		final List<String> pList = new ArrayList<String>();
		List<PortfolioObject> list = DbAdapter.getSingleInstance().fetchPortfolioList();
		final String strCreateANewPortfolio = resources.getString(R.string.add_new_portfolio);
		if (list.size() > 0) {
			for (PortfolioObject po : list) {
				pList.add(po.getName());
			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, pList);
			builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					int portfId;
					String portfName = pList.get(which);

					PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(portfName);
					portfId = po.getId();

					cashPosObj.setPortfolioId(portfId);
					cashPosObj.insert();

					Toast.makeText(activity.getApplicationContext(), resources.getString(R.string.cash_txn_added), Toast.LENGTH_SHORT)
					.show();
					sendResult(Activity.RESULT_OK);

					}
				}
			);
		} else {

			LayoutInflater inflater = activity.getLayoutInflater();
			View view = inflater.inflate(R.layout.empty_item_layout, null);
			builder.setView(view);
		}
		builder.setPositiveButton(strCreateANewPortfolio, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// user create a new portfolio name
				AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog(null, cashPosObj);     
        		portDialog.show(activity.getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
			}
		});
		builder.create();
		builder.show();
	}

	@Override
	public void OnResult(int type, String amount) {
		etEnterEndTime.setText(amount);
	}

	@Override
	public void OnResult(String amount) {
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.et_enter_start_time:
			myStartTimePicker();
			break;
		case R.id.et_enter_end_time:
			myEndTimePicker();
			break;
		}
	}
	
	private void myStartTimePicker() {
		String sHour = startTime.substring(0, startTime.indexOf(":"));
		String sMin = startTime.substring(startTime.indexOf(":")+1, startTime.indexOf(" "));
		int iHour = 0;
		int iMin = 0;
		
		try {
			iHour = Integer.parseInt(sHour);
			iMin = Integer.parseInt(sMin);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			iHour = 8;
			iMin = 0;
		}
		
		TimePickerDialog dialog = new TimePickerDialog();
		dialog.setTitle(enterStartTime);
		dialog.setStartTime(iHour, iMin);
		dialog.setOnTimeSetListener(new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				startTime = MyTimeFormat.convertIntToStringTime(hourOfDay, minute);
				alertsWindow = startTime + " - " + endTime;				
				etEnterStartTime.setText(startTime);
			}
		});
		dialog.show(getActivity().getSupportFragmentManager());
	}
	
	private void myEndTimePicker() {
		String sHour = endTime.substring(0, endTime.indexOf(":"));
		String sMin = endTime.substring(endTime.indexOf(":")+1, endTime.indexOf(" "));
		String amPm = endTime.substring(endTime.indexOf(" ")+1, endTime.length());
		int iHour = 0;
		int iMin = 0;
		
		try {
			iHour = Integer.parseInt(sHour);
			iMin = Integer.parseInt(sMin);
			if (amPm.toUpperCase().equals("PM") && iHour < 12)
				iHour += 12;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			iHour = 8;
			iMin = 0;
		}
		
		TimePickerDialog dialog = new TimePickerDialog();		
		dialog.setTitle(enterEndTime);
		dialog.setStartTime(iHour, iMin);
		dialog.setOnTimeSetListener(new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				endTime = MyTimeFormat.convertIntToStringTime(hourOfDay, minute);
				alertsWindow = startTime + " - " + endTime;				
				etEnterEndTime.setText(MyTimeFormat.convertIntToStringTime(hourOfDay, minute));
			}
		});
		dialog.show(getActivity().getSupportFragmentManager());
	}
	
	protected void showAlertDialog(String msg) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setIcon(R.drawable.ic_action_warning);
		dialog.setTitle(getString(R.string.warning));
		dialog.setMessage(msg);
		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.create().show();
	}
	
	private boolean preCheckForSave() {
		startTime = etEnterStartTime.getText().toString();
		endTime = etEnterEndTime.getText().toString();
		Date startDate = new Date();
		Date endDate = new Date();
		
		DateFormat formatter = new SimpleDateFormat("h:mm a");
		try {
			startDate = formatter.parse(startTime);
			endDate = formatter.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		
		if (endDate.after(startDate))
			return true;
		else 
			return false;
	}

}
