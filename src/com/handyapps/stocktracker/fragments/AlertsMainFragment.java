package com.handyapps.stocktracker.fragments;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.RadioButton;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewAlert;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.NewsAlertObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class AlertsMainFragment extends Fragment implements OnClickListener {

	private String na;
	private int currPriceInt;
	private int currNewsInt;
	private boolean isNewsAlert = false;
	
	private ImageButton btnAddAlert;
	private RadioButton mRBPrice;
	private RadioButton mRBNews;
	private ImageView mIVShadowPrice;
	private ImageView mIVShadowNews;
	
	private List<Category> priceList;
	private List<Category> newsList;
	private TableLayout tlPrice;
	private TableLayout tlNews;
	private TableRow trPrice;
	private TableRow trNews;
	private CustomAdapter priceAdapter;
	private CustomAdapter newsAdapter;
	private Resources res;
	private AlertObject alertObject;
	private NewsAlertObject newsAlertObject;	

	public static AlertsMainFragment newInstance() {
		AlertsMainFragment fragment = new AlertsMainFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		na = res.getString(R.string.na);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshAdapter();
		buildPriceTable();
		buildNewsTable();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alert_list_fragment, container, false);

		tlPrice = (TableLayout) view.findViewById(R.id.table_price_alert);
		trPrice = (TableRow) view.findViewById(R.id.tr_price_alert);
		tlNews = (TableLayout) view.findViewById(R.id.table_news_alert);
		trNews = (TableRow) view.findViewById(R.id.tr_news_alert);
		mIVShadowNews = (ImageView) view.findViewById(R.id.shadow_news);
		mIVShadowPrice = (ImageView) view.findViewById(R.id.shadow_price);
		if (isNewsAlert) {
			tlPrice.setVisibility(View.GONE);
			mIVShadowPrice.setVisibility(View.GONE);
			tlNews.setVisibility(View.VISIBLE);
			mIVShadowNews.setVisibility(View.VISIBLE);
		} else {
			tlPrice.setVisibility(View.VISIBLE);
			tlNews.setVisibility(View.GONE);
			mIVShadowNews.setVisibility(View.GONE);
			mIVShadowPrice.setVisibility(View.VISIBLE);
		}
		
		mRBNews = (RadioButton) view.findViewById(R.id.radio_news_alert);
		mRBPrice = (RadioButton) view.findViewById(R.id.radio_price_alert);
		mRBNews.setOnClickListener(this);
		mRBPrice.setOnClickListener(this);

		priceList = new ArrayList<Category>();
		priceAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, priceList, na, false);
		newsList = new ArrayList<Category>();
		newsAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, newsList, na, true);

		btnAddAlert = (ImageButton) view.findViewById(R.id.btn_add_alert);
		btnAddAlert.setOnClickListener(AlertsMainFragment.this);

		return view;
	}
	
	private void buildNewsTable() {
		if (tlNews.getChildCount() != 0) {
			tlNews.removeAllViews();
			tlNews.addView(trNews);
		}
		
		for (int i = 0; i < newsAdapter.getCount(); i++) {
			Category catObj = (Category) newsAdapter.getItem(i);
			
			View item = newsAdapter.getView(i, null, null);
			tlNews.addView(item);

			if (catObj.getCompanyName().equals(res.getString(R.string.no_alert_added)))
				;
			else {
				item.setTag(i);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currNewsInt = (Integer) v.getTag();
						newsAlertObject = newsList.get(currNewsInt).getNewsAlertObject();
						boolean isChangeAlert = true;
						showAddNewsAlertActivity(isChangeAlert);
					}
				});
			}
		}
	}
	
	
	private void buildPriceTable() {
		if (tlPrice.getChildCount() != 0) {
			tlPrice.removeAllViews();
			tlPrice.addView(trPrice);
		}
		
		for (int i = 0; i < priceAdapter.getCount(); i++) {
			Category catObj = (Category) priceAdapter.getItem(i);
			
			View item = priceAdapter.getView(i, null, null);
			tlPrice.addView(item);

			if (catObj.getCompanyName().equals(res.getString(R.string.no_alert_added)))
				;
			else {
				item.setTag(i);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currPriceInt = (Integer) v.getTag();
						alertObject = priceList.get(currPriceInt).getAlertObject();
						boolean isChangeAlert = true;
						showAddPriceAlertActivity(isChangeAlert);
					}
				});
			}
		}
	}


	private void refreshAdapter() {

		List<Category> mList = new ArrayList<Category>();

		List<AlertObject> mAlertList = DbAdapter.getSingleInstance().fetchAlertObjectAll();

		if (mAlertList.size() > 0) {
			Category mCate;
			for (AlertObject ao : mAlertList) {
				mCate = new Category();
				mCate.setAlertObject(ao);

				String mSymbol = ao.getSymbol();
				StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
				if (mSo != null) {
					String mCompanyName = mSo.getName();
					mCate.setCompanyName(mCompanyName);
				}
				mList.add(mCate);
			}
		}

		if (mList != null && mList.size() > 0) {
			priceList = mList;
			priceAdapter.clear();
			for (Category cate : priceList) {
				priceAdapter.add(cate);
			}
		} else {
			priceList = mList;
			priceAdapter.clear();
			Category emptyCate = new Category();
			emptyCate.setCompanyName(res.getString(R.string.no_alert_added));
			priceAdapter.add(emptyCate);
		}
		priceAdapter.notifyDataSetChanged();
		
		List<Category> mNewsList = new ArrayList<Category>();
		List<NewsAlertObject> mNewsAlertList = DbAdapter.getSingleInstance().fetchNewsAlertObjectAll();
		
		if (mNewsAlertList.size() > 0) {
			Category mCate;
			for (NewsAlertObject ao : mNewsAlertList) {
				mCate = new Category();
				mCate.setNewsAlertObject(ao);

				String mSymbol = ao.getSymbol();
				StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
				if (mSo != null) {
					String mCompanyName = mSo.getName();
					mCate.setCompanyName(mCompanyName);
				}
				mNewsList.add(mCate);
			}
		}

		if (mNewsList != null && mNewsList.size() > 0) {
			newsList = mNewsList;
			newsAdapter.clear();
			for (Category cate : newsList) {
				newsAdapter.add(cate);
			}
		} else {
			newsList = mNewsList;
			newsAdapter.clear();
			Category emptyCate = new Category();
			emptyCate.setCompanyName(res.getString(R.string.no_alert_added));
			newsAdapter.add(emptyCate);
		}
		newsAdapter.notifyDataSetChanged();
	}

	public class Category {

		String companyName;
		AlertObject alertObject;
		NewsAlertObject newsAlertObject;

		public String getCompanyName() {
			return companyName;
		}

		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}

		public AlertObject getAlertObject() {
			return alertObject;
		}

		public void setAlertObject(AlertObject alertObject) {
			this.alertObject = alertObject;
		}
		
		public NewsAlertObject getNewsAlertObject() {
			return newsAlertObject;
		}

		public void setNewsAlertObject(NewsAlertObject newsAlertObject) {
			this.newsAlertObject = newsAlertObject;
		}
	}

	public class CustomAdapter extends ArrayAdapter<Category> {

		private List<Category> adItemList;
		private String mNa;
		private boolean isNewsAlert;

		CustomAdapter(Context context, int resource, int textViewResourceId, List<Category> objects, 
				String strNa, boolean isNewsAlert) {
			super(context, resource, textViewResourceId, objects);
			this.adItemList = objects;
			this.mNa = strNa;
			this.isNewsAlert = isNewsAlert;
		}

		public int getCount() {
			return adItemList.size();
		}

		public Category getItem(int position) {
			return adItemList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder vh = new ViewHolder();
			NewsViewHolder nvh = new NewsViewHolder();
			if (convertView == null) {

				LayoutInflater layoutInflater = getLayoutInflater();
				if (!isNewsAlert) {
					convertView = layoutInflater.inflate(R.layout.alert_price_row_item, null);
					vh = new ViewHolder();
					vh.tvSymbolCompanyName = (TextView) convertView.findViewById(R.id.price_alert_first_col);
					vh.tvUpperPrice = (TextView) convertView.findViewById(R.id.price_alert_second_col);
					vh.tvLowerPrice = (TextView) convertView.findViewById(R.id.price_alert_third_col);
					vh.priceSwitch = (CheckBox) convertView.findViewById(R.id.price_alert_toggle);
					convertView.setTag(vh);
				} else {
					convertView = layoutInflater.inflate(R.layout.alert_news_row_item, null);
					nvh.tvSymbolCompanyName = (TextView) convertView.findViewById(R.id.news_alert_first_col);
					nvh.tvAlertFrequency = (TextView) convertView.findViewById(R.id.news_alert_second_col);
					nvh.newsSwitch = (CheckBox) convertView.findViewById(R.id.news_alert_toggle);
					convertView.setTag(vh);
				}
				
			} else {
				if (!isNewsAlert)
					vh = (ViewHolder) convertView.getTag();
				else
					nvh = (NewsViewHolder) convertView.getTag();
			}

			Category cate = adItemList.get(position);
			final AlertObject ao = cate.getAlertObject();
			final NewsAlertObject nao = cate.getNewsAlertObject();

			String mCompanyName = cate.getCompanyName();
			
			if (!isNewsAlert && mCompanyName.equals(res.getString(R.string.no_alert_added))) {
				vh.tvSymbolCompanyName.setText(mCompanyName);
				vh.priceSwitch.setEnabled(false);
			} else if (isNewsAlert && mCompanyName.equals(res.getString(R.string.no_alert_added))) {
				nvh.tvSymbolCompanyName.setText(mCompanyName);
				nvh.newsSwitch.setEnabled(false);
			} else {	
				if (!isNewsAlert) {
					// 1.symbol & company name:
					String mSymbol = ao.getSymbol();
					Spannable firstColSpan = TextColorPicker.getStockPosFirstCol(mSymbol, mCompanyName);
					vh.tvSymbolCompanyName.setText(firstColSpan);
					
					// 2.upper price:
					String mUpperPrice = ao.getUpperPrice();
					if (!mUpperPrice.equals(""))
						mUpperPrice = DecimalsConverter.convertToStringValueBaseOnLocale
								(Double.valueOf(mUpperPrice), Constants.NUMBER_OF_DECIMALS, getActivity());
		
					if (mUpperPrice.length() < 1) {
						mUpperPrice = mNa;
					}
					vh.tvUpperPrice.setText(mUpperPrice);
		
					// 3. lower price:
					String mLowerPrice = ao.getLowerPrice();
					if (!mLowerPrice.equals(""))
						mLowerPrice = DecimalsConverter.convertToStringValueBaseOnLocale
								(Double.valueOf(mLowerPrice), Constants.NUMBER_OF_DECIMALS, getActivity());
		
					if (mLowerPrice.length() < 1) {
						mLowerPrice = mNa;
					}
					vh.tvLowerPrice.setText(mLowerPrice);
					
					// 4. switch:
					int isSwitchOn = ao.getIsNotifyOn();
					boolean bIsSwitchOn = false;
					if (isSwitchOn == 0)
						bIsSwitchOn = false;
					else
						bIsSwitchOn = true;
					
					vh.priceSwitch.setChecked(bIsSwitchOn);
					vh.priceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							if (isChecked) {								
								ao.setIsNotifyOn(AlertObject.DO_NOTIFY);								
							} else {
								ao.setIsNotifyOn(AlertObject.DO_NOT_NOTIFY);
							}
							
							ao.update();
						}
					});
			
				} else {
					// 1.symbol & company name:
					String mSymbol = nao.getSymbol();
					Spannable firstColSpan = TextColorPicker.getStockPosFirstCol(mSymbol, mCompanyName);
					nvh.tvSymbolCompanyName.setText(firstColSpan);
					
					// 2.alert frequency:
					int alertFrequency = nao.getAlertFrequency();
					String sAlertFreq = "";
					
					switch (alertFrequency) {
					case 15:
						sAlertFreq = "15 minutes";
						break;
					case 30:
						sAlertFreq = "30 minutes";
						break;
					case 60:
						sAlertFreq = "hourly";
						break;
					case 180:
						sAlertFreq = "3 hourly";
						break;
					case 360:
						sAlertFreq = "6 hourly";
						break;
					case 720:
						sAlertFreq = "12 hourly";
						break;
					case 1440:
						sAlertFreq = "daily";
						break;
					}
					nvh.tvAlertFrequency.setText(sAlertFreq);
		
					// 3. switch:
					int isSwitchOn = nao.getIsNotifyOn();
					boolean bIsSwitchOn = false;
					if (isSwitchOn == 0)
						bIsSwitchOn = false;
					else
						bIsSwitchOn = true;
					
					nvh.newsSwitch.setChecked(bIsSwitchOn);
					nvh.newsSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								nao.setIsNotifyOn(NewsAlertObject.DO_NOTIFY);
								
							} else {
								nao.setIsNotifyOn(NewsAlertObject.DO_NOT_NOTIFY);								
							}
							nao.update();
						}
					});
				}
			}

			return convertView;
		}
	}

	public class ViewHolder {
		TextView tvSymbolCompanyName;
		TextView tvUpperPrice;
		TextView tvLowerPrice;
		CheckBox priceSwitch;
	}
	
	public class NewsViewHolder {
		TextView tvSymbolCompanyName;
		TextView tvAlertFrequency;
		CheckBox newsSwitch;
	}
	

	private void showAddPriceAlertActivity(boolean isChangeAlert) {

		Intent i = new Intent(getActivity(), AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_NEWS_ALERT, false);
		if (isChangeAlert) {
			String mSymbol = alertObject.getSymbol();
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, true);
			i.putExtra(Constants.KEY_UPPER_PRICE66, alertObject.getUpperPrice());
			i.putExtra(Constants.KEY_LOWER_PRICE66, alertObject.getLowerPrice());
		} else {
			i.putExtra(Constants.KEY_SYMBOL, "");
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		}
		startActivity(i);
	}
	
	private void showAddNewsAlertActivity(boolean isChangeAlert) {

		Intent i = new Intent(getActivity(), AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_NEWS_ALERT, true);
		if (isChangeAlert) {
			String mSymbol = newsAlertObject.getSymbol();
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, true);
			i.putExtra(Constants.KEY_START_TIME, newsAlertObject.getStartTime());
			i.putExtra(Constants.KEY_ALERT_FREQUENCY, newsAlertObject.getAlertFrequency());
		} else {
			i.putExtra(Constants.KEY_SYMBOL, "");
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		}
		startActivity(i);
	}
	

	@Override
	public void onClick(View view) {

		int mId = view.getId();
		Intent i = new Intent(getActivity(), AddNewAlert.class);
		
		switch (mId) {
		case R.id.btn_add_alert:
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
			i.putExtra(Constants.KEY_IS_NEWS_ALERT, isNewsAlert);
			getActivity().startActivity(i);
			break;
		case R.id.radio_price_alert:
			isNewsAlert = false;
			tlPrice.setVisibility(View.VISIBLE);
			tlNews.setVisibility(View.GONE);
			mIVShadowPrice.setVisibility(View.VISIBLE);
			mIVShadowNews.setVisibility(View.GONE);
			break;
		case R.id.radio_news_alert:
			isNewsAlert = true;
			tlPrice.setVisibility(View.GONE);
			mIVShadowPrice.setVisibility(View.GONE);
			tlNews.setVisibility(View.VISIBLE);
			mIVShadowNews.setVisibility(View.VISIBLE);
			break;
		}
	}
}
