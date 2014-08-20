package com.handyapps.stocktracker.adapter;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.handyapps.stocktracker.R;

public class MyNavDrawerAdapter extends ArrayAdapter<String> {
	
	private FragmentActivity mContext;
	private String[] mNavDrawerValues;
	private int[] mResIds = {R.drawable.ic_overview, R.drawable.icon_news, R.drawable.ic_portfolio, 
			R.drawable.icon_alert, R.drawable.icon_performance, R.drawable.icon_chart, R.drawable.nav_drawer_settings};
	
	public MyNavDrawerAdapter(Context ctx, int txtViewResourceId, String[] objects) {
        super(ctx, txtViewResourceId, objects);
        this.mContext = (Activity) ctx;
        this.mNavDrawerValues = objects;
    }
	
    
    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {
    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        TextView tv = (TextView) inflater.inflate(R.layout.nav_drawer_list_item, prnt, false);
        tv.setText(mNavDrawerValues[pos]);
        
        Drawable x = mContext.getResources().getDrawable(mResIds[pos]);
        x.setBounds(0, 0, x.getIntrinsicWidth() - 20, x.getIntrinsicHeight() -20);
        tv.setCompoundDrawables(x, null, null, null);
        //tv.setCompoundDrawablesWithIntrinsicBounds(mResIds[pos], 0, 0, 0);
        return tv;
    }
    
}

