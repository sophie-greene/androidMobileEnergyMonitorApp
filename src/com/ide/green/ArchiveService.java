package com.ide.green;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ArchiveService extends Service {
	/**
	 * @author Somoud
	 * 
	 */
	private class LoadArchive extends AsyncTask<Void, GraphData, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			
			
			SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dfd = new SimpleDateFormat("HH:mm:ss");
			int id;
			String api;
			// read the channel number

			try {
				id = Integer.parseInt(Commons.readFromFileS("eidFile",ArchiveService.this));
			} catch (NumberFormatException e1) {
				id = 0;
				e1.printStackTrace();
			} catch (IOException e1) {
				id = 0;
				e1.printStackTrace();
			}

			try {
				api = Commons.readFromFileS("apiFile",ArchiveService.this);
			} catch (IOException e) {
				e.printStackTrace();
				api = null;
			}
			
			Date start = new Date();
			Date end=new Date();
			start.setHours(start.getTimezoneOffset());
			
			int thisMonth=start.getMonth();
			int thisDay=start.getDate()+2;
			
			String s1, s2, urlS;
			start.setDate(1);
			start.setHours(0);
			start.setMinutes(0);
			start.setSeconds(0);
			

			if (id > 0 & api != null) {
				for (int j = 0; j < 3; j++) {
					for (int i = 1; i < 
							Commons.noOfDaysInMonth(start); i++) {
						start.setMonth(thisMonth-j);
						start.setDate(i);
						s1 = dfm.format(start) + "T" + dfd.format(start) + "Z";
						end=start;
						end.setDate(end.getDate()+1);
						s2 = dfm.format(end) + "T" + dfd.format(end) + "Z";
						urlS = Commons.getArchiveDataURL(s1, s2, id, api);
						Log.i("Dates",urlS);
						try {
							Commons.readData(urlS,ArchiveService.this);
						} catch (IOException e) {
							errorMsg = Commons.INTERNET_CONNECTION;
							e.printStackTrace();
						}
						//if today is reached go to previous month
						if(j==0&& i>thisDay)break;
							
					}
					
					
				}
			} else {
				errorMsg = Commons.INVALID_FEED;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			announceServiceFinished();
		}
	}

	/**
	 * @author Somoud
	 * 
	 */
	public class MyBinder extends Binder {
		ArchiveService getService() {
			return ArchiveService.this;
		}
	}

	/**
	 * notification string for the broadcast
	 */
	public static final String ARCHIVE_LOADED = "Archive_Loaded";

	
	
	private String errorMsg = "";

	/**
	 * binds the service
	 */
	private final IBinder binder = new MyBinder();

	/**
	 * asycTask type
	 */
	private LoadArchive history = null;

	
	/**
	 * 
	 */
	private void announceServiceFinished() {
		Intent intent = new Intent(ARCHIVE_LOADED);
		if (errorMsg.length() > 0)
			intent.putExtra("msg", errorMsg);
		this.sendBroadcast(intent);
	}
	/**
	 * 
	 */
	private void getHistory() {
		if (this.history == null) {
			this.history = new LoadArchive();
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
