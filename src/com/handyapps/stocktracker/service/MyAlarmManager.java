package com.handyapps.stocktracker.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.NewsAlertObject;

public class MyAlarmManager {

	private Context ctx;
	private SharedPreferences sp;
	private String[] arrRefreshFrequncyValues;
	public static final int REQUEST_CODE_PENDING_INTENT = 987650000;
	public static final int REQUEST_CODE_PENDING_INTENT_CHART = 987650011;
	public static final int REQUEST_CODE_PENDING_INTENT_SINGLE_QUOTE = 987650022;

	public MyAlarmManager(Context context) {
		this.ctx = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		this.arrRefreshFrequncyValues = ctx.getResources().getStringArray(R.array.sp_refresh_frequency_values);
	}

	public void setAlarm() {

		boolean isAppFirstAlarmDone = sp.getBoolean(Constants.SP_KEY_IS_APP_FIRST_ALARM_DONE, false);
		boolean isAutoRefreshOn = getAutoRefreshSettings();
		boolean isAlarmStoped = sp.getBoolean(Constants.SP_KEY_IS_ALARM_STOPPED, false);
		boolean isSingleQuoteUpdateStarted = sp.getBoolean(Constants.SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED, false);
		List<NewsAlertObject> naoList = DbAdapter.getSingleInstance().fetchNewsAlertObjectAll();

		if (isAutoRefreshOn == true && isAppFirstAlarmDone == true) {
			if (isAlarmStoped) {
				startNewAlarm();
				for (NewsAlertObject nao : naoList) {
					startNewNewsAlarm(nao);
				}
				sp.edit().putBoolean(Constants.SP_KEY_IS_ALARM_STOPPED, false).commit();
			} else {
				cancelPreviousAlarm();
				startNewAlarm();
				for (NewsAlertObject nao : naoList) {
					stopNewsAlarm(nao);
					startNewNewsAlarm(nao);
				}
			}
		} else if (isAutoRefreshOn == true && isAppFirstAlarmDone == false) {
			startNewAlarm();
			for (NewsAlertObject nao : naoList) {
				startNewNewsAlarm(nao);
			}
			sp.edit().putBoolean(Constants.SP_KEY_IS_APP_FIRST_ALARM_DONE, true).commit();
		} else if (!isAutoRefreshOn) {
			cancelPreviousAlarm();
			for (NewsAlertObject nao : naoList) {
				stopNewsAlarm(nao);
			}
			sp.edit().putBoolean(Constants.SP_KEY_IS_ALARM_STOPPED, true).commit();
			
			//this is for TransactionActivity:(used for the symbol may not be in watchlist or portfolio, and quote can be auto updated)
			if(isSingleQuoteUpdateStarted){
				stopRepeatAlarmUpdateSingleQuote("");
				sp.edit().putBoolean(Constants.SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED, false).commit();
			}
		}
	}

	private boolean getAutoRefreshSettings() {
		boolean isAutoRefreshOn = sp.getBoolean(Constants.SP_KEY_AUTO_REFRESH, Constants.SP_AUTO_REFRESH_DEFAULT_ON_OFF);
		return isAutoRefreshOn;
	}

	private void startNewAlarm() {
		long interval = getDelay();
		long tiggerTime = System.currentTimeMillis() + interval;
		startRepeatingAlarm(tiggerTime, interval);
	}
	
	public void startNewNewsAlarm(NewsAlertObject nAo) {			
		Calendar c = Calendar.getInstance();
        //c.add(Calendar.DAY_OF_MONTH, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long midnightYesterday = c.getTimeInMillis();
        
		long newsInterval = getNewsDelay(nAo.getAlertFrequency());
		long newsTriggerTime = nAo.getStartTime();
		if (nAo.getIsNotifyOn() == 1)
			startRepeatingNewsAlarm(midnightYesterday + newsTriggerTime, newsInterval, nAo);
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(midnightYesterday + newsTriggerTime);
		
		Log.d("AlarmReceiver", "["+nAo.getSymbol()+"]Set alarmManager.setRepeating to: " + formatter.format(calendar.getTime()));
	}
	
	public void stopNewsAlarm(NewsAlertObject nAo) {			
		Intent intent = new Intent(ctx, ReceiverNews.class);
		PendingIntent sender = PendingIntent.getBroadcast(ctx, nAo.getId(), intent, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
		Log.d("AlarmReceiver", "alarm cancelled");
	}

	private void cancelPreviousAlarm() {
		Intent intent = new Intent(Constants.ACTION_RECEIVER_QUOTE_ALL);// ReceiverQuoteAll.class
		PendingIntent sender = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT, intent, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	private void startRepeatingAlarm(long triggerTime, long interval) {
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ctx, ReceiverQuoteAll.class);
		intent.setAction(Constants.ACTION_RECEIVER_QUOTE_ALL);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pi);
	}
	
