package com.ide.green;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.androidplot.Plot;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class Main extends Activity {
/**
 * @author Somoud
 * 
 */
public class DataReceiver extends BroadcastReceiver {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		// if (intent.hasExtra("msg")) {
		// Commons.displayAlert(intent.getExtras().getString("msg"),
		// EMain.this);
		// }
		updateSeries(intent);

	}

	/**
	 * @param intent
	 */
	private void updateSeries(Intent intent) {
		long time = 0;
		double power = 0;
		double temp = 0;
		if (intent.hasExtra("date"))
			time = intent.getExtras().getLong("date");
		if (intent.hasExtra("value1"))
			power = intent.getExtras().getDouble("value1");
		if (intent.hasExtra("value1"))
			temp = intent.getExtras().getDouble("value0");
		final TextView txt = (TextView) findViewById(R.id.txtPower);

		final TextView txtTemp = (TextView) findViewById(R.id.txtTemp);
		txtTemp.setText(String.valueOf(temp) + "˚C");
		final TextView txtCon = (TextView) findViewById(R.id.txtCon);
		final TextView txtCo2 = (TextView) findViewById(R.id.txtCo2);
		final TextView txtCost = (TextView) findViewById(R.id.txtCost);
		if (time > 0 && power > 0) {
			if (series2 == null) {
				series2 = new SimpleXYSeries("power");
			}

			series2.addLast(time, power);
			Date d = new Date();
			d.setHours(d.getTimezoneOffset());
			// get date at midnight today YYYY-MM-dd00:00:00
			d.setHours(0);
			d.setMinutes(0);
			d.setSeconds(0);
			// read the time of the last entry in the series
			long ti = 0;
			if (series2.size() > 1)
				ti = series2.getX(series2.size() - 2).longValue();
			else
				ti = time - (intUpdate * 60 * 1000);
			double sum = 0;
			try {
				sum = Commons.addKWH(power, ti, time);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i(intent.getDataString(),
					"dataRecieved:w=" + String.valueOf(power) + ", time="
							+ String.valueOf(time) + ", computed energy="
							+ String.valueOf(sum));
			KWHenergy = KWHenergy + sum;
			DecimalFormat df = new DecimalFormat("#.##");
			txt.setText(df.format(power) + " " + dataType);
			txtCon.setText(df.format(KWHenergy) + " KWh");
			txtCo2.setText(df.format(KWHenergy * convertionFactor)
					+ " KgCo2e");
			txtCost.setText("£" + df.format(KWHenergy * KWHprice));
			plot();
		}
	}
}

/**
 * @author Somoud
 * 
 */
public class HistoryReceiver extends BroadcastReceiver {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		/*
		 * initialise the data receiver to load data when a NEW_DATA_FOUND
		 * broadcast is received
		 */
		if (intent.hasExtra("msg")) {
			Commons.displayAlert(intent.getExtras().getString("msg"),
					Main.this);
			unregisterReceiver(historyReceiver);
			stopHistoryService();
		} else {

			IntentFilter filter;
			try {

				filter = new IntentFilter(DataService.NEW_DATA_FOUND);
				dataReceiver = new DataReceiver();

				startDataService();
				registerReceiver(dataReceiver, filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			unregisterReceiver(historyReceiver);
			stopHistoryService();
			Log.i(intent.getDataString(), "historyRecieved");
			try {
				loadDataFromProvider();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.swtch);
		switcher.showNext();
	}
}

/**
 * conversion factor form KWh to kgCO2e
 */
final static double convertionFactor = 0.54522;
/**
 * font size for plot axes labels
 */
private static final float FONT_LABEL_SIZE = 16;
/**
 * Receiver of broadcasts from DataService
 */
private DataReceiver dataReceiver;
/**
 * stores the display data type can be W or KW
 */
private String dataType = "W";
/**
 * stores the graph type can be line or bar
 */
private String graphType = "line";
/**
 * Receiver of broadcasts from HistoryService
 */
private HistoryReceiver historyReceiver;

/**
 * stores the domain length of the graph
 */
private int intHours = 6;

// stores the energy for the day
private double KWHenergy = 0;
/**
 * stores update frequency
 */
private int intUpdate = 2;
/**
 * stores the energy price in pence
 */
private double KWHprice = .14;
/**
 * the View represnting the graph
 */
private XYPlot plt;

/**
 * stores all power data
 */
private SimpleXYSeries series2;

/**
 * the format of line or bar of the graph
 */
private LineAndPointFormatter series2Format;

// perform aesthetic operations on the graph view
private void formatplt() {
	int color = getResources().getColor(R.color.textColor);
	int gridColor = getResources().getColor(R.color.gridColor);
	int bg = getResources().getColor(R.color.bgColor);
	int alph = 200;
	int str = 2;
	plt.setBorderStyle(Plot.BorderStyle.NONE, null, null);
	plt.getGraphWidget().getBackgroundPaint().setColor(bg);

	// plt.getGraphWidget().getDomainOriginLinePaint().setAlpha(50);
	// plt.getGraphWidget().getRangeOriginLinePaint().setAlpha(50);

	plt.getGraphWidget().getGridBackgroundPaint().setAlpha(50);
	plt.getLayoutManager().remove(plt.getLegendWidget());

	plt.getGraphWidget().getGridLinePaint().setColor(gridColor);

	// plt.getGraphWidget().getGridLinePaint().setStrokeWidth(2);
	plt.getGraphWidget().setDomainLabelTickExtension(10);
	plt.getGraphWidget().setRangeLabelTickExtension(10);
	plt.getGraphWidget().getGridLinePaint().setAlpha(100);

	plt.getGraphWidget().getDomainLabelPaint().setTextSize(FONT_LABEL_SIZE);
	plt.getGraphWidget().getDomainLabelPaint().setStrokeWidth(str);
	plt.getGraphWidget().getDomainLabelPaint().setColor(color);
	plt.getGraphWidget().getDomainLabelPaint().setAlpha(alph);

	plt.getGraphWidget().getDomainOriginLabelPaint()
			.setTextSize(FONT_LABEL_SIZE);
	plt.getGraphWidget().getDomainOriginLabelPaint().setColor(color);
	plt.getGraphWidget().getDomainOriginLabelPaint().setAlpha(alph);
	plt.getGraphWidget().getDomainOriginLabelPaint().setStrokeWidth(str);

	plt.getGraphWidget().getRangeLabelPaint().setTextSize(FONT_LABEL_SIZE);
	plt.getGraphWidget().getRangeLabelPaint().setColor(color);
	plt.getGraphWidget().getRangeLabelPaint().setAlpha(alph);
	plt.getGraphWidget().getRangeLabelPaint().setStrokeWidth(str);

	plt.getGraphWidget().getRangeOriginLabelPaint()
			.setTextSize(FONT_LABEL_SIZE);
	plt.getGraphWidget().getRangeOriginLabelPaint().setColor(color);
	plt.getGraphWidget().getRangeOriginLabelPaint().setAlpha(alph);
	plt.getGraphWidget().getRangeOriginLabelPaint().setStrokeWidth(str);

	plt.getGraphWidget().setMargins(20, 20, 30, 20);

	plt.getGraphWidget().setRangeLabelWidth(50);
	// Customise domain/range labels

	plt.setRangeLabel("");
	plt.getDomainLabelWidget().getLabelPaint().setColor(color);
	plt.getDomainLabelWidget().getLabelPaint().setAlpha(alph);
	plt.getDomainLabelWidget().setMarginTop(2);
	plt.setDomainLabel("Graph's Domain: " + intHours + " Hours "
			+ "                  Graph is updated every " + intUpdate
			+ " Minutes");

	/*
	 * Create a formatter to use for drawing a series using
	 * LineAndPointRenderer:
	 */
	plt.setDomainValueFormat(new CDateFormat());
	/*
	 * Create a formatter to use for drawing a series using
	 * LineAndPointRenderer:
	 */
	series2Format = new LineAndPointFormatter(Color.rgb(200, 0, 0), // line
			// colour
			null, // point colour
			null); // fill colour

	// change line thickness
	series2Format.getLinePaint().setStrokeWidth(2);

}

/**
 * gets a specific column from the SQLite database through the content
 * provider
 * 
 * @param col
 * @param factor
 * @return
 * @throws SQLException
 */
private Number[] getCol(int col, Date d) throws SQLException {
	Number[] array = null;
	// get the data for the required period only

	ContentResolver cr = getContentResolver();
	String s = DataProvider.KEY_DATE + " >= ?";
	String[] sa;
	sa = new String[1];
	sa[0] = String.valueOf(d.getTime());
	Cursor c = cr.query(DataProvider.CONTENT_URI, null, s, sa, null);
	startManagingCursor(c);
	if (c != null) {
		if (c.getCount() > 0) {
			array = new Number[c.getCount()];

			int cnt = 0;

			if (c.moveToFirst()) {
				do {
					array[cnt] = c.getDouble(col);
					cnt = cnt + 1;
				} while (c.moveToNext());

			}
		}
		c.close();
	}
	return array;
}

/**
 * gets the most recent value stored in a specific column of the database
 * 
 * @param col
 * @return
 * @throws SQLException
 */
private double getVal(int col) throws SQLException {
	double value = 0;
	ContentResolver cr = getContentResolver();
	Cursor c = cr.query(DataProvider.CONTENT_URI, null, null, null, null);
	startManagingCursor(c);
	if (c != null) {
		if (c.getCount() > 0) {
			c.moveToLast();
			value = c.getDouble(col);
		}
		c.close();
	}
	c = null;
	return value;
}

/**
 * reads all data and its time stamps and update the text views
 */
private void loadDataFromProvider() {

	Number[] timeStamp = null;

	Number[] series2Numbers = null;

	double temperature = 0;
	final TextView txt = (TextView) findViewById(R.id.txtPower);
	final TextView txtTemp = (TextView) findViewById(R.id.txtTemp);
	final TextView txtCon = (TextView) findViewById(R.id.txtCon);
	final TextView txtCo2 = (TextView) findViewById(R.id.txtCo2);
	final TextView txtCost = (TextView) findViewById(R.id.txtCost);

	// get rid of decimal points in our range labels:

	try {
		Date d = new Date();
		d.setHours(0 + d.getTimezoneOffset());
		series2Numbers = getCol(DataProvider.COLUMN_POWER, d);
		timeStamp = getCol(DataProvider.COLUMN_DATE, d);
		temperature = getVal(DataProvider.COLUMN_TEMPERATURE);

		if ((series2Numbers != null) && (timeStamp != null)) {
			if (series2Numbers.length > 0 && timeStamp.length > 0) {
				try {
					KWHenergy = Commons.computeKWH(series2Numbers,
							timeStamp);

					DecimalFormat df = new DecimalFormat("#.##");
					txt.setText(df
							.format(series2Numbers[series2Numbers.length - 1])
							+ " " + dataType);
					txtTemp.setText(String.valueOf(temperature) + " ˚C");
					txtCon.setText(df.format(KWHenergy) + "KWh");
					txtCo2.setText(df.format(KWHenergy * convertionFactor)
							+ " KgCo2e");
					txtCost.setText("£" + df.format(KWHenergy * KWHprice));
					plt.removeSeries(series2);
					series2 = new SimpleXYSeries(Arrays.asList(timeStamp),
							Arrays.asList(series2Numbers), "Power");
					plot();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	} catch (SQLException s) {
		s.printStackTrace();
	}

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
	setContentView(R.layout.main);
	getWindow().setBackgroundDrawableResource(R.color.bgColor);

	// initialise the graph
	final TextView txtChannel = (TextView) findViewById(R.id.txtChannelDescription);
	try {
		graphType = Commons.readFromFile("graphType", this);
	} catch (IOException e) {
		graphType = "line";
		e.printStackTrace();
	}

	try {
		dataType = Commons.readFromFile("dataType", this);
	} catch (IOException e) {
		dataType = "W";
		e.printStackTrace();
	}

	// loadDataFromProvider();
	try {
		intHours = Integer.parseInt(Commons.readFromFile("intHours", this));
	} catch (IOException e) {
		intHours = 6;
	}
	try {
		intUpdate = Integer.parseInt(Commons
				.readFromFile("intUpdate", this));
	} catch (IOException e) {
		intUpdate = 2;
		e.printStackTrace();
	}
	try {
		KWHprice = Double.parseDouble(Commons.readFromFile("price", this)) / 100;
	} catch (IOException e) {
		KWHprice = .14;
		e.printStackTrace();
	}
	// inflate the zoom control and catch the click event
	final Button btnGraph = (Button) this
			.findViewById(R.id.btnGraphSettings);

	btnGraph.setOnClickListener(new OnClickListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View arg0) {
			showGraphSettings();

		}
	});
	plt = (XYPlot) findViewById(R.id.plt);
	/*
	 * by default, AndroidPlot displays developer guides to aid in laying
	 * out the plot. To get rid of them call disableAllMarkup():
	 */
	plt.disableAllMarkup();
	formatplt();
	// get channel
	int i = 0;
	try {
		i = Integer.parseInt(Commons.readFromFile("channel", this));
	} catch (IOException e) {
		i = 1;
		try {
			Commons.writeToFile("1", "channel", this);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		e.printStackTrace();
	}
	String s = DataProvider.KEY_CHANNEL + " = ?";
	String[] sa;
	sa = new String[1];
	sa[0] = String.valueOf(i);
	// If the data is new, insert it into the provider.
	Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_APP,
			null, s, sa, null);
	startManagingCursor(c);
	if (c != null) {
		if (c.getCount() > 0) {
			c.moveToFirst();

			txtChannel
					.setText(c.getString(DataProvider.COLUMN_DESCRIPTION));

		} else if (c.getCount() == 0) {
			ContentValues values = new ContentValues();
			// add main monitor
			values.put(DataProvider.KEY_CHANNEL, 1);
			values.put(DataProvider.KEY_DESCRIPTION, "Electricity Meter");
			try {
				getContentResolver().insert(DataProvider.CONTENT_URI_APP,
						values);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			for (int j = 2; j <= 10; j++) {

				values.put(DataProvider.KEY_CHANNEL, j);
				values.put(DataProvider.KEY_DESCRIPTION, "Channel " + j);
				try {
					getContentResolver().insert(
							DataProvider.CONTENT_URI_APP, values);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
		c.close();
	}
	c = getContentResolver().query(DataProvider.CONTENT_URI, null, null,
			null, null);
	if (c == null) {
		IntentFilter filter;
		filter = new IntentFilter(HistoryService.HISTORY_LOADED);
		historyReceiver = new HistoryReceiver();
		startHistoryService();
		registerReceiver(historyReceiver, filter);
	} else if (c.getCount() == 0) {
		IntentFilter filter;
		filter = new IntentFilter(HistoryService.HISTORY_LOADED);
		historyReceiver = new HistoryReceiver();
		startHistoryService();
		registerReceiver(historyReceiver, filter);
		c.close();
	} else {
		IntentFilter filter;
		try {

			filter = new IntentFilter(DataService.NEW_DATA_FOUND);
			dataReceiver = new DataReceiver();

			startDataService();
			registerReceiver(dataReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			loadDataFromProvider();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ViewSwitcher switcher = (ViewSwitcher) this
				.findViewById(R.id.swtch);
		switcher.showNext();
		c.close();
	}
	c = null;
}

/*
 * (non-Javadoc)
 * 
 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 */
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	// add the main menu items
	menu.add(0, 0, 0, "Select or Edit Appliance");
	menu.add(0, 1, 1, "Change Feed ID/API Key");
	menu.add(0, 2, 2, "Energy Prices");
	menu.add(0, 3, 3, "Detailed History");
	menu.add(0, 4, 4, "Exit");
	return true;
}

/** Called when the activity is first created. */
/*
 * (non-Javadoc)
 * 
 * @see android.app.Activity#onDestroy()
 */
/*
 * (non-Javadoc)
 * 
 * @see android.app.Activity#onDestroy()
 */
@Override
public void onDestroy() {

	try {
		unregisterReceiver(historyReceiver);
		stopHistoryService();
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
	}
	// unload service
	try {
		unregisterReceiver(dataReceiver);
		stopDataService();
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
	}
	super.onDestroy();
}

/*
 * (non-Javadoc)
 * 
 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 */
@Override
public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case 0:
		showAppliance();
		return true;
	case 1:
		showSettings();
		return true;
	case 2:
		showPrice();
		return true;
	case 3:
		showHistory();
		return true;
	case 4:
		finish();
		return true;
	}
	return true;
}

/**
 * draws a graph
 * 
 */
private void plot() {

	// calculate the max and min of the y value to format the y axis
	double max = Commons.getMax(intHours, series2, 2);
	double min = Commons.getMin(intHours, series2, 2);
	plt.setRangeBoundaries(min, max, BoundaryMode.AUTO);
	if (max > 0) {
		double step = (max - min) / 5.0;
		int nearest = (int) Math.round(Math.log10(max - min) / 2);
		double rounded = Commons.roundToNextTen(step, nearest);
		if (rounded < 1)
			rounded = 1; // make sure the step is not zero
		plt.setRangeStep(XYStepMode.INCREMENT_BY_VAL, rounded);
		plt.setTicksPerDomainLabel(4);
	}

	plt.setDomainStep(XYStepMode.INCREMENT_BY_VAL, intHours * 60 * 60
			* 1000 / 8);

	if (dataType.equals("KW")) {
		plt.setRangeValueFormat(new KwFormat("#.###KW"));
	} else {
		plt.setRangeValueFormat(new DecimalFormat("0" + dataType));
	}
	if (graphType.equals("bar")) {
		plt.addSeries(
				series2,
				BarRenderer.class,
				new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0,
						80, 0)));
	} else {
		plt.addSeries(series2, LineAndPointRenderer.class, series2Format);
	}
	plt.redraw();

	long maxTime = Commons.getMaxTime();
	if (maxTime > 0) {
		plt.setDomainBoundaries(maxTime - (intHours * 3600000) - 1800000,
				maxTime, BoundaryMode.AUTO);
	} else {
		if (intHours == 2) {
			plt.setTicksPerDomainLabel(4);
		} else {
			plt.setTicksPerDomainLabel(2);
		}
	}
}

/**
 * launch ApplianceActivity
 */
private void showAppliance() {
	Intent applianceActivity = new Intent(this, Appliance.class);
	startActivity(applianceActivity);
	finish();
}

/**
 * launch Graph Settings Activity
 */
private void showGraphSettings() {
	Intent graphSettingsActivity = new Intent(this, GSettings.class);
	// If your target activity can handle many intentions you would
	// need to pass the code for the intention that you have
	startActivity(graphSettingsActivity);
	finish();
}

/**
 * launch Detailed History activity
 */
private void showHistory() {

	Intent activity = new Intent(this, DHistory.class);
	startActivity(activity);
	finish();
}

/**
 * launch price activity
 */
private void showPrice() {

	Intent activity = new Intent(this, Price.class);
	startActivity(activity);
	finish();

}

/**
 * launch Settings activity
 */
private void showSettings() {

	Intent settingsActivity = new Intent(this, Settings.class);
	startActivity(settingsActivity);
	finish();
}

/**
 * start services explicitly
 */

private void startDataService() {
	startService(new Intent(this, DataService.class));
}

/**
 * start services explicitly
 */

private void startHistoryService() {
	startService(new Intent(this, HistoryService.class));
}

/**
 * stop service explicitly
 */
private void stopDataService() {
	stopService(new Intent(this, DataService.class));
}

/**
 * stop service explicitly
 */
private void stopHistoryService() {
	stopService(new Intent(this, HistoryService.class));
}
}
