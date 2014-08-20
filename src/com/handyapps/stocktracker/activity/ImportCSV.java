package com.handyapps.stocktracker.activity;

import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Spinner;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.handyapps.stocktracker.Archive;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.ThemeUtils;

public class ImportCSV extends Activity implements OnClickListener {
	
	private Resources res;
	
	private Spinner spnFileName;
	private Spinner spnPortName;
	//private Spinner spnDecSep;
	//private Spinner spnFieldDel;
	//private Spinner spnFirstLineFormat;
	private Button btnCancel;
	private Button btnImport;
	
	private String[] portArray;
	
	private Archive mArchive;
	
	private ArrayAdapter<String> portAdapter;
	//private ArrayAdapter<String> decimalSeparatorAdapter;
	//private ArrayAdapter<String> fieldDelimiterAdapter;
	//private ArrayAdapter<String> firstLineAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ThemeUtils.onActivityCreateSetTheme(this, false);
		res = getResources();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
		String title = res.getString(R.string.import_csv_title);
		actionBar.setTitle(title);
		
		setContentView(R.layout.import_csv);
		
		baseComponantSetup();
	}
	
	
	private void baseComponantSetup() {
		spnFileName = (Spinner) findViewById(R.id.file);
		spnPortName = (Spinner) findViewById(R.id.portfolio);
		//spnDecSep = (Spinner) findViewById(R.id.decimal_separator);
		//spnFieldDel = (Spinner) findViewById(R.id.field_delimiter);
		//spnFirstLineFormat = (Spinner) findViewById(R.id.first_line_format);
		
		mArchive = new Archive(this);
		//mArchive.open();
		String[] filesArray = mArchive.getCSVFileList();
		//mArchive.close();
		ArrayAdapter<String> fileAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_light, filesArray);

		fileAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		spnFileName.setAdapter(fileAdapter);
		
		List<PortfolioObject> portList = DbAdapter.getSingleInstance().fetchPortfolioList();
		portArray = new String[portList.size()];
		for (int i=0; i<portList.size(); i++) {
			portArray[i] = portList.get(i).getName();
		}
		portAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_light, portArray);
		portAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		spnPortName.setAdapter(portAdapter);
		
		/*decimalSeparatorAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, res.getStringArray(R.array.spinner_decimal_sep_type));
		decimalSeparatorAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
		spnDecSep.setAdapter(decimalSeparatorAdapter);
		
		fieldDelimiterAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, res.getStringArray(R.array.spinner_delimiter_type));
		fieldDelimiterAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
		spnFieldDel.setAdapter(fieldDelimiterAdapter);
		
		firstLineAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, res.getStringArray(R.array.spinner_first_line_format));
		firstLineAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
		spnFirstLineFormat.setAdapter(firstLineAdapter);*/
		
		btnCancel = (Button) findViewById(R.id.btn_cancel_import);
		btnImport = (Button) findViewById(R.id.btn_import);
		btnCancel.setOnClickListener(this);
		btnImport.setOnClickListener(this);
	}
	
	
	private void importCSV() {
		boolean hasColumnNames = true;

		/*if (spnFirstLineFormat.getSelectedItemPosition() == 0) {
			hasColumnNames = true;
		} else {
			hasColumnNames = false;
		}*/
		String fileName = "";
		if (spnFileName.getSelectedItem() != null){
			fileName = spnFileName.getSelectedItem().toString();
		}

		String fieldDelimiter = ",";
		/*int selectedFieldDelimiterPos = spnFieldDel.getSelectedItemPosition();
		if (selectedFieldDelimiterPos == 0) {
			fieldDelimiter = ",";
		} else if (selectedFieldDelimiterPos == 1) {
			fieldDelimiter = ";";
		} else if (selectedFieldDelimiterPos == 2) {
			fieldDelimiter = "\t";
		} else if (selectedFieldDelimiterPos == 3) {
			fieldDelimiter = "\\|";
		}*/
		String decimalSeparator = ".";
		/*int selectedDecimalSeparatorPos = spnDecSep.getSelectedItemPosition();
		if (selectedDecimalSeparatorPos == 0) {
			decimalSeparator = ".";
		} else if (selectedDecimalSeparatorPos == 1) {
			decimalSeparator = ",";
		}*/

		PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(spnPortName.getSelectedItem().toString());

		if (mArchive != null){
			mArchive.importFromCSV(fileName, po.getId(), hasColumnNames, fieldDelimiter, decimalSeparator);
		}
		
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int mID = item.getItemId();

		switch (mID) {

		case android.R.id.home:
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			break;
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.btn_cancel_import:
			finish();
			break;
		case R.id.btn_import:
			importCSV();
			break;
		}
	}

}
