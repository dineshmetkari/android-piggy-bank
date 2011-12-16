package com.hangman.piggybank.Models;

import java.util.ArrayList;

public class WishiesProcessor {
	
	/**
	 * Number of priorities.
	 */
	final static int PrioritiesCount = 6;
	
	/**
	 * Priority for ignoring.
	 */
	final static int IgnorePriority = 5;
	
	/**
	 * List for all wishies.
	 */
    ArrayList<StuffElement> _dataList;

    /**
     * Sum of priorities weights for each priority.
     */
	ArrayList<Double> _priorityWeightCount = new ArrayList<Double>();
	
	/**
	 * Count of elements of each priority. 
	 */
	ArrayList<Integer> _priorityCount = new ArrayList<Integer>();
	
	/**
	 * Sum of all priorities weights.
	 */
	Double _prioritiesWeight = 0.0;
	
    /**
     * Constructor.
     * @param list List of users wishies.
     */
    public WishiesProcessor(ArrayList<StuffElement> list) {
    	_dataList = list;
    	reset();
    }
    
    /**
     * Set class to first initiate state.
     */
    public void reset() {
		for(int i = 0; i < PrioritiesCount; i++) {
			_priorityWeightCount.add(0.0);
			_priorityCount.add(0);
		}    	
    	
        for(int i = 0; i < _dataList.size(); i++) {
        	StuffElement element = _dataList.get(i);
        	if(element.getPriority() == IgnorePriority)
        		continue;
        	double priorityWeight = 0.5 / (double)(element.getPriority() + 1);
        	_prioritiesWeight += priorityWeight; 
        	_priorityWeightCount.set(element.getPriority(), _priorityWeightCount.get(element.getPriority()).doubleValue() + priorityWeight);
        	_priorityCount.set(element.getPriority(), _priorityCount.get(element.getPriority()).intValue() + 1);
        }
    }
    
	/**
	 * Compute weight for selected priority.
	 * @param priority Priority for which needed weight.
	 * @return weight form 0 to 1.
	 */
	public double getWeightForPriority(int priority) {
		if((priority >= PrioritiesCount) || (priority == IgnorePriority))
			return 0.0;
		return _priorityWeightCount.get(priority)/_priorityCount.get(priority)/_prioritiesWeight;
	}

	/**
	 * Exclude one weight for priority from processing.
	 * @param priority
	 */
	public void excludeWeightForPriority(int priority) {
		double priorityWeight = 0.5 / (double)(priority + 1);
		_prioritiesWeight -= priorityWeight; 
		_priorityWeightCount.set(priority, _priorityWeightCount.get(priority).doubleValue() - priorityWeight);
		_priorityCount.set(priority, _priorityCount.get(priority).intValue() - 1);
	}
	
	/**
	 * Compute current amount for all wishies and exclude overweight weights.
	 * @param Amount for wishies.
	 * @return Sum which wasn't needed for updation on that stage or null if all wishies overweight. 
	 */
	public double updateWishiesWithAmount(double value) {
		double economy = 0.0;
		boolean allOverweight = true;
		final ArrayList<Integer> prioritiesForExclude = new ArrayList<Integer>();
        for(int i = 0; i < _dataList.size(); i++) {
        	StuffElement element = _dataList.get(i);
        	if(element.isEnded() || (element.getPriority() == IgnorePriority))
        		continue;
            double amount = value *	getWeightForPriority(element.getPriority());
            if(amount > element.getNeededAmount()) {
            	economy += amount - element.getNeededAmount();
            	amount = element.getNeededAmount();
            	
            	prioritiesForExclude.add(element.getPriority());
                element.setCurrentAmount(amount);
            }
            else {
            	allOverweight = false;
            	element.setCurrentAmount(element.getCurrentAmount() + amount);
            }
            _dataList.set(i, element);        	
        }
        
        for(Integer priority: prioritiesForExclude)
        	excludeWeightForPriority(priority.intValue());
        
        if(!allOverweight)
        	return economy;
        else
        	return 0.0;
	}
	
	/**
	 * Compute currentAmount for all wishies with point on overweighted and etc.
	 * @param amount Amount for processing.
	 */
	public void processWishiesWithAmount(double amount) {
		double currentAmount = amount;
		while(currentAmount > 0.0)
			currentAmount = updateWishiesWithAmount(currentAmount);
	}
	
	/**
	 * Get list of wishies.
	 * @return List of wishies.
	 */
	public ArrayList<StuffElement> getDataList() {
		return _dataList;
	}
}
