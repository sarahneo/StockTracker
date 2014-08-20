package com.handyapps.stocktracker.utils;

import org.holoeverywhere.widget.Toast;
import android.content.Context;
import android.view.Gravity;

public class MyToastUtils {
	
	public static void show(Context context, String msg){
		Context mCtx = context;
		String mMsg = msg;
		Toast toast = Toast.makeText(mCtx, mMsg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}
