package com.hangman.piggybank.ViewControllers;

import java.io.Serializable;

import com.hangman.piggybank.R;
import com.hangman.piggybank.Models.PiggyBank;
import com.hangman.piggybank.Utils.Formatter;
import com.hangman.piggybank.Utils.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;

public class AddStuffActivity extends Activity {

	/**
	 * It's modes for adding stuff activities. 
	 * @author pavel.todorov
	 */
	enum AddStuffModes {
		AddingMode,		///< Mode for adding new wish.
		EditMode		///< Mode for edit existing wish.
	}
	
	/**
	 * Actions with wish in edit mode. 
	 * @author pavel.todorov
	 */
	public enum AddStuffResultActions {
		Update,	///< Update wish data.
		Delete	///< Delete wish.
	}
	
	final static String TAG = "AddStuffActivity";
	final static int ChangeAmountActivityId = 0;
	
	public final static String AddStuffActivityResultDataKey = "AddStuffActivityResultDataKey";
	public final static String ChangeStuffActivityWishIdOptionKey = "ChangeStuffActivityWishIdOptionKey";
	public final static String ChangeStuffActivityActionOptionKey = "ChangeStuffActivityActionOptionKey";
	
	AddStuffModes _currentMode = AddStuffModes.AddingMode;
	int _key;					///< Key for wish for EditMode.
	PiggyBank.WishRecord _wish;	///< Wish for EditMode.
	
	EditText _note;
	EditText _name;
	EditText _value;
	Spinner _priority;
	
	double _amount = 0.0;
	
	/**
	 * Class for transferring user wish to parent activity.
	 * It doesn't contain wish id. 
	 * @author pavel.todorov
	 */
	public static class AddStuffActivityResult implements Serializable {
		private static final long serialVersionUID = -3695736214848989515L;

		public String name = "";
		public String note = "";
		public int priority = 0;
		public double amount = 0.0;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.add_desired_stuff_layout);

        extractIntentOptions();
        
        initUIElements();
        
        _value.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Amount value clicked.");
				final Intent i = new Intent(AddStuffActivity.this, ChangeAmountActivity.class);
				i.putExtra(ChangeAmountActivity.modeKey, ChangeAmountActivity.Modes.EnterMode.toString());
				i.putExtra(ChangeAmountActivity.amountKey, _amount);
				AddStuffActivity.this.startActivityForResult(i, ChangeAmountActivityId);
			}
        });
        
        ((ImageButton)findViewById(R.id.add_stuff_ok)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!dataValid()) {
					// Show message box.
					AlertDialog alertDialog = new AlertDialog.Builder(AddStuffActivity.this).create();
					alertDialog.setTitle(getString(R.string.add_stuff_alert_title));
					alertDialog.setMessage(getString(R.string.add_stuff_alert_message));
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					alertDialog.show();
					return;
				}
				
			    Intent i = prepareResultIntent(AddStuffResultActions.Update);
				AddStuffActivity.this.setResult(RESULT_OK, i);
				finish();
			}
        });
        
        ((ImageButton)findViewById(R.id.add_stuff_cancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AddStuffActivity.this.setResult(RESULT_CANCELED);
				finish();
			}
        });
        
        ((ImageButton)findViewById(R.id.add_stuff_amount_plus)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int level = Tools.numberLevel(_amount);
				double offset = Math.pow(10, (double)level);
				_amount += offset;
				AddStuffActivity.this.updateAmount();
			}
        });
        
        ((ImageButton)findViewById(R.id.add_stuff_amount_minus)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int level = Tools.numberLevel(_amount);
				double offset = Math.pow(10, (double)level);
				if(Tools.numberLevel(_amount - offset) != level) {
					level -= 1;
					offset = Math.pow(10, (double)level);
				}
				_amount -= offset;
				if(_amount < 0.0)
					_amount = 0.0;
				AddStuffActivity.this.updateAmount();
			}
        });
        
        if(_currentMode == AddStuffModes.EditMode) {
        	_amount = _wish.amount;
        	_name.setText(_wish.name);
        	_priority.setSelection(_wish.priority);
        	_note.setText(_wish.note);
        	
        	((TableRow)findViewById(R.id.add_stuff_button_row)).setWeightSum(1.5f);
        	ImageButton deleteButton = (ImageButton)findViewById(R.id.add_stuff_delete);
        	deleteButton.setVisibility(View.VISIBLE);
        	deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				    Intent i = prepareResultIntent(AddStuffResultActions.Delete);
					AddStuffActivity.this.setResult(RESULT_OK, i);
					finish();
				}
        	});
        }
        
        updateAmount();
    }

    /**
     * Need to get all needed option from intent.
     */
    private void extractIntentOptions() {
        /// Determining that mode for add stuff activity now.
        _key = getIntent().getIntExtra(ChangeStuffActivityWishIdOptionKey, -1);
        if(_key != -1) {
        	_wish = PiggyBank.getInstance().getWishes().get(_key);
        	if(_wish == null)
        		Log.e(TAG, String.format("onCreate:No wish for key: %d.", _key));
        	else 
        		_currentMode = AddStuffModes.EditMode;
        }    	
    }

    /**
     * Get reference to UI elements that could be needed while using activity.
     */
    private void initUIElements() {
        _priority = (Spinner) findViewById(R.id.add_stuff_priority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.priority_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.checked_dropdown_list);
        _priority.setAdapter(adapter);
        _priority.setSelection(adapter.getCount()/2);
        
        _note = (EditText)findViewById(R.id.addStuffNoteEditText);
        _name = (EditText)findViewById(R.id.addStuffNameEditText);
        
        _value = (EditText)findViewById(R.id.addStuffValueEditText);
    }
    
    /**
     * Create result data for this activity.
     * @return AddSuffActivityResult struct.
     */
    private AddStuffActivityResult createActivityResult() {
    	AddStuffActivityResult resultData = new AddStuffActivityResult();
    	resultData.amount = _amount;
    	resultData.name = _name.getText().toString().trim();
    	resultData.note = _note.getText().toString();
    	resultData.priority = _priority.getSelectedItemPosition();
    	return resultData;
    }

    /**
     * Prepare intent for activity's result.
     * @return Prepared intent.
     */
    private Intent prepareResultIntent(AddStuffResultActions action) {
		AddStuffActivityResult resultData = createActivityResult();
		
		Intent i = new Intent();
		i.putExtra(AddStuffActivityResultDataKey, resultData);
		if(_currentMode == AddStuffModes.EditMode) {
			i.putExtra(ChangeStuffActivityWishIdOptionKey, _key);
			i.putExtra(ChangeStuffActivityActionOptionKey, action.toString());
		}
		return i;
    }
    
    /**
     * Update amount value on it's EditText.
     */
	protected void updateAmount() {
		_value.setText(Formatter.getValueFormatter(_amount, 2));
	}

	/**
	 * Validate data on form to make decision is it could be added to data base. 
	 * @return true if valid, or false otherwise.
	 */
	private boolean dataValid() {
		if(_name.getText().toString().trim().length() == 0)
			return false;
		
		if(_amount <= 0.0)
			return false;
		
		return true;
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == ChangeAmountActivityId) {
    		Log.i(TAG, "Change amount activity result processing...");
    		if(resultCode != Activity.RESULT_OK) {
    			Log.i(TAG, "Result is not OK.");
    			return;
    		}
    			
			Log.i(TAG, "Result is OK.");
    		_amount = data.getDoubleExtra(ChangeAmountActivity.amountKey, 0.0);
    		updateAmount();
    	}
    }
}
