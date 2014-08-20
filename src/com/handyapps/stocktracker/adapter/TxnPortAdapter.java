package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.TxnPortObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class TxnPortAdapter extends ArrayAdapter<TxnPortObject> implements Filterable {

	private List<TxnPortObject> adItemList;
	private List<TxnPortObject> originalList;
	private FragmentActivity activity;

	public TxnPortAdapter(Activity activity, int resource, int textViewResourceId, 
			ArrayList<TxnPortObject> objects, ArrayList<TxnPortObject> originalObjects) {
		super(activity, resource, textViewResourceId, objects);
		this.adItemList = objects;
		this.originalList = originalObjects;
		this.activity = activity;
	}

	public int getCount() {
		return adItemList.size();
	}

	public TxnPortObject getItem(int position) {
		return adItemList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		int numOfDecimals = Constants.NUMBER_OF_DECIMALS;

		ViewHolder vh;
		if (convertView == null) {

			LayoutInflater layoutInflater = activity.getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.txn_port_row_item, null);

			vh = new ViewHolder();
			vh.tvDate = (TextView) convertView.findViewById(R.id.txn_port_first_col);
			vh.tvTickerSymbol = (TextView) convertView.findViewById(R.id.txn_port_second_col);
			vh.tvQtyPrice = (TextView) convertView.findViewById(R.id.txn_port_third_col);
			vh.tvCashValue = (TextView) convertView.findViewById(R.id.txn_port_fourth_col);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		TxnPortObject details = adItemList.get(position);
		
		if (details.getNotes().equals(activity.getResources().getString(R.string.no_txns_added))) {
			Spannable spannable = new SpannableString(details.getNotes());
			spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, details.getNotes().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			vh.tvDate.setText("");
			vh.tvTickerSymbol.setText("");
			vh.tvQtyPrice.setText("");
			vh.tvCashValue.setText(spannable);
		} else {
			String tradeDate = Integer.toString(details.getTradeDate()); //yyyyMMdd
			String sTradeDate = tradeDate.substring(4, 6) + "/" + tradeDate.substring(6, 8) + "/" + tradeDate.substring(2, 4); //MM/dd/yy
			String tickerSymbol = "N.A.";
			String qtyPrice = "";
			String total = DecimalsConverter.convertToStringValueBaseOnLocale(details.getTotal(), numOfDecimals, activity);
			
			
			if (!details.getType().contains("c")) {
				tickerSymbol = details.getSymbol();
				if (!details.getType().equals("ds")) {
					int qty = details.getNumOfShares();
					String sQty = Integer.toString(qty);
					String price = details.getPrice(); 
					
					if (details.getType().equals("b")) {
						qtyPrice = "BUY " + sQty + "\n@ $" + price;
						total = "-" + total;
					} else if (details.getType().equals("s")) {
						qtyPrice = "SELL " + sQty + "\n@ $" + price;
						total = "+" + total;
					} else if (details.getType().equals("d"))
						qtyPrice = "DIVIDEND " + sQty + "\n @ $" + price;
				} else {
					qtyPrice = "DIVIDEND 0\nN.A.";
				}
			} else if (details.getType().equals("cd")) {
				qtyPrice = "CASH\nDEPOSIT";
				double dTot = Double.parseDouble(details.getPrice());
				total = "+" + DecimalsConverter.convertToStringValueBaseOnLocale(dTot, numOfDecimals, activity);
			} else if (details.getType().equals("cw")) {
				qtyPrice = "CASH\nWITHDRAWAL";
				double dTot = Double.parseDouble(details.getPrice());
				total = "-" + DecimalsConverter.convertToStringValueBaseOnLocale(dTot, numOfDecimals, activity);
			}
			
			vh.tvDate.setText(sTradeDate);
			vh.tvTickerSymbol.setText(tickerSymbol);
			vh.tvQtyPrice.setText(TextColorPicker.getTxnPortThirdCol(qtyPrice, activity.getResources()));
			vh.tvCashValue.setText(total);
		}
		
		return convertView;
	}
	
	
	@Override
	public Filter getFilter() {
		return new Filter() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<TxnPortObject> txnPortObjList = new ArrayList<TxnPortObject>();
				txnPortObjList = (ArrayList<TxnPortObject>)results.values;
				adItemList.clear();
				for(int i = 0, l = txnPortObjList.size(); i < l; i++)
					adItemList.add(txnPortObjList.get(i));
				
				notifyDataSetChanged();
			}
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				constraint = constraint.toString().toLowerCase();
				FilterResults result = new FilterResults();
				if(constraint != null && constraint.toString().length() > 0) {
					ArrayList<TxnPortObject> filteredItems = new ArrayList<TxnPortObject>();

					for(int i = 0, l = originalList.size(); i < l; i++) {
						TxnPortObject txnPortObj = originalList.get(i);
						if(txnPortObj.getSymbol().toLowerCase().equals(constraint))
							filteredItems.add(txnPortObj);
					}
					result.count = filteredItems.size();
					result.values = filteredItems;
				}
				else {
					synchronized(this) {
						result.values = originalList;
						result.count = originalList.size();
					}
				}

				return result;
			}
		};
	}
	
	
	public class ViewHolder {
		TextView tvDate;
		TextView tvTickerSymbol;
		TextView tvQtyPrice;
		TextView tvCashValue;
	}
}

	