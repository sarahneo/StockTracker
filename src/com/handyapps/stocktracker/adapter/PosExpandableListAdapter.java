package com.handyapps.stocktracker.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.holoeverywhere.widget.ExpandableListView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.CashManager;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.StockPosObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;
 
public class PosExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context _context;
    private Resources _res;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<StockPosObject>> _listDataChild;
    private List<CashPosObject> _cpoList;
    private List<String> _initialCash;
 
    public PosExpandableListAdapter(Context context, ArrayList<String> listDataHeader,
            HashMap<String, List<StockPosObject>> listChildData, List<CashPosObject> cpoList,
            List<String> initialCash) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._cpoList = cpoList;
        this._initialCash = initialCash;
        this._res = context.getResources();
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	try {
    		if (groupPosition == getGroupCount()-1)
    			return _cpoList;
    		else
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
 
    	String headerTitle = (String) getGroup(groupPosition);
        
        LayoutInflater infalInflater = (LayoutInflater) this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (headerTitle.equals(_res.getString(R.string.cash_positions)))
        	convertView = infalInflater.inflate(R.layout.cash_positions_row, null);
        else {
       		convertView = infalInflater.inflate(R.layout.stock_pos_row_item, null);
        }
 
        if (headerTitle.toUpperCase().equals(_res.getString(R.string.cash_positions))) {
        	TextView tvInitialBalance = (TextView) convertView.findViewById(R.id.cash_pos_first_col);
        	TextView tvTotDeposits = (TextView) convertView.findViewById(R.id.cash_pos_second_col);
        	TextView tvTotWithdrawals = (TextView) convertView.findViewById(R.id.cash_pos_third_col);
        	TextView tvFinalBalance = (TextView) convertView.findViewById(R.id.cash_pos_fourth_col);
        	
        	double dTotDeposits = 0, dTotWithdrawals = 0, dInitialBalance = 0, dFinalBalance = 0;
    		int numDecimals = Constants.NUMBER_OF_DECIMALS;
    		
    		ArrayList<Double> cashAmounts = CashManager.getCashAmountsArray(_cpoList, _initialCash.get(0));
    		dInitialBalance = cashAmounts.get(0);
    		dTotDeposits = cashAmounts.get(1);
    		dTotWithdrawals = cashAmounts.get(2);
    		dFinalBalance = cashAmounts.get(3);
    		
    		tvInitialBalance.setText(DecimalsConverter.convertToStringValueBaseOnLocale(dInitialBalance, numDecimals, _context));
    		tvTotDeposits.setText(DecimalsConverter.convertToStringValueBaseOnLocale(dTotDeposits, numDecimals, _context));
    		tvTotWithdrawals.setText(DecimalsConverter.convertToStringValueBaseOnLocale(dTotWithdrawals, numDecimals, _context));
    		tvFinalBalance.setText(DecimalsConverter.convertToStringValueBaseOnLocale(dFinalBalance, numDecimals, _context));
        } else {
        	final StockPosObject childObj = (StockPosObject) getChild(groupPosition, childPosition);
	        TextView tvFirstCol = (TextView) convertView
	                .findViewById(R.id.stock_pos_first_col);
	        TextView tvSecondCol = (TextView) convertView
	                .findViewById(R.id.stock_pos_second_col);
	        TextView tvThirdCol = (TextView) convertView
	                .findViewById(R.id.stock_pos_third_col);
	        TextView tvFourthCol = (TextView) convertView
	        		.findViewById(R.id.stock_pos_fourth_col);
	        
	        if (childObj.getName().equals(_res.getString(R.string.no_stock_added))) {
				Spannable spannable = new SpannableString(childObj.getName());
				spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, childObj.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvFirstCol.setText(spannable);
				tvSecondCol.setText("");
				tvThirdCol.setText("");
				tvFourthCol.setText("");
			} else {
			
				int numOfDecimals = Constants.NUMBER_OF_DECIMALS;
				String typeOfPos = childObj.getStockType();
				Double dLastPrice = Double.parseDouble(childObj.getLastPrice());
				Double dChange = Double.parseDouble(childObj.getChange());
				String lastPrice = DecimalsConverter.convertToStringValueBaseOnLocale(dLastPrice, numOfDecimals, _context);	
				String change = DecimalsConverter.convertToStringValueBaseOnLocale(dChange, numOfDecimals, _context);
				
				Spannable firstColSpan = TextColorPicker.getStockPosFirstCol(childObj.getSymbol(), childObj.getName());
				String secondCol = TextColorPicker.getStockPosSecondCol(childObj.getQuantity(), childObj.getAvgPrice());
				Spannable thirdCol = TextColorPicker.getStockPosThirdCol(lastPrice, change, childObj.getChangeInPercent(), _res);
				Spannable fourthCol;
				if (typeOfPos.equals(StockPosObject.CLOSED_POS_TYPE))
					fourthCol = TextColorPicker.getStockPosFourthCol(childObj.getGainLoss(), _res);
				else
					fourthCol = TextColorPicker.getStockPosFourthCol(childObj.getMktValue(), childObj.getGainLoss(), _res);
				
				tvFirstCol.setText(firstColSpan);
				tvSecondCol.setText(secondCol);
				tvThirdCol.setText(thirdCol);
				tvFourthCol.setText(fourthCol);
			}
        }
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	if (groupPosition == getGroupCount()-1)
    		return 1;
    	else if (this._listDataChild.get(this._listDataHeader.get(groupPosition)) != null)
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
        TextView lblListHeader = null;
        TableRow cardFirstRow = null;
        TextView fourthColTitle = null;

            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        
        if (headerTitle.equals(_res.getString(R.string.cash_positions))) {
        	convertView = infalInflater.inflate(R.layout.card_cash_positions, null);
        	lblListHeader = (TextView) convertView
                    .findViewById(R.id.card_cash_title);
        } else {
        	convertView = infalInflater.inflate(R.layout.card_stock_positions, null);
        	lblListHeader = (TextView) convertView
                    .findViewById(R.id.card_title);
        	cardFirstRow = (TableRow) convertView
        			.findViewById(R.id.card_first_row_layout);
        	fourthColTitle = (TextView) convertView
        			.findViewById(R.id.fourth_column_title);
        }
 
        int blueColor = _context.getResources().getColor(R.color.blue);
        int redColor = _context.getResources().getColor(R.color.red_tab);
        int greenColor = _context.getResources().getColor(R.color.green);
        String strLong = _context.getResources().getString(R.string.string_long);
        String strShort = _context.getResources().getString(R.string.string_short);
        String strClosed = _context.getResources().getString(R.string.string_closed);
        
        lblListHeader.setText(headerTitle);
        if (headerTitle.contains(strLong)) {
        	lblListHeader.setBackgroundColor(greenColor);
        	cardFirstRow.setBackgroundColor(greenColor);
        } else if (headerTitle.contains(strShort)) {
        	lblListHeader.setBackgroundColor(redColor);
        	cardFirstRow.setBackgroundColor(redColor);
        } else if (headerTitle.contains(strClosed)) {
        	lblListHeader.setBackgroundColor(blueColor);
        	cardFirstRow.setBackgroundColor(blueColor);
        }
        
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
    
}