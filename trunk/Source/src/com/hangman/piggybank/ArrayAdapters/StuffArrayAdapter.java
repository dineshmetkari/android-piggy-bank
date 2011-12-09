package com.hangman.piggybank.ArrayAdapters;

import java.util.List;

import com.hangman.piggybank.PiggyBankApplication;
import com.hangman.piggybank.R;
import com.hangman.piggybank.Models.PiggyBank;
import com.hangman.piggybank.Models.StuffElement;
import com.hangman.piggybank.Utils.Formatter;
import com.hangman.piggybank.Utils.Tools;

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
		
		double progressPerSecond = (element.getNeededAmount() - element.getCurrentAmount()) / PiggyBank.getInstance().getResourceProgress(0);
		
		String progressString = Tools.getProgressString(progressPerSecond, element.getNeededAmount() - element.getCurrentAmount());
		((TextView)rowView.findViewById(R.id.stuffElementEstimationTime)).setText(progressString);
		rowView.setTag(new Integer(element.getId()));
		
		String currentValue = String.format(PiggyBankApplication.getContext().getString(R.string.prepared_value_text), 
				Formatter.getValueFormatter(element.getCurrentAmount(), 2),
				Formatter.getValueFormatter(element.getNeededAmount(), 2));
		((TextView)rowView.findViewById(R.id.stuffElementPreparedValue)).setText(currentValue);
		
		return rowView;
	}

}
