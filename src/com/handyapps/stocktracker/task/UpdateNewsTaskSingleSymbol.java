package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.service.MyNewsNotification;

public class UpdateNewsTaskSingleSymbol extends AsyncTask<String, Void, ArrayList<NewsObject>> {

	private String symbol;
	private Context context;

	public UpdateNewsTaskSingleSymbol(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected ArrayList<NewsObject> doInBackground(String... arrSymbols) {
		ArrayList<NewsObject> mList = null;
		if (arrSymbols[0] != null && !arrSymbols[0].equals("")) {
			this.symbol = arrSymbols[0];

			try {
				if (!isCancelled()) {
					mList = new ArrayList<NewsObject>();
					mList = quoteNewsList(symbol);

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
		if (mList != null) {

			// Start Alert
			MyNewsNotification.alertNotification(mList, symbol, context);

			// Update Widget
			/*Intent iUpdateWidget = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
			context.startService(iUpdateWidget);*/
			
		} else {

			Intent iTransationFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
			iTransationFragmentActivity.putExtra(Constants.KEY_IS_QUOTE_OK, false);
			context.sendBroadcast(iTransationFragmentActivity);

			Intent iAddAlertDialog = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
			iAddAlertDialog.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, false);
			iAddAlertDialog.putExtra(Constants.KEY_IS_QUOTE_OK, false);
			context.sendBroadcast(iAddAlertDialog);
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