package com.example.watchin;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class SignUp extends Activity {

	protected EditText mUsername;
	protected EditText mPassword;
	protected EditText mPhone;
	protected EditText mEmail;
	protected EditText mName;
	protected Button mSignUpButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
	}
}
