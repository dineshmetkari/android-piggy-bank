package com.hangman.piggybank.Models;

public class Resource {
	double _amount = 0.0;
	String _valueUnit = ""; 
	
	/**
	 * Get resource number value.
	 * @return Resource number in some values.
	 */
	public double getAmount() {
		return _amount;
	}
	
	/**
	 * Get resource number representation as string.
	 * @return String with resource number representation.
	 */
	public String getAmountStringRepresentation() {
		return String.format("%f", _amount);
	}
	
	/**
	 * Set resource number.
	 * @param resource New resource number value.
	 */
	public void setAmount(double resource) {
		_amount = resource;
	}
	
	/**
	 * Get values units for resource.
	 * @return String which represent values unit for resource.
	 */
	public String getValuesUnit() {
		return _valueUnit;
	}
	
	/**
	 * Set unit string for resource values.
	 * @param valueUnit String for resource values.
	 */
	public void setValuesUnit(String valueUnit) {
		_valueUnit =  valueUnit;
	}
}
