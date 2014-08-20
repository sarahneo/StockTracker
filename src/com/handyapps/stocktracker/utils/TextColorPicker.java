package com.handyapps.stocktracker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.handyapps.stocktracker.R;

public class TextColorPicker {
	
	/***
	 * 
	 * App Lable:
	 * Orange - color code: #FFC933
	 * 
	 * 
	 * Tab Text:
	 * color code: #9fad00
	 * 
	 * Green - color code: #69971F 
	 * 
	 * Red - color code: #FF0000
	 * 
	 * Tab pager indicator
	 * color code: #9fad00
	 * 
	 */
	
	
	public static Spanned getAppLabel(Context context){
		Resources rs = context.getResources();
		String strStock = rs.getString(R.string.app_name_stock);
		String strTracker = rs.getString(R.string.app_name_tracker);
		String strAppLabel = strStock + "<font color='#FFC933'>" + strTracker + "</font>";
		Spanned appLabel = Html.fromHtml(strAppLabel);
		return appLabel;
	}
	
	public static Spanned getAppLabelSettings(Context context){
		Resources rs = context.getResources();
		String strSettings = rs.getString(R.string.settings);
		String strAppLabel = "<font color='#FFC933'>" + strSettings + "</font>";
		Spanned appLabel = Html.fromHtml(strAppLabel);
		return appLabel;
	}
	
	public static Spanned getAppLabelWidgetConfigure(Context context){
		Resources rs = context.getResources();
		String strSettings = rs.getString(R.string.configure_widget);
		String strAppLabel = "<font color='#FFC933'>" + strSettings + "</font>";
		Spanned appLabel = Html.fromHtml(strAppLabel);
		return appLabel;
	}
	
