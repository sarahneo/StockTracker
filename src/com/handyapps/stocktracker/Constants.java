package com.handyapps.stocktracker;

import com.handyapps.library.store.IStore.STORE;


public class Constants {

	public static final int PURGE_CHART_OLDER_DAYS = 2;
	public static final int NUMBER_OF_DECIMALS = 2;
	public static final String DATE_FORMAT = "dd-MMM-yyyy";
	public static final String URLPrefix = "http://www.google.com/gwt/x?u=";
    public static final String URLNoImg = "&noimg=1";
	
	public static final String GOOGLE_PLAY_MORE_APPS = "market://search?q=pub:\"Handy+Apps\"";
	public static final String STOCK_TRACKERS_URL = "https://play.google.com/store/apps/developer?id=Handy+Apps+Inc.#?";
	
	public static final String STOCKTACKER_DIRECTORY = "/StockTracker_X";
	public static final String STOCKTACKER_CHART_DIRECTORY = "/StockTracker_X/Charts";
	public static final String STOCKTACKER_NOMEDIA_FILE = "/StockTracker_X/Charts/.nomedia";
	
	public static final String DIALOG_ADD_PORTFOLIO = "DIALOG_ADD_PORTFOLIO";
	public static final String DIALOG_ADD_WATCHLIST = "DIALOG_ADD_WATCHLIST";
	public static final String DIALOG_ADD_CASH_TXN_S = "DIALOG_ADD_CASH_TXN";
	public static final String DIALOG_EDIT_CASH_TXN_S = "DIALOG_EDIT_CASH_TXN_S";
	public static final String DIALOG_SORT_BY_S = "DIALOG_SORT_BY";
	public static final String DIALOG_TIME_PICKER = "DIALOG_TIME_PICKER";
	public static final String DIALOG_DATE_PICKER = "DIALOG_DATE_PICKER";
	public static final String DIALOG_WRONG_CURRENCY = "DIALOG_WRONG_CURRENCY";
	public static final String DIALOG_CSV_ERROR = "DIALOG_CSV_ERROR";
	public static final int DIALOG_ADD_CASH_TXN_I = 56;
	public static final int DIALOG_SORT_BY_I = 57;
	
