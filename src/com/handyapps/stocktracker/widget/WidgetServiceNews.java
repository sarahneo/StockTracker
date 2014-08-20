package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.StockObject;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetServiceNews extends RemoteViewsService {
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {		
		return new StackRemoteViewsFactoryNews(this.getApplicationContext(), intent);
	}

	class StackRemoteViewsFactoryNews implements RemoteViewsService.RemoteViewsFactory {
		private Context mContext = null;	
		private List<NewsObject> list;

		public StackRemoteViewsFactoryNews(Context context, Intent intent) {
			this.mContext = context;
		}

		@Override
		public void onCreate() {

			// 1. Get NewsObject list.
			this.list = new ArrayList<NewsObject>();

			List<NewsObject> mList = DbAdapter.getSingleInstance().fetchNewsList();
			if (mList != null && mList.size() > 0) {
				this.list = mList;
			}	
			
		}

		@Override
		public int getCount() {
			return (this.list.size());
		}

		@Override
		public long getItemId(int position) {
			return (position);
		}

		@Override
		public RemoteViews getLoadingView() {
			return (null);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public RemoteViews getViewAt(int position) {
			String mSymbol = "";
			String mCompanyName = "";

			RemoteViews rvRow = new RemoteViews(this.mContext.getPackageName(), R.layout.single_news_row_layout);
			NewsObject mNObject = this.list.get(position);

			if (mNObject != null) {

				mSymbol = mNObject.getSymbol();
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);

				if (so != null) {

					if (position % 2 == 0) { // even
						mCompanyName = so.getName();					
						// 0. set color:
						rvRow.setInt(R.id.tv_company_news, "setBackgroundColor", so.getColorCode());
						
						// 1.set company:
						rvRow.setTextViewText(R.id.tv_company_news, mCompanyName + " News");
						rvRow.setViewVisibility(R.id.tv_company_news, View.VISIBLE);
					} else {
						rvRow.setViewVisibility(R.id.tv_company_news, View.GONE);
					}

					// 2.set headline:
					rvRow.setTextViewText(R.id.tv_news_headline, mNObject.getTitle());

					int stockId = so.getId();
					Intent i = new Intent();
					i.putExtra(Constants.KEY_STOCK_ID, stockId);
					i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
					rvRow.setOnClickFillInIntent(R.id.row_body_single_news, i);
					
				}
			}
			return (rvRow);
		}

		@Override
		public void onDataSetChanged() {
			
			List<NewsObject> mList = DbAdapter.getSingleInstance().fetchNewsList();
			if (mList != null && mList.size() > 0) {
				this.list = mList;
			}else{
				this.list = new ArrayList<NewsObject>();
			}
			
		}

		@Override
		public int getViewTypeCount() {
			return (1);
		}

		@Override
		public boolean hasStableIds() {
			return (true);
		}

		@Override
		public void onDestroy() {
		}
	}
}