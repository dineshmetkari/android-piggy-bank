package com.hangman.piggybank.ViewControllers;
import com.hangman.piggybank.R;
import com.hangman.piggybank.Models.PiggyBank;
import com.hangman.piggybank.Utils.Formatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ChangeAmountActivity extends Activity {
	public enum Modes {
		ChangeMode,
		EnterMode
	}
	
	final static String TAG = "ChangeAmountActivity";
	public final static String modeKey = "ChangeAmountActivityMode";
	public final static String amountKey = "ChangeAmountActivityAmount";
	
	Modes _currentMode = Modes.ChangeMode;
	
	Button _numeric_buttons[] = new Button[10];
	
	Button _button_set;
	Button _button_point;
	Button _button_plus;
	Button _button_minus;
	Button _button_clear;
	Button _button_back;
	TextView _amount;
	TextView _computation;
	int _pointPosition = -1;	///< Position of the point sign.
	int _digitalSignsCount = 2;
	double _amountValue;	///< Current amount value.

	double _currentOperationSign = 1.0;
	String _currentArgumentText = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_amount_layout);
     
        _currentMode =  Modes.valueOf(getIntent().getStringExtra(ChangeAmountActivity.modeKey));
        Log.i(TAG, String.format("Current mode is \'%s\'", _currentMode.toString()));
        
        if(_currentMode == Modes.EnterMode)
        	_amountValue = getIntent().getDoubleExtra(amountKey, 0.0);
        
        _amount = (TextView)findViewById(R.id.text_view_amount);
        _computation = (TextView)findViewById(R.id.text_view_computation);
        
        _numeric_buttons[0] = (Button)findViewById(R.id.button_0);
        _numeric_buttons[1] = (Button)findViewById(R.id.button_1);
        _numeric_buttons[2] = (Button)findViewById(R.id.button_2);
        _numeric_buttons[3] = (Button)findViewById(R.id.button_3);
        _numeric_buttons[4] = (Button)findViewById(R.id.button_4);
        _numeric_buttons[5] = (Button)findViewById(R.id.button_5);
        _numeric_buttons[6] = (Button)findViewById(R.id.button_6);
        _numeric_buttons[7] = (Button)findViewById(R.id.button_7);
        _numeric_buttons[8] = (Button)findViewById(R.id.button_8);
        _numeric_buttons[9] = (Button)findViewById(R.id.button_9);

        _button_point = (Button)findViewById(R.id.button_point);
        _button_set = (Button)findViewById(R.id.button_set);
        _button_clear = (Button)findViewById(R.id.button_clear);
        _button_plus = (Button)findViewById(R.id.button_plus);
        _button_minus = (Button)findViewById(R.id.button_minus);
        _button_back = (Button)findViewById(R.id.button_back);
        
        _button_clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ChangeAmountActivity.this.resetButtonsState();
				vibrate();
			}
        });        
        
        final OnClickListener operationClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.button_plus)
					_currentOperationSign = 1.0;
				else
					_currentOperationSign = -1.0;
				setNumberButtonsEnabled(true);
				repaintComputationText(true, _currentArgumentText);
				vibrate();
			}
        };
        _button_plus.setOnClickListener(operationClickListener);
        _button_minus.setOnClickListener(operationClickListener);
        
        final OnClickListener numberInputElementClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = (String)v.getTag();
				vibrate();
				if(tag != null) {
					if((_pointPosition > 0) && 
						(_pointPosition <= _currentArgumentText.length() - _digitalSignsCount - 1))
						return;
					_currentArgumentText = _currentArgumentText + tag;
				}
				switch(v.getId()) {
				case R.id.button_back:
					if(_currentArgumentText.length() == 0)
						return;
					if(_currentArgumentText.endsWith("."))
						_pointPosition = -1;
					_currentArgumentText = _currentArgumentText.substring(0, _currentArgumentText.length()-1);
					break;
				case R.id.button_point:
					if((_currentArgumentText.length() == 0))
						return;
					if(_currentArgumentText.indexOf(".") != -1)
						return;
					_currentArgumentText = _currentArgumentText + ".";
					_pointPosition = _currentArgumentText.length() - 1;
					break;
				}
				repaintComputationText(true, _currentArgumentText);
				_button_set.setEnabled(_currentArgumentText.length() > 0);
			}
        };
        
        for(Button button: _numeric_buttons)
        	button.setOnClickListener(numberInputElementClickListener);
        
        _button_point.setOnClickListener(numberInputElementClickListener);
        _button_back.setOnClickListener(numberInputElementClickListener);
        
        _button_set.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				if(_currentMode == Modes.ChangeMode) {
					double currentAmount = _amountValue;
					double currentArgument = Double.parseDouble(_currentArgumentText);
					currentAmount = currentAmount + _currentOperationSign*currentArgument;
					_amountValue = currentAmount;
					repaintComputationText(false, "");
					_currentArgumentText = "";
					PiggyBank.getInstance().setResourceValue(0, _amountValue);
				}
				else {
					double currentArgument = Double.parseDouble(_currentArgumentText);
					_amountValue = currentArgument;
					Log.d(TAG, String.format("Result from ChangeAmountActivity is %f.", _amountValue));
					i.putExtra(amountKey, _amountValue);
				}
				i.putExtra(modeKey, _currentMode.toString());
				_amount.requestLayout();
				_amount.setText(Formatter.getValueFormatter(_amountValue, 2));
				vibrate();
				ChangeAmountActivity.this.setResult(RESULT_OK, i);
				finish();
			}
        });
        
        _currentArgumentText = "";

        resetButtonsState();
    }

    private void resetButtonsState() {
		if(_currentMode == Modes.ChangeMode) {
	        _amountValue = PiggyBank.getInstance().getResourceValue(0);
			
			setOperationButtonsEnabled(true);
			setNumberButtonsEnabled(true);
			_button_set.setEnabled(false);
			((TextView)findViewById(R.id.title_text_view)).setText(getString(R.string.total));
			
			_computation.setText(Formatter.getValueFormatter(_amountValue, 2));
		}
		else {
			setOperationButtonsEnabled(false);
			setNumberButtonsEnabled(true);
			_button_set.setEnabled(true);
			((TextView)findViewById(R.id.title_text_view)).setText(getString(R.string.current));
			
	        _computation.setText("");
		}
		
		_currentArgumentText = "";
        _amount.setText(Formatter.getValueFormatter(_amountValue, 2));
		repaintComputationText(false, _currentArgumentText);
    }
    
	private void repaintComputationText(boolean needToPaintSign, String argument) {
		if(_currentMode == Modes.ChangeMode) {
			String string1 = Formatter.getValueFormatter(_amountValue, 2);
			String string2 = "";
			if(needToPaintSign) {
				if(_currentOperationSign > 0.0)
					string2 = "+";
				else 
					string2 = "-";
			}
			_computation.setText(string1 + "\n" + string2 + "\n" + argument);
		}
		else
			_computation.setText("\n" + argument + "\n");
	}

    public void setNumberButtonsEnabled(boolean flag) {
        for(Button button: _numeric_buttons)
        	button.setEnabled(flag);
    	_button_point.setEnabled(flag);
    	_button_back.setEnabled(flag);    	
    }

    public void setOperationButtonsEnabled(boolean flag) {
        _button_plus.setEnabled(flag);
        _button_minus.setEnabled(flag);    	
    }
    
    public void vibrate() {
    	Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    	vibrator.vibrate(25);
    }
    
    @Override
    public void onBackPressed() {
    	setResult(RESULT_CANCELED);
    	finish();
    }
}
