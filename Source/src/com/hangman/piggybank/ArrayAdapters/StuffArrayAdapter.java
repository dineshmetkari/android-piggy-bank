package com.hangman.piggybank.ArrayAdapters;

import java.util.List;

import com.hangman.piggybank.R;
import com.hangman.piggybank.Models.PiggyBank;
import com.hangman.piggybank.Models.StuffElement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StuffArrayAdapter extends ArrayAdapter<StuffElement> {
	private final Context _context;
	private final List<StuffElement> _list;
	
	/**
	 * Constructor.
	 * @param context Context.
	 * @param resourceId Resource for stuff element view. 
	 * @param objects List with data.
	 */
	public StuffArrayAdapter(Context context, int resourceId,
			List<StuffElement> objects) {
		super(context, resourceId, objects);
		
		_context = context;
		_list = objects;
	}

	private String getProgressString(double progress, double amountToComplite) {
		String progressString;
		if(amountToComplite == 0.0)
			progressString = _context.getString(R.string.estimation_time_complete);
		else if(progress < 1)
			progressString = String.format("%s (%f)", _context.getString(R.string.estimation_time_soon), progress);
		else
			progressString = String.format("%s (%f).", String.format(_context.getString(R.string.estimation_time_days), Math.round(progress)), progress);
		return progressString;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.stuff_element_layout, parent, false);
		StuffElement element = _list.get(position);
		((TextView)rowView.findViewById(R.id.stuffElementName)).setText(element.getName());
		ProgressBar progressBar = (ProgressBar)rowView.findViewById(R.id.stuffElementProgress);
		progressBar.setMax(100);
		progressBar.setProgress((int) Math.round(element.getCurrentAmount()*100.0/element.getNeededAmount()));
		
		/** It's right code.
			double progressPerDay = (element.getNeededAmount() - element.getCurrentAmount()) / PiggyBank.getInstance().getResourceProgress(0)/60.0/60.0/24.0;
		*/
		
		double progressPerDay = (element.getNeededAmount() - element.getCurrentAmount()) / PiggyBank.getInstance().getResourceProgress(0)/60.0;
		
		String progressString = this.getProgressString(progressPerDay, element.getNeededAmount() - element.getCurrentAmount());
		((TextView)rowView.findViewById(R.id.stuffElementEstimationTime)).setText(progressString);
		rowView.setTag(new Integer(element.getId()));
		return rowView;
	}

}
