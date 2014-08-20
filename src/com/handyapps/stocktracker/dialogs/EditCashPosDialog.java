package com.handyapps.stocktracker.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.handyapps.stocktracker.utils.MyDateFormat;

@SuppressLint("ValidFragment")
public class EditCashPosDialog extends DialogFragment implements TextWatcher,
		OnDateSetListener, OnClickListener, CalculatorCallbacks {
	
	private static final int CALCULATOR_DIALOG_ID = 15;
	
	private Resources resources;
	private AlertDialog aDialog;
	private EditText etEnterCash;
	private EditText etEnterDate;
	private RadioButton rdWithdraw;
	private RadioButton rdDeposit;
	private Spinner spPort;
	private FragmentActivity activity;
	
	private int portId;
	private DatePair datePair;
	
	private CashPosObject cpo;
	
	public EditCashPosDialog(CashPosObject cpo) {
		this.cpo = cpo;
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
				.inflate(R.layout.create_new_cashpos, null);
		
		activity = getActivity();
		resources = activity.getResources();
		etEnterCash = (EditText) v.findViewById(R.id.et_enter_cash_amount); 
		etEnterCash.setOnClickListener(this);
		etEnterDate = (EditText) v.findViewById(R.id.et_date_of_txn); 
		etEnterDate.setOnClickListener(this);
		rdDeposit = (RadioButton) v.findViewById(R.id.radio_deposit);
		rdWithdraw = (RadioButton) v.findViewById(R.id.radio_withdraw);
		spPort = (Spinner) v.findViewById(R.id.sp_portfolio_list);
		String dialogTitle = resources.getString(R.string.edit_cash_txn);
				
		portId = cpo.getPortfolioId();
		String cashType = cpo.getTxnType();
		int currentPortIndex = -1;
		
		int numDateYYYYMMDD = cpo.getTxnDate();
		String mDateYYYYMMDD = String.valueOf(numDateYYYYMMDD);
		Calendar mCalendar = MyDateFormat.convertYYYYMMDDToCalendar(mDateYYYYMMDD);
		String mShowDate = MyDateFormat.calendarToDateStringFormater(mCalendar);

		datePair = new DatePair();
		datePair.setStrDateYYYYMMDD(mDateYYYYMMDD);
		datePair.setStrDate(mShowDate);
		etEnterDate.setText(mShowDate);

		
		if (cashType.equals(CashPosObject.CASH_DEPOSIT_TYPE)) {
			rdDeposit.setChecked(true);
			rdWithdraw.setChecked(false);
		} else {
			rdWithdraw.setChecked(true);
			rdDeposit.setChecked(false);
		}
		
		etEnterCash.setText(cpo.getAmount());
		
		final List<PortfolioObject> portList = DbAdapter.getSingleInstance().fetchPortfolioList();
		ArrayList<String> portNameList = new ArrayList<String>();
		for (PortfolioObject po : portList) {
			portNameList.add(po.getName());
			if (portId == po.getId())
				currentPortIndex = portList.indexOf(po);
		}
		ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item, portNameList);
		portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPort.setAdapter(portAdapter);
		if (currentPortIndex != -1)
			spPort.setSelection(currentPortIndex);
		else
			spPort.setSelection(0);
		spPort.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int which, long arg3) {
				int prevId = portId;
				portId = portList.get(which).getId();
				if (prevId != portId)
					aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		etEnterCash.addTextChangedListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(v);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(resources.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String enteredAmt = etEnterCash.getText().toString();
				String txnType = "cd";
				String tradeDate = datePair.getStrDateYYYYMMDD();
				int numberTradeDate = Integer.parseInt(tradeDate);
				
				if (rdDeposit.isChecked())
					txnType = "cd";
				else if (rdWithdraw.isChecked())
					txnType = "cw";

				cpo.setAmount(enteredAmt);
				cpo.setTxnType(txnType);
				//cpo.setPortfolioId(portId);
				cpo.setTxnDate(numberTradeDate);
				//cpo.insert();

				cpo.setPortfolioId(portId);
				cpo.update();

				Toast.makeText(activity.getApplicationContext(), resources.getString(R.string.cash_txn_updated), Toast.LENGTH_SHORT)
				.show();
				sendBroadcastNotifyDatasetChange();
				sendResult(Activity.RESULT_OK);
				
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
	
		aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		
        return aDialog;
    }


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {	
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {

			String name = s.toString();

			if (!name.equals("")) {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			} else {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}

		} else {
			aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}	
	}
	

	@Override
	public void OnResult(int type, String amount) {
		etEnterCash.setText(amount);
	}

	@Override
	public void OnResult(String amount) {
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.et_enter_cash_amount:
			CalculatorDialogFragment fragment = CalculatorDialogFragment.newInstance(CALCULATOR_DIALOG_ID, "0");
			fragment.setTargetFragment(this, CALCULATOR_DIALOG_ID);
			fragment.show(getActivity().getSupportFragmentManager());
			break;
		case R.id.et_date_of_txn:			
			String mDate = datePair.getStrDateYYYYMMDD();
			Calendar mCalendar = MyDateFormat.convertYYYYMMDDToCalendar(mDate);
			
			int year = 0;
			int month = 0;
			int day = 0;

			year = mCalendar.get(Calendar.YEAR);
			month = mCalendar.get(Calendar.MONTH);
			day = mCalendar.get(Calendar.DAY_OF_MONTH);
			
			DatePickerDialog dialog = new DatePickerDialog(); 
			dialog.setDate(year, month, day);
			dialog.setOnDateSetListener(this);
			dialog.show(activity.getSupportFragmentManager());
			break;
		}
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		final Calendar c2 = Calendar.getInstance();
		c2.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

		String strDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(c2);
		String strDateFormat = MyDateFormat.calendarToDateStringFormater(c2);

		datePair.setStrDateYYYYMMDD(strDateYYYYMMDD);// using to save
														// database;
		datePair.setStrDate(strDateFormat);// using to display on EditText;

		etEnterDate.setText(datePair.getStrDate());
		aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
	}
	
	
	public class DatePair {

		String strDateYYYYMMDD = "";
		String strDate = "";

		public String getStrDateYYYYMMDD() {
			return strDateYYYYMMDD;
		}

		public void setStrDateYYYYMMDD(String strDateYYYYMMDD) {
			this.strDateYYYYMMDD = strDateYYYYMMDD;
		}

		public String getStrDate() {
			return strDate;
		}

		public void setStrDate(String strDate) {
			this.strDate = strDate;
		}
	}
	
	private void sendBroadcastNotifyDatasetChange() {

		// 1. Update Positions Tab
		Intent iPosTabList = new Intent(Constants.ACTION_TRADES_FRAGMENT);		
		activity.sendBroadcast(iPosTabList);

		// 2. Update Performance/Distribution Tab
		Intent iMainAct = new Intent(Constants.ACTION_MAIN_ACTIVITY);	
		iMainAct.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		activity.sendBroadcast(iMainAct);		
		
		// 3. Update Transactions Activity
		Intent iTxn = new Intent(Constants.ACTION_TRANSACTIONS_ACTIVITY);
		activity.sendBroadcast(iTxn);
	}

}
