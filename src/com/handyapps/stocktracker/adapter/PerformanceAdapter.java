package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;

import org.holoeverywhere.widget.ArrayAdapter;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.PerformanceObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class PerformanceAdapter extends ArrayAdapter<PerformanceObject> implements Filterable {

	private FragmentActivity activity;
    private ArrayList<PerformanceObject> poList;
    private ArrayList<PerformanceObject> originalList;
    
     /* This will create a new ExpandableListItemAdapter, providing a custom layout resource, 
     * and the two child ViewGroups' id's. If you don't want this, just pass either just the
     * Context, or the Context and the List<T> up to super. */
     
    public PerformanceAdapter(FragmentActivity activity, int resource, int textViewResourceId, 
    		ArrayList<PerformanceObject> objects, ArrayList<PerformanceObject> orignalObjectsbjects) {
        super(activity, resource, textViewResourceId, objects);
        this.activity = activity;
        this.poList = objects;
        this.originalList = orignalObjectsbjects;
    }
    
    
    public int getCount() {
		return poList.size();
	}

	public PerformanceObject getItem(int position) {
		return poList.get(position);
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
			convertView = layoutInflater.inflate(R.layout.card_performance, null);

			vh = new ViewHolder();
			vh.colorBar = (View) convertView.findViewById(R.id.color_bar);
			vh.tvName = (TextView) convertView.findViewById(R.id.tv_perf_name);
			vh.tvSymbol = (TextView) convertView.findViewById(R.id.tv_perf_symbol);
			vh.tvNetGainLossTitle = (TextView) convertView.findViewById(R.id.tv_net_gain_loss_title);
			vh.tvNetGainLoss = (TextView) convertView.findViewById(R.id.tv_net_gain_loss_value);
			vh.tvROI = (TextView) convertView.findViewById(R.id.tv_roi_value);
			vh.tvRealized = (TextView) convertView.findViewById(R.id.tv_realized_gain_loss_value);
			vh.tvUnrealized = (TextView) convertView.findViewById(R.id.tv_unrealized_gain_loss_value);
			vh.tvRealizedTitle = (TextView) convertView.findViewById(R.id.tv_realized_gain_loss_title);
			vh.tvUnrealizedTitle = (TextView) convertView.findViewById(R.id.tv_unrealized_gain_loss_title);
			vh.tvNetAssetTitle = (TextView) convertView.findViewById(R.id.tv_net_asset_title);
			vh.tvNetAsset = (TextView) convertView.findViewById(R.id.tv_net_asset_value);
			vh.tvNetCashTitle = (TextView) convertView.findViewById(R.id.tv_net_cash_title);
			vh.tvNetCash = (TextView) convertView.findViewById(R.id.tv_net_cash_value);
			vh.llNetPos = (LinearLayout) convertView.findViewById(R.id.ll_net_asset_and_cash);
			//vh.bg = (BarGraph) convertView.findViewById(R.id.bargraph);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		PerformanceObject details = poList.get(position);
		
		//vh.tvSymbol.setTypeface(helveticaSReg, Typeface.BOLD);
		vh.tvNetGainLossTitle.setTypeface(helveticaSReg);
		vh.tvNetGainLoss.setTypeface(helveticaSBold, Typeface.BOLD);
		vh.tvROI.setTypeface(helveticaSBold, Typeface.BOLD);
		vh.tvRealizedTitle.setTypeface(helveticaSReg);
		vh.tvUnrealizedTitle.setTypeface(helveticaSReg);
		vh.tvRealized.setTypeface(helveticaSBold, Typeface.BOLD);
		vh.tvUnrealized.setTypeface(helveticaSBold, Typeface.BOLD);	
		vh.tvNetAssetTitle.setTypeface(helveticaSReg);
		vh.tvNetAsset.setTypeface(helveticaSBold, Typeface.BOLD);
		vh.tvNetCashTitle.setTypeface(helveticaSReg);
		vh.tvNetCash.setTypeface(helveticaSBold, Typeface.BOLD);
		
		if (details.getName().equals(activity.getResources().getString(R.string.no_stock_added))) {
			vh.tvName.setText(details.getName());
			vh.tvSymbol.setText("");
			vh.tvNetGainLossTitle.setText("");
			vh.tvNetGainLoss.setText("");
			vh.tvROI.setText("-");
			vh.tvRealized.setText("");
			vh.tvUnrealized.setText("");
			vh.tvRealizedTitle.setText("");
			vh.tvUnrealizedTitle.setText("");
			vh.llNetPos.setVisibility(View.GONE);
		} else {
			
			if (details.getType().equals(PerformanceObject.PORTFOLIO_TYPE)) {
				vh.colorBar.setBackgroundColor(activity.getResources().getColor(R.color.green));
				vh.tvName.setText(details.getName());
				if (details.getCurrency() != null)
					vh.tvSymbol.setText(details.getCurrency().toUpperCase());
				else 
					vh.tvSymbol.setText("");
				vh.llNetPos.setVisibility(View.VISIBLE);
				vh.tvNetAsset.setText(details.getLastPrice());
				vh.tvNetCash.setText(details.getNetCash());
				vh.tvName.setTypeface(helveticaSReg, Typeface.BOLD);				
				vh.tvSymbol.setTypeface(helveticaSReg, Typeface.NORMAL);				
			} else {
				vh.colorBar.setBackgroundColor(details.getColorCode());
				vh.tvName.setText(details.getName());
				vh.tvSymbol.setText(details.getSymbol());	
				vh.llNetPos.setVisibility(View.GONE);
				vh.tvName.setTypeface(helveticaSReg, Typeface.NORMAL);				
				vh.tvSymbol.setTypeface(helveticaSReg, Typeface.BOLD);				
			}
			
			vh.tvNetGainLossTitle.setText(activity.getResources().getString(R.string.net_gain_or_loss));
			vh.tvRealizedTitle.setText(activity.getResources().getString(R.string.realized_gain_loss_title));
			vh.tvUnrealizedTitle.setText(activity.getResources().getString(R.string.unrealized_gain_loss_title));
			
			int numOfDecimals = Constants.NUMBER_OF_DECIMALS;
			
			String sNetGainLoss = DecimalsConverter.convertToStringValueBaseOnLocale
					(details.getOverallGainLoss(), numOfDecimals, activity);
			String sRealized = DecimalsConverter.convertToStringValueBaseOnLocale
							(details.getDRealizedGainLoss(), numOfDecimals, activity);
			String sUnrealized = DecimalsConverter.convertToStringValueBaseOnLocale
					(details.getDUnrealizedGainLoss(), numOfDecimals, activity);
			
			SpannableString spannedGainLoss = new SpannableString(sNetGainLoss);
			SpannableString spannedRealized = new SpannableString(sRealized);
			SpannableString spannedUnrealized = new SpannableString(sUnrealized);
			
			if (sNetGainLoss.contains("-")) 
				spannedGainLoss = TextColorPicker.getRedText(sNetGainLoss);
						
			if (sRealized.contains("-")) 
				spannedRealized = TextColorPicker.getRedText(sRealized);
			
			if (sUnrealized.contains("-")) 
				spannedUnrealized = TextColorPicker.getRedText(sUnrealized);

			vh.tvNetGainLoss.setText(spannedGainLoss);
			vh.tvRealized.setText(spannedRealized);
			vh.tvUnrealized.setText(spannedUnrealized);
			
			
			Double dROI = Double.parseDouble(details.getPercentROI().substring(0, details.getPercentROI().length()-1));
			String sROI = DecimalsConverter.convertToStringValueBaseOnLocale(dROI, 1, activity);
			if (sROI.contains("-"))
				sROI = sROI.replace("-", "");
			if (sROI.contains("$"))
				sROI = sROI.replace("$", "");
			sROI += "%";
			
			/*GradientDrawable bgShape = (GradientDrawable)vh.tvROI.getBackground();
			if (dROI < 0)
				bgShape.setColor(activity.getResources().getColor(R.color.perf_roi_orange));
			else
				bgShape.setColor(activity.getResources().getColor(R.color.perf_roi_green));*/
			
			if (dROI < 0) {
				vh.tvROI.setBackgroundResource(R.drawable.arrow_down_orange);
				vh.tvROI.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
			} else {
				vh.tvROI.setBackgroundResource(R.drawable.arrow_up_green);
				vh.tvROI.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
			}
				
			vh.tvROI.setText(sROI);
			
			/*ArrayList<Bar> points = new ArrayList<Bar>();
			Bar d = new Bar();
			d.setColor(activity.getResources().getColor(R.color.orange));
			d.setName("Realized Gain/Loss");
			d.setValue((float) details.getDRealizedGainLoss());
			Bar d2 = new Bar();
			d2.setColor(activity.getResources().getColor(R.color.dark_blue));
			d2.setName("Unrealized Gain/Loss");
			d2.setValue((float) details.getDUnrealizedGainLoss());
			points.add(d);
			points.add(d2);
			
			vh.bg.setBars(points);*/
		}
		
		return convertView;
	}
	
	
	public class ViewHolder {
		View colorBar;
		TextView tvName;
		TextView tvSymbol;
		TextView tvNetGainLossTitle;
		TextView tvNetGainLoss;
		TextView tvROI;
		TextView tvRealized;
		TextView tvUnrealized;
		TextView tvRealizedTitle;
		TextView tvUnrealizedTitle;
		TextView tvNetAssetTitle;
		TextView tvNetAsset;
		TextView tvNetCashTitle;
		TextView tvNetCash;
		LinearLayout llNetPos;
		//BarGraph bg;
	}



	@Override
	public Filter getFilter() {
		return new Filter() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<PerformanceObject> perfObjList = new ArrayList<PerformanceObject>();
				perfObjList = (ArrayList<PerformanceObject>)results.values;
				poList.clear();
				for(int i = 0, l = perfObjList.size(); i < l; i++)
					poList.add(perfObjList.get(i));
				
				notifyDataSetChanged();
			}
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				constraint = constraint.toString().toLowerCase();
				FilterResults result = new FilterResults();
				if(constraint != null && constraint.toString().length() > 0) {
					ArrayList<PerformanceObject> filteredItems = new ArrayList<PerformanceObject>();

					for(int i = 0, l = originalList.size(); i < l; i++) {
						PerformanceObject perfObj = originalList.get(i);
						if(perfObj.getName().toLowerCase().contains(constraint))
							filteredItems.add(perfObj);
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
}