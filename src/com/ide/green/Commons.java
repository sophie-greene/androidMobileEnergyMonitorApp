package com.ide.green;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;

import com.androidplot.xy.SimpleXYSeries;

public class Commons {

	/**
	 * notification string for the broadcast
	 */
	public static final String INTERNET_CONNECTION = "Internet Connection Failure";

	public static final String INVALID_FEED = "Invalid Feed,check api and feed id";

	public static boolean isNumber(String input) {
		boolean isDouble = true;
		boolean isInteger = true;

		try {
			Double.parseDouble(input);

		} catch (NumberFormatException e) {
			isDouble = false;
			e.printStackTrace();
		}
		try {
			Integer.parseInt(input);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			isInteger = false;
		}
		return isInteger | isDouble;

	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFromFile(String file, Activity a)
			throws IOException {

		/*
		 * Prepare a char-Array that will hold the chars we read back in.
		 */
		char[] data = null;
		/*
		 * use the openFileInput()-method the ActivityContext provides. for
		 * security reasons with openFileInput(...)
		 */
		FileInputStream fIn = a.openFileInput(file);
		InputStreamReader isr = new InputStreamReader(fIn);
		// Fill the Buffer with data from the file
		data = new char[100];
		isr.read(data);
		// Transform the chars to a String
		String readString = new String(data);
		readString = readString.trim();
		// WOHOO lets Celebrate =)
		// Log.i(readString, readString);
		return readString;
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFromFileS(String file, Service a)
			throws IOException {
		/*
		 * Prepare a char-Array that will hold the chars we read back in.
		 */
		char[] data = null;
		/*
		 * use the openFileInput()-method the ActivityContext provides. for
		 * security reasons with openFileInput(...)
		 */
		FileInputStream fIn = a.openFileInput(file);
		InputStreamReader isr = new InputStreamReader(fIn);
		// Fill the Buffer with data from the file
		data = new char[100];
		isr.read(data);
		// Transform the chars to a String
		String readString = new String(data);
		readString = readString.trim();
		// WOHOO lets Celebrate =)
		// Log.i(readString, readString);
		return readString;
	}

	/**
	 * @param data
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean writeToFile(String data, String file, Activity a)
			throws IOException {

		// catches IOException below

		/*
		 * openFileOutput()-method the ActivityContext provides, to protect file
		 * from other apps and users and This is done for security-reasons. use
		 * Context.MODE_PRIVATE, because we have to hide in our file
		 */
		FileOutputStream fOut = a.openFileOutput(file, Context.MODE_PRIVATE);
		OutputStreamWriter osw = new OutputStreamWriter(fOut);

		// Write the string to the file
		osw.write(data);
		/*
		 * ensure that everything is really written out and close
		 */
		osw.flush();
		osw.close();
		return true;
	}

	/**
	 * @param input
	 * @return
	 */

	public static Number parse(String input) {

		try {
			return Double.parseDouble(input);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		try {
			return Integer.parseInt(input);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void displayAlert(String msg, Activity a) {
		Builder builder = new AlertDialog.Builder(a);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.content.DialogInterface.OnClickListener
			 * #onClick(android.content.DialogInterface, int)
			 */
			@Override
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		AlertDialog alert = builder.create();
		alert.setMessage(msg);
		alert.show();
	}

	/**
	 * round num to the next p multiple of 10s
	 * 
	 * @param num
	 * @param p
	 * @return
	 */
	public static double roundToNextTen(double num, int p) {
		num = num / (Math.pow(10, p));
		long s = Math.round(num);
		int l = (int) s;
		num = l * (Math.pow(10, p));
		return num;
	}

	/**
	 * @param power
	 * @param time
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public static double computeKWH(Number[] power, Number[] time) throws Exception {

		double sum = 0;

		Number deltaTime;
		Number deltaPower;

		int factor = 1000;

		for (int i = 0; i < power.length - 1; i++) {
			if ((time[i + 1] != null) && (time[i] != null)
					&& (power[i] != null)
					&& (power[i + 1] != null)) {
				deltaTime = time[i + 1].longValue() - time[i].longValue();
				deltaPower = (power[i].doubleValue() + power[i + 1]
						.doubleValue()) / factor;
				// time stamp is in millisecond we devide by 1000 to convert to
				// seconds
				// by 60 to convert to minutes and by 60 to convert to hours
			
					sum = sum + deltaTime.doubleValue() * deltaPower.doubleValue()
							/ (2.0 * 60 * 60 * 1000);
				
			}
		}
		return sum;
	}

	/**
	 * calculate the date of the first day of the week which is (weeksDiff)
	 * weeks away for dayInWeek date
	 * 
	 * @param dayInWeek
	 * @param weeksDiff
	 * @return
	 */

	public static Date weekFirstDay(Date dayInWeek, int weeksDiff) {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, week(dayInWeek) - weeksDiff);
		int currentDOW = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DAY_OF_YEAR, (currentDOW * -1) + 1);
		// get the first day of week.
		Date date = cal.getTime();
		date.setYear(dayInWeek.getYear());
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);

		return date;
	}

	/**
	 * computes the week number that the date occur in
	 * 
	 * @param date
	 * @return
	 */

	public static int week(Date date) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		return week;
	}

	/**
	 * @param prd
	 * @param series2
	 * @param f
	 * @return
	 */
	public static double getMax(int prd, SimpleXYSeries series2, int f) {
		double max = 0;
		Date d = new Date();
		d.setHours(d.getHours() - prd);
		d.setMinutes(0);
		d.setSeconds(0);
		long minTime = d.getTime();
		for (int i = 0; i < series2.size(); i++) {
			if (series2.getX(i).longValue() > minTime) {
				if (series2.getY(i).doubleValue() > max) {
					max = series2.getY(i).doubleValue();
				}
			}
		}
		// round max to nearest 10^log10(max)
		max = Commons.roundToNextTen(max, (int) Math.log10(max) / f)
				+ Math.pow(10, (int) Math.log10(max) / f);
		;
		return max;
	}

