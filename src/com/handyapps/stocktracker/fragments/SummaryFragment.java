package com.handyapps.stocktracker.fragments;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.ChartTitlesInitialPage;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.NewsWebViewActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.ChartManager;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.task.UpdateChartTaskOneDay;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public final class SummaryFragment extends Fragment implements OnClickListener {

	private int fromId;
	private int mStockId;
	private int winWidth;	
	private ArrayList<NewsObject> mNewsList;
	private static final String KEY_BUNDLE_SUMMARY = "KEY_BUNDLE_SUMMARY";
	private static final String KEY_SYMBOL = "KEY_SYMBOL";
	private static final String KEY_STOCK_ID = "KEY_STOCK_ID";

	private TextView tvPrevClose;
	private TextView tvOpen;
	private TextView tvBid;
	private TextView tvAsk;
	private TextView tvYearTargetEst;
	private TextView tvBeta;
	//private TextView tvNextEarningDate;
	private TextView tvDayRange;
	private TextView tvYearRange;
	private TextView tvVolume;
	private TextView tvAvgVol;
	private TextView tvMarketCap;
	private TextView tvPriceEarningRatio;
	private TextView tvEarningPerShare;
	private TextView tvDivAndYield;
	private TextView tvFirstTitle;
	private TextView tvSecondTitle;
	private TextView tvThirdTitle;
	private TextView tvFirstDesc;
	private TextView tvSecondDesc;
	private TextView tvThirdDesc;
	private TextView tvFirstTimestamp;
	private TextView tvSecondTimestamp;
	private TextView tvThirdTimestamp;

	private RelativeLayout rlFirstRow;
	private RelativeLayout rlSecondRow;
	private RelativeLayout rlThirdRow;

	private ProgressBar pb;
	private QuoteNewsTask quoteNewsTask;
	private ImageView ivChart;
	private Resources res;
	private Bundle bundle;
	private SharedPreferences sp;
	private StockObject so;
	private MyBroadcastReceiver receiver;
	private UpdateQuoteTaskSingleSymbol quoteTask = null;
	private UpdateChartTaskOneDay chartTaskOneDay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getActivity().getResources();
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// Handle Bundle:
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			fromId = bundle.getInt(Constants.KEY_FROM);
		}
		if (savedInstanceState != null) {
			bundle = savedInstanceState.getBundle(KEY_BUNDLE_SUMMARY);
			fromId = bundle.getInt(Constants.KEY_FROM);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerBroadcast();
		
		mStockId = sp.getInt(Constants.SP_KEY_STOCK_ID, -1);
		if (mStockId != -1)
			so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
		else {
			switch (fromId) {
	
			case Constants.FROM_FIND_STOCKS:
				findStockSetup(bundle);
				break;
			case Constants.FROM_PORTFOLIO_LIST:
				watchlistOrPortfolioSetup(bundle);
				break;
			case Constants.FROM_WATCH_LIST:
				watchlistOrPortfolioSetup(bundle);
				break;
			}
		}

		chartSetUp(ChartManager.chartDateRanges[0], so.getSymbol());
		quoteSetUp(so.getSymbol());

		boolean hasConn = NetworkConnectivity.hasNetworkConnection(getActivity());
		if (hasConn) {
			updateQuoteAndChartFromYahoo();
			quoteNews(so.getSymbol());
		}
		
		/*
		 * String strRFC3339Time = "2013-04-07T08:14:38Z"; TimeZone tz =
		 * TimeZone.getTimeZone("UTC"); SimpleDateFormat sdf = new
		 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); sdf.setTimeZone(tz);
		 * 
		 * final Calendar cal = Calendar.getInstance(tz); try {
		 * cal.setTime(sdf.parse(strRFC3339Time)); } catch (ParseException e) {
		 * } final Calendar c =
		 * Calendar.getInstance(TimeZone.getTimeZone("GMT-04:00"));
		 * c.setTimeInMillis(cal.getTimeInMillis());
		 * System.out.println(c.getTime());
		 */
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			getActivity().unregisterReceiver(receiver);
    	} catch (IllegalArgumentException e) {
    	}
	}

	private void updateQuoteAndChartFromYahoo() {

		getActivity().setProgressBarIndeterminateVisibility(true);

		if (quoteTask == null) {
			quoteTask = new UpdateQuoteTaskSingleSymbol(getActivity(), null, 0, 0);
			quoteTask.execute(so.getSymbol());
		} else {
			quoteTask.cancel(true);
			quoteTask = null;
			quoteTask = new UpdateQuoteTaskSingleSymbol(getActivity(), null, 0, 0);
			quoteTask.execute(so.getSymbol());
		}
	}
	
	private void updateChartFromYahoo(String symbol) {

		getActivity().setProgressBarIndeterminateVisibility(true);

		if (chartTaskOneDay == null) {
			chartTaskOneDay = new UpdateChartTaskOneDay(getActivity());
			chartTaskOneDay.execute(symbol);
		} else {
			chartTaskOneDay.cancel(true);
			chartTaskOneDay = null;
			chartTaskOneDay = new UpdateChartTaskOneDay(getActivity());
			chartTaskOneDay.execute(symbol);
		}
	}
	
	private void quoteNews(String sym) {
		boolean hasNetConn = NetworkConnectivity.hasNetworkConnection(getActivity());
		if (hasNetConn) {
			quoteNewsTask(sym);
		} else {
			if(quoteNewsTask != null){
				quoteNewsTask.cancel(true);
			}		
			pb.setVisibility(View.INVISIBLE);
			//lvExp.setVisibility(View.VISIBLE);
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}
	
	private void quoteNewsTask(String sym) {
		getActivity().setProgressBarIndeterminateVisibility(true);
		pb.setVisibility(View.VISIBLE);
		//lvExp.setVisibility(View.INVISIBLE);
		
		if (quoteNewsTask == null) {
			quoteNewsTask = new QuoteNewsTask();
			quoteNewsTask.execute(sym);
		} else {
			quoteNewsTask.cancel(true);
			quoteNewsTask = null;
			quoteNewsTask = new QuoteNewsTask();
			quoteNewsTask.execute(sym);
		}
	}
	
	public class QuoteNewsTask extends AsyncTask<String, Void, ArrayList<NewsObject>> {
		private String searchSymbol;
		private ArrayList<NewsObject> newsList;

		public QuoteNewsTask() {
			newsList = new ArrayList<NewsObject>();
		}

		@Override
		protected ArrayList<NewsObject> doInBackground(String... params) {

			if (params[0] != null && !params[0].equals("")) {
				this.searchSymbol = params[0];
	
				try {
					if (!isCancelled()) {
						newsList = new ArrayList<NewsObject>();
						newsList = quoteNewsList(searchSymbol);

					} else {
						return null;
					}
				} catch (Exception e) {
					return null;
				}
			}
	
			return newsList;
		}


		@Override
		protected void onPostExecute(ArrayList<NewsObject> mList) {
			super.onPostExecute(mList);
						
			if (!mList.isEmpty() && mList.size() >= 3 ) {
				mNewsList = new ArrayList<NewsObject> ();
				mNewsList.addAll(mList);
			
				if (mList.get(0).getDescription().equals("")) {
					tvFirstDesc.setVisibility(View.GONE);
				} else {
					tvFirstDesc.setVisibility(View.VISIBLE);				
				}
				tvFirstTitle.setText(mList.get(0).getTitle());
				tvFirstDesc.setText(mList.get(0).getDescription());
				tvFirstTimestamp.setText(mList.get(0).getPubDate());
				
				
				if (mList.get(1).getDescription().equals("")) {
					tvSecondDesc.setVisibility(View.GONE);
				} else {
					tvSecondDesc.setVisibility(View.VISIBLE);
				}
				tvSecondTitle.setText(mList.get(1).getTitle());
				tvSecondDesc.setText(mList.get(1).getDescription());
				tvSecondTimestamp.setText(mList.get(1).getPubDate());
				
				
				if (mList.get(2).getDescription().equals("")) {
					tvThirdDesc.setVisibility(View.GONE);
				} else {
					tvThirdDesc.setVisibility(View.VISIBLE);
				}
				tvThirdTitle.setText(mList.get(2).getTitle());
				tvThirdDesc.setText(mList.get(2).getDescription());
				tvThirdTimestamp.setText(mList.get(2).getPubDate());
			}
			
			//lvExp.setVisibility(View.VISIBLE);

			if (getActivity() != null) {
				pb.setVisibility(View.INVISIBLE);
				getActivity().setProgressBarIndeterminateVisibility(false);
			}
		}


		private ArrayList<NewsObject> quoteNewsList(String mSymbol) {

			HttpURLConnection httpConnection;
			String query = mSymbol;
			String urlNews = String.format("http://finance.yahoo.com/rss/headline?s=%s", query);
			ArrayList<NewsObject> listObj = new ArrayList<NewsObject>();
			try {
				URL url = new URL(urlNews);

				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestMethod("GET");
				httpConnection.setDoInput(true);
				httpConnection.setRequestProperty("Connection", "close");
				httpConnection.setInstanceFollowRedirects(true);
				httpConnection.setUseCaches(false);
				httpConnection.connect();

				InputStream in = new BufferedInputStream(httpConnection.getInputStream());

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(in);
				Element docEle = dom.getDocumentElement();

				NodeList nl = docEle.getElementsByTagName("item");

				if ((nl != null) && (nl.getLength() > 0)) {
					for (int i = 0; i < nl.getLength(); i++) {
						listObj.add(dissectNode(nl, i));
					}

					if (listObj.size() > 0) {
						return listObj;
					}
				}

			} catch (IOException e) {
				return null;

			} catch (ParserConfigurationException e) {
				return null;

			} catch (SAXException e) {
				return null;
			}
			return null;
		
		}

		private NewsObject dissectNode(NodeList nl, int i) {

			String title = "";
			String description = "";
			String pubDate = "";
			String link = "";
			
			Element entry = (Element) nl.item(i);
			Element eTitle = (Element) entry.getElementsByTagName("title").item(0);
			Element eDescription = (Element) entry.getElementsByTagName("description").item(0);
			Element ePubDate = (Element) entry.getElementsByTagName("pubDate").item(0);
			Element eLink = (Element) entry.getElementsByTagName("link").item(0);

			if (eTitle != null) {
				try {
					Node node = eTitle.getFirstChild();
					if (node != null) {
						title = node.getNodeValue();
					}
				} catch (DOMException e) {
				}
			}

			if (eDescription != null) {
				try {

					Node node = eDescription.getFirstChild();

					if (node != null) {
						description = node.getNodeValue();
					}

				} catch (DOMException e) {
				}
			}

			if (ePubDate != null) {
				try {
					Node node = ePubDate.getFirstChild();
					if (node != null) {
						pubDate = node.getNodeValue();
					}
				} catch (DOMException e) {
				}
			}

			if (eLink != null) {
				try {
					Node node = eLink.getFirstChild();
					if (node != null) {
						link = node.getNodeValue();
					}

				} catch (DOMException e) {
				}
			}
			
			NewsObject obj = new NewsObject();
			obj.setTitle(title);
			obj.setDescription(description);
			obj.setPubDate(pubDate);
			obj.setLink(link);
			return obj;
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_BUNDLE_SUMMARY, bundle);
		if (so != null)
			outState.putString(KEY_SYMBOL, so.getSymbol());
		outState.putInt(KEY_STOCK_ID, mStockId);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			String symbol = savedInstanceState.getString(KEY_SYMBOL);
			so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
			mStockId = savedInstanceState.getInt(Constants.KEY_STOCK_ID);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (quoteTask != null) {
			quoteTask.cancel(true);
		}
		if (chartTaskOneDay != null) {
			chartTaskOneDay.cancel(true);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	private void watchlistOrPortfolioSetup(Bundle mBundle) {
		int mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
		so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.summary_fragment, container, false);
		ivChart = (ImageView) view.findViewById(R.id.iv_summary_chart);

		pb = (ProgressBar) view.findViewById(R.id.pb_detail_loading);
		pb.setVisibility(View.INVISIBLE);		

		tvPrevClose = (TextView) view.findViewById(R.id.tv_prev_close_price);
		tvOpen = (TextView) view.findViewById(R.id.tv_open_price);
		tvBid = (TextView) view.findViewById(R.id.tv_bit_price);
		tvAsk = (TextView) view.findViewById(R.id.tv_ask_price);
		tvYearTargetEst = (TextView) view.findViewById(R.id.tv_year_targe_price);
		tvBeta = (TextView) view.findViewById(R.id.tv_beta_number);
		//tvNextEarningDate = (TextView) view.findViewById(R.id.tv_next_earning_date_txt);
		tvDayRange = (TextView) view.findViewById(R.id.tv_days_range_prices);
		tvYearRange = (TextView) view.findViewById(R.id.tv_year_range_price);
		tvVolume = (TextView) view.findViewById(R.id.tv_valume_number);
		tvAvgVol = (TextView) view.findViewById(R.id.tv_avg_vol_number);
		tvMarketCap = (TextView) view.findViewById(R.id.Tv_market_cap_number);
		tvPriceEarningRatio = (TextView) view.findViewById(R.id.tv_p_e_number);
		tvEarningPerShare = (TextView) view.findViewById(R.id.tv_eps_number);
		tvDivAndYield = (TextView) view.findViewById(R.id.tv_div_yield_txt);
		
		tvFirstTitle = (TextView) view.findViewById(R.id.tv_news_title1);
		tvSecondTitle = (TextView) view.findViewById(R.id.tv_news_title2);
		tvThirdTitle = (TextView) view.findViewById(R.id.tv_news_title3);
		tvFirstDesc = (TextView) view.findViewById(R.id.tv_news_desciption_with_link1);
		tvSecondDesc = (TextView) view.findViewById(R.id.tv_news_desciption_with_link2);
		tvThirdDesc = (TextView) view.findViewById(R.id.tv_news_desciption_with_link3);
		tvFirstTimestamp = (TextView) view.findViewById(R.id.tv_news_pub_date1);
		tvSecondTimestamp = (TextView) view.findViewById(R.id.tv_news_pub_date2);
		tvThirdTimestamp = (TextView) view.findViewById(R.id.tv_news_pub_date3);
		
		rlFirstRow = (RelativeLayout) view.findViewById(R.id.news_first_row);
		rlSecondRow = (RelativeLayout) view.findViewById(R.id.news_second_row);
		rlThirdRow = (RelativeLayout) view.findViewById(R.id.news_third_row);
		rlFirstRow.setOnClickListener(this);
		rlSecondRow.setOnClickListener(this);
		rlThirdRow.setOnClickListener(this);

		ivChart.setOnClickListener(SummaryFragment.this);
		/*btnAddToPortfolio.setOnClickListener(SummaryFragment.this);
		btnAddToWatchlist.setOnClickListener(SummaryFragment.this);*/
		
		mNewsList = new ArrayList<NewsObject>();

		return view;
	}

	public static SummaryFragment newInstance() {
		SummaryFragment detailsFragment = new SummaryFragment();
		return detailsFragment;
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_SUMMARY_FRAGMENT);
		receiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);
	}

	private void findStockSetup(Bundle fBundle) {
		String mSymbol = fBundle.getString(Constants.KEY_SYMBOL);
		String mCompanyName = fBundle.getString(Constants.KEY_COMPANY_NAME);
		String mExch = fBundle.getString(Constants.KEY_EXCH);
		String mType = fBundle.getString(Constants.KEY_TYPE);
		String mTypeDisp = fBundle.getString(Constants.KEY_TYPE_DISP);
		String mExchDisp = fBundle.getString(Constants.KEY_EXCH_DISP);
		so = new StockObject();
		so.setSymbol(mSymbol);
		so.setName(mCompanyName);
		so.setExch(mExch);
		so.setExchDisp(mExchDisp);
		so.setType(mType);
		so.setTypeDisp(mTypeDisp);
	}

	private void quoteSetUp(String symbol) {
		QuoteObject mObject = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
		if (mObject != null) {
			setMyQuotes(mObject);
		}
	}

	private void chartSetUp(String chartrange, String symbol) {

		String rangeOneDay = chartrange;
		String chartName = symbol + rangeOneDay;

		File mFile = ChartManager.getIntenalChart(chartName);
		if (mFile != null) {

			Bitmap mBitmap = getBitmap(mFile);
			int bWidth = mBitmap.getWidth();
			int bHeight = mBitmap.getHeight();
			float aspectRatio = 1;
			if (bWidth > 0)
				aspectRatio = (float)getWinWidth()/(float)bWidth;
			
			int newHeight = (int)(bHeight * aspectRatio);
			mBitmap = Bitmap.createScaledBitmap(mBitmap, winWidth, newHeight, true);

			if (mBitmap != null) {
				ivChart.setImageBitmap(mBitmap);
			}
		}
	}

	private Bitmap getBitmap(File file) {

		File mFile = file;

		String path = mFile.getAbsolutePath();

		Options opt = new BitmapFactory.Options();
		opt.inDither = false; // important
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap mBitmap = BitmapFactory.decodeFile(path, opt);

		// DisplayMetrics metrics =
		// getActivity().getResources().getDisplayMetrics();

		// int screenWidth = metrics.widthPixels;

		// Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,
		// screenWidth/2, true);

		// Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,
		// screenWidth/2, true);

		if (mBitmap != null) {
			return mBitmap;
		}
		return null;
	}

	private void setMyQuotes(QuoteObject obj) {

		String naWithSlash = res.getString(R.string.na_with_slash);
		String naBracesNa = res.getString(R.string.na_braces_na);

		String preClose = obj.getPrevClosePrice();
		String open = obj.getOpenPrice();
		String bid = obj.getBidPrice();
		String ask = obj.getAskPrice();
		String yearTargetEst = obj.getYearTargetEst();
		String beta = obj.getBeta();
		String nextEarningDate = obj.getNextEarningDate();
		String dayRange = obj.getDayRange();
		String yearRange = obj.getYearRange();
		String volume = obj.getVolume();
		String avgVol = obj.getAvgVolume();
		String marketCap = obj.getMarketCap();
		String priceEarnignRatio = obj.getPeRatioTTM();
		String earningPerShare = obj.getEpsTTM();
		String divident = obj.getDividend();
		String dividendYield = obj.getDividendYield();
		String deividentAndYield = "";

		if (preClose.equals("null")) {
			preClose = naWithSlash;
		}
		if (open.equals("null")) {
			open = naWithSlash;
		}
		if (bid.equals("null")) {
			bid = naWithSlash;
		}
		if (ask.equals("null")) {
			ask = naWithSlash;
		}
		if (yearTargetEst.equals("null")) {
			yearTargetEst = naWithSlash;
		}
		if (beta == null) {
			beta = naWithSlash;
		}
		if (nextEarningDate == null) {
			nextEarningDate = naWithSlash;
		}

		if (marketCap.equals("null")) {
			marketCap = naWithSlash;
		}

		if (priceEarnignRatio.equals("null")) {
			priceEarnignRatio = naWithSlash;
		}

		if (divident.equals("null")) {
			divident = naWithSlash;
		}

		if (dividendYield.equals("null")) {
			dividendYield = naWithSlash;
			deividentAndYield = naBracesNa;
		} else {
			deividentAndYield = divident + "(" + dividendYield + "%)";
		}

		tvPrevClose.setText(preClose);
		tvOpen.setText(open);
		tvBid.setText(bid);
		tvAsk.setText(ask);
		tvYearTargetEst.setText(yearTargetEst);
		tvBeta.setText(beta);
		//tvNextEarningDate.setText(nextEarningDate);
		tvDayRange.setText(dayRange);
		tvYearRange.setText(yearRange);
		tvVolume.setText(volume);
		tvAvgVol.setText(avgVol);
		tvMarketCap.setText(marketCap);
		tvPriceEarningRatio.setText(priceEarnignRatio);
		tvEarningPerShare.setText(earningPerShare);
		tvDivAndYield.setText(deividentAndYield);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		NewsObject newsObj = new NewsObject();
		String link = "";

		switch (id) {

		/*case R.id.btn_summary_add_to_portfolio:

			if (so != null) {
				addTickerToPortfolioDialog();
			}
			break;
		case R.id.btn_summary_add_to_watchlist:
			if (so != null) {
				addSymbolToWatchlist();
			}
			break;*/
		case R.id.iv_summary_chart:
			startMyIntentViewEnlargeChart();
			break;
		case R.id.news_first_row:
			if (mNewsList != null && mNewsList.size() >= 1) {
				newsObj = mNewsList.get(0);
				link = newsObj.getLink();
			}

			if (link.length() > 1) {
				Intent i = new Intent(getActivity(), NewsWebViewActivity.class);
				i.putExtra(Constants.KEY_NEWS_URL, link);
				getActivity().startActivity(i);
			}
			break;
		case R.id.news_second_row:
			if (mNewsList != null && mNewsList.size() >= 2) {
				newsObj = mNewsList.get(1);
				link = newsObj.getLink();
			}

			if (link.length() > 1) {
				Intent i = new Intent(getActivity(), NewsWebViewActivity.class);
				i.putExtra(Constants.KEY_NEWS_URL, link);
				getActivity().startActivity(i);
			}
			break;
		case R.id.news_third_row:
			if (mNewsList != null && mNewsList.size() >= 3) {
				newsObj = mNewsList.get(2);
				link = newsObj.getLink();
			}

			if (link.length() > 1) {
				Intent i = new Intent(getActivity(), NewsWebViewActivity.class);
				i.putExtra(Constants.KEY_NEWS_URL, link);
				getActivity().startActivity(i);
			}
			break;

		/*case R.id.tv_chart_to_enlarge:
			startMyIntentViewEnlargeChart();
			break;*/
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		int orientation = newConfig.orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
		    Log.d("tag", "Portrait");
		else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    Log.d("tag", "Landscape");
		    startMyIntentViewEnlargeChart();
		} else
		    Log.w("tag", "other: " + orientation);
	}

	private void startMyIntentViewEnlargeChart() {
		Intent i = new Intent(getActivity(), ChartTitlesInitialPage.class);
		i.putExtra(Constants.KEY_SYMBOL, so.getSymbol());
		getActivity().startActivity(i);
	}

	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private int getWinWidth() {
		Point size = new Point();
		DisplayMetrics metrics = new DisplayMetrics();
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
        	Display display = getActivity().getWindowManager().getDefaultDisplay(); 
        	display.getMetrics(metrics);
        	winWidth = display.getWidth();  // deprecated	        
        }
        else {
	        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
	        Display display = wm.getDefaultDisplay();
	        display.getMetrics(metrics);
	        display.getSize(size);
	        winWidth = size.x;	        
        }		
		
		return winWidth;
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {

			if (intent.getAction().equals(Constants.ACTION_SUMMARY_FRAGMENT) && getActivity() != null) {

				Bundle mBundle = intent.getExtras();

				if (mBundle != null) {
					String mSymbol = mBundle.getString(Constants.KEY_SYMBOL);
					/*if (so != null) {
						_symbol = so.getSymbol();
						if (mSymbol != null && mSymbol.length() > 0 && mSymbol.equals(_symbol)) {

							// chart refresh:
							chartSetUp(ChartManager.chartDateRanges[0]);

							// quote refresh:
							quoteSetUp();
						}
					}*/
					if (mSymbol != null && mSymbol.length() > 0) {
						so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
						// chart refresh:
						chartSetUp(ChartManager.chartDateRanges[0], mSymbol);

						// quote refresh:
						quoteSetUp(mSymbol);
						quoteNews(mSymbol);
						
						boolean isNeedUpdateChart = mBundle.getBoolean(Constants.KEY_IS_UPDATE_CHART, false);
						if (isNeedUpdateChart) {
							boolean hasConn = NetworkConnectivity.hasNetworkConnection(getActivity());
							if (hasConn) {
								updateChartFromYahoo(mSymbol);
							}
						}
					}

					
				}
			}
		}
	}
}
