package com.handyapps.stocktracker.fragments;

import java.io.BufferedInputStream;
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
import org.holoeverywhere.widget.ListView;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.NewsWebViewActivity;
import com.handyapps.stocktracker.adapter.NewsAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public final class NewsStocksFragment extends Fragment {

	private int fromId;
	private ArrayList<String> nameList;
	private static final String KEY_SYMBOL_NEWS = "KEY_SYMBOL_NEWS";
	private static final String KEY_BUNDLE_NEWS = "KEY_BUNDLE_NEWS";

	private StockObject so;
	private QuoteNewsTask quoteNewsTask;
	private ListView lvNews;
	private NewsAdapter listAdapter;
    private ArrayList<NewsObject> listDataChild;

	private Bundle bundle;
	private ProgressBar pb;
	private TextView tvNetConn;
	private MyBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Handle Bundle:
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			fromId = bundle.getInt(Constants.KEY_FROM);
		}
		/*if (savedInstanceState != null) {
			bundle = savedInstanceState.getBundle(KEY_BUNDLE_NEWS);
			fromId = bundle.getInt(Constants.KEY_FROM);
		}*/
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.news_stock_layout, container, false);

		pb = (ProgressBar) view.findViewById(R.id.pb_news_loading);
		pb.setVisibility(View.INVISIBLE);
		
		nameList = new ArrayList<String>();

		tvNetConn = (TextView) view.findViewById(R.id.tv_no_network_connection);
		tvNetConn.setVisibility(View.INVISIBLE);
		
		lvNews = (ListView) view.findViewById(R.id.lv_news_stock);
		LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.empty_news_elv);
		View footerView = ((LayoutInflater) getActivity().getApplicationContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shadow_container, null, false);
		lvNews.setEmptyView(emptyView);
		lvNews.addFooterView(footerView);
		
		listDataChild = new ArrayList<NewsObject>();
		listAdapter = new NewsAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, listDataChild);
		lvNews.setAdapter(listAdapter);
		lvNews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NewsObject newsObj = listDataChild.get(position);
				String link = newsObj.getLink();

				if (link.length() > 1) {
					Intent i = new Intent(getActivity(), NewsWebViewActivity.class);
					i.putExtra(Constants.KEY_NEWS_URL, link);
					getActivity().startActivity(i);
				}
			}
        });
		
		lvNews.setVisibility(View.INVISIBLE);

		return view;
	}
	

	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_NEWS_LIST_FRAGMENT);
		receiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}

	@Override
	public void onResume() {
		super.onResume();
		registerBroadcast();
		

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
		
		
		if (listAdapter != null && listDataChild.size() < 1) {
			quoteNews(so.getSymbol());
		}
	}


	private void quoteNews(String sym) {
		boolean hasNetConn = NetworkConnectivity.hasNetworkConnection(getActivity());
		if (hasNetConn) {
			nameList.clear();
			listDataChild.clear();
			listAdapter.notifyDataSetChanged();
			quoteNewsTask(sym);
		} else {
			if(quoteNewsTask != null){
				quoteNewsTask.cancel(true);
			}
			tvNetConn.setVisibility(View.VISIBLE);
			pb.setVisibility(View.INVISIBLE);
			lvNews.setVisibility(View.VISIBLE);
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}

	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (quoteNewsTask != null) {
			quoteNewsTask.cancel(true);
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_BUNDLE_NEWS, bundle);
		if (so != null)
			outState.putString(KEY_SYMBOL_NEWS, so.getSymbol());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			String symbol = savedInstanceState.getString(KEY_SYMBOL_NEWS);
			so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
		}
	}

	public static NewsStocksFragment newInstance() {
		NewsStocksFragment newsFragment = new NewsStocksFragment();
		return newsFragment;
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			String action = intent.getAction();
			Bundle bundleIntent = intent.getExtras();

			if (action.equals(Constants.ACTION_NEWS_LIST_FRAGMENT) && getActivity() != null) {

				if (bundleIntent != null) {
					String mSymbol = bundleIntent.getString(Constants.KEY_SYMBOL);
					String mName = bundleIntent.getString(Constants.KEY_COMPANY_NAME);
					if (mSymbol != null && mSymbol.length() > 0) {
						so = new StockObject();
						so.setSymbol(mSymbol);
						so.setName(mName);
						quoteNews(mSymbol);
					}
				}
			}
		}
		
	}

	private void watchlistOrPortfolioSetup(Bundle mBundle) {
		int mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
		so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
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


	private void quoteNewsTask(String sym) {
		getActivity().setProgressBarIndeterminateVisibility(true);
		pb.setVisibility(View.VISIBLE);
		lvNews.setVisibility(View.INVISIBLE);
		
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

			ArrayList<NewsObject> mList = null;
			if (params[0] != null && !params[0].equals("")) {
				this.searchSymbol = params[0];
	
				try {
					if (!isCancelled()) {
						mList = new ArrayList<NewsObject>();
						mList = quoteNewsList(searchSymbol);
	
						if (mList.size() > 0) {			
							newsList = mList;
						}
					} else {
						return null;
					}
				} catch (Exception e) {
					return null;
				}
			}
	
			return mList;
		}


		@Override
		protected void onPostExecute(ArrayList<NewsObject> mList) {
			super.onPostExecute(mList);
						
			if (!newsList.isEmpty()) {
				nameList.add("");
				listDataChild.addAll(newsList);		
				
				listAdapter.notifyDataSetChanged();
			}
			
			lvNews.setVisibility(View.VISIBLE);

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
}
