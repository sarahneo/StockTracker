package com.handyapps.stocktracker.activity;

import java.util.Arrays;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.widget.datetimepicker.time.RadialPickerLayout;
import org.holoeverywhere.widget.datetimepicker.time.TimePickerDialog;
import org.holoeverywhere.widget.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.FindStocksDialogActivityAlert;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment.CalculatorCallbacks;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.NewsAlertObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateQuoteTaskForAlert;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.MyTimeFormat;
import com.handyapps.stocktracker.utils.OrientationUtils;
import com.handyapps.stocktracker.utils.ThemeUtils;

public class AddNewAlert extends Activity implements OnClickListener, 
								OnTimeSetListener, CalculatorCallbacks {

	private String symbol = "";
	private String errorUpperTargetPrice;
	private String errorLowerTargetPrice;
	private String errorEmptyStockCode;
	private String errorEmptyStartTime;
	private static final String KEY_ADD_ALERT_BUNDLE = "KEY_ADD_ALERT_BUNDLE";
	private static final String KEY_IS_EDIT_PRESET_DONE = "KEY_IS_EDIT_PRESET_DONE";
	private String KEY_EDIT_TEXT = "KEY_EDIT_TEXT";
	private boolean isEditPresetDone = false;
	private boolean isEditAlert = false;
	private boolean isNewsAlert = false;
	private long lStartTime;
	private int iAlertFreq = 1;
	private int spnIndex = -1;
	private int etId;

	private LinearLayout llNewsAlertTitle;
	private LinearLayout llNewsAlertInput;
	private LinearLayout llPriceAlertTargetTitle;
	private LinearLayout llPriceAlertTargetInput;
	private LinearLayout pd;
	private EditText etStockCode;
	private EditText etCurrentPrice;
	private EditText etUpperTarget;
	private EditText etLowerTarget;
	private EditText etStartTime;
	private TextView tvCurrentPrice;
	private Button btnCancel;
	private Button btnSave;
	private Spinner spinnerAlertFreq;
	private ImageButton btnSearch;
	//private ArrayAdapter<CharSequence> adapterAlertType;
	private ArrayAdapter<CharSequence> adapterAlertFreq;

	private Bundle bundle;
	private Resources res;

	private MyBroadcastReceiver receiver;

	private UpperPriceTextWatcher upperTextWatcher;
	private LowerPriceTextWatcher lowerTextWatcher;

	private SharedPreferences sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeUtils.onActivityCreateSetTheme(this, false);
		sp = PreferenceManager.getDefaultSharedPreferences(this);		

		res = getResources();
		ActionBar actionBar = getSupportActionBar();	
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		
		setContentView(R.layout.add_new_alert_layout);
		viewSetup();

		errorUpperTargetPrice = res.getString(R.string.error_upper_target_price);
		errorLowerTargetPrice = res.getString(R.string.error_lower_target_price);
		errorEmptyStockCode = res.getString(R.string.empty_stock_code_error);
		errorEmptyStartTime = res.getString(R.string.empty_start_time_error);

		initializeValue();

		registerBroadcast();
		OrientationUtils.lockOrientation(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshUi();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bundle = savedInstanceState.getBundle(KEY_ADD_ALERT_BUNDLE);
		isEditPresetDone = savedInstanceState.getBoolean(KEY_IS_EDIT_PRESET_DONE);
		etId = savedInstanceState.getInt(KEY_EDIT_TEXT);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_ADD_ALERT_BUNDLE, bundle);
		outState.putBoolean(KEY_IS_EDIT_PRESET_DONE, isEditPresetDone);
		outState.putInt(KEY_EDIT_TEXT, etId);
	}

	private void refreshUi() {
		
		isNewsAlert = bundle.getBoolean(Constants.KEY_IS_NEWS_ALERT);
		toggleAlertView(isNewsAlert);
		
		String title = "";
		if (isNewsAlert) 
			title = res.getString(R.string.add_news_alert_title);
		else
			title = res.getString(R.string.add_price_alert_title);
		getSupportActionBar().setTitle(title);

		if (bundle != null) {
			symbol = bundle.getString(Constants.KEY_SYMBOL);
			etStockCode.setText(symbol);
			isEditAlert = bundle.getBoolean(Constants.KEY_IS_EDIT_ALERT);
		}
		
	}

	private void viewSetup() {

		etStockCode = (EditText) findViewById(R.id.et_search_stock_code);
		etCurrentPrice = (EditText) findViewById(R.id.et_current_price);
		etUpperTarget = (EditText) findViewById(R.id.et_upper_target);
		etLowerTarget = (EditText) findViewById(R.id.et_lower_target);
		etStartTime = (EditText) findViewById(R.id.et_start_time);
		tvCurrentPrice = (TextView) findViewById(R.id.tv_current_price);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnSave = (Button) findViewById(R.id.btn_save);
		btnSearch = (ImageButton) findViewById(R.id.ib_search_stock_code);
		spinnerAlertFreq = (Spinner) findViewById(R.id.spinner_alert_frequency);
		llNewsAlertInput = (LinearLayout) findViewById(R.id.layout_news_alert_input);
		llNewsAlertTitle = (LinearLayout) findViewById(R.id.layout_news_alert_title);
		llPriceAlertTargetInput = (LinearLayout) findViewById(R.id.layout_price_alert_target_input);
		llPriceAlertTargetTitle = (LinearLayout) findViewById(R.id.layout_price_alert_target_title);
		pd = (LinearLayout) findViewById(R.id.lin_progress_bar);

		upperTextWatcher = new UpperPriceTextWatcher();
		lowerTextWatcher = new LowerPriceTextWatcher();
		etUpperTarget.addTextChangedListener(upperTextWatcher);
		etLowerTarget.addTextChangedListener(lowerTextWatcher);
		etUpperTarget.setOnClickListener(this);
		etLowerTarget.setOnClickListener(this);
		etCurrentPrice.setOnClickListener(this);
		etStockCode.setOnClickListener(this);
		etStartTime.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.new_price_alert_menu, menu);

		MenuItem itemDelete = menu.findItem(R.id.price_alert_menu_item_delete);
		MenuItem itemCancel = menu.findItem(R.id.price_alert_menu_item_cancel);

		if (isEditAlert) {
			itemDelete.setVisible(true);
			itemCancel.setVisible(true);
		} else {
			itemDelete.setVisible(false);
			itemCancel.setVisible(true);
		}

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int mID = item.getItemId();

		switch (mID) {
		case R.id.price_alert_menu_item_cancel:
			showUnSaveChangeTransactionDialog();
			break;

		case R.id.price_alert_menu_item_delete:
			preCheckForDelete();
			break;

		case android.R.id.home:
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			break;
		}
		return true;
	}
	
	
	private void initializeValue() {

		bundle = getIntent().getExtras();
		
		if (bundle != null) {
			isNewsAlert = bundle.getBoolean(Constants.KEY_IS_NEWS_ALERT);
			symbol = bundle.getString(Constants.KEY_SYMBOL);
			isEditAlert = bundle.getBoolean(Constants.KEY_IS_EDIT_ALERT);
			lStartTime = bundle.getLong(Constants.KEY_START_TIME);
			if (symbol != null && symbol.length() > 0) {
				etStockCode.setText(symbol);
				if (!isNewsAlert) {
					startQuote();
					pd.setVisibility(View.VISIBLE);
				}
			}
			if (!isNewsAlert) {
				etUpperTarget.requestFocus();
				String upperTarget = bundle.getString(Constants.KEY_UPPER_PRICE66);
				String lowerTarget = bundle.getString(Constants.KEY_LOWER_PRICE66);
				if (upperTarget != null)
					etUpperTarget.setText(upperTarget);
				if (lowerTarget != null)
					etLowerTarget.setText(lowerTarget);
			} else {
				int alertFreq = bundle.getInt(Constants.KEY_ALERT_FREQUENCY);
				if (alertFreq != 0) {
					String[] valuesArr = res.getStringArray(R.array.news_alert_frequency_values);					
					List<String> valuesAL = Arrays.asList(valuesArr);
					spnIndex = valuesAL.indexOf(Integer.toString(alertFreq));								
				}
					
				this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
			if (lStartTime != 0L) {
				// convert long to hour and minute
				int minutes = (int) ((lStartTime / (1000*60)) % 60);
				int hours   = (int) ((lStartTime / (1000*60*60)) % 24);
				
				etStartTime.setText(MyTimeFormat.convertIntToStringTime(hours, minutes));
			} else
				etStartTime.setText("8:00 AM");
		}

		adapterAlertFreq = ArrayAdapter.createFromResource(this, R.array.news_alert_frequency_entries, R.layout.spinner_item_light);
		adapterAlertFreq.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		spinnerAlertFreq.setAdapter(adapterAlertFreq);
		if (spnIndex != -1)
			spinnerAlertFreq.setSelection(spnIndex);
		spinnerAlertFreq.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
				String[] valuesArr = res.getStringArray(R.array.news_alert_frequency_values);
				switch (position) {
				case 0:
					iAlertFreq = Integer.parseInt(valuesArr[0]);
					break;
				case 1:
					iAlertFreq = Integer.parseInt(valuesArr[1]);
					break;
				case 2:
					iAlertFreq = Integer.parseInt(valuesArr[2]);
					break;
				case 3:
					iAlertFreq = Integer.parseInt(valuesArr[3]);
					break;
				case 4:
					iAlertFreq = Integer.parseInt(valuesArr[4]);
					break;
				case 5:
					iAlertFreq = Integer.parseInt(valuesArr[5]);
					break;
				case 6:
					iAlertFreq = Integer.parseInt(valuesArr[6]);
					break;
				
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		OrientationUtils.unlockOrientation(this);
	}

	@Override
	public void onClick(View view) {

		int mId = view.getId();
		switch (mId) {

		case R.id.et_search_stock_code:
			searchStockCode();
			break;
		case R.id.btn_cancel:
			showUnSaveChangeTransactionDialog();
			break;
		case R.id.btn_save:
			if (!isNewsAlert)
				preCheckForSavePriceAlert();
			else
				preCheckForSaveNewsAlert();
			break;
		case R.id.ib_search_stock_code:
			searchStockCode();
			break;
		case R.id.et_start_time:
			myTimePicker();
			break;
		case R.id.et_current_price:
			etId = R.id.et_current_price;
			showCalcDialog();
			break;
		case R.id.et_upper_target:
			etId = R.id.et_upper_target;
			showCalcDialog();
			break;
		case R.id.et_lower_target:
			etId = R.id.et_lower_target;
			showCalcDialog();
			break;
		}
	}
	
	private void showCalcDialog() {
		CalculatorDialogFragment dialog = new CalculatorDialogFragment();
		dialog.show(getSupportFragmentManager());
	}
	
	@Override
	public void OnResult(int type, String amount) {
		
		switch (etId) {
		case R.id.et_current_price:
			etCurrentPrice.setText(amount);
			break;
		case R.id.et_upper_target:
			etUpperTarget.setText(amount);
			break;
		case R.id.et_lower_target:
			etLowerTarget.setText(amount);
		}
		
	}

	@Override
	public void OnResult(String amount) {
			
	}
	
	private void toggleAlertView(boolean isNewsAlert) {
		if (!isNewsAlert) {
			etCurrentPrice.setVisibility(View.VISIBLE);
			tvCurrentPrice.setVisibility(View.VISIBLE);
			etUpperTarget.setVisibility(View.VISIBLE);
			etLowerTarget.setVisibility(View.VISIBLE);
			llPriceAlertTargetInput.setVisibility(View.VISIBLE);
			llPriceAlertTargetTitle.setVisibility(View.VISIBLE);
			llNewsAlertInput.setVisibility(View.GONE);
			llNewsAlertTitle.setVisibility(View.GONE);
		} else {
			etCurrentPrice.setVisibility(View.GONE);
			tvCurrentPrice.setVisibility(View.GONE);
			etUpperTarget.setVisibility(View.GONE);
			etLowerTarget.setVisibility(View.GONE);
			llPriceAlertTargetInput.setVisibility(View.GONE);
			llPriceAlertTargetTitle.setVisibility(View.GONE);
			llNewsAlertInput.setVisibility(View.VISIBLE);
			llNewsAlertTitle.setVisibility(View.VISIBLE);
		} 

	}
	
	private void myTimePicker() {
		int iHour = 8;
		int iMin = 0;
		String sStartTime = etStartTime.getText().toString();
		if (!sStartTime.equals("")) {
			String sHour = sStartTime.substring(0, sStartTime.indexOf(":"));
			String sMin = sStartTime.substring(sStartTime.indexOf(":")+1, sStartTime.indexOf(" "));
			String amPm = sStartTime.substring(sStartTime.indexOf(" ")+1, sStartTime.length());
			
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
		}
		
		TimePickerDialog dialog = new TimePickerDialog();  
		dialog.setStartTime(iHour, iMin);
		dialog.setOnTimeSetListener(this);
		dialog.show(getSupportFragmentManager());
	}
	
	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		etStartTime.setText(MyTimeFormat.convertIntToStringTime(hourOfDay, minute));
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);
	}

	private void searchStockCode() {
		isEditAlert = bundle.getBoolean(Constants.KEY_IS_EDIT_ALERT);
		if (!isEditAlert) {
			Intent i = new Intent(AddNewAlert.this, FindStocksDialogActivityAlert.class);
			
			if (isNewsAlert)
				i.putExtra(Constants.KEY_FROM, Constants.FROM_ADD_NEWS_ALERT);
			else
				i.putExtra(Constants.KEY_FROM, Constants.FROM_ADD_ALERT_DIALOG);
			
			i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(i);
		}
	}

	private void preCheckForSaveNewsAlert() {

		String mSymbol = etStockCode.getText().toString().trim();
		String mStartTime = etStartTime.getText().toString().trim();

		boolean isToSave = false;

		if (mSymbol.equals(""))
			etStockCode.setError(errorEmptyStockCode);
		if (mStartTime.equals(""))
			etStartTime.setError(errorEmptyStartTime);
		
		if (!mSymbol.equals("") && !mStartTime.equals("")) {
			isToSave = true;
			String sHour = mStartTime.substring(0, mStartTime.indexOf(":"));
			String sMin = mStartTime.substring(mStartTime.indexOf(":")+1, mStartTime.indexOf(" "));
			String amPm = mStartTime.substring(mStartTime.indexOf(" ")+1, mStartTime.length());
			int iHour = Integer.parseInt(sHour);
			int iMin = Integer.parseInt(sMin);
			if (amPm.equals("PM"))
				iHour += 12;
			lStartTime = iHour*60*60*1000 + iMin*60*1000;
		}

		if (isToSave) {

			NewsAlertObject mAo = DbAdapter.getSingleInstance().fetchNewsAlertObjectBySymbol(mSymbol);

			if (mAo != null) {
				mAo.setAlertFrequency(iAlertFreq);
				mAo.setStartTime(lStartTime);
				mAo.setSymbol(mSymbol);
				mAo.setIsNotifyOn(NewsAlertObject.DO_NOTIFY);
				boolean isDone = mAo.update();

				if (isDone) {
					String updated = res.getString(R.string.alert_updated);
					Toast.makeText(this, updated, Toast.LENGTH_SHORT).show();
					MyAlarmManager mAlarm = new MyAlarmManager(AddNewAlert.this);
					mAlarm.stopNewsAlarm(mAo);
					mAlarm.startNewNewsAlarm(mAo);
				}
			} else {
				mAo = new NewsAlertObject();
				mAo.setAlertFrequency(iAlertFreq);
				mAo.setStartTime(lStartTime);
				mAo.setSymbol(mSymbol);
				mAo.setIsNotifyOn(AlertObject.DO_NOTIFY);
				boolean isDone = mAo.insert();
				if (isDone) {
					String added = res.getString(R.string.alert_added);
					Toast.makeText(this, added, Toast.LENGTH_SHORT).show();
					MyAlarmManager mAlarm = new MyAlarmManager(AddNewAlert.this);
					mAlarm.startNewNewsAlarm(mAo);
				}
			}
			finish();
		}
	}
	
	private void preCheckForSavePriceAlert() {

		double dCurrenPrice = 0;
		String mSymbol = etStockCode.getText().toString().trim();
		String mCurrenPrice = etCurrentPrice.getText().toString().trim();
		String mUpperTarget = etUpperTarget.getText().toString().trim();
		String mLowerTarget = etLowerTarget.getText().toString().trim();

		if (mCurrenPrice.length() > 0) {
			try {
				dCurrenPrice = Double.parseDouble(mCurrenPrice);
			} catch (NumberFormatException e) {
			}
		}

		boolean isToSave = false;

		int isUpperTargetOn = 0;
		int isLowerTargetOn = 0;
		if (mUpperTarget.length() > 0 && mLowerTarget.length() > 0) {
			isToSave = isUperTargetAndLowerTargetValide(mUpperTarget, mLowerTarget, dCurrenPrice);
			isUpperTargetOn = 1;
			isLowerTargetOn = 1;
		} else if (mUpperTarget.length() > 0 && mLowerTarget.length() == 0) {
			isToSave = isUpperPriceValid(mUpperTarget, dCurrenPrice);
			isUpperTargetOn = 1;
		} else if (mUpperTarget.length() == 0 && mLowerTarget.length() > 0) {
			isToSave = isLowerPriceValid(mLowerTarget, dCurrenPrice);
			isLowerTargetOn = 1;
		} else {
			etUpperTarget.setError(errorUpperTargetPrice);
			etLowerTarget.setError(errorLowerTargetPrice);
		}

		if (isToSave) {

			AlertObject mAo = DbAdapter.getSingleInstance().fetchAlertObjectBySymbol(mSymbol);
			String mCurrentPrice = etCurrentPrice.getText().toString().trim();

			String mmUpperTarget = removeStringPriceWithLastDot(mUpperTarget);
			String mmLowerTarget = removeStringPriceWithLastDot(mLowerTarget);

			if (mAo != null) {
				mAo.setUpperPrice(mmUpperTarget);
				mAo.setLowerPrice(mmLowerTarget);
				mAo.setIsNotifyOn(AlertObject.DO_NOTIFY);
				mAo.setIsUpperTargetOn(isUpperTargetOn);
				mAo.setIsLowerTargetOn(isLowerTargetOn);
				mAo.setLastTradePrice(mCurrentPrice);
				boolean isDone = mAo.update();

				if (isDone) {
					String updated = res.getString(R.string.alert_updated);
					Toast.makeText(this, updated, Toast.LENGTH_SHORT).show();
				}
			} else {
				mAo = new AlertObject();
				String symbol = etStockCode.getText().toString();
				mAo.setUpperPrice(mmUpperTarget);
				mAo.setLowerPrice(mmLowerTarget);
				mAo.setIsUpperTargetOn(isUpperTargetOn);
				mAo.setIsLowerTargetOn(isLowerTargetOn);
				mAo.setLastTradePrice(mCurrentPrice);
				mAo.setSymbol(symbol);
				mAo.setIsNotifyOn(AlertObject.DO_NOTIFY);
				boolean isDone = mAo.inset();
				if (isDone) {
					String added = res.getString(R.string.alert_added);
					Toast.makeText(this, added, Toast.LENGTH_SHORT).show();
				}
			}
			finish();
		}
	}

	private boolean isUperTargetAndLowerTargetValide(String upperTarget, String lowerTarget, double currenPrice) {

		String mUpperTarget = upperTarget;
		String mLowerTarget = lowerTarget;
		double dCurrentPrice = currenPrice;

		boolean isLowerTargetValid = false;
		boolean isUpperTargetValid = false;

		isUpperTargetValid = isUpperPriceValid(mUpperTarget, dCurrentPrice);
		isLowerTargetValid = isLowerPriceValid(mLowerTarget, dCurrentPrice);

		if (isUpperTargetValid == true && isLowerTargetValid == true) {
			return true;
		}
		return false;
	}

	private boolean isUpperPriceValid(String upperTargetPrice, double currentPrice) {

		String mUpperTarget = upperTargetPrice;
		double dUpperTargetPrice = 0;
		double dCurrentPrice = currentPrice;

		mUpperTarget = removeStringPriceWithLastDot(mUpperTarget);

		try {
			dUpperTargetPrice = Double.parseDouble(mUpperTarget);
		} catch (NumberFormatException e) {
		}

		if (dUpperTargetPrice > dCurrentPrice) {
			return true;
		} else {
			etUpperTarget.setError(errorUpperTargetPrice);
			return false;
		}
	}

	private boolean isLowerPriceValid(String lowerTargetPrice, double currentPrice) {

		String mLowerTarget = lowerTargetPrice;
		double dLowerTargetPrice = 0;
		double dCurrentPrice = currentPrice;
		mLowerTarget = removeStringPriceWithLastDot(mLowerTarget);
		try {
			dLowerTargetPrice = Double.parseDouble(mLowerTarget);
		} catch (NumberFormatException e) {
		}

		if (dLowerTargetPrice < dCurrentPrice) {
			return true;
		} else {
			etLowerTarget.setError(errorLowerTargetPrice);
			return false;
		}
	}

	private String removeStringPriceWithLastDot(String price) {

		String mPrice = price;

		if (mPrice.length() > 0) {

			String subPrice = mPrice.substring(mPrice.length() - 1, mPrice.length());
			if (subPrice.contains(".")) {
				mPrice = mPrice.substring(0, mPrice.length() - 1);
			}
		}

		return mPrice;
	}

	private void showUnSaveChangeTransactionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		builder.setIcon(typedValue.resourceId);
		builder.setTitle(getString(R.string.dialog_title_unchanged));
		builder.setMessage(getString(R.string.dialog_msg_unchanged));

		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void preCheckForDelete() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(getString(R.string.delete_alert));
		dialog.setMessage(getString(R.string.delete_alert_msg));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				deleteAlert();
			}
		});
		dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = dialog.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void deleteAlert() {
		String mSymbol = etStockCode.getText().toString().trim();

		
		if (!isNewsAlert) {
			AlertObject mAo = DbAdapter.getSingleInstance().fetchAlertObjectBySymbol(mSymbol);
			if (mAo != null) {
				boolean idDeleted = mAo.delete();
				if (idDeleted) 
					Toast.makeText(this, res.getString(R.string.alert_deleted), Toast.LENGTH_SHORT).show();
			}
		} else {
			NewsAlertObject nAo = DbAdapter.getSingleInstance().fetchNewsAlertObjectBySymbol(mSymbol);
			if (nAo != null) {
				boolean isDeleted = nAo.delete();
				if (isDeleted) { 
					Toast.makeText(this, res.getString(R.string.alert_deleted), Toast.LENGTH_SHORT).show();
					MyAlarmManager mAlarm = new MyAlarmManager(AddNewAlert.this);
					mAlarm.stopNewsAlarm(nAo);
				}
			}
		}
		
		finish();
	}

	public class UpperPriceTextWatcher implements TextWatcher {

		int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;

		@Override
		public void afterTextChanged(Editable s) {

			String sUpperPrice = s.toString();

			if (sUpperPrice.length() == 1 && sUpperPrice.contains(".")) {
				sUpperPrice = "0.";
				etUpperTarget.setText(sUpperPrice);
				etUpperTarget.setSelection(etUpperTarget.length());
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	}

	public class LowerPriceTextWatcher implements TextWatcher {

		int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;

		@Override
		public void afterTextChanged(Editable s) {

			String sLowerPrice = s.toString();

			if (sLowerPrice.length() == 1 && sLowerPrice.contains(".")) {
				sLowerPrice = "0.";
				etLowerTarget.setText(sLowerPrice);
				etLowerTarget.setSelection(etLowerTarget.length());
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {

			if (intent.getAction().equals(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY)) {

				Bundle mBundle = intent.getExtras();
				boolean isStartQuoteService = false;
				boolean isQuoteOk = false;

				// THis is return by find stocks:
				if (mBundle != null) {

					// 1. set symbol to ui:
					symbol = mBundle.getString(Constants.KEY_SYMBOL);
					if (symbol != null && symbol.length() > 0) {
						bundle.putString(Constants.KEY_SYMBOL, symbol);
						etStockCode.setText(symbol);
					}

					// 2. start update
					// 2.1 required for update current price by find stock
					// result.
					// 2.2 if quote current price success, set price to edittext
					// box
					// 2.2 if quote current price fail, show alert dialog
					isStartQuoteService = mBundle.getBoolean(Constants.KEY_IS_START_QUOTE_SERVICE);
					isQuoteOk = mBundle.getBoolean(Constants.KEY_IS_QUOTE_OK);
					if (isStartQuoteService) {
						startQuote();						
						pd.setVisibility(View.VISIBLE);
					} else if (isStartQuoteService == false && isQuoteOk == true) {
						if (pd != null) {
							pd.setVisibility(View.GONE);
						}
						QuoteObject mQuote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(etStockCode.getText().toString());
						if (mQuote != null) {
							String mLastTradePrice = mQuote.getLastTradePrice();
							etCurrentPrice.setText(mLastTradePrice);
						}
					} else if (isStartQuoteService == false && isQuoteOk == false) {
						if (pd != null) {
							pd.setVisibility(View.GONE);
						}
						boolean isServerDown = sp.getBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false);
						if (isServerDown) {
							alertDialog();
						} 							
					}
				}
			}
		}
	}

	private void startQuote() {
		UpdateQuoteTaskForAlert task = new UpdateQuoteTaskForAlert(AddNewAlert.this);
		task.execute(etStockCode.getText().toString().trim());
	}

	private void alertDialog() {

		// reset
		sp.edit().putBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false).commit();
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(res.getString(R.string.warning));
		dialog.setMessage(res.getString(R.string.domain_server_updating));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		AlertDialog aDialog = dialog.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}