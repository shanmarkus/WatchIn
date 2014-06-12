package com.example.watchin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class HomeFragment extends Fragment {
	protected static final String TAG = HomeFragment.class.getSimpleName();
	ProgressDialog progressDialog;

	// UI Variables
	ParseImageView mHomeProfilePicture;
	TextView mHomeUserName;
	TextView mHomeNumberCheckIn;
	TextView mHomeNumberFollower;
	TextView mHomeNumberFollowing;
	TextView mHomeTextClaimedPromotion;
	TextView mHomeTextRewardPoints;
	ImageButton mHomeStashButton;

	// Fixed Variables
	Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	ArrayList<HashMap<String, String>> userActivities = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> userActivity = new HashMap<String, String>();
	ArrayList<String> promotionsId = new ArrayList<String>();

	// Parse Constants
	String userId;
	ParseObject currentUser;

	public static HomeFragment newInstance(String param1, String param2) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public HomeFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);

		if (ParseUser.getCurrentUser() == null) {
			navigateToLogin();
		} else {
			userId = ParseUser.getCurrentUser().getObjectId();
			currentUser = ParseUser.createWithoutData(
					ParseConstants.TABLE_USER, userId);
		}

		// UI Declaration

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/*
	 * Navigate to Login
	 */
	private void navigateToLogin() {
		Intent intent = new Intent(getActivity(),
				ParseStarterProjectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Progress Dialog initiate
	 */

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	/*
	 * Get User Information include number of check in, follower, following
	 */

	// Get All

	/*
	 * Error Dialog Parse
	 */
	private void errorAlertDialog(ParseException e) {
		// failed
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
