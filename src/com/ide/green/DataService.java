package com.ide.green;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class DataService extends Service {
	private class LoadData extends AsyncTask<Void, GraphData, Void> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			try {
				String urlString = Commons.getCurrentDataURL(intChannel,
						DataService.this);
				if (urlString != null) {
					GraphData d = new GraphData();

					URL url = new URL(urlString);

					InputStream is = (InputStream) url.getContent();

					BufferedReader r = new BufferedReader(
							new InputStreamReader(is), 8 * 1024);

					String line;

					while ((line = r.readLine()) != null) {
						String[] temp = line.split(",");
						if ((Integer.parseInt(temp[0]) == DataService.this.intChannel)
								|| (Integer.parseInt(temp[0]) == 0)) {
							temp[1] = temp[1].substring(0, 19);
							temp[1] = temp[1] + "+0000";

							SimpleDateFormat dateFormat = new SimpleDateFormat(
									"yyyy-MM-dd'T'HH:mm:ssZ");

							Date lTime = dateFormat.parse(temp[1]);

							d.setUpdatedTime(lTime.getTime());
							if (Integer.parseInt(temp[0]) == 0) {
								d.setTemperature(Double.parseDouble(temp[2]));
							}
							if ((Integer.parseInt(temp[0]) == intChannel)
									&& temp[2].length() < 6) {
								d.setPower(Double.parseDouble(temp[2]));
								addNewData(d);
							}
						}
					}
				}else{
					errorMsg=Commons.INVALID_FEED;
					announceError();
				}
			} catch (NumberFormatException e) {
				errorMsg = Commons.INTERNET_CONNECTION;
				e.printStackTrace();
			} catch (IOException e) {
				errorMsg = Commons.INTERNET_CONNECTION;
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
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
		}
	}

	public class MyBinder extends Binder {
		DataService getService() {
			return DataService.this;
		}
	}

	/**
	 * 
	 */
	public static final String NEW_DATA_FOUND = "New_Data_Found";

	/**
	 * 
	 */
	private final IBinder binder = new MyBinder();

	/**
	 * 
	 */
	private TimerTask doRefresh = new TimerTask() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {

			DataService.this.refreshData();
		}
	};

	/**
	 * 
	 */
	private int intChannel = 1;

	/*
	 * create and execute a new LoadData. check to see if another
	 * asynchronous task has already begun. To avoid stacking refresh requests
	 * an update should begin only if one is not already in progress.
	 */
	/**
	 * 
	 */
	private LoadData lastLookup = null;

	/**
	 * 
	 */
	private Timer updateTimer;
	/**
	 * notification string for the broadcast
	 */
	private String errorMsg = "";

	private int updatePeriod=4;

	/**
	 * @param _data
	 */
	private void addNewData(GraphData _data) {
		ContentResolver cr = this.getContentResolver();
		// Construct a where clause to make sure we don’t already have
		// this data in the provider.

		// If the data is new, insert it into the provider.
		String s = DataProvider.KEY_DATE + " = ?";
		String[] sa;
		sa = new String[1];
		sa[0] = String.valueOf(_data.getUpdatedTime());
		// If the data is new, insert it into the provider.
		Cursor c = cr.query(DataProvider.CONTENT_URI, null, s, sa, null);
		if (c != null) {
			if (c.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(DataProvider.KEY_DATE, _data.getUpdatedTime());
				values.put(DataProvider.KEY_TEMPERATURE, _data.getTemperature());
				values.put(DataProvider.KEY_POWER, _data.getPower());
				cr.insert(DataProvider.CONTENT_URI, values);
				this.announceNewData(_data);
			}

			c.close();
		}
	}

	private void announceError() {
		//stop the service
		updateTimer.cancel();
		Intent intent = new Intent(NEW_DATA_FOUND);
		if (errorMsg.length() > 0)
			intent.putExtra("msg", errorMsg);
		this.sendBroadcast(intent);
	}

	/**
	 * @param data
	 */
	private void announceNewData(GraphData data) {

		Intent intent = new Intent(NEW_DATA_FOUND);
		if (errorMsg.length() > 0)
			intent.putExtra("msg", errorMsg);

		intent.putExtra("date", data.getUpdatedTime());
		intent.putExtra("value0", data.getTemperature());
		intent.putExtra("value1", data.getPower());
		this.sendBroadcast(intent);
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
		updateTimer = new Timer("dataUpdates");
		// read the channel number
		try {
			intChannel = Integer.parseInt(Commons
					.readFromFileS("channel", this));
		} catch (NumberFormatException e) {
			intChannel = 1;
			e.printStackTrace();
		} catch (IOException e) {
			intChannel = 1;
			e.printStackTrace();
		}
		try {
			updatePeriod = Integer.parseInt(Commons.readFromFileS("intUpdate",
					this));
		} catch (NumberFormatException e) {
			updatePeriod = 4;
			e.printStackTrace();
		} catch (IOException e) {
			updatePeriod = 4;
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
		updateTimer.cancel();
		updateTimer.purge();
		lastLookup.cancel(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		updateTimer.cancel();

		updateTimer = new Timer("dataUpdates");
		updateTimer.scheduleAtFixedRate(this.doRefresh, 0,
				updatePeriod * 60 * 1000);

		refreshData();
		return Service.START_STICKY;
	}

	/**
	 * 
	 */
	private void refreshData() {

		if ((this.lastLookup == null)
				|| this.lastLookup.getStatus()
						.equals(AsyncTask.Status.FINISHED)) {

			this.lastLookup = new LoadData();
			this.lastLookup.execute((Void[]) null);
		}
	}
}