	private void startRepeatingNewsAlarm(long triggerTime, long interval, NewsAlertObject nAo) {
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ctx, ReceiverNews.class);// ReceiverNews.class
		intent.setAction(Constants.ACTION_RECEIVER_NEWS);
		Bundle args = new Bundle();
		args.putString(Constants.KEY_SYMBOL, nAo.getSymbol());
		intent.putExtras(args);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, nAo.getId(), intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pi);
	}

	private long getDelay() {
		// By default for 5mins:
		String sValue = sp.getString(Constants.SP_KEY_REFRESH_FREQUENCY, arrRefreshFrequncyValues[1]);
		int delayMins = Integer.valueOf(sValue);
		long mDelay = delayMins * 60000;
		return mDelay;
	}
	
	private long getNewsDelay(int min) {
		long mDelay = min * 60000;
		return mDelay;
	}

	// **For start update nine-chart:(only the activity onCreate)
	public void startRepeatAlarmUpdateChart(String symbol) {
		String mSymbol = symbol;
		boolean isAutoRefreshOn = sp.getBoolean(Constants.SP_KEY_AUTO_REFRESH, Constants.SP_AUTO_REFRESH_DEFAULT_ON_OFF);
		if (isAutoRefreshOn) {
			long interval = getDelay();
			long tiggerTime = System.currentTimeMillis() + interval;
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			Intent mIntent = new Intent(Constants.ACTION_RECEIVER_UPDATE_NINE_CHARTS);
			mIntent.putExtra(Constants.KEY_SYMBOL, mSymbol);
			PendingIntent mPi = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT_CHART, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.setRepeating(AlarmManager.RTC_WAKEUP, tiggerTime, interval, mPi);
		}
	}

	// **For stop update nine-chart:(only the activity onDestroyed)
	public void stopRepeatAlarmUpdateChart(String symbol) {
		String mSymbol = symbol;
		Intent mIntent = new Intent(Constants.ACTION_RECEIVER_UPDATE_NINE_CHARTS);
		mIntent.putExtra(Constants.KEY_SYMBOL, mSymbol);
		PendingIntent mSender = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT_CHART, mIntent, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(mSender);
	}

	// **For start update single quote:(only the activity onCreate)
	public void startRepeatAlarmUpdateSingleQuote(String symbol) {
		String mSymbol = symbol;
		boolean isAutoRefreshOn = sp.getBoolean(Constants.SP_KEY_AUTO_REFRESH, Constants.SP_AUTO_REFRESH_DEFAULT_ON_OFF);
		if (isAutoRefreshOn) {
			long interval = getDelay();
			long tiggerTime = System.currentTimeMillis() + interval;
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			Intent mIntent = new Intent(Constants.ACTION_RECEIVER_UPDATE_SINGLE_QUOTE);
			mIntent.putExtra(Constants.KEY_SYMBOL, mSymbol);
			PendingIntent mPi = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT_SINGLE_QUOTE, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.setRepeating(AlarmManager.RTC_WAKEUP, tiggerTime, interval, mPi);
		}
	}

	// **For stop update single quote:(only the activity onDestroyed)
	public void stopRepeatAlarmUpdateSingleQuote(String symbol) {
		String mSymbol = symbol;
		Intent mIntent = new Intent(Constants.ACTION_RECEIVER_UPDATE_SINGLE_QUOTE);
		mIntent.putExtra(Constants.KEY_SYMBOL, mSymbol);
		PendingIntent mSender = PendingIntent.getBroadcast(ctx, REQUEST_CODE_PENDING_INTENT_SINGLE_QUOTE, mIntent, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(mSender);
	}
	
	public void addNewsAlert(String symbol) {
		boolean isAutoAdd = sp.getBoolean(Constants.SP_KEY_AUTO_ADD_NEWS_ALERT, true);
		if (isAutoAdd) {
			long lStartTime = 8*60*60*1000;				
			NewsAlertObject mAo = DbAdapter.getSingleInstance().fetchNewsAlertObjectBySymbol(symbol);

			if (mAo == null) {
				mAo = new NewsAlertObject();
				mAo.setAlertFrequency(60);
				mAo.setStartTime(lStartTime);
				mAo.setSymbol(symbol);
				mAo.setIsNotifyOn(AlertObject.DO_NOTIFY);
				boolean isDone = mAo.insert();
				if (isDone) {
					Log.i("isDone", "isDone");
				}
			}
		}
	}
}