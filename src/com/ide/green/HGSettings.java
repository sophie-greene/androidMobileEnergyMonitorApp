package com.ide.green;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class HGSettings extends Activity {
	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, DHistory.class);
		startActivity(activity);
	}

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_graph_settings);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);

		final RadioButton hrs = (RadioButton) findViewById(R.id.hrs);
		final RadioButton dys = (RadioButton) findViewById(R.id.dys);
		final RadioButton wks = (RadioButton) findViewById(R.id.wks);
		final RadioButton mnths = (RadioButton) findViewById(R.id.mnths);
		final RadioButton power = (RadioButton) findViewById(R.id.power);
		final RadioButton money = (RadioButton) findViewById(R.id.money);
		final RadioButton co2 = (RadioButton) findViewById(R.id.co2);

		// read stored value for units
		String value = "something";
		try {
			value = Commons.readFromFile("units",this);
		} catch (Exception e) {
			value = "days";
			e.printStackTrace();
		}
		if (value.equals("hours")) {
			hrs.setChecked(true);
		} else if (value.equals("days")) {
			dys.setChecked(true);
		} else if (value.equals("weeks")) {
			wks.setChecked(true);
		} else {
			mnths.setChecked(true);
		}
		value = "something";
		try {
			value = Commons.readFromFile("historyDataType",this);
		} catch (Exception e) {
			value = "power";
			e.printStackTrace();
		}
		if (value.equals("money")) {
			money.setChecked(true);
		} else if (value.equals("co2")) {
			co2.setChecked(true);
		} else {
			power.setChecked(true);
		}
		final Button SaveButton = (Button) findViewById(R.id.btnSaveH);

		SaveButton.setOnClickListener(new View.OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				String value = "";
				if (hrs.isChecked()) {
					value = "hours";
				} else if (dys.isChecked()) {
					value = "days";
				} else if (wks.isChecked()) {
					value = "weeks";
				} else {
					value = "months";
				}
				try {
					Commons.writeToFile(value, "units",HGSettings.this);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				value = "";
				if (money.isChecked()) {
					value = "money";
				} else if (co2.isChecked()) {
					value = "co2";
				} else {
					value = "power";
				}
				try {
					Commons.writeToFile(value, "historyDataType",HGSettings.this);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				launch();
				finish();
			}
		});
		final Button arCancelButton = (Button) this
				.findViewById(R.id.btnCancelH);

		arCancelButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				// Perform action on clicks
				finish();
				launch();
			}

		});
	}
}