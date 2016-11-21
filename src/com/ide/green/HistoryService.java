package com.ide.green;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class HistoryService extends Service {
	private class LoadHistory extends AsyncTask<Void, GraphData, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {

			GraphData data = new GraphData();
			Date s = new Date();
			s.setHours(s.getTimezoneOffset());

			int no = (s.getHours() + 6) / 6;
			String urlString = null;
			String urlString1 = null;

			for (int i = 0; i < no; i++) {

				try {
					urlString = Commons.getHistoryDataURL(intChannel, i,
							HistoryService.this);
					Log.i(urlString, urlString);
					urlString1 = Commons.getHistoryDataURL(0, i,
							HistoryService.this);
				} catch (IOException e4) {
					if (i == 0)
						errorMsg = Commons.INVALID_FEED;
					if(i>0 &&  errorMsg ==Commons.INVALID_FEED){
						
					}else{
						errorMsg = "";
					}
					e4.printStackTrace();
				}
				if (urlString != null) {
					try {
						URL url = new URL(urlString);

						InputStream is = (InputStream) url.getContent();

						BufferedReader r = new BufferedReader(
								new InputStreamReader(is), 8 * 1024);
						URL url1 = new URL(urlString1);

						InputStream is1 = (InputStream) url1.getContent();

						BufferedReader r1 = new BufferedReader(
								new InputStreamReader(is1), 8 * 1024);
						String line, line1;

						while (((line = r.readLine()) != null)
								&& ((line1 = r1.readLine()) != null)) {

							String[] temp = line.split(",");

							String[] temp1 = line1.split(",");

							double v0 = Double.parseDouble(temp1[1]);
							double v1 = Double.parseDouble(temp[1]);
							if ((v0 < 1000000) && (v1 < 1000000)) {

								SimpleDateFormat df = new SimpleDateFormat(
										"yyyy-MM-dd'T'HH:mm:ssZ");
								temp[0] = temp[0].substring(0, 19);

								try {
									data.setUpdatedTime(df.parse(
											temp[0] + "+0000").getTime());
								} catch (ParseException e1) {

									e1.printStackTrace();
								}

								data.setPower(v1);
								temp1[0] = temp[0].substring(0, 19);
								try {
									data.setUpdatedTime(df.parse(
											temp1[0] + "+0000").getTime());
								} catch (ParseException e) {

									e.printStackTrace();
								}
								data.setTemperature(v0);
								HistoryService.this.addNewData(data);
							}
						}
					} catch (NumberFormatException e4) {
						// errorMsg = Commons.INVALID_FEED;
						e4.printStackTrace();
					} catch (IOException e4) {
						if (i == 0)
							errorMsg = Commons.INVALID_FEED;
						if(i>0 &&  errorMsg ==Commons.INVALID_FEED){
							
						}else{
							errorMsg = "";
						}
						e4.printStackTrace();
					}
				}

			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			HistoryService.this.announceServiceFinished();
		}
	}

	public class MyBinder extends Binder {
		/**
		 * @return
		 */
		HistoryService getService() {
			return HistoryService.this;
		}
	}

	/**
	 * 
	 */
	public static final String HISTORY_LOADED = "History_Loaded";

	/**
	 * 
	 */
	private final IBinder binder = new MyBinder();

	/**
	 * 
	 */
	private LoadHistory history = null;

	/**
	 * 
	 */
	private int intChannel = 1;
	/**
	 * notification string for the broadcast
	 */

	private String errorMsg = "";

	/**
	 * @param _data
	 */
	private void addNewData(GraphData _data) {

		// Construct a where clause to make sure we don’t already have
		// this data in the provider.
		String s = DataProvider.KEY_DATE + " = ?";
		String[] sa;
		sa = new String[1];
		sa[0] = String.valueOf(_data.getUpdatedTime());
		// If the data is new, insert it into the provider.
		Cursor c = this.getContentResolver().query(DataProvider.CONTENT_URI,
				null, s, sa, null);
		// If the data is new, insert it into the provider.

		if (c.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(DataProvider.KEY_DATE, _data.getUpdatedTime());
			values.put(DataProvider.KEY_TEMPERATURE, _data.getTemperature());
			values.put(DataProvider.KEY_POWER, _data.getPower());
			this.getContentResolver().insert(DataProvider.CONTENT_URI, values);

		}
		c.close();

	}

	/**
	 * 
	 */
	private void announceServiceFinished() {
		Intent intent = new Intent(HISTORY_LOADED);
		if (errorMsg.length() > 0)
			intent.putExtra("msg", errorMsg);
		this.sendBroadcast(intent);

	}

	/**
	 * 
	 */
	private void getHistory() {
		if (this.history == null) {

			this.history = new LoadHistory();
			this.history.execute((Void[]) null);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return this.binder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {

		// read the channel number
		try {
			this.intChannel = Integer.parseInt(Commons.readFromFileS("channel",
					this));
		} catch (IOException e) {
			this.intChannel = 1;
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {

		this.history.cancel(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		this.getHistory();

		return Service.START_NOT_STICKY;
	}
}
