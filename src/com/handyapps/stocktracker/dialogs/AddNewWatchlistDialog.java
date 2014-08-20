package com.handyapps.stocktracker.dialogs;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;

@SuppressLint("ValidFragment")
public class AddNewWatchlistDialog extends DialogFragment implements TextWatcher  {
	
	private Resources resources;
	private AlertDialog aDialog;
	private EditText etEnterName;
	private SymbolCallBackObject scbo = null;
	
	AddNewWatchlistDialogListener mListener;
	
	public AddNewWatchlistDialog() {}
	
	public AddNewWatchlistDialog(SymbolCallBackObject scbo) {
		this.scbo = scbo;
	}
	
	public interface AddNewWatchlistDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    
        try {
            mListener = (AddNewWatchlistDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddNewWatchlistDialogListener");
        }
    }
	
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater()        
				.inflate(R.layout.create_new_watchlist, null);
		
		resources = getActivity().getResources();
		etEnterName = (EditText) v.findViewById(R.id.et_enter_watchlist); 
		String dialogTitle = resources.getString(R.string.add_new_watchlist);
		
		etEnterName.addTextChangedListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String enteredName = etEnterName.getText().toString();

				WatchlistObject wo = new WatchlistObject();
				wo.setName(enteredName);// Watchlist Name
				wo.insert();
				int watchId = DbAdapter.getSingleInstance().fetchWatchlistByName(enteredName).getId();
				
				if (scbo != null) {
					int stockId;

					String symbol = scbo.getSymbol();
					String name = scbo.getName();
					String exch = scbo.getExch();
					String type = scbo.getType();
					String typeDisp = scbo.getTypeDisp();
					String exchDisp = scbo.getExchDisp();

					StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
					if (so == null) {

						so = new StockObject();
						so.setSymbol(symbol);
						so.setName(name);
						so.setExch(exch);
						so.setType(type);
						so.setTypeDisp(typeDisp);
						so.setExchDisp(exchDisp);
						so.insert();
						so = new StockObject();
						so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
						stockId = so.getId();

					} else {
						stockId = so.getId();
					}

					WatchlistStockObject ws = new WatchlistStockObject();
					ws.setWatchId(watchId);
					ws.setStockId(stockId);
					ws.insert();
				}
				
				Toast.makeText(getActivity().getApplicationContext(), resources.getString(R.string.watchlist_added), Toast.LENGTH_SHORT).show();
				mListener.onDialogPositiveClick(AddNewWatchlistDialog.this);
			}
		});

		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogNegativeClick(AddNewWatchlistDialog.this);
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

			WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByName(name);
			if (wo == null) {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			} else {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}

		} else {
			aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}	
	}

}
