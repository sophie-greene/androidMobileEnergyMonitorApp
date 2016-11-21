package com.ide.green;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditApp extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_appliance);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		final Spinner channel = (Spinner) findViewById(R.id.sprChannelE);
		final TextView description = (TextView) findViewById(R.id.txtDescriptionE);
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(
				getBaseContext(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		channel.setAdapter(adapter);
		for (int i = 1; i <= 10; i++) {
			adapter.add(i);
		}
		int chn = 0;
		try {
			chn = getIntent().getExtras().getInt("channel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// if the data sent is not zero the operation is edit;
		if (chn > 0) {
			channel.setSelection(chn - 1);
			channel.setEnabled(false);
			// read data from database
			String s = DataProvider.KEY_CHANNEL + " = ?";
			String[] sa;
			sa = new String[1];
			sa[0] = String.valueOf(chn);
			// If the data is new, insert it into the provider.
			Cursor c = getContentResolver().query(
					DataProvider.CONTENT_URI_APP, null, s, sa, null);
			c.moveToFirst();
			if (c.getCount() > 0) {
				description.setText(c
						.getString(DataProvider.COLUMN_DESCRIPTION));
			}
			c.close();

			final Button SaveButton = (Button) findViewById(R.id.btnSaveE);
			SaveButton.setText("Edit");
			SaveButton.setOnClickListener(new View.OnClickListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * android.view.View.OnClickListener#onClick(android.view.View)
				 */
				@Override
				public void onClick(View v) {

					if (description.getText().toString() != null) {

						String s = DataProvider.KEY_CHANNEL + " = ?";
						String[] sa;
						sa = new String[1];
						sa[0] = String.valueOf(channel
								.getSelectedItem());
						ContentValues values = new ContentValues();
						// add main monitor
						values.put(DataProvider.KEY_CHANNEL,
								channel.getSelectedItem()
										.toString());
						values.put(DataProvider.KEY_DESCRIPTION,
								description.getText()
										.toString());
						try {
							getContentResolver()
									.update(DataProvider.CONTENT_URI_APP,
											values, s, sa);
						} catch (SQLException e) {
							Toast.makeText(
									getApplicationContext(),
									"Error while writing to database",
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						launch();
						finish();
					} else {
						Toast.makeText(
								getApplicationContext(),
								"empty description", Toast.LENGTH_SHORT).show();
					}

				}
			});
		}
		final Button arCancelButton = (Button) this
				.findViewById(R.id.btnCancelE);

		arCancelButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				// Perform action on clicks
				launch();
				finish();
			}
		});

	}
	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, Appliance.class);
		startActivity(activity);

	}
}