package com.hangman.piggybank.ViewControllers;

import java.util.ArrayList;
import java.util.Hashtable;

import com.hangman.piggybank.R;
import com.hangman.piggybank.ArrayAdapters.StuffArrayAdapter;
import com.hangman.piggybank.Models.PiggyBank;
import com.hangman.piggybank.Models.StuffElement;
import com.hangman.piggybank.Models.WishiesProcessor;
import com.hangman.piggybank.Utils.Formatter;
import com.hangman.piggybank.ViewControllers.AddStuffActivity.AddStuffActivityResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class PiggyBankActivity extends Activity {
	
	/**
	 * Variants for user selection while wish deletion. 
	 * @author pavel.todorov
	 */
	protected enum WishDeleteSelectors {
		Substruct(0),	//!< Substruct wish amount from total amount.
		UserSpecify(1),	//!< User want to specify value for substruct from total.
		LeaveAlone(2),		//!< Do not substruct anything.
		Unspecified(100);	//!< Selector is not selected yet. 
		
		private int _id = 0;
		
		WishDeleteSelectors(int id) {
			_id = id;
		}
		
		int getId() {
			return _id;
		}
		
		public static String getClassName() {
			return WishDeleteSelectors.class.getName();
		}
		
	};
	
	final static private int ChangeAmountActivityId = 1;
	final static private int AddStuffActivityId = 2;
	
	final static private String TAG = "PiggyBankActivity";
	
	TextView TextView_amount;
	ListView ListView_wishes;
	
	/**
	 * It's current selection for delete wish operation. 
	 */
	private WishDeleteSelectors _currentWishDeleteSelector = WishDeleteSelectors.Unspecified;

	/**
	 * It's current result data while change wish activity operation.
	 */
	private AddStuffActivity.AddStuffActivityResult _currentChangeWishActivityResult = null;
	
	/**
	 * Current changing wish key.
	 */
	private int _currentChangeWishActivityKey = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((View)findViewById(R.id.change_amount)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "Change amount button clicked.");
				final Intent i = new Intent(PiggyBankActivity.this, ChangeAmountActivity.class);
				i.putExtra(ChangeAmountActivity.modeKey, ChangeAmountActivity.Modes.ChangeMode.toString());
				PiggyBankActivity.this.startActivityForResult(i, ChangeAmountActivityId);
			}
        });
        
        TextView_amount = (TextView)findViewById(R.id.amount);
        double amount = PiggyBank.getInstance().getResourceValue(0);
        TextView_amount.setText(Formatter.getValueFormatter(amount, 2));
        
        findViewById(R.id.add_stuff).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Add stuff button clicked.");
				final Intent i = new Intent(PiggyBankActivity.this, AddStuffActivity.class);
				PiggyBankActivity.this.startActivityForResult(i, AddStuffActivityId);
			}
        });
        
        
        ListView_wishes = (ListView)findViewById(R.id.stuffList);
        ListView_wishes.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Log.i(TAG, "Change stuff item clicked.");
				final Intent i = new Intent(PiggyBankActivity.this, AddStuffActivity.class);
				Integer key = (Integer)view.getTag();
				Log.d(TAG, String.format("Changing record id key is %d.", key.intValue()));
				i.putExtra(AddStuffActivity.ChangeStuffActivityWishIdOptionKey, key.intValue());
				PiggyBankActivity.this.startActivityForResult(i, AddStuffActivityId);
			}
        });
        updateWishesList();
    }

    /**
     * Create data for UI wish list updation.
     * @return Array list with info about each element in UI list.
     */
    protected ArrayList<StuffElement> createStuffUIElementsData() {
        final Hashtable<Integer, PiggyBank.WishRecord> wishes = PiggyBank.getInstance().getWishes();
        
        ArrayList<StuffElement> dataList = new ArrayList<StuffElement>();
        for(int key: wishes.keySet()) {
        	PiggyBank.WishRecord wish = wishes.get(key);
            StuffElement element = new StuffElement(key, wish.name, wish.amount, wish.priority);
            element.setCurrentAmount(0.0);
            dataList.add(element);
        }

        WishiesProcessor wishiesProcessor = new WishiesProcessor(dataList);
        wishiesProcessor.processWishiesWithAmount(PiggyBank.getInstance().getResourceValue(0));
        
    	return  wishiesProcessor.getDataList();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == ChangeAmountActivityId) {
    		Log.i(TAG, "Change amount activity result processing...");
    		if(resultCode != Activity.RESULT_OK) {
    			Log.d(TAG, "Change amount activity cancelled.");
    			return;
    		}
    		
    		updateAmount();
    		updateWishesList();
    	}
    	else if(requestCode == AddStuffActivityId) {
    		Log.i(TAG, "Add stuff activity result processing...");
    		if(resultCode != Activity.RESULT_OK) {
    			Log.d(TAG, "Add stuff activity cancelled.");
    			return;
    		}

    		if(data == null) {
    			Log.d(TAG, "No data has returned. Nothing to do.");
    			return;
    		}
    		
    		AddStuffActivity.AddStuffActivityResult result = (AddStuffActivityResult) data.getSerializableExtra(AddStuffActivity.AddStuffActivityResultDataKey);
    		int key = data.getIntExtra(AddStuffActivity.ChangeStuffActivityWishIdOptionKey, -1);
    		
    		if(key == -1) {
    			//Adding wish mode.
    			addData(result);
    			return;
    		}
    	
			//Changing wish mode.
    		String actionString = data.getStringExtra(AddStuffActivity.ChangeStuffActivityActionOptionKey);
    		AddStuffActivity.AddStuffResultActions action = AddStuffActivity.AddStuffResultActions.valueOf(actionString);
    		if(action == AddStuffActivity.AddStuffResultActions.Update) {
    			updateData(key, result);
    		}
    		else if(action == AddStuffActivity.AddStuffResultActions.Delete) {
    			PiggyBankActivity.this._currentChangeWishActivityResult = result;
    			PiggyBankActivity.this._currentChangeWishActivityKey  = key;
    			showDeleteSelectionDialog();
    		}
    	}
    }

    /**
     * Update current amount value on UI.
     */
    private void updateAmount() {
		double value = PiggyBank.getInstance().getResourceValue(0);
		TextView_amount.setText(Formatter.getValueFormatter(value, 2));
		TextView_amount.requestLayout();
	}

	/**
     * Add wish to database.
     * @param result Wish data.
     */
	private void addData(AddStuffActivityResult result) {
		Log.d(TAG, String.format("Adding data to data base: %s, %s, %d, \'%s\'.", result.name, Formatter.getValueFormatter(result.amount, 2), result.priority, result.note));
		
		PiggyBank.getInstance().changeWishedStuff(0, result.name, result.amount, result.priority, result.note);
		
		updateWishesList();
	}
	
	/**
	 * Update wish in database.
	 * @param key Wish id.
	 * @param result Wish data.
	 */
	private void updateData(int key, AddStuffActivityResult result) {
		Log.d(TAG, String.format("Updating data in data base for key %d: %s, %s, %d, \'%s\'.", key, result.name, Formatter.getValueFormatter(result.amount, 2), result.priority, result.note));
		
		PiggyBank.getInstance().changeWishedStuff(key, result.name, result.amount, result.priority, result.note);
		
		updateWishesList();
	}

	/**
	 * Delete wish data from database.
	 * @param key Id for data to delete.
	 */
	private void deleteData(int key) {
		Log.d(TAG, String.format("Deleting data in data base for key %d", key));
		
		PiggyBank.getInstance().deleteWish(key);
		
		updateWishesList();
	}
	
	/**
	 * Update UI wishes list.
	 */
	private void updateWishesList() {
        ArrayList<StuffElement> dataList = createStuffUIElementsData();

        StuffArrayAdapter stuff = new StuffArrayAdapter(this, R.layout.stuff_element_layout, dataList);
        ListView_wishes.setAdapter(stuff);	
	}
	
	/**
	 * Show dialog for selection how to change current amount after wish deletion.
	 */
	private void showDeleteSelectionDialog() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.drop_down_dialog_layout, (ViewGroup)findViewById(R.id.drop_down_dialog_root));

		Spinner spinner = (Spinner)layout.findViewById(R.id.drop_down_dialog_selection);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.wish_delete_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.checked_dropdown_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, String.format("Item selected: %d", id));
				switch((int)id) {
				case 0: _currentWishDeleteSelector = WishDeleteSelectors.Substruct; break;
				case 1: _currentWishDeleteSelector = WishDeleteSelectors.UserSpecify; break;
				case 2: _currentWishDeleteSelector = WishDeleteSelectors.LeaveAlone; break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				_currentWishDeleteSelector = WishDeleteSelectors.Unspecified;
				Log.w(TAG, "Nothing deletion options selected");
			}
        });
		
		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setCancelable(true);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, String.format("Ok button on selection dialog pressed. Item selected: %s.", PiggyBankActivity.this._currentWishDeleteSelector));
				switch(_currentWishDeleteSelector) {
				case Substruct: 
					double value = PiggyBank.getInstance().getResourceValue(0);
					value -= _currentChangeWishActivityResult.amount;
					PiggyBank.getInstance().setResourceValueWithoutProgressChanging(0, value);
					updateAmount();
					deleteData(_currentChangeWishActivityKey);
					break;
				case UserSpecify:
					// TODO
					updateAmount();
					deleteData(_currentChangeWishActivityKey);
					break;
				case LeaveAlone: 
					deleteData(_currentChangeWishActivityKey);
					break;
				case Unspecified: break;
				}
				_currentChangeWishActivityResult = null;
				_currentChangeWishActivityKey = 0;
				_currentWishDeleteSelector = WishDeleteSelectors.Unspecified;
				return;
			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}
}
