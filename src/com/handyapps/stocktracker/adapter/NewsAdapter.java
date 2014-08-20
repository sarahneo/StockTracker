package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.NewsObject;

public class NewsAdapter extends ArrayAdapter<NewsObject> {

	private ArrayList<NewsObject> mItemList;
	private Context context;

	public NewsAdapter(Context context, int resource, int textViewResourceId, ArrayList<NewsObject> objects) {
		super(context, resource, textViewResourceId, objects);
		this.mItemList = objects;
		this.context = context;		
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return this.mItemList.size();
	}

	public NewsObject getItem(int position) {
		// TODO Auto-generated method stub
		return this.mItemList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		MyViewHolder vh;
		
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.news_row_layout, parent, false);
		}			
	
		vh = new MyViewHolder();
		vh.tvMyTitle = (TextView) convertView.findViewById(R.id.tv_news_title);
		vh.tvMyDescription = (TextView) convertView.findViewById(R.id.tv_news_desciption_with_link);
		vh.tvMyPubDate = (TextView) convertView.findViewById(R.id.tv_news_pub_date);
		convertView.setTag(vh);


		NewsObject dbo = this.mItemList.get(position);
		String title = dbo.getTitle();
		String description = dbo.getDescription();
		String sPubDate = dbo.getPubDate();
		
		if (description.equals("")) {
			vh.tvMyDescription.setVisibility(View.GONE);
		} else {
			vh.tvMyDescription.setVisibility(View.VISIBLE);
		}

		vh.tvMyTitle.setText(title);
		vh.tvMyDescription.setText(description);
		vh.tvMyPubDate.setText(sPubDate);

		return convertView;
	}
	
	public class MyViewHolder {
		TextView tvMyTitle;
		TextView tvMyPubDate;
		TextView tvMyDescription;
	}

}