	/**
	 * @return
	 */
	public static long getMaxTime() {

		long max = 0;
		
			// get whole hour
			Date s = new Date();
			s.setSeconds(0);
			s.setMinutes(0);
			s.setHours(s.getHours() + 1);
			max = s.getTime();
		
		return max;
	}

	/**
	 * @param prd
	 * @param series2
	 * @param f
	 * @return
	 */
	public static double getMin(int prd, SimpleXYSeries series2, int f) {
		double min = 0;
		Date d = new Date();
		d.setHours(d.getHours() - prd);
		d.setMinutes(0);
		d.setSeconds(0);
		long minTime = d.getTime();
		min = series2.getY(series2.size() - 1).doubleValue();
		for (int i = 0; i < series2.size(); i++) {
			if (series2.getX(i).longValue() > minTime) {
				if (series2.getY(i).doubleValue() < min) {
					min = series2.getY(i).doubleValue();
				}
			}
		}
		min = Commons.roundToNextTen(min, (int) Math.log10(min) / f)
				- Math.pow(10, (int) Math.log10(min) / f);

		return min;
	}

	/**
	 * read data from Internet as XML files and store it in the database
	 * 
	 * @param urlString
	 * @throws IOException
	 */
	public static void readData(String urlString, Service srv)
			throws IOException, MalformedURLException {
		GraphData data = new GraphData();
		URL url = null;
		url = new URL(urlString);
		InputStream is = (InputStream) url.getContent();
		BufferedReader r = new BufferedReader(new InputStreamReader(is),
				8 * 1024);

		String line;

		while ((line = r.readLine()) != null) {

			String[] temp = line.split(",");

			double v1 = Double.parseDouble(temp[1]);
			if (temp[1].contains(".") && (v1 < 100000)) {

				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssZ");
				temp[0] = temp[0].substring(0, 19);

				try {
					data.setUpdatedTime(df.parse(temp[0] + "+0000").getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				data.setPower(v1);
				addNewData(data, srv);
			}
		}
	}

	/**
	 * Subtracts i months from the "date's" month and return the first day of
	 * that month
	 * 
	 * @param date
	 * @param i
	 * @return
	 */

	public static Date monthFirstDay(Date date, int i) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);
		calendar.add(Calendar.MONTH, i);
		date = calendar.getTime();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setDate(1);
		// get the first day of month.

		return date;

	}

	/**
	 * @param date
	 * @return
	 */
	public static int noOfDaysInMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		return days;
	}

	/**
	 * @param _data
	 */
	public static void addNewData(GraphData _data, Service srv) {
		ContentResolver cr = (srv).getContentResolver();
		// Construct a where clause to make sure we don’t already have
		// this data in the provider.

		String s = DataProvider.KEY_DATE_H + " = ?";
		String[] sa;
		sa = new String[1];
		sa[0] = String.valueOf(_data.getUpdatedTime());
		// If the data is new, insert it into the provider.
		Cursor c = cr.query(DataProvider.CONTENT_URI_H, null, s, sa, null);

		if (c.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(DataProvider.KEY_DATE_H, _data.getUpdatedTime());
			values.put(DataProvider.KEY_POWER_H, _data.getPower());
			cr.insert(DataProvider.CONTENT_URI_H, values);
		}
		c.close();
	}

	/**
	 * @param intChannel
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static String getCurrentDataURL(int intChannel, Service svr)
			throws NumberFormatException, IOException {

		String api = readFromFileS("apiFile", svr);
		int id = Integer.parseInt(readFromFileS("eidFile", svr));
		return "http://api.pachube.com/v2/feeds/" + id + ".csv?key=" + api;
	}

	/**
	 * @param dateS
	 * @param dateE
	 * @param id
	 * @param api
	 * @return
	 */
	public static String getArchiveDataURL(String dateS, String dateE, int id,
			String api) {
		if ((id > 0) && (api != null)) {
			String urlS = "http://api.pachube.com/v2/feeds/" + id
					+ "/datastreams/" + 1 + ".csv?start=" + dateS + "&end="
					+ dateE + "&interval=3600&key=" + api;
			return urlS;
		} else {
			return null;
		}
	}

	/**
	 * @param intChannel
	 * @param i
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static String getHistoryDataURL(int intChannel, int i, Service srv)
			throws NumberFormatException, IOException {

		int id = Integer.parseInt(readFromFileS("eidFile", srv));
		String api = readFromFileS("apiFile", srv);
		Date start = new Date();
		start.setHours(i * 6);
		start.setMinutes(0);
		start.setSeconds(0);
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dfd = new SimpleDateFormat("HH:mm:ss");

		String s2 = dfm.format(start) + "T" + dfd.format(start) + "Z";

		String s = "http://api.pachube.com/v2/feeds/" + id + "/datastreams/"
				+ intChannel + ".csv?start=" + s2 + "&interval=300&key=" + api;
		return s;

	}

	/**
	 * @param kWHenergy
	 *            in KHW
	 * @param power
	 *            in Watt
	 * @param ti
	 *            in milliseconds
	 * @return
	 */
	public static double addKWH(double power, long ti, long tf) {
		power = power / 1000;// convert to KW
		double diff = tf - ti;// find the time over which the power occurred
		diff = diff / (60 * 60 * 1000);// convert to hours
		return power * diff;
	}
}
