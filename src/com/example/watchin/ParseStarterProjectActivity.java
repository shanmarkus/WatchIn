package com.example.watchin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.storein.LoginActivity;
import com.example.storein.MainActivity;
import com.example.storein.R;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ParseStarterProjectActivity extends Activity {
	/** Called when the activity is first created. */

	// UI Declaration
	Button mLoginButton;
	EditText mUserNameField;
	EditText mPasswordField;

	// Variables
	String username;
	String password;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Set up the login form.
		mUserNameField = (EditText) findViewById(R.id.usernameField);
		mPasswordField = (EditText) findViewById(R.id.passwordField);
		mLoginButton = (Button) findViewById(R.id.loginButton);
		mLoginButton.setOnClickListener(normalLogin);

		ParseAnalytics.trackAppOpened(getIntent());
	}

	/*
	 * Navigate to main Activity function
	 */

	private void navigateToMainActivity(String placeId) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Navigate to main Activity function
	 */

	private void navigateToMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Normal Login Function
	 */

	OnClickListener normalLogin = new OnClickListener() {

		@Override
		public void onClick(View v) {
			username = mUserNameField.getText().toString();
			password = mPasswordField.getText().toString();

			username = username.trim();
			password = password.trim();

			if (username.isEmpty() || password.isEmpty()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ParseStarterProjectActivity.this);
				builder.setMessage(R.string.login_error_message);
				builder.setTitle(R.string.login_error_title);
				builder.setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				// Login
				setProgressBarIndeterminateVisibility(true);
				ParseUser.logInInBackground(username, password,
						new LogInCallback() {

							@Override
							public void done(ParseUser user, ParseException e) {
								setProgressBarIndeterminateVisibility(false);
								if (e == null) {
									navigateToMainActivity();
								} else {
									errorAlertDialog(e);
								}
							}
						});
			}
		}
	};

	/*
	 * Parse Error handling
	 */

	private void errorAlertDialog(ParseException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(e.getMessage()).setTitle(R.string.login_error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
