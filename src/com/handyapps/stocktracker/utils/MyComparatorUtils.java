package com.handyapps.stocktracker.utils;

import java.util.Comparator;

import com.handyapps.stocktracker.model.DistributionObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.StockPosObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.model.WatchlistObject;

public class MyComparatorUtils {
	
	public static class PortfolioNameComparator implements Comparator<PortfolioObject> {
		@Override
		public int compare(PortfolioObject lhs, PortfolioObject rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());			
		}
	}
	
	public static class WatchlistNameComparator implements Comparator<WatchlistObject> {
		@Override
		public int compare(WatchlistObject lhs, WatchlistObject rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());			
		}
	}
	
	public static class SymbolCallbackObjectNameComparator implements Comparator<SymbolCallBackObject> {
		@Override
		public int compare(SymbolCallBackObject lhs, SymbolCallBackObject rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());			
		}
	}
	
	public static class DistributionObjectValueComparator implements Comparator<DistributionObject> {
		@Override
		public int compare(DistributionObject lhs, DistributionObject rhs) {
			return Double.compare(rhs.getDValue(), lhs.getDValue());		
		}
	}
	
	public static class StockPosObjectMktValueComparator implements Comparator<StockPosObject> {
		@Override
		public int compare(StockPosObject lhs, StockPosObject rhs) {
			return Double.compare(rhs.getDMktValue(), lhs.getDMktValue());		
		}
	}
	
	public static class StockPosObjectAbsGainLossComparator implements Comparator<StockPosObject> {
		@Override
		public int compare(StockPosObject lhs, StockPosObject rhs) {
			return Double.compare(Math.abs(rhs.getDGainLoss()), Math.abs(lhs.getDGainLoss()));		
		}
	}

}
