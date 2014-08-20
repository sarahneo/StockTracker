package com.handyapps.stocktracker.model;

import java.util.ArrayList;
import java.util.List;

public class CashManager {

	public static ArrayList<Double> getCashAmountsArray(List<CashPosObject> cpoList, String initialCash) {
		double dTotDeposits = 0, dTotWithdrawals = 0, dInitialBalance = 0, dFinalBalance = 0;
		ArrayList<Double> cashAmounts = new ArrayList<Double>(4);
		
		for (CashPosObject cpo : cpoList) {
			if (cpo.getTxnType().equals("cd"))
				dTotDeposits += Double.parseDouble(cpo.getAmount());
			else if (cpo.getTxnType().equals("cw"))
				dTotWithdrawals += Double.parseDouble(cpo.getAmount());
			
		}
		
		if (!initialCash.equals(""))
			dInitialBalance = Double.parseDouble(initialCash);
		
		dFinalBalance = dInitialBalance + dTotDeposits - dTotWithdrawals;
		cashAmounts.add(dInitialBalance);
		cashAmounts.add(dTotDeposits);
		cashAmounts.add(dTotWithdrawals);
		cashAmounts.add(dFinalBalance);
		
		return cashAmounts;
	
	}
	
	
	public static double getTotalWithdrawals(List<CashPosObject> cpoList, String initialCash) {
		double dTotWithdrawals = 0;
		
		for (CashPosObject cpo : cpoList) {
			if (cpo.getTxnType().equals("cw"))
				dTotWithdrawals += Double.parseDouble(cpo.getAmount());
			
		}
		
		return dTotWithdrawals;
	
	}
	
	
	public static double getTotalDeposits(List<CashPosObject> cpoList, String initialCash) {
		double dTotDeposits = 0;
		
		for (CashPosObject cpo : cpoList) {
			if (cpo.getTxnType().equals("cd"))
				dTotDeposits += Double.parseDouble(cpo.getAmount());
			
		}
						
		return dTotDeposits;
	}
	
	
	public static double getFinalBalance(List<CashPosObject> cpoList, String initialCash) {
		double dTotDeposits = 0, dTotWithdrawals = 0, dInitialBalance = 0, dFinalBalance = 0;
		
		for (CashPosObject cpo : cpoList) {
			if (cpo.getTxnType().equals("cd"))
				dTotDeposits += Double.parseDouble(cpo.getAmount());
			else if (cpo.getTxnType().equals("cw"))
				dTotWithdrawals += Double.parseDouble(cpo.getAmount());
			
		}
		
		if (!initialCash.equals(""))
			dInitialBalance = Double.parseDouble(initialCash);
		
		dFinalBalance = dInitialBalance + dTotDeposits - dTotWithdrawals;
		
		return dFinalBalance;
	
	}

}
