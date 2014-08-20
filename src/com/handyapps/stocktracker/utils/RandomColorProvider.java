package com.handyapps.stocktracker.utils;

import java.util.Random;

import android.graphics.Color;

public class RandomColorProvider {
	public static int generateRandomColor(int mix) {
		Random random = new Random();
		int red = random.nextInt(255);
		int green = random.nextInt(255);
		int blue = random.nextInt(255);

		red = (red + Color.red(mix)) / 2;
		green = (green + Color.green(mix)) / 2;
		blue = (blue + Color.blue(mix)) / 2;


		return Color.rgb(red, green, blue);
	}


}
