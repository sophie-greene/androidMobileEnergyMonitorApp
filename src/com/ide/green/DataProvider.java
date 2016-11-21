package com.ide.green;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DataProvider extends ContentProvider {
	// Helper class for opening, creating, and managing database version control
	private static class dataDatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE = "create table "
				+ TABLE_DATA + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_DATE
				+ " INTEGER, " + KEY_TEMPERATURE + " FLOAT, " + KEY_POWER
				+ " FLOAT);";
		private static final String DATABASE_CREATE_APP = "create table "
				+ TABLE_APP + " (" + KEY_ID_APP
				+ " integer primary key autoincrement, " + KEY_CHANNEL
				+ " INTEGER, " + KEY_DESCRIPTION + " FLOAT);";
		private static final String DATABASE_CREATE_H = "create table "
				+ TABLE_HISTORY + " (" + KEY_ID_H
				+ " integer primary key autoincrement, " + KEY_DATE_H
				+ " INTEGER, " + KEY_POWER_H + " FLOAT);";

		public dataDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE_APP);
			db.execSQL(DATABASE_CREATE_H);
		}

		// public void onDestroy(SQLiteDatabase db) {
		// db.close();
		// }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
			this.onCreate(db);
		}
	}

	/**
	 * 
	 */
	private static final int APP = 3;
	/**
	 * 
	 */
	private static final int ID_APP = 4;

	/**
	 * 
	 */
	private static final String TABLE_APP = "appliance";

	/**
	 * 
	 */
	public static final int COLUMN_CHANNEL = 1;

	/**
	 * 
	 */
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.ide.provider.green/data");

	/**
	 * 
	 */
	public static final Uri CONTENT_URI_APP = Uri
			.parse("content://com.ide.provider.green/app");

	/**
	 * 
	 */
	public static final Uri CONTENT_URI_H = Uri
			.parse("content://com.ide.provider.green/h");

	// Create the constants used to differentiate between the different URI
	// requests.
	/**
	 * 
	 */
	private static final int DATA = 1;
	/**
	 * 
	 */
	private static final int ID_DATA = 2;
	/**
	 * 
	 */
	private static final String TABLE_DATA = "data";
	/**
	 * 
	 */
	private static final String DATABASE_NAME = "data.db";
	/**
	 * 
	 */
	private static final int DATABASE_VERSION = 1;
	// Column indexes
	/**
	 * 
	 */
	public static final int COLUMN_DATE = 1;
	/**
	 * 
	 */
	public static final int COLUMN_DESCRIPTION = 2;
	/**
	 * 
	 */
	private static final int HISTORY = 5;
	/**
	 * 
	 */
	public static final int COLUMN_DATE_H = 1;
	/**
	 * 
	 */
	private static final int ID_HISTORY = 6;
	/**
	 * 
	 */
	private static final String TABLE_HISTORY = "history";

	/**
	 * 
	 */
	public static final int COLUMN_POWER_H = 2;
	/**
	 * 
	 */
	public static final String KEY_CHANNEL = "channel";
	/**
	 * 
	 */
	public static final String KEY_DATE = "date";

	/**
	 * 
	 */
	public static final String KEY_DATE_H = "date";
	/**
	 * 
	 */
	public static final String KEY_DESCRIPTION = "description";
	// Column Names
	/**
	 * 
	 */
	public static final String KEY_ID = "_id";

	/**
	 * 
	 */
	public static final String KEY_ID_APP = "_id";
	/**
	 * 
	 */
	public static final String KEY_ID_H = "_id";
	/**
	 * 
	 */
	public static final String KEY_TEMPERATURE = "value0";

	/**
	 * 
	 */
	public static final String KEY_POWER = "value1";
	/**
	 * 
	 */
	public static final String KEY_POWER_H = "value1";

	/**
	 * 
	 */
	private static final String TAG = "DataProvider";
	/**
	 * 
	 */
	private static final UriMatcher uriMatcher;

	/**
	 * 
	 */
	public static final int COLUMN_TEMPERATURE = 2;

	/**
	 * 
	 */
	public static final int COLUMN_POWER = 3;
	// Allocate the UriMatcher object, where a URI ending in ‘data’ will
	// correspond to a request for all data, and ‘data’ with a
	// trailing ‘/[rowID]’ will represent a single data row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.ide.provider.green", "data", DATA);
		uriMatcher.addURI("com.ide.provider.green", "data/#", ID_DATA);
		uriMatcher.addURI("com.ide.provider.green", "app", APP);
		uriMatcher.addURI("com.ide.provider.green", "app/#", ID_APP);
		uriMatcher.addURI("com.ide.provider.green", "h", HISTORY);
		uriMatcher.addURI("com.ide.provider.green", "h/#", ID_HISTORY);
	}
	// The underlying database
	/**
	 * 
	 */
	private SQLiteDatabase DataDB;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case DATA:
			count = this.DataDB.delete(TABLE_DATA, where, whereArgs);
			break;
		case APP:
			count = this.DataDB.delete(TABLE_APP, where, whereArgs);
			break;
		case HISTORY:
			count = this.DataDB.delete(TABLE_HISTORY, where, whereArgs);
			break;
		case ID_DATA:
			String segment = uri.getPathSegments().get(1);
			count = this.DataDB.delete(TABLE_DATA,
					KEY_ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case ID_APP:
			String segment1 = uri.getPathSegments().get(1);
			count = this.DataDB.delete(TABLE_APP,
					KEY_ID_APP
							+ "="
							+ segment1
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case ID_HISTORY:
			String segment2 = uri.getPathSegments().get(1);
			count = this.DataDB.delete(TABLE_HISTORY,
					KEY_ID_H
							+ "="
							+ segment2
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		this.getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case DATA:
			return "vnd.android.cursor.dir/vnd.ide";
		case ID_DATA:
			return "vnd.android.cursor.item/vnd.ide";
		case APP:
			return "vnd.android.cursor.dir/vnd.ide";
		case ID_APP:
			return "vnd.android.cursor.item/vnd.ide";
		case HISTORY:
			return "vnd.android.cursor.dir/vnd.ide";
		case ID_HISTORY:
			return "vnd.android.cursor.item/vnd.ide";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri _uri, ContentValues _initialValues) {
		// Insert the new row, will return the row number if
		// successful.
		Uri uri = null;
		switch (uriMatcher.match(_uri)) {
		case DATA:
		case ID_DATA:
			long rowID = this.DataDB.insert(TABLE_DATA, "data", _initialValues);
			// Return a URI to the newly inserted row on success.
			if (rowID > 0) {
				uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
				this.getContext().getContentResolver().notifyChange(uri, null);
			}
			break;
		case APP:
		case ID_APP:
			rowID = this.DataDB.insert(TABLE_APP, "app", _initialValues);
			// Return a URI to the newly inserted row on success.
			if (rowID > 0) {
				uri = ContentUris.withAppendedId(CONTENT_URI_APP, rowID);
				this.getContext().getContentResolver().notifyChange(uri, null);
			}
			break;
		case HISTORY:
		case ID_HISTORY:
			rowID = this.DataDB.insert(TABLE_HISTORY, "hoha", _initialValues);
			// Return a URI to the newly inserted row on success.
			if (rowID > 0) {
				uri = ContentUris.withAppendedId(CONTENT_URI_H, rowID);
				this.getContext().getContentResolver().notifyChange(uri, null);
			}
			break;
		default:
			throw new SQLException("Failed to insert row into " + _uri);
		}
		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		Context context = this.getContext();
		dataDatabaseHelper dbHelper = new dataDatabaseHelper(context,
				DATABASE_NAME, null, DATABASE_VERSION);
		try {
			this.DataDB = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			this.DataDB = dbHelper.getReadableDatabase();
			e.printStackTrace();
		}
		return this.DataDB == null ? false : true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Cursor c = null;
		String orderBy = null;
		switch (uriMatcher.match(uri)) {
		case DATA:
		case ID_DATA:
			qb.setTables(TABLE_DATA);
			// If this is a row query, limit the result set to the passed in
			// row.
			switch (uriMatcher.match(uri)) {
			case ID_DATA:
				qb.appendWhere(KEY_ID + "= ?");
				selectionArgs = new String[] { uri.getPathSegments().get(1) };
				break;
			default:
				break;
			}
			// If no sort order is specified sort by date / time

			if (TextUtils.isEmpty(sort)) {
				orderBy = KEY_DATE;
			} else {
				orderBy = sort;
			}

			break;
		case APP:
		case ID_APP:
			qb.setTables(TABLE_APP);
			// If this is a row query, limit the result set to the passed in
			// row.
			switch (uriMatcher.match(uri)) {
			case ID_APP:
				qb.appendWhere(KEY_ID_APP + "= ?");
				selectionArgs = new String[] { uri.getPathSegments().get(1) };
				break;
			default:
				break;
			}
			// If no sort order is specified sort by date / time

			if (TextUtils.isEmpty(sort)) {
				orderBy = KEY_CHANNEL;
			} else {
				orderBy = sort;
			}

			break;
		case HISTORY:
		case ID_HISTORY:
			qb.setTables(TABLE_HISTORY);
			// If this is a row query, limit the result set to the passed in
			// row.
			switch (uriMatcher.match(uri)) {
			case ID_HISTORY:
				qb.appendWhere(KEY_ID_H + "= ?");
				selectionArgs = new String[] { uri.getPathSegments().get(1) };
				break;
			default:
				break;
			}
			// If no sort order is specified sort by date / time

			if (TextUtils.isEmpty(sort)) {
				orderBy = KEY_DATE_H;
			} else {
				orderBy = sort;
			}

			break;
		default:
			break;
		}
		// Apply the query to the underlying database.
		try {
			c = qb.query(this.DataDB, projection, selection, selectionArgs,
					null, null, orderBy);
			// Register the contexts ContentResolver to be notified if
			// the cursor result set changes.
			c.setNotificationUri(this.getContext().getContentResolver(), uri);

			// Return a cursor to the query result.
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case DATA:
			count = this.DataDB.update(TABLE_DATA, values, where, whereArgs);
			break;
		case APP:
			count = this.DataDB.update(TABLE_APP, values, where, whereArgs);
			break;
		case HISTORY:
			count = this.DataDB.update(TABLE_HISTORY, values, where, whereArgs);
			break;
		case ID_DATA:
			String segment = uri.getPathSegments().get(1);
			count = this.DataDB.update(TABLE_DATA, values,
					KEY_ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case ID_APP:
			String segment1 = uri.getPathSegments().get(1);
			count = this.DataDB.update(TABLE_APP, values,
					KEY_ID_APP
							+ "="
							+ segment1
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case ID_HISTORY:
			String segment2 = uri.getPathSegments().get(1);
			count = this.DataDB.update(TABLE_HISTORY, values,
					KEY_ID_H
							+ "="
							+ segment2
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		this.getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}