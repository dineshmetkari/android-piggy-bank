package com.hangman.piggybank.Utils;

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
}
