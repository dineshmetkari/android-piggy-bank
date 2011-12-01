package com.hangman.piggybank.Utils;

public class Formatter {
	public static String getValueFormatter(double value, int numberOfSigns) {
		return String.format("%.2f", value);
	}
	
	public static String getValueSQLiteFormatter(double value, int numberOfSigns) {
		String result = String.format("%.2f", value);
		result = result.replace(',', '.');
		return result;
	}
}
