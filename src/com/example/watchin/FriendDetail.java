package com.example.watchin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FriendDetail extends ActionBarActivity {

	private static final String TAG = FriendDetail.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		// UI Variables
		TextView mFriendUsername;
		ParseImageView mFriendProfilePicture;
		Button mButtonStatus;

		protected String friendId;

		protected String userId = ParseUser.getCurrentUser().getObjectId();
		protected int countThread = 0;

		boolean isFriendExist;
		boolean isFriend;

		ProgressDialog progressDialog;

		Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60
				* 1000L);

		// Parse Variables
		ParseObject friendObj;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_friend_detail,
					container, false);

			// Intent Extra
			friendId = getActivity().getIntent().getStringExtra(
					ParseConstants.KEY_OBJECT_ID);

			friendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);
			mFriendUsername = (TextView) rootView
					.findViewById(R.id.friendUserName);
			mButtonStatus = (Button) rootView.findViewById(R.id.buttonStatus);
			mFriendProfilePicture = (ParseImageView) rootView
					.findViewById(R.id.friendProfilePicture);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			getFriendInformation(); // 4 in 1
			checkRelation();
		}

		/*
		 * Progress Dialog init
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
		 * Check Relation between friend and user
		 */

		private void checkRelation() {
			ParseObject tempFriendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING, tempFriendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						if (total != 0) {
							mButtonStatus.setText("Unfollow");
							mButtonStatus.setOnClickListener(unFollowFriend);
						} else {
							mButtonStatus.setText("Follow");
							mButtonStatus.setOnClickListener(followFriend);
						}
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Action Listener for follow / unfollow friend
		 */

		OnClickListener followFriend = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject tempFriendObj = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, friendId);
				ParseObject object = new ParseObject(
						ParseConstants.TABLE_REL_USER_USER);
				object.put(ParseConstants.KEY_USER_ID, userId);
				object.put(ParseConstants.KEY_FOLLOWING, tempFriendObj);
				object.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							// success
							Toast.makeText(getActivity(),
									"Successful Folowing", Toast.LENGTH_SHORT)
									.show();
							// Reload the page
							onResume();
						} else {
							// error
							errorAlertDialog(e);
						}
					}
				});

			}
		};

		OnClickListener unFollowFriend = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject tempFriendObj = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, friendId);
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(ParseConstants.TABLE_REL_USER_USER);
				query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
				query.whereEqualTo(ParseConstants.KEY_FOLLOWING, tempFriendObj);
				query.getFirstInBackground(new GetCallback<ParseObject>() {

					@Override
					public void done(ParseObject object, ParseException e) {
						if (e == null) {
							object.deleteInBackground();
							Toast.makeText(getActivity(), "Unfollow the user",
									Toast.LENGTH_SHORT).show();
							onResume();
						} else {
							errorAlertDialog(e);
						}
					}
				});

			}
		};

		/*
		 * Get FriendInformation Information
		 */

		// Get All

		private void getFriendInformation() {
			// Set progress dialog
			initProgressDialog();

			String tempFriend = friendId;

			ParseQuery<ParseUser> query = ParseQuery
					.getQuery(ParseConstants.TABLE_USER);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, tempFriend);
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser friend, ParseException e) {
					if (e == null) {
						// success
						String friendName = friend
								.getString(ParseConstants.KEY_NAME);
						ParseFile image = friend
								.getParseFile(ParseConstants.KEY_IMAGE);
						if (image.isDataAvailable() == true) {
							mFriendProfilePicture.setParseFile(image);
							mFriendProfilePicture.loadInBackground();
						}
						mFriendUsername.setText(friendName);
						progressDialog.dismiss();

					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

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

}
