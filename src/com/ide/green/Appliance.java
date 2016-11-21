package com.ide.green;

import java.io.IOException;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;

public class Appliance extends ListActivity {
	private Cursor c;
	private int intChannel;
	

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
		ListView list;
		SimpleCursorAdapter myAdapter;
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.appliance);
		getWindow().setBackgroundDrawableResource(R.color.bgColor);
		ContentResolver cr = this.getContentResolver();
		// Return todays data

		this.c = cr.query(DataProvider.CONTENT_URI_APP, null, null, null, null);
		if (this.c.getCount() > 0) {
			// appView = (ListView) findViewById(R.id.lstAppliance);

			String[] fromColumns = new String[] {

			DataProvider.KEY_DESCRIPTION };
			int[] toLayoutIDs = new int[] { R.id.description };

			myAdapter = new SimpleCursorAdapter(this, R.layout.app_view,
					this.c, fromColumns, toLayoutIDs);
			this.setListAdapter(myAdapter);
		}
		list = this.getListView();
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		list.setPadding(5, 20, 5, 5);
		try {
			this.intChannel = Integer.parseInt(Commons.readFromFile("channel",this));
		} catch (IOException e) {
			this.intChannel = 1;
			e.printStackTrace();
		}
		if (this.intChannel > 0) {
			list.setSelection(this.intChannel - 1);
			list.setItemChecked(this.intChannel - 1, true);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.c.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		RadioButton select = (RadioButton) this.findViewById(R.id.select);
		RadioButton edit = (RadioButton) this.findViewById(R.id.edit);
		if (edit.isChecked()) {
			Intent intent = new Intent(this, EditApp.class);
			intent.putExtra("channel", l.getCheckedItemPosition() + 1);
			this.startActivity(intent);
			finish();
		} else if (select.isChecked()) {
			try {
				String FILENAME = "channel";
				if (l.getCheckedItemPosition() + 1 != this.intChannel) {
					Commons.writeToFile(String.valueOf(
							l.getCheckedItemPosition() + 1), FILENAME,Appliance.this);
					ContentResolver cr = this.getContentResolver();
					cr.delete(DataProvider.CONTENT_URI, null, null);
				}
				this.launch();

			} catch (Exception e) {
				e.printStackTrace();
			}
			this.finish();
		}
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
			this.finish();
			return true;
		}
		return true;
	}
}