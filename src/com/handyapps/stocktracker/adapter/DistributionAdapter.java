package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.DistributionObject;

public class DistributionAdapter extends ArrayAdapter<DistributionObject> {

	private List<DistributionObject> adItemList;
	private FragmentActivity activity;
	private Resources res;

	public DistributionAdapter(FragmentActivity activity, int resource, int textViewResourceId, ArrayList<DistributionObject> objects) {
		super(activity, resource, textViewResourceId, objects);
		this.adItemList = objects;
		this.activity = activity;
		this.res = activity.getResources();
	}

	public int getCount() {
		return adItemList.size();
	}

	public DistributionObject getItem(int position) {
		return adItemList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder vh;
		if (convertView == null) {

			LayoutInflater layoutInflater = activity.getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.distribution_row_item, null);

			vh = new ViewHolder();
			vh.ivColor = (ImageView) convertView.findViewById(R.id.dist_stock_color);
			vh.tvName = (TextView) convertView.findViewById(R.id.dist_first_col);
			vh.tvPercent = (TextView) convertView.findViewById(R.id.dist_second_col);
			vh.tvValue = (TextView) convertView.findViewById(R.id.dist_third_col);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		DistributionObject details = adItemList.get(position);
		
		if (details.getName().equals(res.getString(R.string.no_stock_added))) {
			vh.tvName.setText(details.getName());
			vh.tvPercent.setText("");
			vh.tvValue.setText("");
		} else {
			String name = details.getName();
			String percent = Double.toString(details.getPercent());
			String value = details.getValue();
			if (value.contains("$"))
				value = value.replaceAll("\\$", "");
			
			GradientDrawable bgShape = (GradientDrawable)vh.ivColor.getBackground();
			bgShape.setColor(details.getColor());	
			
			vh.tvName.setText(name);
			vh.tvPercent.setText(percent);
			vh.tvValue.setText(value);
		}
		
		return convertView;
	}
	
	
	public class ViewHolder {
		ImageView ivColor;
		TextView tvName;
		TextView tvPercent;
		TextView tvValue;

	}
}

	