package com.handyapps.stocktracker;

import java.util.Calendar;
import java.util.Random;

import junit.framework.TestCase;

public class TestRandomNum extends TestCase {
	
	public void testRandomNumGen() {

		Calendar cal = Calendar.getInstance();
		int nId = cal.get(Calendar.MILLISECOND);
		Random rand = new Random(nId);
		int rId = rand.nextInt(1000);
		int finalNum = nId*1000 + rId;

		System.out.println("nId: " + nId);
		System.out.println("rId: " + rId);
		System.out.println("Random number is: " + finalNum);
	}

}
