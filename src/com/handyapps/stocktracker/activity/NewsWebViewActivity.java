package com.handyapps.stocktracker.activity;

import org.holoeverywhere.app.Activity;

import com.handyapps.stocktracker.R;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.MyBannerAd;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class NewsWebViewActivity extends Activity {

	private WebView mWebView;
	private ProgressBar pb;
	private String url;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.web_view_layout);
		actionBar.hide();
		
		MyBannerAd ad = new MyBannerAd(findViewById(android.R.id.content), getApplicationContext());
		ad.loadAd();
		
		url = getIntentUrl();

		// Makes Progress bar Visible
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		// Get ProgressBar
		pb = (ProgressBar) findViewById(R.id.pb_webloading);

		// Get Web view
		mWebView = (WebView) findViewById(R.id.web_view_news); // This is the id you
															// gave
															// to the WebView in
															// the main.xml
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true); // Zoom Control on web (You
														// don't need this
														// if ROM supports
														// Multi-Touch
		mWebView.getSettings().setBuiltInZoomControls(true); // Enable
																// Multitouch if
																// supported by
																// ROM

		// Load URL
		// mWebView.loadUrl("http://www.firstdroid.com/advertisement.htm");
		// mWebView.loadUrl("http://developer.android.com/reference/android/app/LoaderManager.html");

		// Sets the Chrome Client, and defines the onProgressChanged
		// This makes the Progress bar be updated.
		final Activity MyActivity = this;
		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Make the bar disappear after URL is loaded, and changes
				// string to Loading...
				
				Spanned spanned = TextColorPicker.getAppLabelContentLoading(getApplicationContext());
				ActionBar actionBar = getSupportActionBar();
				actionBar.setTitle(spanned);
				//MyActivity.setProgress(progress * 100); // Make the bar
				MyActivity.setProgress(progress * 100);										// disappear after URL
														// is loaded
				pb.setProgress(progress);

				// Return the app name after finish loading
				if (progress == 100) {

					Spanned spannedAppLable = TextColorPicker.getAppLabel(getApplicationContext());
					actionBar.setTitle(spannedAppLable);
				}
				
				pb.setVisibility(View.VISIBLE);
				if (progress == 100) {
					pb.setVisibility(View.GONE);
				}

			}
		});

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

				// Handle the error
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		if (!url.contains(getResources().getString(R.string.video)))
			url = Constants.URLPrefix + url;
		mWebView.loadUrl(url);

	}// End of Method onCreate
	
	private String getIntentUrl() {
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null){
			return bundle.getString(Constants.KEY_NEWS_URL);
		}
		return "";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
