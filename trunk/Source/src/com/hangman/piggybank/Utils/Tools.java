package com.hangman.piggybank.Utils;

import com.hangman.piggybank.PiggyBankApplication;
import com.hangman.piggybank.R;

public class Tools {
	
	/**
	 * Return number of signs in number.
	 * @param number Source number.
	 * @return Number of signs.
	 * @throws RuntimeException if number equals nil. 
	 */
	public static int numberLevel(double number) {
		if(number == 0.0)
			return 0;
			//throw new RuntimeException("Couldn't compute number level.");
		return (int)Math.log10(number);
	}
	
	/**
	 * Return string progress representation.
	 * @param progress Current progress in values per second.
	 * @param amountToComplite Number of values to complite wish.
	 * @return String with progress estimation.
	 */
	public static String getProgressString(double progress, double amountToComplite) {
		String progressString;
		double theProgress = progress/60.0;
		
		if(amountToComplite == 0.0)
			progressString = PiggyBankApplication.getContext().getString(R.string.estimation_time_complete);
		else if(theProgress < 1)
			progressString = String.format("%s.", PiggyBankApplication.getContext().getString(R.string.estimation_time_soon));
		else if(theProgress < 60.0)
			progressString = String.format(PiggyBankApplication.getContext().getString(R.string.estimation_time_minuts), Math.round(theProgress));
		else if(theProgress/60.0 < 24.0)
			progressString = String.format(PiggyBankApplication.getContext().getString(R.string.estimation_time_hours), Math.round(theProgress/60.0));
		else 
			progressString = String.format(PiggyBankApplication.getContext().getString(R.string.estimation_time_days), Math.round(theProgress/60.0/24.0));
		
		//It's debug code. Need to comment in release.
		progressString = String.format("%s (%f)", progressString, progress);
		
		return progressString;
	}
	
}
