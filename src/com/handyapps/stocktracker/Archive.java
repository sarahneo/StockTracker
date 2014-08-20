package com.handyapps.stocktracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.holoeverywhere.app.Activity;

import android.database.SQLException;
import android.os.Environment;
import android.widget.ListView;

import com.handyapps.stocktracker.adapter.FindStocksAdapter;
import com.handyapps.stocktracker.dialogs.CSVErrorDialog;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.model.TransactionObject;
import com.handyapps.stocktracker.task.FindStocksCallbackTask;
import com.handyapps.stocktracker.utils.MyDateFormat;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class Archive {

	public static String BASE = Environment.getExternalStorageDirectory()
			.getPath();

	public static String CSV_INPUT_FOLDER_PATH = BASE + "/StocksIQ/csv_input/";
	public static final String BUY_TYPE = "Buy";
	public static final String SELL_TYPE = "Sell";
	public static final String BUY_TO_COVER = "Buy to Cover";
	public static final String SELL_SHORT = "Sell Short";
	public static final int GOOGLE_FINANCE_NUM_COL = 8;
	

	public int errorLineNum = -1;
	public String csvError = "";
	public String csvErrorMsg = "";
	
	private Activity activity;		

	public Archive(Activity activity) {
		this.activity = activity;		
		CSVText.loadText(activity);
	}

	public String[] getCSVFileList() {
		File folder = new File(CSV_INPUT_FOLDER_PATH);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase(Locale.getDefault()).endsWith(".csv");
			}
		};

		String[] fileList = folder.list(filter);
		if (fileList == null) {
			fileList = new String[0];
		}
		return fileList;
	}

	public int importFromCSV(String filename, int portId, boolean hasColumnNames, 
			String fieldDelimiter, String decimalSeparator) {

		int recordsInserted = -1;

		try {
			FileReader f = new FileReader(CSV_INPUT_FOLDER_PATH + filename);
			File folder = new File(CSV_INPUT_FOLDER_PATH);
			folder.list();

			recordsInserted = importCSV(f, portId, hasColumnNames,
					decimalSeparator, fieldDelimiter);

		} catch (Exception e) {

		} finally {
			// Update Widgets with new data
			WidgetUtils.updateWidget(activity);
		}
		return recordsInserted;
	}

	public int importCSV(FileReader fileReader, int portId,
			boolean hasColumnNames, String decimalSeparator,
			String fieldDelimiter) {
		int recordsInserted = 0;
		int lineNum = 0;
		errorLineNum = 0;
		csvError = "";
		csvErrorMsg = "";

		//mDb.beginTransaction();
		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(fileReader);
			try {
				String line = null; // not declared within while loop			

				
				 /* readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				 
				while ((line = input.readLine()) != null) {
					// contents.append(line);
					if ((lineNum == 0) && hasColumnNames) {
						// do nothing
					} else {
						String[] columns = line.split(fieldDelimiter);
						int index = 0;
						String symbol = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								symbol += fieldDelimiter + columns[index];
							}
							symbol = symbol.substring(1, symbol.length() - 1);
							symbol = symbol.replace("\"\"", "\"");
						}

						index++;
						String lineMinusSym = line.substring(symbol.length()+1);
						String type = "";
						String googleType = "";
						if (lineMinusSym.contains(BUY_TYPE)) {
							type = TransactionObject.BUY_TYPE;
							googleType = BUY_TYPE;
						} else if (lineMinusSym.contains(SELL_TYPE)) {
							type = TransactionObject.SELL_TYPE;
							googleType = SELL_TYPE;
						} else if (lineMinusSym.contains(BUY_TO_COVER)) {
							type = TransactionObject.BUY_TYPE;
							googleType = BUY_TO_COVER;
						} else if (lineMinusSym.contains(SELL_SHORT)) {
							type = TransactionObject.SELL_TYPE;
							googleType = SELL_SHORT;
						}
						String name = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								name += fieldDelimiter + columns[index];
							}
							name = name.substring(1,
									name.length() - 1);
							name = name.replace("\"\"", "\"");
							name = name.trim();
						} else 
							name = lineMinusSym.substring(0, lineMinusSym.indexOf(googleType)-1);
						
						/**
						 * BUY, SELL, BUY TO COVER, SELL SHORT
						 */
						index++;
						/*String type = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								type += fieldDelimiter + columns[index];
							}
							type = type.substring(1,
									type.length() - 1);
							type = type.replace("\"\"", "\"");
							type = type.trim();
						}*/
						index++;
						
						String date = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								date += fieldDelimiter + columns[index];
							}
							date = date.substring(1,
									date.length() - 1);
							date = date.replace("\"\"", "\"");
							date = date.trim();
						}
						
						index++;
						String shares = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								shares += fieldDelimiter + columns[index];
							}
							shares = shares.substring(1,
									shares.length() - 1);
							shares = shares.replace("\"\"", "\"");
							shares = shares.trim();
						}
						
						index++;
						String price = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								price += fieldDelimiter + columns[index];
							}
							price = price.substring(1,
									price.length() - 1);
							price = price.replace("\"\"", "\"");
							price = price.trim();
						}
						
						index++;
						String commission = columns[index];
						if (columns[index].startsWith("\"")) {
							while (!columns[index].endsWith("\"")) {
								index++;
								commission += fieldDelimiter + columns[index];
							}
							commission = commission.substring(1,
									commission.length() - 1);
							commission = commission.replace("\"\"", "\"");
							commission = commission.trim();
						}
						
						index++;
						String notes = "";
						if (index <= columns.length - 1) {

							notes = columns[index];
							if (!notes.equals("")) {
								if (columns[index].startsWith("\"")) {
									while (!columns[index].endsWith("\"")) {
										index++;
										notes += fieldDelimiter
												+ columns[index];
									}
									notes = notes.substring(1,
											notes.length() - 1);
									notes = notes.replace("\"\"", "\"");
								}
							}

						}

						if (shares.equals("0")){
							/*csvError += CSVText.ERROR_AT_LINE + " " + String.valueOf(lineNum) + ": " +
									CSVText.ERROR_NUM_SHARES_ZERO + "\n";*/
						} else if (price.equals("0")) {
							/*csvError += CSVText.ERROR_AT_LINE + " " + String.valueOf(lineNum) + ": " +
									CSVText.ERROR_PRICE_ZERO + "\n";*/
							type = TransactionObject.DIVIDEND_TYPE_SHARES;
						} else { 
							double dTotal = Double.parseDouble(price) * Integer.parseInt(shares);
							Date dateDate = new Date();							
							int numberTradeDate = 0;
							if (date.equals("")) {
								// get current date
								Calendar calToday = Calendar.getInstance();
								String strDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(calToday);
								numberTradeDate = Integer.parseInt(strDateYYYYMMDD);
							} else {
								dateDate = dateFormat.parse(date); 
								Calendar cal = Calendar.getInstance();
								cal.setTime(dateDate);
								String strDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(cal);
								numberTradeDate = Integer.parseInt(strDateYYYYMMDD);
							}
							TransactionObject newTo = new TransactionObject();
							newTo.setFee(commission);
							newTo.setNotes(notes.replace("'", "''"));
							newTo.setPrice(price);
							newTo.setNumOfShares(Integer.parseInt(shares));
							newTo.setTotal(dTotal);
							newTo.setTradeDate(numberTradeDate);
							newTo.setType(type);
							newTo.setPortId(portId);
							
							/*sql = "INSERT INTO " + TransactionObject.TABLE_NAME + 
									"(port_id, title, category_id, amount, status, tran_date, remarks) VALUES("
									+ symbol
									+ ",'"
									+ name.replace("'", "''")
									+ "',"
									+ type
									+ ","
									+ date.replace("'", "''")
									+ ",'"
									+ shares.replace("'", "''")
									+ "',"
									+ price
									+ ",'" 
									+ commission
									+ ",'"
									+ notes.replace("'", "''") + "')";
							Log.i("sql", sql);*/
							
							// 1. Get Yahoo! Symbol
							getYahooSymbols(symbol, portId, newTo);																						
							recordsInserted++;
						} 
						
					}
					lineNum++;
				}
				//mDb.setTransactionSuccessful();
			} catch (NumberFormatException e) {
				recordsInserted = -1;
				csvError = CSVText.ERROR_READING_AMOUNT_FIELD;
			} catch (Exception e) {
				recordsInserted = -1;
			} finally {
				//mDb.endTransaction();
				input.close();
			}
		} catch (SQLException e) {
			recordsInserted = -1;
		} catch (IOException ex) {
			recordsInserted = -1;
		} catch (Exception e) {
			recordsInserted = -1;
		}

		if (recordsInserted == -1) {
			// hit some error, prepare error details
			errorLineNum = lineNum;
			if (csvError == null || csvError.equals(""))
				csvError = CSVText.UNKNOWN_ERROR;
			csvErrorMsg = csvError + " " + CSVText.WHILE_PARSING_LINE + " "
					+ errorLineNum;
		} else {
			csvErrorMsg = CSVText.SUCCESSFUL_RECORDS + " " + String.valueOf(recordsInserted) + "/" + String.valueOf(lineNum-1);
			csvErrorMsg += "\n";
			csvErrorMsg += csvError;
			//Toast.makeText(activity, activity.getResources().getString(R.string.trade_added), Toast.LENGTH_SHORT).show();
		}
		
		CSVErrorDialog dialog = new CSVErrorDialog(csvErrorMsg);
		dialog.show(activity.getSupportFragmentManager(), Constants.DIALOG_CSV_ERROR);

		return recordsInserted;
	}
	
	private void getYahooSymbols(String mQuote, int portId, TransactionObject to) {

		ArrayList<SymbolCallBackObject> ls = new ArrayList<SymbolCallBackObject>();
		org.holoeverywhere.widget.ProgressBar pb = new org.holoeverywhere.widget.ProgressBar(activity);
		ListView lv = new ListView(activity);
		FindStocksAdapter customAdapter = new FindStocksAdapter(activity, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, ls);
		FindStocksCallbackTask callBackTask = new FindStocksCallbackTask(activity, pb, ls, customAdapter, lv, 
				Constants.FROM_ADD_TRADE, portId, to);
		callBackTask.execute(mQuote);
	}

}
