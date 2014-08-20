package com.handyapps.stocktracker.model;

import java.util.ArrayList;
import java.util.List;

public class UrlManager {

	public static final String summary = "http://finance.yahoo.com/q?s=%s";
	public static final String orderBook = "http://finance.yahoo.com/q/ecn?s=%s+Order+Book";
	public static final String options = "http://finance.yahoo.com/q/op?s=%s+Options";
	public static final String historicalPrices = "http://finance.yahoo.com/q/hp?s=%s+Historical+Prices";

	public static final String interactive = "http://finance.yahoo.com/echarts?s=%s";
	public static final String basicChart = "http://finance.yahoo.com/q/bc?s=%s+Basic+Chart";
	public static final String basicTechAnalysis = "http://finance.yahoo.com/q/ta?s=%s+Basic+Tech.+Analysis";

	public static final String headlines = "http://finance.yahoo.com/q/h?s=%s+Headlines";
	public static final String pressReleases = "http://finance.yahoo.com/q/p?s=%s";
	public static final String companyEvents = "http://finance.yahoo.com/q/ce?s=%s+Company+Events";
	public static final String messageBoards = "http://finance.yahoo.com/mb/%s/";
	public static final String marketPulse = "http://finance.yahoo.com/marketpulse/%s";

	public static final String profile = "http://finance.yahoo.com/q/pr?s=%s+Profile";
	public static final String keyStatistics = "http://finance.yahoo.com/q/ks?s=%s+Key+Statistics";
	public static final String secFilings = "http://finance.yahoo.com/q/sec?s=%s+SEC+Filings";
	public static final String competitors = "http://finance.yahoo.com/q/co?s=%s+Competitors";
	public static final String industry = "http://finance.yahoo.com/q/in?s=%s+Industry";
	public static final String components = "http://finance.yahoo.com/q/ct?s=%s+Components";

	public static final String analystOpinion = "http://finance.yahoo.com/q/ao?s=%s+Analyst+Opinion";
	public static final String analystEstimates = "http://finance.yahoo.com/q/ae?s=%s+Analyst+Estimates";
	public static final String researchReports = "http://finance.yahoo.com/q/rr?s=%s+Research+Reports";

	public static final String majorHolders = "http://finance.yahoo.com/q/mh?s=%s+Major+Holders";
	public static final String insiderTransactions = "http://finance.yahoo.com/q/it?s=%s+Insider+Transactions";
	public static final String insiderRoster = "http://finance.yahoo.com/q/ir?s=%s+Insider+Roster";

	public static final String incomStatement = "http://finance.yahoo.com/q/is?s=%s+Income+Statement&annual";
	public static final String balanceSheet = "http://finance.yahoo.com/q/bs?s=%s+Balance+Sheet&annual";
	public static final String cashFlow = "http://finance.yahoo.com/q/cf?s=%s+Cash+Flow&annual";

	public static List<String> quotesUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(summary);
			add(orderBook);
			add(options);
			add(historicalPrices);
		}
	};

	public static List<String> chartUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(interactive);
			add(basicChart);
			add(basicTechAnalysis);
		}
	};

	public static List<String> newsInfoUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(headlines);
			add(pressReleases);
			add(companyEvents);
			add(messageBoards);
			add(marketPulse);
		}
	};

	public static List<String> companyUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(profile);
			add(keyStatistics);
			add(secFilings);
			add(competitors);
			add(industry);
			add(components);
		}
	};

	public static List<String> analystCoverrageUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(analystOpinion);
			add(analystEstimates);
			add(researchReports);
		}
	};

	public static List<String> ownershipUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(majorHolders);
			add(insiderTransactions);
			add(insiderRoster);

		}
	};

	public static List<String> financialsUrlList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(incomStatement);
			add(balanceSheet);
			add(cashFlow);
		}
	};

	public static List<List<String>> getAllList() {

		List<List<String>> list = new ArrayList<List<String>>();

		list.add(quotesUrlList);
		list.add(chartUrlList);
		list.add(newsInfoUrlList);
		list.add(companyUrlList);
		list.add(analystCoverrageUrlList);
		list.add(ownershipUrlList);
		list.add(financialsUrlList);

		return list;
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////

	/*public static final String chartUrlA = "http://chart.finance.yahoo.com/z?s=%s&t=1d&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlB = "http://chart.finance.yahoo.com/z?s=%s&t=5d&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlC = "http://chart.finance.yahoo.com/z?s=%s&t=1m&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlD = "http://chart.finance.yahoo.com/z?s=%s&t=3m&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlE = "http://chart.finance.yahoo.com/z?s=%s&t=6m&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlF = "http://chart.finance.yahoo.com/z?s=%s&t=1y&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlG = "http://chart.finance.yahoo.com/z?s=%s&t=2y&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlH = "http://chart.finance.yahoo.com/z?s=%s&t=5y&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";
	public static final String chartUrlI = "http://chart.finance.yahoo.com/z?s=%s&t=my&q=&l=&z=l&a=v&p=s&lang=en-US&region=US";*/
	
	public static final String chartUrlA = "http://chart.finance.yahoo.com/z?s=%s&t=1d&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlB = "http://chart.finance.yahoo.com/z?s=%s&t=5d&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlC = "http://chart.finance.yahoo.com/z?s=%s&t=1m&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlD = "http://chart.finance.yahoo.com/z?s=%s&t=3m&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlE = "http://chart.finance.yahoo.com/z?s=%s&t=6m&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlF = "http://chart.finance.yahoo.com/z?s=%s&t=1y&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlG = "http://chart.finance.yahoo.com/z?s=%s&t=2y&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlH = "http://chart.finance.yahoo.com/z?s=%s&t=5y&q=&l=&z=l&p=s&lang=en-US&region=US";
	public static final String chartUrlI = "http://chart.finance.yahoo.com/z?s=%s&t=my&q=&l=&z=lv&p=s&lang=en-US&region=US";

	public static List<String> chartRangeList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add(chartUrlA);
			add(chartUrlB);
			add(chartUrlC);
			add(chartUrlD);
			add(chartUrlE);
			add(chartUrlF);
			add(chartUrlG);
			add(chartUrlH);
			add(chartUrlI);
		}
	};

}
