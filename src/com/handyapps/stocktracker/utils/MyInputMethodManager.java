package com.handyapps.stocktracker.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MyInputMethodManager {
	
	/**
	 * View == EditText etSearchTerm;
	 */
	public static void showKeyboard(Context c){
		InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
	public static void hideKeyboard(Context c, View v){
		InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
	}

}
