package com.example.watchin;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import android.app.Application;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "4EkDooudMyzhEGlZLQbGBk1fI1KSy5q4HKnXST2E",
				"7XLkG5yFifLaSqZlGHeN3syJyflphTemixCmVV7c");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		defaultACL.setPublicReadAccess(true);

		ParseACL.setDefaultACL(defaultACL, true);
	}

}
