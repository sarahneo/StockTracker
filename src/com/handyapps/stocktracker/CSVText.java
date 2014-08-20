package com.handyapps.stocktracker;

import android.content.Context;

public class CSVText {
	
	public static String ERROR_READING_AMOUNT_FIELD;
	public static String ERROR_NUM_SHARES_ZERO;
	public static String ERROR_PRICE_ZERO;

	public static String UNKNOWN_ERROR;

	public static String WHILE_PARSING_LINE;
	public static String ERROR_AT_LINE;
	
	public static String SUCCESSFUL_RECORDS;
	
	public static void loadText(Context ctx) {
    	
		ERROR_READING_AMOUNT_FIELD = ctx.getString(R.string.error_reading_amount_field);		
		ERROR_NUM_SHARES_ZERO = ctx.getString(R.string.error_num_shares_zero);
		ERROR_PRICE_ZERO = ctx.getString(R.string.error_price_zero);
	
		UNKNOWN_ERROR = ctx.getString(R.string.unknown_error);
		
		WHILE_PARSING_LINE = ctx.getString(R.string.while_parsing_line);
		ERROR_AT_LINE = ctx.getString(R.string.error_at_line);
		
		SUCCESSFUL_RECORDS = ctx.getString(R.string.successful_records);

	}

}
