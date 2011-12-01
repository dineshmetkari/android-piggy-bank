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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PiggyBankActivity extends Activity {
	final static private int ChangeAmountActivityId = 1;
	final static private int AddStuffActivityId = 2;
	
	final static private String TAG = "PiggyBankActivity";
	
	TextView TextView_amount;
	ListView ListView_wishes;
	
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
    		double value = PiggyBank.getInstance().getResourceValue(0);
    		TextView_amount.setText(Formatter.getValueFormatter(value, 2));
    		TextView_amount.requestLayout();
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
    			PiggyBank.getInstance().deleteWish(key);
    			double value = PiggyBank.getInstance().getResourceValue(0);
    			value -= result.amount;
    			PiggyBank.getInstance().setResourceValueWithoutProgressChanging(0, value);
    			// TODO Need to ask user: Do you want to substruct amount of wish from total amount?
    		}
    	}
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
	 * Update UI wishes list.
	 */
	private void updateWishesList() {
        ArrayList<StuffElement> dataList = createStuffUIElementsData();

        StuffArrayAdapter stuff = new StuffArrayAdapter(this, R.layout.stuff_element_layout, dataList);
        ListView_wishes.setAdapter(stuff);	
	}
}