	public static final String KEY_SYMBOL = "KEY_SYMBOL";
	public static final String KEY_COMPANY_NAME = "KEY_COMPANY_NAME";
	public static final String KEY_EXCH = "KEY_EXCH";
	public static final String KEY_TYPE = "KEY_TYPE";
	public static final String KEY_TYPE_DISP = "KEY_TYPE_DISP";
	public static final String KEY_EXCH_DISP = "KEY_EXCH_DISP";
	public static final String KEY_NEWS_URL = "KEY_NEWS_URL";
	public static final String KEY_LAST_TRADE_PRICE = "KEY_LAST_TRADE_PRICE";
	public static final String KEY_CHANGE = "KEY_CHANGE";
	public static final String KEY_CHANGE_IN_PERCENT = "KEY_CHANGE_IN_PERCENT";
	public static final String KEY_CURRENCY = "KEY_CURRENCY";
	public static final String KEY_FILTER_PORTFOLIO_ID = "KEY_FILTER_PORTFOLIO_ID"; // INT
	public static final String KEY_FILTER_PORTFOLIOS_PAGER = "KEY_FILTER_PORTFOLIOS_LIST_PAGER"; // boolean
	public static final String KEY_UPDATE_PORTFOLIOS_PAGER = "KEY_UPDATE_PORTFOLIOS_LIST_PAGER"; // boolean
	public static final String KEY_UPDATE_WATCHLISTS_PAGER = "KEY_UPDATE_WATCHLISTS_PAGER"; // boolean
	public static final String KEY_SELECTED_PORTFOLIO_ID = "KEY_SELECTED_PORTFOLIO_ID"; // INT
	public static final String KEY_SELECTED_PORTFOLIO_NAME = "KEY_SELECTED_PORTFOLIO_NAME"; // String
	public static final String KEY_CURRENT_ITEM_POSITION = "KEY_CURRENT_ITEM_POSITION"; // INT
	public static final String KEY_ADD_STOCK_IN_SINGLE_PORTFOLIO = "KEY_ADD_STOCK_IN_SINGLE_PORTFOLIO_FRAGMENT"; //boolean
	public static final String KEY_ADD_STOCK_IN_SINGLE_WATCHLIST = "KEY_ADD_STOCK_IN_SINGLE_FRAGMENT"; //boolean
	public static final String KEY_PORTFOLIO_ID = "KEY_PORTFOLIO_ID"; //INT
	public static final String KEY_PORTFOLIO_REFRESH_ID = "KEY_PORTFOLIO_REFRESH_ID"; //INT
	public static final String KEY_PORTFOLIO_LEFT_ID = "KEY_PORTFOLIO_LEFT_ID"; //INT
	public static final String KEY_PORTFOLIO_RIGHT_ID = "KEY_PORTFOLIO_RIGHT_ID"; //INT
	public static final String KEY_WATCHLIST_ID = "KEY_WATCHLIST_ID"; //INT
	public static final String KEY_WATCHLIST_LEFT_ID = "KEY_WATCHLIST_LEFT_ID"; //INT
	public static final String KEY_WATCHLIST_RIGHT_ID = "KEY_WATCHLIST_RIGHT_ID"; //INT
	public static final String KEY_STOCK_ID = "KEY_STOCK_ID";
	public static final String KEY_PORTFOLIO_NAME = "KEY_PORTFOLIO_NAME"; //INT
	public static final String KEY_WATCHLIST_NAME = "KEY_WATCHLIST_NAME"; //INT
	public static final String KEY_THEME_NAME = "KEY_THEME_NAME"; //INT
	public static final String KEY_TAB_POSITION = "KEY_TAB_POSITION";
	public static final String KEY_IS_CHANGE_PORT_NAME = "KEY_IS_CHANGE_PORT_NAME";
	public static final String KEY_IS_UPDATE_CHART = "KEY_IS_UPDATE_CHART";
	public static final String KEY_IS_UPDATE_CHART_AND_QUOTE = "KEY_IS_UPDATE_CHART_AND_QUOTE";
	public static final String KEY_IS_SINGLE_CHART_DONE = "KEY_IS_SINGLE_CHART_DONE";
	public static final String KEY_IS_SINGLE_QUOTE_DONE = "KEY_IS_SINGLE_QUOTE_DONE";
	public static final String KEY_IS_HIDE_CARDS = "KEY_IS_HIDE_CARDS";
	public static final String KEY_IS_CHANGE_TAB = "KEY_CHANGE_TAB";
	public static final String KEY_CHANGE_TAB_TO = "KEY_CHANGE_TAB_TO";
	public static final String KEY_CASH_TRADES_ALL = "KEY_CASH_TRADES_ALL";
	public static final String KEY_FILTER_BY_SYMBOL = "KEY_FILTER_BY_SYMBOL";
	public static final String KEY_IS_NEWS_ALERT = "KEY_IS_NEWS_ALERT";
	public static final String KEY_START_TIME = "KEY_START_TIME";
	public static final String KEY_ALERT_FREQUENCY = "KEY_ALERT_FREQUENCY";
	