	public static Spannable getAppLabelNewTrade(Context context){
		Resources rs = context.getResources();
		String strAddNewTrade = rs.getString(R.string.add_new_trade);
		SpannableString spannable = new SpannableString(strAddNewTrade);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, strAddNewTrade.length(), 0);
		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, strAddNewTrade.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getAppLabelImportCSV(Context context){
		Resources rs = context.getResources();
		String strImportCSV = rs.getString(R.string.import_csv_title);
		SpannableString spannable = new SpannableString(strImportCSV);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, strImportCSV.length(), 0);
		spannable.setSpan(new ForegroundColorSpan(rs.getColor(R.color.orange)), 0, 
				strImportCSV.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getAppLabelNewPriceAlert(Context context){
		Resources rs = context.getResources();
		String strAddNewTrade = rs.getString(R.string.add_price_alert_title);
		SpannableString spannable = new SpannableString(strAddNewTrade);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, strAddNewTrade.length(), 0);
		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, strAddNewTrade.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getAppLabelNewNewsAlert(Context context){
		Resources rs = context.getResources();
		String strAddNewTrade = rs.getString(R.string.add_news_alert_title);
		SpannableString spannable = new SpannableString(strAddNewTrade);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, strAddNewTrade.length(), 0);
		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, strAddNewTrade.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getAppLabelTransactions(Context context){
		Resources rs = context.getResources();
		String strTxns = rs.getString(R.string.txn_activity_title);
		SpannableString spannable = new SpannableString(strTxns);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, strTxns.length(), 0);
		spannable.setSpan(new ForegroundColorSpan(rs.getColor(R.color.orange)), 0, 
				strTxns.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spanned getAppLabelStocksActivity(Context context){
		Resources rs = context.getResources();
		String strStock = rs.getString(R.string.stocks_activity_title);
		String strAppLabel = "<font color='#FFC933'><b><small>" + strStock + "</small></b></font>";
		Spanned appLabel = Html.fromHtml(strAppLabel);
		return appLabel;
	}
	
	public static Spanned getAppLabelContentLoading(Context context){
		Resources rs = context.getResources();
		String strLoading = rs.getString(R.string.loading_content);
		String strAppLabel = "<font color='#FFC933'>" + strLoading + "</font>";
		Spanned appLabel = Html.fromHtml(strAppLabel);
		return appLabel;
	}
	
	public static Spanned getRedText(String title, String redText){
		String after = "<font color='#FF0000'>" + redText + "</font>";
		Spanned apanned = Html.fromHtml(title + after);
		return apanned;
	}
	
	public static SpannableString getRedText(String redText){
		SpannableString spannable = new SpannableString(redText);
		spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, redText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		return spannable;
	}
	
	public static Spanned getOrangeText(String title, String redText){
		String after = "<font color='#ebb901'>" + redText + "</font>";
		Spanned apanned = Html.fromHtml(title + after);
		return apanned;
	}
	
	public static Spanned getGreenText(String title, String greenText){ //For all green text color
		String after = "<font color='#4d6f20'>" + greenText + "</font>";
		Spanned apanned = Html.fromHtml(title + after);
		return apanned;
	}
	
	public static Spanned getBlackText(String title, String greenText){ //For all black text color
		String after = "<font color='#000000'>" + greenText + "</font>";
		Spanned apanned = Html.fromHtml(title + after);
		return apanned;
	}
	
	public static Spannable getStockPosFirstCol(String symbol, String companyName) {
		String spannableString = symbol + "\n" + companyName;
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(new RelativeSizeSpan(1.5f), 0, spannableString.indexOf("\n"), 0);
		return spannable;
	}
	
	public static String getStockPosSecondCol(String qty, String price) {
		String normalString = qty + " x\n" + price;
		if (qty.equals("0"))
			normalString = price;
		return normalString;
	}
	
	public static Spannable getStockPosThirdCol(String lastPrice, String change, String changeInPercent, Resources res) {
		String spannableString = lastPrice + "\n" + change + "(" + changeInPercent.substring(1) + ")";
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(new RelativeSizeSpan(1.4f), 0, spannableString.indexOf("\n"), 0);		
		if (change.contains("-"))
			spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.red)), spannableString.indexOf("\n"), 
					spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		else 
			spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.green)), spannableString.indexOf("\n"), 
					spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getStockPosFourthCol(String mktValue, String gainLoss, Resources res) {
		String spannableString = "";
		SpannableString spannable = null;
		if (mktValue.equals("")) {
			spannableString = gainLoss;
			spannable = new SpannableString(spannableString);
			spannable.setSpan(new RelativeSizeSpan(1.1f), 0, spannableString.length(), 0);
			if (gainLoss.contains("-"))
				spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.red)), 0, 
						spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			else 
				spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.green)), 0, 
						spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			spannableString = mktValue + "\n" + gainLoss;
			spannable = new SpannableString(spannableString);
			if (gainLoss.contains("-"))
				spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.red)), spannableString.indexOf("\n"), 
						spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			else 
				spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.green)), spannableString.indexOf("\n"), 
						spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}
	
	public static Spannable getStockPosFourthCol(String gainLoss, Resources res) {
		SpannableString spannable = new SpannableString(gainLoss);
		
		if (gainLoss.contains("-"))
			spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.red)), 0, 
					gainLoss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		else 
			spannable.setSpan(new ForegroundColorSpan(res.getColor(R.color.green)), 0, 
					gainLoss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	
	public static Spannable getTxnPortThirdCol(String qty, Resources res) {
		String spannableString = qty;
		SpannableString spannable = new SpannableString(spannableString);
		
		if (qty.contains("@"))
			spannable.setSpan(new RelativeSizeSpan(0.9f), spannableString.indexOf("@")+1, spannableString.length(), 0);
		else 
			spannable.setSpan(new RelativeSizeSpan(0.7f), spannableString.indexOf("\n"), spannableString.length(), 0);
		return spannable;
	}
	
	public static int colorBlack(){
		return Color.parseColor("#000000");
	}
}
