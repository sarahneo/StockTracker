package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class WidgetProviderPortfolioNoHc extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_PORTFOLIO_NO_HC = "APP_WIDGET_UPDATE_PORTFOLIO_NO_HC";
	public static final String APP_WIDGET_ID_PORTFOLIO = WidgetProviderPortfolio.APP_WIDGET_ID_PORTFOLIO;
	public static final String APP_WIDGET_STATUS_OLD_PORTFOLIO_NO_HC = WidgetProviderPortfolio.APP_WIDGET_STATUS_OLD_PORTFOLIO;

	public static final int dividerID[] = { R.id.pager_indicator_port_1, R.id.pager_indicator_port_2, R.id.pager_indicator_port_3,
		R.id.pager_indicator_port_4, R.id.pager_indicator_port_5};
	public static final int rowID[] = { R.id.row_body_single_port_1, R.id.row_body_single_port_2, R.id.row_body_single_port_3,
		R.id.row_body_single_port_4, R.id.row_body_single_port_5 };
	public static final int symbolWithExchID[] = { R.id.tv_symbol_with_exch_1, R.id.tv_symbol_with_exch_2, R.id.tv_symbol_with_exch_3,
			R.id.tv_symbol_with_exch_4, R.id.tv_symbol_with_exch_5 };
	public static final int companyNameID[] = { R.id.tv_company_name_1, R.id.tv_company_name_2, R.id.tv_company_name_3, R.id.tv_company_name_4,
			R.id.tv_company_name_5 };
	public static final int lastPriceID[] = { R.id.tv_last_price_1, R.id.tv_last_price_2, R.id.tv_last_price_3, R.id.tv_last_price_4, R.id.tv_last_price_5 };
	public static final int netGainLossID[] = { R.id.tv_net_gain_loss_1, R.id.tv_net_gain_loss_2, R.id.tv_net_gain_loss_3, R.id.tv_net_gain_loss_4,
			R.id.tv_net_gain_loss_5 };
	public static final int colorID[] = { R.id.tv_color_1, R.id.tv_color_2, R.id.tv_color_3, R.id.tv_color_4,
		R.id.tv_color_5 };
	
	private static SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i], false, false, false);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			WidgetUtils.deleteTitlePrefPortfolio(context, appWidgetIds[i]);
		}
	}

	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
			boolean isRefreshButtonPressed, boolean isLeftPressed, boolean isRightPressed) {

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		final Context mContext = context;
		final int mAppWidgetId = appWidgetId;
		final boolean mIsRefreshBtnPressed = isRefreshButtonPressed;		
		final AppWidgetManager mAppWidgetManager = appWidgetManager;

		int portId = 0;
		String theme = "Light";
		if (isLeftPressed) {
			portId = sp.getInt(Constants.KEY_PORTFOLIO_LEFT_ID, 1);
			Log.d("portId", "leftIsPressedCurrentId="+portId);
		} else if (isRightPressed) {
			portId = sp.getInt(Constants.KEY_PORTFOLIO_RIGHT_ID, 1);
			Log.d("portId", "rightIsPressedCurrentId="+portId);
		} else if (isRefreshButtonPressed)
			portId = sp.getInt(Constants.KEY_PORTFOLIO_ID, 0);
		else
			portId = WidgetUtils.loadIntPrefPortfolio(mContext, mAppWidgetId);
		
		theme = WidgetUtils.loadPortfolioTheme(mContext, mAppWidgetId);
		RemoteViews rv = null;
		if (theme.equals("Dark"))
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_portfolio_no_hc_dark);
		else
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_portfolio_no_hc);
		
		if (!(portId == -1)) {
			// 1. get port_name;
			final PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (po != null) {
				final String portName = po.getName();
				rv.setTextViewText(R.id.appwidget_title_port, portName);
				
				// Set last updated time
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a");
				Date now = new Date();
				String strDate = sdf.format(now);
				rv.setTextViewText(R.id.appwidget_last_update_port, "Last updated: " + strDate);

				// 1. view single watchlist:(onClicked:watchlist_title_name)
				final Intent iChangePortfolio = new Intent(mContext, WidgetConfigPrefsActivityPortfolioNoHc.class);
				iChangePortfolio.putExtra(APP_WIDGET_STATUS_OLD_PORTFOLIO_NO_HC, true);
				iChangePortfolio.putExtra(Constants.KEY_PORTFOLIO_NAME, portName);
				iChangePortfolio.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				iChangePortfolio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				final PendingIntent piChangePortfolio = PendingIntent.getActivity(mContext, mAppWidgetId, iChangePortfolio,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.appwidget_title_port, piChangePortfolio);

				// 2. ListView setup:
				final List<PortfolioStockObject> psList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
				if (psList.size() > 0) {

					// **Reset Ui
					rv.setViewVisibility(R.id.layout_listview_port_items, View.VISIBLE);
					rv.setViewVisibility(R.id.empty_widget_port, View.GONE);
					// **Reset Ui

					// **New Ui:
					final int mCount = psList.size();

					for (int i = 0; i < 5; i++) {
						if (i < mCount) {
							
							final int mStockId = psList.get(i).getStockId();
							
							// 0.set color:
							final StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
							rv.setInt(colorID[i], "setBackgroundColor", so.getColorCode());

							// 1.set company:					
							if (so != null) {
								final String mCompanyName = so.getName();
								rv.setTextViewText(companyNameID[i], mCompanyName);
							}

							// 2. get symbol with exchange:							
							final String _sym = so.getSymbol();
							final String _exch = so.getExchDisp();
							final String symExch = _sym + ":" + _exch;
							rv.setTextViewText(symbolWithExchID[i], symExch);

							// 3. get last price:
							final QuoteObject mQuoteObject = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(so.getSymbol());
							final String mLastPrice = mQuoteObject.getLastTradePrice();
							rv.setTextViewText(lastPriceID[i], mLastPrice);

							// 4. get change:
							String _change = mQuoteObject.getChange();
							String _sign = _change.substring(0, 1);
							double dChange = Double.valueOf(_change.substring(1));
							_change = DecimalsConverter.convertToStringValueBaseOnLocale(dChange, Constants.NUMBER_OF_DECIMALS, mContext);
							_change = _sign + _change.substring(1);
							String _changePercent = mQuoteObject.getChangeInPercent();
							if (_changePercent.contains("-"))
								_changePercent = _changePercent.replace("-", "");
							else if (_changePercent.contains("+"))
								_changePercent = _changePercent.replace("+", "");
							
							final String mChange = _change + " (" + _changePercent + ")";
							Spanned spannedChange;
							if (_change.contains("-")) {
								spannedChange = TextColorPicker.getRedText("", mChange);
							} else {
								spannedChange = TextColorPicker.getGreenText("", mChange);
							}

							rv.setTextViewText(netGainLossID[i], spannedChange);
							
							

						} else {
							rv.setViewVisibility(dividerID[i], View. INVISIBLE);
							rv.setViewVisibility(rowID[i], View. INVISIBLE);
						}
					}
				} else {
					rv.setViewVisibility(R.id.layout_listview_port_items, View.GONE);
					rv.setViewVisibility(R.id.empty_widget_port, View.VISIBLE);
				}

				// 3.refresh:(onClicked:refresh_icon)
				final Intent iQuoteUpdates = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iQuoteUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iQuoteUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO_NO_HC, true);
				iQuoteUpdates.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);
				sp.edit().putInt(Constants.KEY_PORTFOLIO_ID, portId).commit();
				final PendingIntent piQuoteUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId, iQuoteUpdates,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_update_widget_port, piQuoteUpdates);

				final Intent iViewStock = new Intent(mContext, MainFragmentActivity.class);
				iViewStock.putExtra(Constants.KEY_TAB_POSITION, Constants.FROM_FIND_STOCKS-1);
				iViewStock.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piViewPortfolio = PendingIntent.getActivity(mContext, mAppWidgetId, iViewStock,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.layout_listview_port, piViewPortfolio);
			} else {
				final String portNameNa = mContext.getResources().getString(R.string.portfolio_na);
				final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
				rv.setTextViewText(R.id.appwidget_title_port, portNameNa);
				rv.setTextViewText(R.id.appwidget_last_update_port, lastUpdateNa);
			}

			// 4.search:(onClicked:search_icon)			
			final Intent iSearch = new Intent(mContext, MainFragmentActivity.class);
			iSearch.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
			iSearch.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
			iSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			final PendingIntent piSearch = PendingIntent.getActivity(mContext, mAppWidgetId, iSearch, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_port, piSearch);
			rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_port, piSearch);
			
			// 5.previous:(onClicked:btn_left_port)
			int prevPortId = WidgetUtils.loadPrevPort(mContext, mAppWidgetId, portId);	
			if (prevPortId > 0) {
				Log.d("portId", "prevPortId="+prevPortId);
				final Intent iLeftPort = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iLeftPort.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iLeftPort.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iLeftPort.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO_NO_HC, true);
				iLeftPort.putExtra(Constants.KEY_PORTFOLIO_LEFT_ID, prevPortId);
				iLeftPort.putExtra(Constants.KEY_IS_WIDGET_LEFT_PRESSED, true);
				sp.edit().putInt(Constants.KEY_PORTFOLIO_LEFT_ID, prevPortId).commit();
				final PendingIntent piLeftPort = PendingIntent.getBroadcast(mContext, mAppWidgetId + 2, iLeftPort, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_left_port, piLeftPort);
			}
			
			// 5.next:(onClicked:btn_right_port)
			int nextPortId = WidgetUtils.loadNextPort(mContext, mAppWidgetId, portId);	
			if (nextPortId > 0) {
				Log.d("portId", "nextPortId="+nextPortId);
				final Intent iRightPort = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iRightPort.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iRightPort.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iRightPort.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO_NO_HC, true);
				iRightPort.putExtra(Constants.KEY_PORTFOLIO_RIGHT_ID, prevPortId);
				iRightPort.putExtra(Constants.KEY_IS_WIDGET_RIGHT_PRESSED, true);
				sp.edit().putInt(Constants.KEY_PORTFOLIO_RIGHT_ID, nextPortId).commit();
				final PendingIntent piRightPort = PendingIntent.getBroadcast(mContext, mAppWidgetId + 1, iRightPort, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_right_port, piRightPort);
			}

			// 6.update:progressbar:
			if (mIsRefreshBtnPressed) {
				rv.setViewVisibility(R.id.layout_pb, View.VISIBLE);
				rv.setViewVisibility(R.id.btn_update_widget_port, View.GONE);
			} else {
				rv.setViewVisibility(R.id.layout_pb, View.GONE);
				rv.setViewVisibility(R.id.btn_update_widget_port, View.VISIBLE);
			}

			Log.d("No_HC_widget_portfolio", "widget_id:" + String.valueOf(mAppWidgetId));
			// 6.call widget manager do update widget.
			mAppWidgetManager.updateAppWidget(mAppWidgetId, rv);
		}
	}
}