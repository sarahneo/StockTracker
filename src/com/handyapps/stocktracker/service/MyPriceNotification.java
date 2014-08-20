package com.handyapps.stocktracker.service;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.MyDateFormat;

public class MyPriceNotification {

	public static long getAlertCount() {
		long result = DbAdapter.getSingleInstance().countAlertList();
		return result;
	}

	public static void alertNotification(String lastTradePrice, String symbol, Context ctx) {

		String mLastTradePrice = lastTradePrice;
		
		String mSymbol = symbol;
		Context mCtx = ctx;

		double dLastTradePrice = 0;
		if (mLastTradePrice.length() > 0) {
			try {
				dLastTradePrice = Double.parseDouble(mLastTradePrice);
			} catch (NumberFormatException e) {
				dLastTradePrice = -1;
			}
		}

		if (!(dLastTradePrice == -1)) {
			
			AlertObject mAo = DbAdapter.getSingleInstance().fetchAlertObjectBySymbol(mSymbol);

			if (mAo != null && MyDateFormat.isDoUpdate(ctx)) {
				int isNotifyOn = mAo.getIsNotifyOn();
				if (isNotifyOn != 1)
					return;				
				String mUpperPrice = mAo.getUpperPrice();
				String mLowerPrice = mAo.getLowerPrice();
				double dUpperPrice = 0;
				double dLowerPrice = 0;

				if (mUpperPrice.length() > 0) {
					try {
						dUpperPrice = Double.parseDouble(mUpperPrice);

						if (dLastTradePrice == dUpperPrice || dLastTradePrice > dUpperPrice) {
							AlertObject aoUppterUpdate = mAo;
							aoUppterUpdate.setIsUpperTargetOn(0);
							aoUppterUpdate.setUpperPrice("");
							aoUppterUpdate.update();
							
							//title;
							//notifyId:
							//content:
							String title = mCtx.getResources().getString(R.string.stock_price_alert);
							title = "[" + mSymbol + "] " + title;
							String content_1 = mCtx.getResources().getString(R.string.stock_price_hit);						
							String content =  content_1 + ":" + " $" + mLastTradePrice;
							Calendar cal = Calendar.getInstance();
							int nId = cal.get(Calendar.DATE);
							int aoId = mAo.getId();
							int notifyId = nId + aoId; 
							showNotification(notifyId, symbol, title, content, mCtx);
						}
					} catch (NumberFormatException e) {
					}
				}

				if (mLowerPrice.length() > 0) {
					try {
						dLowerPrice = Double.parseDouble(mLowerPrice);
						if (dLastTradePrice == dLowerPrice || dLastTradePrice < dLowerPrice) {
							AlertObject aoLowerUpdate = mAo;
							aoLowerUpdate.setIsLowerTargetOn(0);
							aoLowerUpdate.setLowerPrice("");
							aoLowerUpdate.update();
							
							//title;
							//notifyId:
							//content:
							String title = mCtx.getResources().getString(R.string.stock_price_alert);
							title = "[" + mSymbol + "] " + title;
							String content_1 = mCtx.getResources().getString(R.string.stock_price_hit);						
							String content =  content_1 + ":" + " $" + mLastTradePrice;
							Calendar cal = Calendar.getInstance();
							int nId = cal.get(Calendar.DATE);
							int aoId = mAo.getId();
							int notifyId = nId + aoId + 1; 
							showNotification(notifyId, symbol, title, content, mCtx);
						}
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
	
	private static void showNotification(int notifyId, String symbol, String title, String content, Context context){
		int mNotifyId = notifyId;
		String mTitle = title;
		String mContent = content;
		Context mCtx = context;
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(mTitle).setContentText(mContent);

		QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
		Intent resultIntent = null;
		
		if (qo != null && so != null) {
			resultIntent = new Intent(mCtx, TransactionDetailsFragmentActivity.class);
			resultIntent.putExtra(Constants.KEY_TAB_POSITION, TransactionDetailsFragmentActivity.TAB_POSITION_SUMMARY);
			resultIntent.putExtra(Constants.KEY_FROM, Constants.FROM_FIND_STOCKS);
			resultIntent.putExtra(Constants.KEY_SYMBOL, symbol);
			resultIntent.putExtra(Constants.KEY_COMPANY_NAME, so.getName());
			resultIntent.putExtra(Constants.KEY_EXCH, so.getExch());
			resultIntent.putExtra(Constants.KEY_TYPE, so.getType());
			resultIntent.putExtra(Constants.KEY_TYPE_DISP, so.getTypeDisp());
			resultIntent.putExtra(Constants.KEY_EXCH_DISP, so.getExchDisp());
		} else {
			resultIntent = new Intent(mCtx, MainFragmentActivity.class);
			resultIntent.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);			
		}
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
		stackBuilder.addParentStack(MainFragmentActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(mNotifyId, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		//mBuilder.setProgress(100, 100, true);
		//mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		mBuilder.setAutoCancel(true);
		//mBuilder.setLights(0xFF0000FF,100,3000);
		//mBuilder.setPriority(Notification.PRIORITY_DEFAULT);

		NotificationManager mNotificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(mNotifyId, mBuilder.build());
	}

}
