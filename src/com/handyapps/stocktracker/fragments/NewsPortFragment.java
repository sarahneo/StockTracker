package com.handyapps.stocktracker.fragments;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.ExpandableListView;
import org.holoeverywhere.widget.ExpandableListView.OnChildClickListener;
import org.holoeverywhere.widget.ExpandableListView.OnGroupClickListener;
import org.holoeverywhere.widget.Spinner;
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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.NewsWebViewActivity;
import com.handyapps.stocktracker.adapter.NewsExpandableListAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public final class NewsPortFragment extends Fragment implements OnClickListener, OnFocusChangeListener {

	private int mPortId;
	private int currSpinnerSortByIndex;
	private int currSymIndex;
	private int countSelectionStock = 0;
	private int countSelectionSort = 0;
	private HashMap<String, String> nameSymbolPair;
	private ArrayList<String> symbolList;
	private ArrayList<String> nameList;
	private ArrayList<String> spinnerNameList;
	private ArrayAdapter<String> symbolAdapter;
	private static String allStocks = "";
	private static String latestNews = "";
	private static final int SORT_BY_DATE = 0;
	private static final int SORT_BY_STOCK_NAME = 1;
	private static final int SORT_BY_STOCK_VALUE = 2;
	private static final int ALL_SYMBOLS = -1;

	private QuoteNewsTask quoteNewsTask;
	private ExpandableListView lvExp;
	private NewsExpandableListAdapter listAdapter;
    HashMap<String, List<NewsObject>> listDataChild;
    HashMap<String, List<NewsObject>> originalListDataChild;

	private SharedPreferences sp;
	private ProgressBar pb;
	private Spinner spinnerStock;
	private Spinner spinnerSortBy;
	private ImageButton btnFilter;
	private ImageButton btnCloseSearch;
	private TextView tvNetConn;
	private EditText searchNews;
	private LinearLayout layoutSearch;
	private LinearLayout layoutSpinner;
	private ArrayList<NewsObject> list;
	private MyBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		countSelectionStock = 0;
		countSelectionSort = 0;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.news_port_layout, container, false);
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		Resources res = getActivity().getResources();
		
		allStocks = res.getString(R.string.all_stocks);
		latestNews = res.getString(R.string.latest_news).toUpperCase();

		pb = (ProgressBar) view.findViewById(R.id.pb_news_loading);
		pb.setVisibility(View.INVISIBLE);
		
		nameSymbolPair = new HashMap<String, String>();
		symbolList = new ArrayList<String>();
		spinnerNameList = new ArrayList<String>();
		nameList = new ArrayList<String>();
		//refreshAdapter();
				
		spinnerStock = (Spinner) view.findViewById(R.id.spinner_stock);
		symbolAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item, spinnerNameList);
		symbolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerStock.setAdapter(symbolAdapter);	
		spinnerStock.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				if (countSelectionStock >= 1) {
					String currStock = spinnerNameList.get(arg2);	
					ArrayList<String> copyList = new ArrayList<String>();
					if (!currStock.equals(allStocks)) {
						currSymIndex = arg2;
						copyList.addAll(symbolList);
					} else {
						currSymIndex = ALL_SYMBOLS;
						copyList = sortHelper(currSpinnerSortByIndex);
					}
					
					/*list.clear();
					originalList.clear();				
					newsAdapter.notifyDataSetChanged();*/
					
					nameList.clear();
					listDataChild.clear();
					originalListDataChild.clear();
					listAdapter.notifyDataSetChanged();
	
					quoteNews(copyList);
				}
				countSelectionStock++;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		spinnerSortBy = (Spinner) view.findViewById(R.id.spinner_sort_news_by);
		ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(getActivity(),
		        R.array.spinner_sort_news_by, android.R.layout.simple_spinner_item);
		adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSortBy.setAdapter(adapterSort);
		spinnerSortBy.setSelection(0);
		currSpinnerSortByIndex = SORT_BY_DATE;
		currSymIndex = ALL_SYMBOLS;
		spinnerSortBy.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				if (countSelectionSort >= 1) {
					currSpinnerSortByIndex = arg2;
					ArrayList<String> copyList = new ArrayList<String>();
					copyList = sortHelper(currSpinnerSortByIndex);
	
					list.clear();		
					quoteNews(copyList);
				}
				countSelectionSort++;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		tvNetConn = (TextView) view.findViewById(R.id.tv_no_network_connection);
		tvNetConn.setVisibility(View.INVISIBLE);
		
		layoutSearch = (LinearLayout) view.findViewById(R.id.layout_et_filter_news);
		layoutSearch.setVisibility(View.GONE);
		layoutSpinner = (LinearLayout) view.findViewById(R.id.spinner_layout_news);
		btnFilter = (ImageButton) view.findViewById(R.id.btn_search_stock);
		btnFilter.setOnClickListener(this);
		btnCloseSearch = (ImageButton) view.findViewById(R.id.ib_close_keyboard_news);
		btnCloseSearch.setOnClickListener(this);
		searchNews = (EditText) view.findViewById(R.id.et_filter_news);
		searchNews.setOnFocusChangeListener(this);
				
		list = new ArrayList<NewsObject>();
		
		lvExp = (ExpandableListView) view.findViewById(R.id.lvExp);
		LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.empty_news_elv);
		lvExp.setEmptyView(emptyView);
		lvExp.setOnGroupClickListener(new OnGroupClickListener() {
			  @Override
			  public boolean onGroupClick(ExpandableListView parent, View v,
			                              int groupPosition, long id) { 
			    return true; // This way the expander cannot be collapsed
			  }
			});
		listDataChild = new HashMap<String, List<NewsObject>>();
		originalListDataChild = new HashMap<String, List<NewsObject>>();
		listAdapter = new NewsExpandableListAdapter(getActivity(), nameList, listDataChild, originalListDataChild, false);
		lvExp.setAdapter(listAdapter);
		lvExp.setOnChildClickListener(new OnChildClickListener() {
			 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
            	NewsObject newsObj = listDataChild.get(nameList.get(groupPosition))
            			.get(childPosition);
				String link = newsObj.getLink();

				if (link.length() > 1) {

					Intent i = new Intent(getActivity(), NewsWebViewActivity.class);
					i.putExtra(Constants.KEY_NEWS_URL, link);
					getActivity().startActivity(i);
				}
                return false;
            }
        });
		
		lvExp.setVisibility(View.INVISIBLE);

		return view;
	}
	
	@Override
	public void onClick(View v) {
		 switch(v.getId()) {
			 case R.id.btn_search_stock:
				 layoutSpinner.setVisibility(View.GONE);
				 layoutSearch.setVisibility(View.VISIBLE);
				 searchNews.requestFocus();
				 break;
			 case R.id.ib_close_keyboard_news:
				 layoutSpinner.setVisibility(View.VISIBLE);
				 layoutSearch.setVisibility(View.GONE);
				 searchNews.setText("");
				 searchNews.clearFocus();
				 break;
		 }
		
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.et_filter_news) {
			if (hasFocus)
				showSoftKeyboard();
			else
				hideSoftKeyboard();
		}
	}
	
	private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchNews.getWindowToken(), 0);
	    
	}
	
	
	private void showSoftKeyboard(){
	    if(getActivity().getCurrentFocus()!=null && getActivity().getCurrentFocus() instanceof EditText){
	    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.showSoftInput(searchNews, InputMethodManager.SHOW_IMPLICIT);
	    }
	}
	
	
	private ArrayList<String> sortHelper(int currIndex) {
		ArrayList<String> copyList = new ArrayList<String>();
		copyList.addAll(symbolList);
		if (currIndex == SORT_BY_STOCK_NAME && currSymIndex != ALL_SYMBOLS)
			;
		else if (currIndex == SORT_BY_STOCK_VALUE && currSymIndex != ALL_SYMBOLS)
			;
		else if (currIndex == SORT_BY_STOCK_NAME && !copyList.isEmpty())
			Collections.sort(copyList, new NamesComparator());
		else if (currIndex == SORT_BY_STOCK_VALUE && !copyList.isEmpty()) {
			List<Category> catList = new ArrayList<Category>();
			for (String sym : copyList) {
				Category cate = new Category();
				cate.setSym(sym);
				QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(sym);
				try {
					cate.setValue(Double.parseDouble(qo.getLastTradePrice()));
				} catch (NumberFormatException e) {
					cate.setValue(0);
				}
				catList.add(cate);
			}
			if (!catList.isEmpty()) {
				Collections.sort(catList, new ValuesComparator());
				copyList.clear();
				for (Category cat : catList) {
					copyList.add(cat.getSym());
				}
			}
		}
		
		return copyList;
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void afterTextChanged(Editable s) {
			String search = s.toString();
			listAdapter.getFilter().filter(search);				
		}
	};
	

	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_NEWS_LIST_FRAGMENT);
		filter.addAction(Constants.ACTION_FILTER_PORTFOLIO);
		filter.addAction(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		receiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}

	@Override
	public void onResume() {
		super.onResume();
		searchNews.addTextChangedListener(mTextWatcher);	
		refreshAdapter();
		registerBroadcast();
		
		if (listAdapter != null && listDataChild.size() < 1) {
			if (currSpinnerSortByIndex == SORT_BY_DATE) {
				quoteNews(symbolList);
			}
		}
	}


	private void quoteNews(ArrayList<String> symList) {
		boolean hasNetConn = NetworkConnectivity.hasNetworkConnection(getActivity());
		if (hasNetConn) {
			tvNetConn.setVisibility(View.INVISIBLE);
			lvExp.setVisibility(View.VISIBLE);
			layoutSpinner.setVisibility(View.VISIBLE);
			
			nameList.clear();
			listDataChild.clear();
			originalListDataChild.clear();
			listAdapter.notifyDataSetChanged();
			if (symList != null && currSymIndex == ALL_SYMBOLS) 
				quoteNewsTask(symList);
			else if (symList != null && !symList.isEmpty()) {
				ArrayList<String> singleSymArray = new ArrayList<String>();
				singleSymArray.add(symList.get(currSymIndex));
				quoteNewsTask(singleSymArray);
			}
		} else {
			if(quoteNewsTask != null){
				quoteNewsTask.cancel(true);
			}
			tvNetConn.setVisibility(View.VISIBLE);
			pb.setVisibility(View.INVISIBLE);
			lvExp.setVisibility(View.INVISIBLE);
			layoutSpinner.setVisibility(View.INVISIBLE);
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}

	
	@Override
	public void onPause() {
		super.onPause();
		searchNews.removeTextChangedListener(mTextWatcher);
		searchNews.setText("");
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


	public static NewsPortFragment newInstance() {
		NewsPortFragment newsFragment = new NewsPortFragment();
		return newsFragment;
	}
	
	public class NamesComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			if (currSpinnerSortByIndex == SORT_BY_STOCK_NAME)
				return lhs.compareToIgnoreCase(rhs);
			else
				return 0;
		}
	}
	
	public class MapComparator implements Comparator<String> {
		
		HashMap<String, String> base;
		public MapComparator(HashMap<String, String> base) {
			this.base = base;
		}
		
		@Override
		public int compare(String lhs, String rhs) {
			if (currSpinnerSortByIndex == SORT_BY_STOCK_NAME)
				return base.get(lhs).compareToIgnoreCase(base.get(rhs));
			else
				return 0;
		}
	}
	
	public class ValuesComparator implements Comparator<Category> {
		@Override
		public int compare(Category lhs, Category rhs) {
			if (currSpinnerSortByIndex == SORT_BY_STOCK_VALUE)
				return Double.compare(rhs.getValue(), lhs.getValue());
			else
				return 0;
		}
	}
	
	public class DatesComparator implements Comparator<NewsObject> {
		@Override
		public int compare(NewsObject lhs, NewsObject rhs) {
			if (currSpinnerSortByIndex == SORT_BY_DATE) {
				//Mon, 23 Jun 2014 19:52:00 GMT
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:SS");
	        	try {
					Date date1 = sdf.parse(lhs.getPubDate().substring(5));
					Date date2 = sdf.parse(rhs.getPubDate().substring(5));
					return date2.compareTo(date1);
				} catch (ParseException e) {
					e.printStackTrace();
					return 0;
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					return 0;
				}
			} else
				return 0;
		}
	}
	
	public class Category {

		String sym;
		double value;

		public String getSym() {
			return sym;
		}
		
		public void setSym(String sym) {
			this.sym = sym;
		}
		
		public double getValue() {
			return value;
		}
		
		public void setValue(double value) {
			this.value = value;
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			String action = intent.getAction();
			Bundle bundleIntent = intent.getExtras();

			if (action.equals(Constants.ACTION_NEWS_LIST_FRAGMENT) && getActivity() != null) {
				quoteNews(symbolList);
			} else if (action.equals(Constants.ACTION_FILTER_PORTFOLIO) && getActivity() != null) {
				if (bundleIntent != null) {
					
					boolean isFilterPortList = bundleIntent.getBoolean(Constants.KEY_FILTER_PORTFOLIOS_PAGER);
					boolean isUpdatePort = bundleIntent.getBoolean(Constants.KEY_UPDATE_PORTFOLIOS_PAGER);
					
					if (isFilterPortList || isUpdatePort) {
						mPortId = bundleIntent.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
						if (mPortId != 0) {
							list.clear();
							refreshAdapter();
		
							quoteNews(symbolList);
						}
					} 
				}
			} else if (action.equals(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT) && getActivity() != null) {
				if (bundleIntent != null) {
					
					boolean isUpdatePort = bundleIntent.getBoolean(Constants.KEY_UPDATE_PORTFOLIOS_PAGER);
					
					if (isUpdatePort) {

						list.clear();
						refreshAdapter();
	
						quoteNews(symbolList);
						
					} 
				}
			}
		}
		
	}
	
	private void refreshAdapter() {
		nameSymbolPair.clear();
		symbolList.clear();
		spinnerNameList.clear();
		ArrayList<String> copyList = new ArrayList<String>();
		ArrayList<String> copySpinnerList = new ArrayList<String>();

		if (mPortId == -1) {
			List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockList();
			for (PortfolioStockObject pso : psoList) {
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(pso.getStockId());
				copyList.add(so.getSymbol());
				copySpinnerList.add(so.getName());
				nameSymbolPair.put(so.getName(), so.getSymbol());
				//symbolList.add(so.getSymbol());
				//spinnerNameList.add(so.getName());
			}
		} else {
			List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(mPortId);
			for (PortfolioStockObject pso : psoList) {
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(pso.getStockId());
				copyList.add(so.getSymbol());
				copySpinnerList.add(so.getName());
				nameSymbolPair.put(so.getName(), so.getSymbol());
				//symbolList.add(so.getSymbol());
				//spinnerNameList.add(so.getName());
			}
		}
		
		Collections.sort(copySpinnerList, new NamesComparator());
		Collections.sort(copyList, new NamesComparator());
		
		symbolList.addAll(copyList);
		spinnerNameList.addAll(copySpinnerList);
	
		spinnerNameList.add(allStocks);
		if (symbolAdapter != null)
			symbolAdapter.notifyDataSetChanged();
		
		spinnerStock.setSelection(spinnerNameList.size()-1);
	
	}


	@SuppressWarnings("unchecked")
	private void quoteNewsTask(ArrayList<String> symbols) {
		getActivity().setProgressBarIndeterminateVisibility(true);
		pb.setVisibility(View.VISIBLE);
		lvExp.setVisibility(View.INVISIBLE);
		
		if (quoteNewsTask == null) {
			quoteNewsTask = new QuoteNewsTask();
			quoteNewsTask.execute(symbols);
		} else {
			quoteNewsTask.cancel(true);
			quoteNewsTask = null;
			quoteNewsTask = new QuoteNewsTask();
			quoteNewsTask.execute(symbols);
		}
	}

	
	public class QuoteNewsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<NewsObject>> {
		private String searchSymbol;
		private ArrayList<NewsObject> sortNews;
		private ArrayList<List<NewsObject>> newsListList;
		private ArrayList<String> cardTitles;

		public QuoteNewsTask() {
			sortNews = new ArrayList<NewsObject>();
			newsListList = new ArrayList<List<NewsObject>>();
			cardTitles = new ArrayList<String>();
		}

		@Override
		protected ArrayList<NewsObject> doInBackground(ArrayList<String>... params) {

			ArrayList<NewsObject> mList = null;
			Log.i("test", "params[0].size="+params[0].size());
			for (int i=0; i<params[0].size(); i++) {
				this.searchSymbol = params[0].get(i);
				Log.i("test", "searchSymbol="+searchSymbol);
	
				try {
					if (!isCancelled()) {
						StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(searchSymbol);
						mList = new ArrayList<NewsObject>();
						mList = quoteNewsList(searchSymbol);
	
						if (currSpinnerSortByIndex == SORT_BY_DATE && mList.size() > 0) {
							sortNews.addAll(mList);
						} else if (mList.size() > 0) {			
							//buildCards(mList, stockObj.getName());
							newsListList.add(mList);
							cardTitles.add(stockObj.getName());
						}
					} else {
						return null;
					}
				} catch (Exception e) {
					return null;
				}
			}
	
			return sortNews;
		}
/*		
		private void buildCards(final ArrayList<NewsObject> newsList, final String name) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
	
					
					nameList.add(name);
					listDataChild.put(name, newsList);		
					originalListDataChild.put(name, newsList);
					listAdapter.notifyDataSetChanged();

					Log.i("test", "name="+name);
				}
			});
			
		}*/


		@Override
		protected void onPostExecute(ArrayList<NewsObject> mList) {
			super.onPostExecute(mList);
			
			list.clear();
			nameList.clear();
			listDataChild.clear();
			originalListDataChild.clear();
			
			if (currSpinnerSortByIndex == SORT_BY_DATE && mList != null) {
				Collections.sort(mList, new DatesComparator());
				if (!mList.isEmpty()) {
					list.addAll(mList);
					
					nameList.add(latestNews);
					listDataChild.put(latestNews, list);
					originalListDataChild.put(latestNews, list);
				}
				
				listAdapter.notifyDataSetChanged();
			}
			
			if (!newsListList.isEmpty() && !cardTitles.isEmpty()) {
				for (int i=0; i<cardTitles.size(); i++) {
					nameList.add(cardTitles.get(i));
					listDataChild.put(cardTitles.get(i), newsListList.get(i));		
					originalListDataChild.put(cardTitles.get(i), newsListList.get(i));
				}
				listAdapter.notifyDataSetChanged();
			}
			
			lvExp.setVisibility(View.VISIBLE);

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
						NewsObject newsObj = dissectNode(nl, i);
						newsObj.setSymbol(mSymbol);
						listObj.add(newsObj);
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
