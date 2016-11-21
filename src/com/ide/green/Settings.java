package com.ide.green;

import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Settings extends Activity {
	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, Main.class);
		startActivity(activity);
		finish();
	}

	/** Called when the activity is first created. */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		final TextView api = (TextView) findViewById(R.id.api);
		final TextView eID = (TextView) findViewById(R.id.eID);

		// read the user api key and the environment code
		// if not stored ask the user to provide

		String FILENAME = "apiFile";

		try {
			String data = Commons.readFromFile(FILENAME, this);
			if (data != null)
				api.setText(data);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FILENAME = "eidFile";

		try {
			String data = Commons.readFromFile(FILENAME, this);
			if (data != null)
				eID.setText(data);
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}

		// int id =Integer.parseInt(getString(R.string.id));
		final Button SaveButton = (Button) findViewById(R.id.btnSave);

		SaveButton.setOnClickListener(new View.OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {

				String FILENAME = "apiFile";
				if (api.getTextSize() > 0) {
					try {
						Commons.writeToFile(api.getText().toString(), FILENAME,
								Settings.this);
					} catch (IOException e) {

						e.printStackTrace();
					}
					FILENAME = "eidFile";
					if (eID.getTextSize() > 0
							&& Commons.isNumber(eID.getText().toString())) {
						try {
							Commons.writeToFile(eID.getText().toString(),
									FILENAME, Settings.this);
						} catch (IOException e) {
							e.printStackTrace();
						}
						// clear the previous data stored in the database
						// reset channel
						try {
							Commons.writeToFile("1", "channel", Settings.this);
						} catch (IOException e) {
							e.printStackTrace();
						}
						ContentResolver cr = getContentResolver();
						cr.delete(DataProvider.CONTENT_URI, null, null);
						cr.delete(DataProvider.CONTENT_URI_APP, null, null);
						cr.delete(DataProvider.CONTENT_URI_H, null, null);
						launch();
						finish();
					} else {
						Commons.displayAlert(
								"feed id needs to be a numeric whole number",
								Settings.this);
					}
				} else {
					Commons.displayAlert(
							"All fields are required. you need to enter API key and Feed ID",
							Settings.this);
				}

			}
		});
		final Button arCancelButton = (Button) this
				.findViewById(R.id.btnCancel);

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

}