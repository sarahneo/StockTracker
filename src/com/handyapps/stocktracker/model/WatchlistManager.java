package com.handyapps.stocktracker.model;

import com.handyapps.stocktracker.database.DbAdapter;

public class WatchlistManager {

	public static long getWatchlistCount() {
		long result = DbAdapter.getSingleInstance().countWatchlist();
		return result;
	}

	public static boolean deleteWatchlistByWatchlistId(int watchlistId) {

		int mWatchlistId = watchlistId;
		long isDeletedWatchlist = DbAdapter.getSingleInstance().deleteWatchList(mWatchlistId);

		if (isDeletedWatchlist > -1) {
			long isDeletedWatchStock = DbAdapter.getSingleInstance().deleteWatchStockByWatchlistId(mWatchlistId);

			if (isDeletedWatchStock > -1) {
				return true;
			}
		} else {
			return false;
		}
		return false;
	}

	public static boolean editWatchlist(int watchlistId, String watchlistName) {

		WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByWatchId(watchlistId);

		if (wo != null) {
			wo.setName(watchlistName);
			boolean isUpdated = wo.update();
			if (isUpdated) {
				return true;
			}
			return false;
		}
		return false;
	}

}
