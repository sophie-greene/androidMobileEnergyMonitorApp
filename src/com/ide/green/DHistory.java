package com.ide.green;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.androidplot.Plot;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class DHistory extends Activity {
	private class ArchiveReceiver extends BroadcastReceiver {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("msg"))
				Commons.displayAlert(intent.getExtras().getString("msg"),
						DHistory.this);

			unregisterReceiver(archiveReceiver);
			stopArchiveService();
			Log.i(intent.getDataString(), "archiveRecieved");
			loadDataFromProvider();
			ViewSwitcher switcher = (ViewSwitcher) DHistory.this
					.findViewById(R.id.swch_load);
			switcher.showNext();

		}

	}

	/**
	 * A simple formatter to convert bar indexes into month names.
	 */
	private class MFormat extends Format {

		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.text.Format#format(java.lang.Object,
		 * java.lang.StringBuffer, java.text.FieldPosition)
		 */
		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			Number num = (Number) obj;

			toAppendTo.append(getMonthForInt(num.intValue()));
			return toAppendTo;
		}

		/**
		 * @param m
		 * @return
		 */
		private String getMonthForInt(int m) {
			String month = "invalid";
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] months = dfs.getMonths();
			if ((m >= 1) && (m <= 12)) {
				month = months[m - 1].substring(0, 3);
			}
			return month;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.text.Format#parseObject(java.lang.String,
		 * java.text.ParsePosition)
		 */
		@Override
		public Object parseObject(String source, ParsePosition pos) {
			return null; // We don't use this so just return null for now.
		}
	}

	/**
	 * 
	 */
	final static double conversionFactor = 0.54522;
	/**
	 * 
	 */
	private static final float FONT_LABEL_SIZE = 16;
	/**
	 * 
	 */
	private ArchiveReceiver archiveReceiver;
	/**
	 * 
	 */
	private String dataType = "";

	/**
	 * 
	 */
	private double KWHprice = .14;
	/**
	 * 
	 */
	private XYPlot plt;
	/**
	 * 
	 */
	private SimpleXYSeries series2;
	/**
	 * 
	 */
	private BarFormatter series2Format;

	/**
	 * 
	 */
	private String unit = "";

	/**
	 * 
	 */
	private void formatplt() {
		int color = getResources().getColor(R.color.textColor);
		int alph = 200;
		int str = 2;
		plt.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		plt.getBackgroundPaint().setAlpha(0);
		plt.getGraphWidget().getBackgroundPaint().setAlpha(0);
		plt.getGraphWidget().getDomainOriginLinePaint().setAlpha(50);
		plt.getGraphWidget().getRangeOriginLinePaint().setAlpha(50);

		plt.getGraphWidget().getGridBackgroundPaint().setAlpha(20);
		plt.getLayoutManager().remove(plt.getLegendWidget());
		plt.getGraphWidget().getGridLinePaint().setAlpha(80);
		plt.getGraphWidget().getGridLinePaint().setStrokeWidth(str);

		plt.getGraphWidget().getDomainLabelPaint().setTextSize(FONT_LABEL_SIZE);
		plt.getGraphWidget().getDomainLabelPaint().setColor(color);
		plt.getGraphWidget().getDomainLabelPaint().setAlpha(alph);
		plt.getGraphWidget().getDomainLabelPaint().setStrokeWidth(str);

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

		// Customise our domain/range labels
		plt.setDomainLabel("");
		plt.setRangeLabel("");

		/*
		 * Create a formatter to use for drawing a series using
		 * LineAndPointRenderer:
		 */
		series2Format = new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(
				0, 80, 0)); // fill colour
		plt.getGraphWidget().setRangeLabelWidth(70);
		plt.getGraphWidget().setMargins(20, 20, 30, 20);
	}

	/**
	 * 
	 */
	private void getPower() {
		double factor = 1;
		LinkedList<Number> array = new LinkedList<Number>();
		LinkedList<Number> time = new LinkedList<Number>();

		if (dataType.equals("money")) {
			factor = KWHprice;
		} else if (dataType.equals("co2")) {
			factor = conversionFactor;
		} else {
		}

		// If the data is new, insert it into the provider.
		Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_H, null,
				null, null, null);

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();

				if (unit.equals("hours")) {
					Date day = new Date();
					day.setSeconds(0);
					day.setHours(0);
					day.setMinutes(0);
					do {
						if (c.getLong(DataProvider.COLUMN_DATE_H) > day
								.getTime()) {
							array.add(factor
									* c.getDouble(DataProvider.COLUMN_POWER_H));
							Date d = new Date();
							d.setTime(c.getLong(DataProvider.COLUMN_DATE_H));
							d.setMinutes(0);
							d.setSeconds(0);
							time.add(d.getTime());
						}
					} while (c.moveToNext());
				} else if (unit.equals("days")) {
					Date day = new Date();
					Date dayC = new Date();
					Date range = new Date();
					range.setSeconds(0);
					range.setHours(0);
					range.setMinutes(0);
					range.setTime(range.getTime() - 7 * 24 * 60 * 60 * 1000);
					day.setSeconds(0);
					day.setHours(0);
					day.setMinutes(0);
					double sum = 0;

					for (int i = 0; i < 8; i++) {
						day.setTime(range.getTime() + i * 24 * 60 * 60 * 1000);
						c.moveToFirst();
						sum = 0;
						for (int j = 0; j < c.getCount(); j++) {

							dayC.setTime(c.getLong(DataProvider.COLUMN_DATE_H));
							if (dayC.after(day)
									&& (dayC.getDate() == day.getDate())) {

								sum = sum
										+ factor
										* c.getDouble(DataProvider.COLUMN_POWER_H);
							}
							c.moveToNext();
						}
						if (sum > 0) {
							array.add(sum);
							time.add(day.getTime());
						}
					}

				} else if (unit.equals("weeks")) {
					Date day = new Date();
					Date dayC = new Date();
					Date rangeS = new Date();
					Date rangeE = new Date();
					double sum = 0;

					for (int i = 0; i < 5; i++) {

						rangeS = Commons.weekFirstDay(day, i);
						rangeE = Commons.weekFirstDay(day, i - 1);

						c.moveToFirst();
						sum = 0;
						for (int j = 0; j < c.getCount(); j++) {

							dayC.setTime(c.getLong(DataProvider.COLUMN_DATE_H));
							if (dayC.before(rangeE) && dayC.after(rangeS)) {
								sum = sum
										+ factor
										* c.getDouble(DataProvider.COLUMN_POWER_H);
							}
							c.moveToNext();
						}
						if (sum > 0) {
							array.add(sum);
							time.add(rangeS.getTime());
						}
					}

				} else {
					Date day = new Date();
					Date dayC = new Date();
					Date rangeS = new Date();
					Date rangeE = new Date();
					
					double sum = 0;

					for (int i = 0; i < 3; i++) {

						rangeS = monthFirstDay(day, i);
						rangeE = monthFirstDay(day, i - 1);

						c.moveToFirst();
						sum = 0;
						for (int j = 0; j < c.getCount(); j++) {

							dayC.setTime(c.getLong(DataProvider.COLUMN_DATE_H));
							if (dayC.before(rangeE) && dayC.after(rangeS)) {
								sum = sum
										+ factor
										* c.getDouble(DataProvider.COLUMN_POWER_H);
							}
							c.moveToNext();
						}
						if (sum > 0) {
							array.add(sum);
							time.add(rangeE.getMonth());
						}
					}

				}
				series2 = new SimpleXYSeries(time, array, "power");
			}
			c.close();

		}

	}

	/**
	 * 
	 */
	private void launch() {
		Intent activity = new Intent(this, Main.class);

		startActivity(activity);

	}

	/**
	 * 
	 */
	private void loadDataFromProvider() {
		try {
			dataType = Commons.readFromFile("historyDataType", this);
		} catch (IOException e) {
			dataType = "power";
			e.printStackTrace();
		}
		try {
			unit = Commons.readFromFile("units", this);
		} catch (IOException e) {
			unit = "days";
			e.printStackTrace();
		}

		try {
			KWHprice = Double.parseDouble(Commons.readFromFile("price", this)) / 100;
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		final TextView txtHistory = (TextView) findViewById(R.id.txtHistory);
		txtHistory.setText("Electricity consumed in the last few " + unit
				+ " in " + dataType + " terms");
		getPower();
		if (series2 != null) {
			if(series2.size()>0){
			plt.addSeries(series2, BarRenderer.class, series2Format);
			plot();
			}
		} else {
			Toast.makeText(this,
					"No data to display, click on download history button ",
					Toast.LENGTH_LONG).show();
		}
	}

	private void plot() {
		// calculate the max and min of the y value
		double max = getMax();
		double min = getMin();
		if (max > 0) {
			plt.setRangeStep(
					XYStepMode.INCREMENT_BY_VAL,
					Commons.roundToNextTen((max - min) / 5,
							(int) Math.log10(max - min) / 2));
			plt.setRangeBoundaries(min, max + plt.getRangeStepValue(),
					BoundaryMode.AUTO);
		}
		if (dataType.equals("money")) {
			plt.setRangeValueFormat(new KwFormat("£0.00"));
		} else if (dataType.equals("co2")) {
			plt.setRangeValueFormat(new KwFormat("0.00Kg"));
		} else {
			plt.setRangeValueFormat(new KwFormat("0.00KW"));
		}
		if (unit.equals("hours")) {

			plt.setDomainValueFormat(new SimpleDateFormat("ha"));
			plt.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 60 * 60 * 1000);
			plt.setTicksPerDomainLabel(3);
			plt.setDomainBoundaries(getMinTime() - 60 * 60 * 1000,
					getMaxTime() + 60 * 60 * 1000, BoundaryMode.AUTO);

			BarRenderer barRenderer = (BarRenderer) plt
					.getRenderer(BarRenderer.class);
			if (barRenderer != null) {
				barRenderer.setBarWidth(5);
			}

		} else if (unit.equals("days")) {

			plt.setDomainValueFormat(new SimpleDateFormat("dd/MM"));
			plt.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 24 * 60 * 60 * 1000);
			plt.setDomainBoundaries(getMinTime() - 24 * 60 * 60 * 1000,
					getMaxTime() + 24 * 60 * 60 * 1000, BoundaryMode.FIXED);
			plt.setTicksPerDomainLabel(2);

			BarRenderer barRenderer = (BarRenderer) plt
					.getRenderer(BarRenderer.class);
			if (barRenderer != null) {

				barRenderer.setBarWidth(15);
			}
		} else if (unit.equals("weeks")) {
			plt.setDomainValueFormat(new SimpleDateFormat("dd/MM"));
			plt.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 7 * 24 * 60 * 60
					* 1000);
			plt.setDomainBoundaries(getMinTime() - 7 * 24 * 60 * 60 * 1000,
					getMaxTime() + 7 * 24 * 60 * 60 * 1000, BoundaryMode.FIXED);

			BarRenderer barRenderer = (BarRenderer) plt
					.getRenderer(BarRenderer.class);
			if (barRenderer != null) {

				barRenderer.setBarWidth(30);
			}
		} else {
			plt.setDomainValueFormat(new MFormat());
			plt.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
			plt.setDomainBoundaries(getMinTime() - 1, getMaxTime() + 1,
					BoundaryMode.FIXED);

			BarRenderer barRenderer = (BarRenderer) plt
					.getRenderer(BarRenderer.class);
			if (barRenderer != null) {

				barRenderer.setBarWidth(50);
			}
		}
		plt.redraw();
	}

	/**
	 * Subtracts i months from the "date's" month and return the first day of
	 * that month
	 * 
	 * @param date
	 * @param i
	 * @return
	 */
	private Date monthFirstDay(Date date, int i) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -i);
		date = calendar.getTime();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setDate(1);
		// get the first day of month.

		return date;

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
		setContentView(R.layout.detailed_history);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		// inflate the zoom control and catch the click event
		final Button btnGraph = (Button) this
				.findViewById(R.id.btnGraphSettingsH);

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
		final Button btnHistory = (Button) findViewById(R.id.btnHistory1);

		btnHistory.setOnClickListener(new OnClickListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View arg0) {
				IntentFilter filter;
				filter = new IntentFilter(ArchiveService.ARCHIVE_LOADED);
				archiveReceiver = new ArchiveReceiver();
				startArchiveService();
				registerReceiver(archiveReceiver, filter);

				ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.swch_load);
				switcher.showPrevious();
			}
		});

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
		menu.add(0, 0, 0, "Exit");
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
			unregisterReceiver(archiveReceiver);
			stopArchiveService();
		} catch (Exception e) {
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
			launch();
			finish();
			break;

		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		// initialise the graph

		
		plt = (XYPlot) findViewById(R.id.plt1);
		plt.disableAllMarkup();
		formatplt();
		loadDataFromProvider();
		ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.swtch_hd);
		switcher.showNext();
	}

	/**
	 * 
	 */
	private void showGraphSettings() {
		Intent graphSettingsActivity = new Intent(this,
				HGSettings.class);

		// If your target activity can handle many intentions you would
		// need to pass the code for the intention that you have
		startActivity(graphSettingsActivity);
		finish();
	}

	/**
	 * start services explicitly
	 */
	private void startArchiveService() {
		startService(new Intent(this, ArchiveService.class));
	}

	/**
	 * stop services explicitly
	 */
	private void stopArchiveService() {
		stopService(new Intent(this, ArchiveService.class));
	}

	/**
	 * @param prd
	 * @return
	 */
	private double getMax() {
		double max = 0;
		for (int i = 0; i < series2.size(); i++) {
			if (series2.getY(i).doubleValue() > max) {
				max = series2.getY(i).doubleValue();
			}
		}
		// round max to nearest 10^log10(max)

		max = Commons.roundToNextTen(max, (int) Math.log10(max));
		return max;
	}

	/**
	 * @return
	 */
	private long getMaxTime() {
		long max = 0;
		for (int i = 0; i < series2.size(); i++) {
			if (series2.getX(i).longValue() > max) {
				max = series2.getX(i).longValue();
			}
		}
		Date s = new Date();
		s.setTime(max);
		s.setHours(s.getHours() + 2);
		s.setSeconds(0);
		s.setMinutes(0);
		return max;
	}

	/**
	 * @param prd
	 * @return
	 */
	private double getMin() {
		double min = 0;

		for (int i = 0; i < series2.size(); i++) {
			if (series2.getY(i).doubleValue() < min) {
				min = series2.getY(i).doubleValue();
			}
		}
		min = Commons.roundToNextTen(min, (int) Math.log10(min));
		return min;
	}

	/**
	 * @return
	 */
	private long getMinTime() {
		Date s = new Date();
		long min = s.getTime();

		for (int i = 0; i < series2.size(); i++) {
			if (series2.getX(i).longValue() < min) {
				min = series2.getX(i).longValue();

			}
		}
		// get whole hour
		s.setTime(min);
		s.setSeconds(0);
		s.setMinutes(0);
		min = s.getTime();
		return min;
	}
}
