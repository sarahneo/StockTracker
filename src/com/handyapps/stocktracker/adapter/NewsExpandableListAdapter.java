package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.holoeverywhere.widget.ExpandableListView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.StockObject;
 
public class NewsExpandableListAdapter extends BaseExpandableListAdapter implements Filterable {
 
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<NewsObject>> _listDataChild;
    private HashMap<String, List<NewsObject>> _originalList;
    private boolean _isFromStocksActivity;
 
    public NewsExpandableListAdapter(Context context, ArrayList<String> listDataHeader,
            HashMap<String, List<NewsObject>> listChildData,
            HashMap<String, List<NewsObject>> originalList,
            boolean isFromStocksActivity) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._originalList = originalList;
        this._isFromStocksActivity = isFromStocksActivity;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	try {
	        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
	                .get(childPosititon);
    	} catch (IndexOutOfBoundsException e){
    		Log.i("ioobe", "groupPos="+groupPosition);
    		Log.i("ioobe", "childPos="+childPosititon);
    		Log.i("ioobe", "listDataChild.size="+_listDataChild.size());
    		Log.i("ioobe", "_listDataHeader.size="+_listDataHeader.size());
    		return null;
    	}
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final NewsObject childObj = (NewsObject) getChild(groupPosition, childPosition);
        String title = childObj.getTitle();
        String desc = childObj.getDescription();
        String time = childObj.getPubDate();
        
        if (convertView == null) {
	        LayoutInflater infalInflater = (LayoutInflater) this._context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	        convertView = infalInflater.inflate(R.layout.news_row_layout, null);
        } 
 
        TextView tvColor = (TextView) convertView
                .findViewById(R.id.tv_news_color);
        TextView tvTitle = (TextView) convertView
                .findViewById(R.id.tv_news_title);
        TextView tvDesc = (TextView) convertView
                .findViewById(R.id.tv_news_desciption_with_link);
        TextView tvPubDate = (TextView) convertView
                .findViewById(R.id.tv_news_pub_date);
        
        if (desc.equals("")) {
        	tvDesc.setVisibility(View.GONE);
		} else {
			tvDesc.setVisibility(View.VISIBLE);
		}
 
        String symbol = childObj.getSymbol();
        StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
        if (stockObj != null) {
        	tvColor.setBackgroundColor(stockObj.getColorCode());
        }
        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvPubDate.setText(time);
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	if (this._listDataChild.get(this._listDataHeader.get(groupPosition)) != null)
    		return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    	else
    		return 0;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.card_news, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.card_title);
        lblListHeader.setText(headerTitle);
        
        if (_isFromStocksActivity)
        	lblListHeader.setBackgroundColor(_context.getResources().getColor(R.color.green));
        else
        	lblListHeader.setBackgroundColor(_context.getResources().getColor(R.color.blue));
        
        ExpandableListView mExpandableListView = (ExpandableListView) parent;
        mExpandableListView.expandGroup(groupPosition);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    @Override
	public Filter getFilter() {
		return new Filter() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				HashMap<String, List<NewsObject>> mapObj = new HashMap<String, List<NewsObject>>();
				mapObj = (HashMap<String, List<NewsObject>>) results.values;
				_listDataChild.clear();
				_listDataHeader.clear();
				Iterator<Entry<String, List<NewsObject>>> it = mapObj.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, List<NewsObject>> pairs = (Entry<String, List<NewsObject>>)it.next();
					_listDataChild.put(pairs.getKey(), pairs.getValue());
					_listDataHeader.add(pairs.getKey());
				}
				
				notifyDataSetChanged();
			}
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				constraint = constraint.toString().toLowerCase();
				FilterResults result = new FilterResults();
				if(constraint != null && constraint.toString().length() > 0) {
					HashMap<String, List<NewsObject>> newMap = new HashMap<String, List<NewsObject>>();

					Iterator<Entry<String, List<NewsObject>>> it = _originalList.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, List<NewsObject>> pairs = (Entry<String, List<NewsObject>>)it.next();
						List<NewsObject> newsObjList = pairs.getValue();
						List<NewsObject> newList = new ArrayList<NewsObject>();
						String cardTitle = pairs.getKey();
						
						for (int i=0; i<newsObjList.size(); i++) {
							if (newsObjList.get(i).getTitle().toLowerCase().contains(constraint) ||
									newsObjList.get(i).getDescription().toLowerCase().contains(constraint))
								newList.add(newsObjList.get(i));
						}
						newMap.put(cardTitle, newList);
					}
					result.count = newMap.size();
					result.values = newMap;
				}
				else {
					synchronized(this) {
						result.values = _originalList;
						result.count = _originalList.size();
					}
				}

				return result;
			}
		};
	}
}