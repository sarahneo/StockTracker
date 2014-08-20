package com.handyapps.stocktracker.rater;

import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.Button;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.library.store.StoreBuild;

public class AppRater {
    private static String APP_TITLE = "Curency Exchange";
    //private final static String APP_PNAME = "com.handyapps.stocktracker";
    //private final static String APP_PNAME_PRO = "com.handyapps.stocktracker10";
    
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    
    private final static String PREFS_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREFS_LAUNCH_COUNT= "launch_count";
    private final static String PREFS_DATE_LAUNCHED = "date_firstlaunch";
    
    public static enum VERSION {VERSION_PRO, VERSION_LITE};
    
    private final static String SHARED_PREFS_NAME = "apprater";
    
    public static void app_launched(Context mContext, VERSION APPVERSION) {
        SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFS_NAME, 0);
        if (prefs.getBoolean(PREFS_DONT_SHOW_AGAIN, false)) { return ; }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong(PREFS_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREFS_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREFS_DATE_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREFS_DATE_LAUNCHED, date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor, APPVERSION);
           }
        }
        
        editor.commit();
    }   
    
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor, final VERSION APPVERSION) {
    	
	    APP_TITLE = mContext.getResources().getString(R.string.app_name);
    	
	    String STR_NO_THANKS= mContext.getResources().getString(R.string.app_rater_no);
	    String STR_RATE_APP= mContext.getResources().getString(R.string.app_rater_rate, APP_TITLE);
	    String STR_REMIND_ME= mContext.getResources().getString(R.string.app_rater_reminder_later);
	    String STR_IF_ENJOY = mContext.getResources().getString(R.string.app_rater_if_enjoy, APP_TITLE);
	    
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        dialog.setContentView(R.layout.app_rater);
        
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
        TextView tvMsg = (TextView) dialog.findViewById(R.id.tvMessage);
        Button btnRate = (Button) dialog.findViewById(R.id.btnRate);
        Button btnRemind = (Button) dialog.findViewById(R.id.btnRemind);
        Button btnNo = (Button) dialog.findViewById(R.id.btnNo);
        
        tvTitle.setText(STR_RATE_APP);
        tvMsg.setText(STR_IF_ENJOY);
        btnRate.setText(STR_RATE_APP);
        
        btnRate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	/*
            	if (APPVERSION == VERSION.VERSION_PRO){
                   mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME_PRO)));
            	}else{
                   mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
            	}*/
            	
            	StoreBuild.getSingleInstance().goToProduct(Constants.isPro());
            	
                if (editor != null) {
                    editor.putBoolean(PREFS_DONT_SHOW_AGAIN, true);
                    editor.commit();
                }
                
                dialog.dismiss();
            }
        });        

        btnRemind.setText(STR_REMIND_ME);
        btnRemind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnNo.setText(STR_NO_THANKS);
        btnNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean(PREFS_DONT_SHOW_AGAIN, true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        dialog.show();        
    }
}