	public static final String KEY_TRANSACITON_ROW_ID = "KEY_TRANSACITON_ROW_ID";
	public static final String KEY_TRANSACITON_TXN_ID = "KEY_TRANSACITON_TXN_ID";
	public static final String KEY_DATE_FROM = "KEY_DATE_FROM";
	public static final String KEY_DATE_TO = "KEY_DATE_TO";
	public static final String KEY_DATE_PERIOD_FULL = "KEY_DATE_PERIOD_FULL";
	public static final String KEY_DATE_ALL_TIME = "KEY_DATE_PERIOD_ALL_TIME";
	public static final String KEY_IS_DOWNLOAD_CHART_SERVICE_STARTED = "KEY_IS_DOWNLOAD_CHART_SERVICE_STARTED";
	public static final String KEY_IS_UPDATING_QUOTE_STARTED = "KEY_IS_UPDATING_QUOTE_STARTED";
	public static final String KEY_IS_UPDATE_QUOTE_ALL_TICKERS = "KEY_IS_UPDATE_QUOTE_ALL_TICKERS";
	public static final String KEY_IS_UPDATE_NEWS = "KEY_IS_UPDATE_NEWS";
	public static final String KEY_UPPER_PRICE66 = "KEY_UPPER_PRICE";
	public static final String KEY_LOWER_PRICE66 = "KEY_LOWER_PRICE";
	public static final String KEY_IS_EDIT_ALERT = "KEY_IS_EDIT_ALERT";
	public static final String KEY_BUNDLE_FOR_INTENT = "KEY_BUNDLE_FOR_INTENT";
	public static final String KEY_IS_START_QUOTE_SERVICE = "KEY_IS_START_QUOTE_SERVICE";
	public static final String KEY_IS_QUOTE_OK = "KEY_IS_QUOTE_OK";
	public static final String KEY_WIDGET_ID = "KEY_WIDGET_ID";
	public static final String KEY_IS_WIDGET_PORTFOLIO = "KEY_IS_WIDGET_PORTFOLIO";
	public static final String KEY_IS_WIDGET_WATCHLIST = "KEY_IS_WIDGET_WATCHLIST";
	public static final String KEY_IS_WIDGET_NEWS = "KEY_IS_WIDGET_NEWS";
	public static final String KEY_IS_WIDGET_WATCHLIST_NO_HC = "KEY_IS_WIDGET_WATCHLIST_NO_HC";
	public static final String KEY_IS_WIDGET_PORTFOLIO_NO_HC = "KEY_IS_WIDGET_PORTFOLIO_NO_HC";
	public static final String KEY_IS_WIDGET_NEWS_NO_HC = "KEY_IS_WIDGET_NEWS_NO_HC";
	public static final String KEY_IS_WIDGET_LEFT_PRESSED = "KEY_IS_WIDGET_LEFT_PRESSED";
	public static final String KEY_IS_WIDGET_RIGHT_PRESSED = "KEY_IS_WIDGET_RIGHT_PRESSED";
	public static final String KEY_IS_WIDGET_REFRESH_PRESSED = "KEY_IS_WIDGET_REFRESH_PRESSED";
	
	public static final int KEY_CASH = 0;
	public static final int KEY_TRADES = 1;
	public static final int KEY_ALL = 2;
	
	/**
	 * MAIN SWITCHER
	 */
	public static final String KEY_FROM = "KEY_FROM";
	public static final int FROM_FIND_STOCKS = 0;
	public static final int FROM_PORTFOLIO_LIST = 1;
	public static final int FROM_WATCH_LIST = 2;
	public static final int FROM_EDIT_TRANSACTION = 3;
	public static final int FROM_ADD_ALERT_DIALOG = 4;
	public static final int FROM_WIDGET = 5;
	public static final int FROM_MAIN_ACTIVITY = 6;
	public static final int FROM_POSITIONS_TAB = 7;
	public static final int FROM_ADD_TRADE = 8;
	public static final int FROM_ALERTS = 9;
	public static final int FROM_TRANSACTIONS_ACTIVITY = 10;
	public static final int FROM_ADD_NEWS_ALERT = 11;
	
	public static final int TO_SUMMARY_FRAGMENT = 0;
	public static final int TO_NEWS_FRAGMENT = 1;
	public static final int TO_MY_TRADES_FRAGMENT = 2;
	
	/**
	 * QuickAction:
	 */
	public static final int QUICK_ACTION_ID_VIEW = 1;
	public static final int QUICK_ACTION_ID_EDIT = 2;
	public static final int QUICK_ACTION_ID_DELETE = 3;
	public static final int QUICK_ACTION_ID_ADD_TRADE = 4;
	public static final int QUICK_ACTION_ID_SET_ALERT = 5;
	
	public static final int QUICK_ACTION_ID_VIEW_STOCK_DETAILS = 6;
	public static final int QUICK_ACTION_ID_VIEW_PRICE_CHART = 7;
	public static final int QUICK_ACTION_ID_VIEW_LATEST_NEWS = 8;
	public static final int QUICK_ACTION_ID_ADD_TO_WATCHLIST = 9;
	public static final int QUICK_ACTION_ID_ADD_TO_PORTFOLIO = 10;
	
