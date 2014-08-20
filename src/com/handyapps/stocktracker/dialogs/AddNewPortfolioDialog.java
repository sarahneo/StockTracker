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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment.CalculatorCallbacks;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;

@SuppressLint("ValidFragment")
public class AddNewPortfolioDialog extends DialogFragment implements TextWatcher,
				OnClickListener, CalculatorCallbacks {
	
	private static final int CALCULATOR_DIALOG_ID = 15;
	
	private Resources resources;
	private AlertDialog aDialog;
	private EditText etEnterName;
	private EditText etEnterCash;
	private SymbolCallBackObject scbo = null;
	private CashPosObject cpo = null;
	
	AddNewPortfolioDialogListener mListener;
	
	public AddNewPortfolioDialog() {}
	
	public AddNewPortfolioDialog(SymbolCallBackObject scbo, CashPosObject cpo) {
		this.scbo = scbo;
		this.cpo = cpo;
	}
	
	public interface AddNewPortfolioDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    
        try {
            mListener = (AddNewPortfolioDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddNewPortfolioDialogListener");
        }
    }
	
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater()        
				.inflate(R.layout.create_new_portfolio, null);
		
		resources = getActivity().getResources();
		etEnterName = (EditText) v.findViewById(R.id.et_enter_portfolio_name); 
		etEnterCash = (EditText) v.findViewById(R.id.et_enter_initial_cash);
		etEnterCash.setOnClickListener(this);
		String dialogTitle = resources.getString(R.string.add_new_portfolio);
		
		etEnterName.addTextChangedListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String enteredName = etEnterName.getText().toString();
				String enteredCash = etEnterCash.getText().toString();
				
				if (enteredCash.equals(""))
					enteredCash = "0";

				PortfolioObject po = new PortfolioObject();
				po.setName(enteredName);// Portfolio Name
				po.setInitialCash(enteredCash);
				po.setCurrencyType(PortfolioObject.USD);
				po.insert();
				int portfId = DbAdapter.getSingleInstance().fetchPortfolioByName(enteredName).getId();
				
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
					
					if (exch.equals("NASDAQ") || exch.equals("NYSE"))
						po.setCurrencyType(PortfolioObject.USD);
					

					PortfolioStockObject ps = new PortfolioStockObject();
					ps.setPortfolioId(portfId);
					ps.setStockId(stockId);
					ps.insert();
				}
				
				if (cpo != null) {
					cpo.setPortfolioId(portfId);
					cpo.insert();
				}
				
				Toast.makeText(getActivity().getApplicationContext(), resources.getString(R.string.portfolio_added), Toast.LENGTH_SHORT).show();
				mListener.onDialogPositiveClick(AddNewPortfolioDialog.this);
			}
		});

		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogNegativeClick(AddNewPortfolioDialog.this);
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

			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(name);
			if (po == null) {
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
		etEnterCash.setText(amount);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.et_enter_initial_cash) {
			CalculatorDialogFragment fragment = CalculatorDialogFragment.newInstance(CALCULATOR_DIALOG_ID, "0");
			fragment.setTargetFragment(this, CALCULATOR_DIALOG_ID);
			fragment.show(getActivity().getSupportFragmentManager());
		}
	}

}
