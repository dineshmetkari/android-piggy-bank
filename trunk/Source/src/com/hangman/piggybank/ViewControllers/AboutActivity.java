package com.hangman.piggybank.ViewControllers;

import com.hangman.piggybank.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        
		TextView _version = (TextView)findViewById(R.id.aboutVersion);
		PackageInfo pi;
		try {
			pi = getPackageManager().getPackageInfo("com.hangman.piggybank", 0);
			_version.setText(pi.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
