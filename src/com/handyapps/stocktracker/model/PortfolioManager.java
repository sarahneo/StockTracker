package com.handyapps.stocktracker.model;

import com.handyapps.stocktracker.database.DbAdapter;

public class PortfolioManager {

	public PortfolioManager() {
	}

	public static long getPortfolioCount() {
		long result = DbAdapter.getSingleInstance().countPortfolioList();
		return result;
	}

	/**
	 * delete single portfolio
	 */
	public static boolean deletePorfolioByPortId(int portId) {

		int mPortId = portId;

		long isDeletedPortfolio = DbAdapter.getSingleInstance().deletePortfolio(mPortId);

		if (isDeletedPortfolio > -1) {

			long isDeletedPortStock = DbAdapter.getSingleInstance().deletePortfolioStockByPortId(mPortId);
			if (isDeletedPortStock > -1) {
				long isDeletedTransaction = DbAdapter.getSingleInstance().deleteTransactionByPortfolioId(mPortId);

				if (isDeletedTransaction > -1) {
					long isDeletedAllCash = DbAdapter.getSingleInstance().deleteCashPosByPortId(mPortId);
					if (isDeletedAllCash > -1) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}			
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * update single portfolio
	 */
	public static boolean updatePortfolio(int portId, String portfolioName) {

		PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);

		if (po != null) {
			po.setName(portfolioName);			
			boolean isUpdated = po.update();
			if (isUpdated) {
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean deleteStockTransactionOfSinglePortfolio(int portId, int stockId) {

		int mPortId = portId;
		int mStockId = stockId;

		long isDeletedPortStock = DbAdapter.getSingleInstance().deletePortfolioStockByPortIdAndStockId(mPortId, mStockId);
		if (isDeletedPortStock > -1) {
			long isDeletedTransaction = DbAdapter.getSingleInstance().deleteTransactionByPortIdAndStockId(mPortId, mStockId);
			if (isDeletedTransaction > -1) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
}
