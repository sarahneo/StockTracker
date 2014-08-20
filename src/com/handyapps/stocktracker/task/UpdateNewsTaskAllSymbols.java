package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.widget.WidgetUpdateIntentService;

public class UpdateNewsTaskAllSymbols extends AsyncTask<String, Void, Boolean> {

	private List<NewsObject> nList = null;
	private Context mCtx;	

	public UpdateNewsTaskAllSymbols(Context context) {
		this.mCtx = context;
		this.nList = new ArrayList<NewsObject>();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... arr) {

		try {
			if (!isCancelled()) {
				if (arr.length > 0) {

					for (String s : arr) {
						List<NewsObject> newsList = new ArrayList<NewsObject>();
						newsList = quoteNewsList(s);
						if (newsList != null) {
							List<NewsObject> object = DbAdapter.getSingleInstance().fetchNewsBySymbol(s);
							if (object != null) {
								// Delete first
								for (NewsObject newsObj : object)
									newsObj.delete();	
							} 
							
							// Insert into DB
							for (NewsObject newsObject : newsList) {
								NewsObject newNews = new NewsObject();
								newNews.setDescription(newsObject.getDescription());
								newNews.setLink(newsObject.getLink());
								newNews.setPubDate(newsObject.getPubDate());
								newNews.setSymbol(s);
								newNews.setTitle(newsObject.getTitle());
								newNews.insert();
							}
							nList.addAll(newsList);
						}
					}

					if (nList.size() > 0) {
						return true;
					}
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result) {
			//Log.d("AutoQuoteUpdate", "Done.");
			if (nList.size() > 0) {
				sendMyBroadcastByOnceRefresh();
			}
		} else {
			// update widget: to tell widget to stop progress bar too if failure
			// updates.
			Intent iUpdateWidget = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
			this.mCtx.startService(iUpdateWidget);
		}
	}

	private void sendMyBroadcastByOnceRefresh() {

		// update widget:
		Intent iUpdateWidget = new Intent(mCtx, WidgetUpdateIntentService.class);
		iUpdateWidget.setAction(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
		this.mCtx.startService(iUpdateWidget);
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
				int lengthStop = nl.getLength();
				if (nl.getLength() > 2)
					lengthStop = 2;
				for (int i = 0; i < lengthStop; i++) {
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
