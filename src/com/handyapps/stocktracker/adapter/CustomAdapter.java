package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class CustomAdapter extends ArrayAdapter<SymbolCallBackObject> {

	private List<SymbolCallBackObject> adItemList;
	private FragmentActivity activity;
	private String noStockAdded = "";

	public CustomAdapter(FragmentActivity activity, int resource, int textViewResourceId, ArrayList<SymbolCallBackObject> objects) {
		super(activity, resource, textViewResourceId, objects);
		this.adItemList = objects;
		this.activity = activity;
		noStockAdded = activity.getResources().getString(R.string.no_stock_added);
	}

	public int getCount() {
		return adItemList.size();
	}

	public SymbolCallBackObject getItem(int position) {
		return adItemList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		
		Typeface helveticaSReg = Typeface.createFromAsset(activity.getAssets(), "helvetica-s-regular.ttf");
		Typeface helveticaSBold = Typeface.createFromAsset(activity.getAssets(), "helvetica-s-bold.ttf");

		ViewHolder vh;
		if (convertView == null) {

			LayoutInflater layoutInflater = activity.getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.symbol_callback_row_item, null);

			vh = new ViewHolder();
			vh.tvSymbol = (TextView) convertView.findViewById(R.id.tv_symbol_drop_item);
			vh.tvName = (TextView) convertView.findViewById(R.id.tv_name_drop_item);
			vh.tvPrice = (TextView) convertView.findViewById(R.id.tv_price_drop_item);
			vh.tvChange = (TextView) convertView.findViewById(R.id.tv_change_drop_item);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		vh.tvPrice.setTypeface(helveticaSBold, Typeface.BOLD);
		vh.tvChange.setTypeface(helveticaSReg, Typeface.BOLD);

		SymbolCallBackObject details = adItemList.get(position);

		if (details.getExchDisp().equals(""))
			vh.tvSymbol.setText(details.getSymbol());
		else
			vh.tvSymbol.setText(details.getSymbol()+":"+details.getExchDisp());
		vh.tvName.setText(details.getName());
		
		QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(details.getSymbol());
		
		Spanned spannedChanges;
		
		String mLastTradePrice = "";
		String mChange = "";
		
		int numOfDecimals = Constants.NUMBER_OF_DECIMALS;
		
		if (quote == null) {
			if (details.getName().equals(noStockAdded)) {
				mLastTradePrice = "";
				mChange = "";
			} else {
				mLastTradePrice = "null";
				mChange = "null";
			}
		} else {
			Double dLastPrice = Double.parseDouble(quote.getLastTradePrice());
			Double dChange = Double.parseDouble(quote.getChange());
			mLastTradePrice = DecimalsConverter.convertToStringValueBaseOnLocale(dLastPrice, numOfDecimals, activity);
			mChange = DecimalsConverter.convertToStringValueBaseOnLocale(dChange, numOfDecimals, activity);
			if (!quote.getChangeInPercent().equals("N/A"))
				mChange += " ("+quote.getChangeInPercent().substring(1)+")";
			else
				mChange += " ("+quote.getChangeInPercent()+")";
		}

		if (mChange.contains("-")) {
			spannedChanges = TextColorPicker.getRedText("", mChange);
		} else {
			spannedChanges = TextColorPicker.getGreenText("", mChange);
		}
		
		vh.tvPrice.setText(mLastTradePrice);
		vh.tvChange.setText(spannedChanges);

		
		return convertView;
	}
	
	
	public class ViewHolder {
		TextView tvSymbol;
		TextView tvName;
		TextView tvPrice;
		TextView tvChange;
	}
}

	