	public static final int QUICK_ACTION_ID_VIEW_TRANSACTIONS = 11;
	public static final int QUICK_ACTION_ID_CHANGE_COLOR = 12;
	public static final int QUICK_ACTION_ID_VIEW_DISTRIBUTION = 13;
	
	public static final boolean SP_AUTO_REFRESH_DEFAULT_ON_OFF = true;
	public static final String SP_KEY_IS_FIRST_RUN = "SP_KEY_IS_FIRST_RUN";
	public static final String SP_KEY_IS_APP_FIRST_ALARM_DONE = "SP_KEY_IS_APP_FIRST_ALARM_DONE";
	public static final String SP_KEY_IS_ALARM_STOPPED = "SP_KEY_IS_ALARM_STOPPED";
	public static final String SP_KEY_HAS_AUTO_QUOTE_SERVICE = "SP_KEY_HAS_AUTO_QUOTE_SERVICE";
	public static final String SP_KEY_DEFAULT_STARTING_PAGE = "sp_key_default_staring_page";
	public static final String SP_KEY_AUTO_REFRESH = "sp_key_auto_refresh";
	public static final String SP_KEY_AUTO_ADD_NEWS_ALERT = "sp_key_auto_add_news_alert";
	public static final String SP_KEY_REFRESH_FREQUENCY = "sp_key_refresh_frequency";
	public static final String SP_KEY_APP_THEME = "sp_key_app_theme";
	public static final String SP_KEY_APP_VERSION = "sp_key_app_version";
	public static final String SP_KEY_OTHER_APPLICATIONS = "sp_key_other_applications";
	public static final String SP_KEY_TELL_A_FRIEND = "sp_key_tell_a_friend";
	public static final String SP_KEY_IS_SETTING_ON_CREATE = "sp_key_is_setting_on_crate";
	public static final String SP_KEY_ALERTS_WINDOW = "sp_key_alerts_window";
	public static final String SP_KEY_IS_SET_ALERTS_WINDOW = "sp_key_is_set_alerts_window";
	public static final String SP_KEY_CONFIGURE_PORTFOLIO_WIDGET = "sp_key_configure_portfolio_widget";
	public static final String SP_KEY_CONFIGURE_WATCHLIST_WIDGET = "sp_key_configure_watchlist_widget";
	public static final String SP_KEY_CONFIGURE_WATCHLIST_WIDGET_THEME = "sp_key_configure_watchlist_widget_theme";
	public static final String SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME = "sp_key_configure_portfolio_widget_theme";
	public static final String SP_KEY_IS_YAHOO_SERVICE_DOWN = "SP_KEY_IS_YAHOO_SERVICE_DOWN";
	public static final String SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED = "SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED";
	public static final String SP_KEY_PORTFOLIO_ARRAY_INDEX = "SP_KEY_PORTFOLIO_ARRAY_INDEX";
	public static final String SP_KEY_PORTFOLIO_ID = "SP_KEY_PORTFOLIO_ID";
	public static final String SP_KEY_STOCK_ID = "SP_KEY_STOCK_ID";
	public static final String SP_KEY_INCLUDE_CASH = "SP_KEY_INCLUDE_CASH";
	public static final String SP_KEY_COLOR_CASH = "SP_KEY_COLOR_CASH";
	public static final String SP_KEY_IS_OVERALL_CHECKED = "SP_KEY_IS_OVERALL_CHECKED";
	public static final String SP_KEY_IS_PORTFOLIOS_CHECKED = "SP_KEY_IS_PORTFOLIOS_CHECKED";
	public static final String SP_KEY_CASH_TRADES_ALL = "SP_KEY_CASH_TRADES_ALL";
	public static final String SP_KEY_SORT_TXNS_BY = "SP_KEY_SORT_TXNS_BY";
	
