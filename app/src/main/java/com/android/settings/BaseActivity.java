package com.android.settings;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class BaseActivity extends Activity {

    private static final String THEME_KEY = "theme_mode";
    private SharedPreferences sharedPreferences;
	private int currThem = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
        sharedPreferences = getSharedPreferences(THEME_KEY, Context.MODE_PRIVATE);
        currThem = sharedPreferences.getInt("theme",0);
		//set the menu style
		switch (currThem) {
		case 1:
		    //复古风格
			setTheme(R.style.Theme_Retro);
			break;
		default:
		    //默认炫彩风格
			setTheme(R.style.Theme_Cool);
			break;
		}
		
		//set the menu language 
		Resources resources = getResources();
		Locale locale = resources.getConfiguration().locale;
		String language = locale.getLanguage();
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		Configuration config = resources.getConfiguration();
		if(language.toLowerCase(Locale.getDefault()).contains("zh")) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
		} else if(language.toLowerCase(Locale.getDefault()).contains("en")) {
			config.locale = Locale.ENGLISH;
		}
		resources.updateConfiguration(config, displayMetrics);
		
		super.onCreate(savedInstanceState);
	}
}
