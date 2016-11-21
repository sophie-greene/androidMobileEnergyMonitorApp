package com.ide.green;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

public class GSettings extends Activity {

	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, Main.class);

		startActivity(activity);

	}

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_settings);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		final Spinner spnRange = (Spinner) findViewById(R.id.spnRange);
		final Spinner spnUpdate = (Spinner) findViewById(R.id.spnUpdate);
		final RadioButton line = (RadioButton) findViewById(R.id.line);
		final RadioButton bar = (RadioButton) findViewById(R.id.bar);
		final RadioButton W = (RadioButton) findViewById(R.id.w);
		final RadioButton KW = (RadioButton) findViewById(R.id.kw);
		// read stored value for the spinner
		int pos = 0;
		int val = 0;

		try {
			val = Integer.parseInt(Commons.readFromFile("intHours", this));
		} catch (NumberFormatException e3) {

			e3.printStackTrace();
		} catch (IOException e3) {

			e3.printStackTrace();
		}

		if (val == 6) {
			pos = 1;
		} else if (val == 12) {
			pos = 2;
		} else if (val == 18) {
			pos = 3;
		} else if (val == 24) {
			pos = 4;
		} else {
			pos = 0;
		}
		spnRange.setSelection(pos);
		try {
			val = Integer.parseInt(Commons.readFromFile("intUpdate", this));
		} catch (NumberFormatException e3) {

			e3.printStackTrace();
		} catch (IOException e3) {

			e3.printStackTrace();
		}

		if (val == 2) {
			pos = 1;
		} else if (val == 3) {
			pos = 2;
		} else if (val == 4) {
			pos = 3;
		} else if (val == 5) {
			pos = 4;
		} else if (val == 10) {
			pos = 5;
		} else if (val == 15) {
			pos = 6;
		} else if (val == 30) {
			pos = 7;
		} else {
			pos = 0;
		}
		spnUpdate.setSelection(pos);
		// read stored value for graph type
		String value = "something";
		try {
			value = Commons.readFromFile("graphType", this);
		} catch (Exception e) {
			value = "line";
			e.printStackTrace();
		}
		if (value.equals("bar")) {
			bar.setChecked(true);
		} else {
			line.setChecked(true);
		}
		// read stored value for data type
		value = "something";
		try {
			value = Commons.readFromFile("dataType", this);
		} catch (Exception e) {
			value = "W";
			e.printStackTrace();
		}
		if (value.equals("KW")) {
			KW.setChecked(true);
		} else {
			W.setChecked(true);
		}

		final Button SaveButton = (Button) findViewById(R.id.btnSaveG);

		SaveButton.setOnClickListener(new View.OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				// save hours
				int intHours = Integer.parseInt(spnRange.getSelectedItem()
						.toString());
				try {
					Commons.writeToFile(String.valueOf(intHours), "intHours",
							GSettings.this);
				} catch (IOException e2) {

					e2.printStackTrace();
				}
				int intUpdate = Integer.parseInt(spnUpdate.getSelectedItem()
						.toString());
				try {
					Commons.writeToFile(String.valueOf(intUpdate), "intUpdate",
							GSettings.this);
				} catch (IOException e2) {

					e2.printStackTrace();
				}
				String value = "";
				if (bar.isChecked()) {
					value = "bar";
				} else {
					value = "line";
				}
				try {
					Commons.writeToFile(value, "graphType", GSettings.this);
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				value = "";
				if (KW.isChecked()) {
					value = "KW";
				} else {
					value = "W";
				}
				try {
					Commons.writeToFile(value, "dataType", GSettings.this);
				} catch (IOException e) {

					e.printStackTrace();
				}
				launch();
				finish();

			}

		});
		final Button arCancelButton = (Button) this
				.findViewById(R.id.btnCancelG);

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