package com.ide.green;

import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class Price extends Activity {

	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, Main.class);
		this.startActivity(activity);

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
		this.setContentView(R.layout.price);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		
		final Button SaveButton = (Button) this.findViewById(R.id.btnSavePr);
		final Button arCancelButton = (Button) this
				.findViewById(R.id.btnCancelPr);
		final RadioButton direct = (RadioButton) this.findViewById(R.id.direct);
		final RadioButton calculate = (RadioButton) this
				.findViewById(R.id.calculate);
		final TextView penny = (TextView) this.findViewById(R.id.penny);
		final TextView cost = (TextView) this.findViewById(R.id.cost);
		
		final TextView noKWH = (TextView) this.findViewById(R.id.noKWH);
		try {
			penny.setText(Commons.readFromFile("price",this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		SaveButton.setOnClickListener(new View.OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {

				if (direct.isChecked()) {
					if (penny.getTextSize() > 0) {
						if (Commons.isNumber(penny.getText().toString())) {
							double price = Double.parseDouble(penny.getText()
									.toString());
							try {
								Commons.writeToFile(String.valueOf(price),
										"price",Price.this);
							} catch (IOException e) {
								e.printStackTrace();
							}
							launch();
							finish();
						} else {
						
							Commons.displayAlert("Invalid entry, price should be a numerical value!!",Price.this);
							
						}
					} else {
						Commons.displayAlert("No price was entered, please enter a value!!",Price.this);
					}
				} else if (calculate.isChecked()) {

					double price = 0;
					if (cost.getTextSize() > 0
							&& Commons.isNumber(cost.getText().toString())) {
						if (noKWH.getTextSize() > 0
								&& Commons.isNumber(noKWH.getText().toString())) {

							price = Commons.parse(cost.getText().toString())
									.doubleValue()
									* 100
									/ Commons.parse(noKWH.getText().toString())
											.doubleValue();
							DecimalFormat df=new DecimalFormat("#.#");
							try {
								Commons.writeToFile(df.format(price),
										"price",Price.this);
							} catch (IOException e) {
								e.printStackTrace();
							}
							launch();
							finish();

						}
					} else {
						Commons.displayAlert("Something is wrong",Price.this);
					}

				}
			}
		});
		arCancelButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {

				launch();
				finish();
			}
		});

		direct.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				final ViewSwitcher switcher = (ViewSwitcher) Price.this
						.findViewById(R.id.swchPrice);
				switcher.showNext();
			}
		});
		calculate.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				final ViewSwitcher switcher = (ViewSwitcher) Price.this
						.findViewById(R.id.swchPrice);
				switcher.showPrevious();
			}
		});
	}
	
}
