package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.SymbolCallBackObject;

public class FindStocksAdapter extends ArrayAdapter<SymbolCallBackObject> {

	private List<SymbolCallBackObject> adItemList;
	private FragmentActivity activity;

	public FindStocksAdapter(FragmentActivity activity, int resource, int textViewResourceId, ArrayList<SymbolCallBackObject> objects) {
		super(activity, resource, textViewResourceId, objects);
		this.adItemList = objects;
		this.activity = activity;
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

		ViewHolder vh;
		if (convertView == null) {

			LayoutInflater layoutInflater = activity.getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.find_stocks_row_item, null);

			vh = new ViewHolder();
			vh.tvSymbol = (TextView) convertView.findViewById(R.id.tv_stock_symbol_item);
			vh.tvName = (TextView) convertView.findViewById(R.id.tv_stock_name_item);
			vh.tvType = (TextView) convertView.findViewById(R.id.tv_stock_type_item);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		SymbolCallBackObject details = adItemList.get(position);
		
		vh.tvSymbol.setText(details.getSymbol());
		vh.tvName.setText(details.getName());

		if (details.getExchDisp().equals(""))
			vh.tvType.setText(details.getTypeDisp());
		else
			vh.tvType.setText(details.getTypeDisp()+" - "+details.getExchDisp());
		
		return convertView;
	}
	
	
	public class ViewHolder {
		TextView tvSymbol;
		TextView tvName;
		TextView tvType;
	}
}

	