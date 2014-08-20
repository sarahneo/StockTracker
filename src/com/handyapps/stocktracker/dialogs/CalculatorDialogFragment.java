package com.handyapps.stocktracker.dialogs;

import java.text.DecimalFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.handyapps.stocktracker.R;


public class CalculatorDialogFragment extends DialogFragment{

	private LayoutInflater inflater;
	private EditText mCalcDisplay;
	private String mTempResult;
	private String mLastInput;
	private String mLastOp;
	private String mMemory;

	private String mInitialAmount = "0";
	private int mType;

	public static interface CalculatorCallbacks{
		public void OnResult(int type, String amount);
		public void OnResult(String amount);
	}

	private CalculatorCallbacks mCallbacks;

	public static CalculatorDialogFragment newInstance(int type, String initialAmount){

		CalculatorDialogFragment mDialogFragment = new CalculatorDialogFragment();
		Bundle args = new Bundle();
		args.putString("initial_amount", initialAmount);
		args.putInt("type", type);
		mDialogFragment.setArguments(args);

		return mDialogFragment;
	}

	//	public static CalculatorDialogFragment newInstance(String initialAmount){
	//
	//		CalculatorDialogFragment mDialogFragment = new CalculatorDialogFragment();
	//		Bundle args = new Bundle();
	//		args.putString("initial_amount", initialAmount);
	//		mDialogFragment.setArguments(args);
	//
	//		return mDialogFragment;
	//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null){
			Bundle args = getArguments();
			if (args != null){
				mInitialAmount = args.getString("initial_amount");
				mType = args.getInt("type", -1);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		inflater = getLayoutInflater();

		if (activity instanceof CalculatorCallbacks){
			mCallbacks = (CalculatorCallbacks) activity;
		}else{
			if (getTargetFragment() != null){
				if (getTargetFragment() instanceof CalculatorCallbacks){
					mCallbacks  = (CalculatorCallbacks) getTargetFragment();
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		super.onSaveInstanceState(saveInstanceState);
		saveInstanceState.putString("temp_result", mTempResult);
		saveInstanceState.putString("last_input", mLastInput);
		saveInstanceState.putString("last_op", mLastOp);
		saveInstanceState.putString("memory", mMemory);
		saveInstanceState.putString("calc_display", mCalcDisplay.getText().toString());
		saveInstanceState.putInt("type", mType);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		inflater = (LayoutInflater) getActivity().getLayoutInflater();
		LinearLayout calcLayout = (LinearLayout) inflater.inflate(R.layout.calculator, null);

		mCalcDisplay = (EditText) calcLayout.findViewById(R.id.display);//inflater.inflate(R.layout.calculator_display, null);
		builder.setCustomTitle(null);
		builder.setView(calcLayout);

		if (savedInstanceState == null){
			mCalcDisplay.setText(mInitialAmount.trim().replace(",","."));
			mTempResult = mCalcDisplay.getText().toString();
			mLastInput = "";
			mLastOp = "";
			mMemory = "0";
		}else{
			mTempResult = savedInstanceState.getString("temp_result");
			mLastInput = savedInstanceState.getString("last_input");
			mLastOp = savedInstanceState.getString("last_op");
			mMemory = savedInstanceState.getString("memory");
			mCalcDisplay.setText(savedInstanceState.getString("calc_display"));
			mType = savedInstanceState.getInt("type");
		}

		Button btnMR = (Button) calcLayout.findViewById(R.id.btn_mr);
		btnMR.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mLastInput = mMemory;
				mCalcDisplay.setText(mMemory);
			}
		});
		Button btnMplus = (Button) calcLayout.findViewById(R.id.btn_mplus);
		btnMplus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mMemory = mCalcDisplay.getText().toString().trim();
			}
		});
		Button btnEqual = (Button) calcLayout.findViewById(R.id.btn_equal);
		btnEqual.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mLastOp.equals("")) {
					if (mLastInput.equals(""))
						mLastInput = mTempResult;
					handleEqual();
				}
			}
		});
		Button btnPlus = (Button) calcLayout.findViewById(R.id.btn_plus);
		btnPlus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mLastInput.equals("") && !mLastOp.equals("")) {
					handleEqual();
				} else {
					mTempResult = mCalcDisplay.getText().toString().trim();
					mLastInput = "";
				}
				mLastOp = "+";
			}
		});
		Button btnMinus = (Button) calcLayout.findViewById(R.id.btn_minus);
		btnMinus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mLastInput.equals("") && !mLastOp.equals("")) {
					handleEqual();
				} else {
					mTempResult = mCalcDisplay.getText().toString().trim();
					mLastInput = "";
				}
				mLastOp = "-";
			}
		});
		Button btnMultiply = (Button) calcLayout.findViewById(R.id.btn_multiply);
		btnMultiply.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mLastInput.equals("") && !mLastOp.equals("")) {
					handleEqual();
				} else {
					mTempResult = mCalcDisplay.getText().toString().trim();
					mLastInput = "";
				}
				mLastOp = "x";
			}
		});
		Button btnDivide = (Button) calcLayout.findViewById(R.id.btn_divide);
		btnDivide.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mLastInput.equals("") && !mLastOp.equals("")) {
					handleEqual();
				} else {
					mTempResult = mCalcDisplay.getText().toString().trim();
					mLastInput = "";
				}
				mLastOp = "/";
			}
		});
		Button btnDot = (Button) calcLayout.findViewById(R.id.btn_dot);
		btnDot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mLastInput = mLastInput + ".";
				mCalcDisplay.setText(mLastInput);
			}
		});
		Button btn1 = (Button) calcLayout.findViewById(R.id.btn_1);
		btn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("1");
			}
		});
		Button btn2 = (Button) calcLayout.findViewById(R.id.btn_2);
		btn2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("2");
			}
		});
		Button btn3 = (Button) calcLayout.findViewById(R.id.btn_3);
		btn3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("3");
			}
		});
		Button btn4 = (Button) calcLayout.findViewById(R.id.btn_4);
		btn4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("4");
			}
		});
		Button btn5 = (Button) calcLayout.findViewById(R.id.btn_5);
		btn5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("5");
			}
		});
		Button btn6 = (Button) calcLayout.findViewById(R.id.btn_6);
		btn6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("6");
			}
		});
		Button btn7 = (Button) calcLayout.findViewById(R.id.btn_7);
		btn7.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("7");
			}
		});
		Button btn8 = (Button) calcLayout.findViewById(R.id.btn_8);
		btn8.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("8");
			}
		});
		Button btn9 = (Button) calcLayout.findViewById(R.id.btn_9);
		btn9.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("9");
			}
		});
		Button btn0 = (Button) calcLayout.findViewById(R.id.btn_0);
		btn0.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleNumber("0");
			}
		});

		Button btnPercent = (Button) calcLayout.findViewById(R.id.btn_percent);
		btnPercent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handlePercent();
			}
		});

		Button btnClearAll = (Button) calcLayout.findViewById(R.id.btn_clearall);
		btnClearAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mLastInput = "0";
				mTempResult = "0";
				mLastOp = "";
				mCalcDisplay.setText(mLastInput);
			}
		});

		Button btnCancel = (Button) calcLayout.findViewById(R.id.cancel);

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button btnDone = (Button) calcLayout.findViewById(R.id.done);

		btnDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				try{

					float value = Float.parseFloat(mCalcDisplay.getText().toString());

					if (value >= 0){
						if (mCallbacks != null){
							mCallbacks.OnResult(mCalcDisplay.getText().toString());
							mCallbacks.OnResult(mType, mCalcDisplay.getText().toString());
						}

						dismiss();
					}else{
						Toast.makeText(getActivity(), R.string.err_negative_amount, Toast.LENGTH_SHORT).show();
					}

				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
		});



		//		builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
		//			public void onClick(DialogInterface dialog, int whichButton) {
		//				if (mCallbacks != null){
		//					mCallbacks.OnResult(mCalcDisplay.getText().toString());
		//					mCallbacks.OnResult(mType, mCalcDisplay.getText().toString());
		//				}
		//			}
		//		}); 
		//		builder.setNegativeButton(getString(R.string.cancel), null);

		return builder.create();
	}


	public String calculate(String lastOp, String lastInput, String tempResult) {

		double tmpResult = 0;
		if (!tempResult.equals(""))
			tmpResult = Double.valueOf(tempResult);
		double lstInput = 0;
		if (!lastInput.equals(""))
			lstInput = Double.valueOf(lastInput);
		double result = tmpResult;

		DecimalFormat numFormat = new DecimalFormat();
		numFormat.applyPattern("0.00");


		if (lastOp.equals("+")) {
			result = lstInput + tmpResult;
		} else if (lastOp.equals("-")) {
			result = tmpResult - lstInput;
		}
		if (lastOp.equals("x")) {
			result = tmpResult * lstInput;
		} else if (lastOp.equals("/")) {
			result = tmpResult / lstInput;
		} else if (lastOp.equals("%")) {
			result = (double) (tmpResult * lstInput * 0.01);
		} else if (lastOp.equals("")) {
			result = tmpResult;
		}
		return numFormat.format(result);
	}

	public void handleEqual() {
		mTempResult = calculate(mLastOp, mLastInput, mTempResult);
		mLastOp = "";
		mLastInput = "";
		mCalcDisplay.setText(String.valueOf(mTempResult));
	}

	public void handlePercent() {
		mTempResult = calculate("%", mLastInput, mTempResult);
		mLastOp = "";
		mLastInput = "";
		mCalcDisplay.setText(String.valueOf(mTempResult));
	}

	public void handleNumber(String num) {
		if (mLastInput.equals("0"))
			mLastInput = num;
		else
			mLastInput = mLastInput + num;
		mCalcDisplay.setText(mLastInput);
	}

}