	public static final String ACTION_TRANSACTION_FRAGMENT_ACTIVITY = "com.handyapps.stocktracker.TransactionDetailsFragmentActivity.refresh.activity";
	public static final String ACTION_MAIN_ACTIVITY = "android.intent.action.MAIN";
	public static final String ACTION_FILTER_PORTFOLIO = "android.intent.action.MAIN.refresh";
	public static final String ACTION_PORTFOLIO_LIST_FRAGMENT = "android.intent.action.MAIN.PortfolioListFragment.refresh";
	public static final String ACTION_WATCHLIST_FRAGMENT = "android.intent.action.MAIN.WatchlistFragment.refresh";
	public static final String ACTION_SUMMARY_FRAGMENT = "com.handyapps.stocktracker.summary.fragment.refresh";
	public static final String ACTION_TRANSACTIONS_ACTIVITY = "com.handyapps.stocktracker.TransactionsActivity.refresh";
	public static final String ACTION_PORTFOLIO_INITIAL_PAGER_ACTIVITY = "com.handyapps.stocktracker.PortfolioTitlesInitialPage.refresh";
	public static final String ACTION_WATCHLIST_INITIAL_PAGER_ACTIVITY = "com.handyapps.stocktracker.WatchlistTitlesInitialPage.refresh";
	public static final String ACTION_TRADES_FRAGMENT = "com.handyapps.stocktracker.tradefragemnt.refresh";
	public static final String ACTION_ADD_ALERT_DIALOG_ACTIVITY = "com.handyapps.stocktracker.AddAlertDialogActivity";
	public static final String ACTION_QUOTE_RECEIVER = "com.handyapps.stocktracker.QuoteReceiver";
	public static final String ACTION_UPDATE_WIDGET_INTENT_SERVICE = "com.handyapps.stocktracker.widget.WidgetUpdateIntentService";
	public static final String ACTION_INTENT_SERVICE_UPDATE_QUOTE = "com.handyapps.stocktracker.service.IntentServiceUpdateQuote";
	public static final String ACTION_INTENT_SERVICE_UPDATE_NEWS = "com.handyapps.stocktracker.service.IntentServiceUpdateNews";
	public static final String ACTION_SINGLE_PORT_FRAGMENT_BROADCASE = "com.handyapps.stocktracker.portfragment.";
	public static final String ACTION_SINGLE_WATCH_FRAGMENT_BROADCASE = "com.handyapps.stocktracker.watchfragment.";
	public static final String ACTION_FOR_WATCH_FRAGMENT_BROADCASE = "com.handyapps.stocktracker.watchfragment.";
	public static final String ACTION_ADD_NEW_TRADE = "com.handyapps.stocktracker.AddNewTrade.refresh";
	public static final String ACTION_NEWS_LIST_FRAGMENT = "com.handyapps.stocktracker.newslist_fragment";
	public static final String ACTION_SINGLE_CHART_FRAGMENT = "com.handyapps.stocktracker.single_chart_fragment.";
	public static final String ACTION_WIDGET_RECEIVER_QUOTE_UPDATE = "com.handyapps.stocktracker.widget.WidgetReceiverQuoteUpdate";
	public static final String ACTION_WIDGET_RECEIVER_NEWS_UPDATE = "com.handyapps.stocktracker.widget.WidgetReceiverNewsUpdate";
	public static final String ACTION_RECEIVER_QUOTE_ALL = "com.handyapps.stocktracker.service.ReceiverQuoteAll";
	public static final String ACTION_RECEIVER_UPDATE_NINE_CHARTS = "com.handyapps.stocktracker.service.ReceiverUpdateNineCharts";
	public static final String ACTION_RECEIVER_UPDATE_SINGLE_QUOTE = "com.handyapps.stocktracker.service.ReceiverUpdateSingleQuote";
	public static final String ACTION_MY_BOOT_RECEIVER = "com.handyapps.stocktracker.service.MyBootReceiver";
	public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_RECEIVER_NEWS = "com.handyapps.stocktracker.service.ReceiverNews";
		
	
	public static enum VERSION {
		PRO, TRIAL
	};

	public static VERSION VER = VERSION.TRIAL;

	public static boolean isPro() {
		return VER == VERSION.PRO;
	}
	
	public static STORE APP_STORE= STORE.PLAY;
	public static final String sp_pkg = "com.handyapps.stocktracker";
}
