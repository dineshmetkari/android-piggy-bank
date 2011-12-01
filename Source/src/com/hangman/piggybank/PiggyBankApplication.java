package com.hangman.piggybank;

import android.app.Application;
import android.content.Context;

public class PiggyBankApplication extends Application {
	
	private static Context context;
	
	@Override
	public void onCreate() {
		super.onCreate();
		PiggyBankApplication.context = getApplicationContext();
	}
	
	public static Context getContext() {
		return context;
	}
}
