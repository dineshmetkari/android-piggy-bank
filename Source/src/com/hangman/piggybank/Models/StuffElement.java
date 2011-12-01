package com.hangman.piggybank.Models;

/**
 * Class for saving data about user wish.
 * Used when need to make UI update or etc.
 * @author pavel.todorov
 *
 */
public class StuffElement {
	
	/**
	 * Key for element in database. 
	 */
	int _id = -1;
	
	/**
	 * Name of stuff element.
	 */
	String _name = "";
	
	/**
	 * Amount needed for get the stuff.
	 */
	double _neededAmount = 0.0;
	
	/**
	 * Current amount for the stuff.
	 */
	double _currentAmount = 0.0;
	
	/**
	 * Stuff priority.
	 */
	int _priority = 0;

	/**
	 * Constructor for StuffElement
	 * @param name Stuff name.
	 * @param neededAmount Amount needed for get the stuff.
	 * @param priority Stuff priority.
	 */
	public StuffElement(int key, String name, double neededAmount, int priority) {
		_id = key;
		_name = name;
		_neededAmount = neededAmount;
		_priority = priority;
	}
	
	/**
	 * Get stuff name.
	 * @return Name for stuff.
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Set stuff name.
	 * @param name Stuff name.
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * Get amount needed for stuff.
	 * @return Amount value.
	 */
	public double getNeededAmount() {
		return _neededAmount;
	}
	
	/**
	 * Set stuff amount.
	 * @param needed New amount value.
	 */
	public void setNeededAmount(double needed) {
		_neededAmount = needed;
	}
	
	/**
	 * Get current amount for stuff.
	 * @return Amount value.
	 */
	public double getCurrentAmount() {
		return _currentAmount;
	}
	
	/**
	 * Set current stuff amount.
	 * @param amount New current amount value.
	 */
	public void setCurrentAmount(double amount) {
		_currentAmount = amount;
	}
	
	/**
	 * Return is wish ended.
	 * @return true if current wish amount is equal nedded wish amount. 
	 */
	public boolean isEnded() {
		return (_currentAmount == _neededAmount);
	}
	
	/**
	 * Get key or id for current element in database.
	 * @return id for current element in database.
	 */
	public int getId() {
		return _id;
	}
	
	/**
	 * Get priority for stuff element.
	 * @return Priority value.
	 */
	public int getPriority() {
		return _priority;
	}
}
