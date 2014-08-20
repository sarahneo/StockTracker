package com.handyapps.stocktracker.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsAlertObject;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.MyDateFormat;

public class MyNewsNotification {

	public static long getAlertCount() {
		long result = DbAdapter.getSingleInstance().countNewsAlertList();
		return result;
	}

	public static void alertNotification(ArrayList<NewsObject> noList, String symbol, Context ctx) {
		
		// Get stock ID
		StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
		
		NewsAlertObject nAo = DbAdapter.getSingleInstance().fetchNewsAlertObjectBySymbol(symbol);
		
		if (nAo == null)
			return;
		else if (!MyDateFormat.isDoUpdate(ctx))
			return;
		else {
			int isNotifyOn = nAo.getIsNotifyOn();
			if (isNotifyOn != 1)
				return;	
		}
		
		// Check if news articles are within the duration of the alert frequency
		int alertFreq = nAo.getAlertFrequency();
		DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		Date newsDate;
		Date currDate = new Date();
		int numNewArticles = 0;
		
		for (NewsObject newsObj : noList) {
			try {
				newsDate = formatter.parse(newsObj.getPubDate());
				/*String newsDateSting = formatter.format(newsDate);
				String currDateString = formatter.format(currDate);
				Log.i("alarmreceiver", "newsDateString="+newsDateSting);
				Log.i("alarmreceiver", "currDateString="+currDateString);
				Log.i("alarmreceiver", "timeDiff="+Long.toString(currDate.getTime() - newsDate.getTime()));
				Log.i("alarmreceiver", "alertFreq="+Long.toString(alertFreq*60*1000));*/
				if (currDate.getTime() - newsDate.getTime() <= alertFreq*60*1000)
					numNewArticles++;
			} catch (ParseException e) {
				e.printStackTrace();
				numNewArticles = 1;
			}
		}
		
		if (numNewArticles == 0)
			Log.i("alarmreceiver", "no new articles"); // dont' send alert
		else {			
			//title;
			//notifyId:
			//content:
			String title = "[" + symbol + "] News Alerts (" + Integer.toString(numNewArticles) + ")";
			String content =  noList.get(0).getTitle();
			Calendar cal = Calendar.getInstance();
			int nId = cal.get(Calendar.DATE);
			int aoId = nAo.getId();
			int notifyId = nId + aoId + 1; 
			showNotification(notifyId, title, content, ctx, noList.get(0), stockObj);		
		}
	}
	
	private static void showNotification(int notifyId, String title, String content, Context context,
			NewsObject newsObj, StockObject stockObj){
		int mNotifyId = notifyId;
		String mTitle = title;
		String mContent = content;
		Context mCtx = context;
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(mTitle).setContentText(mContent);
		
		Intent resultIntent = new Intent(mCtx, TransactionDetailsFragmentActivity.class);
		resultIntent.putExtra(Constants.KEY_TAB_POSITION, TransactionDetailsFragmentActivity.TAB_POSITION_NEWS);
		resultIntent.putExtra(Constants.KEY_FROM, Constants.FROM_FIND_STOCKS);
		resultIntent.putExtra(Constants.KEY_SYMBOL, stockObj.getSymbol());
		resultIntent.putExtra(Constants.KEY_COMPANY_NAME, stockObj.getName());
		resultIntent.putExtra(Constants.KEY_EXCH, stockObj.getExch());
		resultIntent.putExtra(Constants.KEY_TYPE, stockObj.getType());
		resultIntent.putExtra(Constants.KEY_TYPE_DISP, stockObj.getTypeDisp());
		resultIntent.putExtra(Constants.KEY_EXCH_DISP, stockObj.getExchDisp());
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
		stackBuilder.addParentStack(MainFragmentActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		//PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(mCtx, mNotifyId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
