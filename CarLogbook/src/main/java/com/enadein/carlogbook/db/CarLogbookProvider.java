/*
    CarLogbook.
    Copyright (C) 2014  Eugene Nadein

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.enadein.carlogbook.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.text.MessageFormat;
import java.util.HashMap;

public class CarLogbookProvider extends ContentProvider {
	public static final String LIMIT_PARAM = "limit_param";
	protected DBOpenHelper dbHelper;

	protected HashMap<Integer, String> tables = new HashMap<Integer, String>();
	protected HashMap<Integer, String> types = new HashMap<Integer, String>();


	@Override
	public boolean onCreate() {
		dbHelper = new DBOpenHelper(getContext());

		tables.put(ProviderDescriptor.Car.PATH_TOKEN, ProviderDescriptor.Car.TABLE_NAME);
		tables.put(ProviderDescriptor.DataValue.PATH_TOKEN, ProviderDescriptor.DataValue.TABLE_NAME);
		tables.put(ProviderDescriptor.Log.PATH_TOKEN, ProviderDescriptor.Log.TABLE_NAME);
		tables.put(ProviderDescriptor.Notify.PATH_TOKEN, ProviderDescriptor.Notify.TABLE_NAME);
		tables.put(ProviderDescriptor.LogView.PATH_TOKEN, ProviderDescriptor.LogView.TABLE_NAME);
		tables.put(ProviderDescriptor.FuelRate.PATH_TOKEN, ProviderDescriptor.FuelRate.TABLE_NAME);

		types.put(ProviderDescriptor.Car.PATH_TOKEN, ProviderDescriptor.Car.CONTENT_TYPE_DIR);
		types.put(ProviderDescriptor.Car.PATH_ID_TOKEN, ProviderDescriptor.Car.CONTENT_TYPE_ITEM);

		types.put(ProviderDescriptor.DataValue.PATH_TOKEN, ProviderDescriptor.DataValue.CONTENT_TYPE_DIR);
		types.put(ProviderDescriptor.DataValue.PATH_ID_TOKEN, ProviderDescriptor.DataValue.CONTENT_TYPE_ITEM);

		types.put(ProviderDescriptor.Log.PATH_TOKEN, ProviderDescriptor.Log.CONTENT_TYPE_DIR);
		types.put(ProviderDescriptor.Log.PATH_ID_TOKEN, ProviderDescriptor.Log.CONTENT_TYPE_ITEM);

		types.put(ProviderDescriptor.Notify.PATH_TOKEN, ProviderDescriptor.Notify.CONTENT_TYPE_DIR);
		types.put(ProviderDescriptor.Notify.PATH_ID_TOKEN, ProviderDescriptor.Notify.CONTENT_TYPE_ITEM);

		types.put(ProviderDescriptor.FuelRate.PATH_TOKEN, ProviderDescriptor.FuelRate.CONTENT_TYPE_DIR);
		types.put(ProviderDescriptor.FuelRate.PATH_ID_TOKEN, ProviderDescriptor.FuelRate.CONTENT_TYPE_ITEM);

		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		Cursor result;

		String tableName = tables.get(token);
		boolean isPathId = false;
		if (tableName == null) {
			token++;
			isPathId = true;
			tableName = tables.get(token);
		}

		if (tableName == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(tableName);
		if (isPathId) {
			String id = uri.getLastPathSegment();
			result = builder.query(db, null, "_id = ?", new String[]{id}, null, null, null);
		} else {
			String limit = uri.getQueryParameter(LIMIT_PARAM);
			result = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
		}

		if (result != null) {
			result.setNotificationUri(getContext().getContentResolver(), uri);
			if (token == ProviderDescriptor.LogView.PATH_TOKEN) {
				result.setNotificationUri(getContext().getContentResolver(), ProviderDescriptor.Log.CONTENT_URI);
			}
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		String result = types.get(token);
		if (result == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		Uri result;

		String tableName = tables.get(token);
		if (tableName == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		long id = db.insert(tableName, null, values);
		getContext().getContentResolver().notifyChange(uri, null);
		result = uri.buildUpon().appendPath(String.valueOf(id)).build();
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		int result;

		String tableName = tables.get(token);
		if (tableName == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		result = db.delete(tableName, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		int result;

		String tableName = tables.get(token);
		if (tableName == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		result = db.update(tableName, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int token = ProviderDescriptor.URI_MATCHER.match(uri);

		String tableName = tables.get(token);
		if (tableName == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		db.beginTransaction();
		try {
			for (ContentValues cv : values) {
				db.insert(tableName, null, cv);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return values.length;
	}

	public class DBOpenHelper extends SQLiteOpenHelper {
		private static final int CURRENT_DB_VERSION = 44;
		private static final String DB_NAME = "com_carlogbook.db";
		private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {0} ({1})";
		private static final String DROP_TABLE = "DROP TABLE IF EXISTS {0}";

		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, CURRENT_DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db, ProviderDescriptor.Car.TABLE_NAME,
					ProviderDescriptor.Car.CREATE_FIELDS);

			createTable(db, ProviderDescriptor.DataValue.TABLE_NAME,
					ProviderDescriptor.DataValue.CREATE_FIELDS);

			createTable(db, ProviderDescriptor.Log.TABLE_NAME,
					ProviderDescriptor.Log.CREATE_FIELDS);

			createTable(db, ProviderDescriptor.Notify.TABLE_NAME,
					ProviderDescriptor.Notify.CREATE_FIELDS);

			createTable(db, ProviderDescriptor.FuelRate.TABLE_NAME,
					ProviderDescriptor.FuelRate.CREATE_FIELDS);

			db.execSQL(ProviderDescriptor.LogView.CREATE_QUERY);

			DataBaseDefaulter defaulter = new DataBaseDefaulter();
			defaulter.initDataBase(db, getContext());
		}

		public void reset() {
			dropAllTables(getWritableDatabase());
			onCreate(getWritableDatabase());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//nothing
			dropAllTables(db);
			onCreate(db);
		}

		private void dropAllTables(SQLiteDatabase db) {
			db.execSQL("DROP VIEW IF EXISTS log_view");
			dropTable(db, ProviderDescriptor.Car.TABLE_NAME);
			dropTable(db, ProviderDescriptor.Log.TABLE_NAME);
			dropTable(db, ProviderDescriptor.DataValue.TABLE_NAME);
			dropTable(db, ProviderDescriptor.Notify.TABLE_NAME);
		}

		public void dropTable(SQLiteDatabase db, String name) {
			String query = MessageFormat.format(DBOpenHelper.DROP_TABLE, name);
			db.execSQL(query);
		}

		public void createTable(SQLiteDatabase db, String name, String fields) {
			String query = MessageFormat.format(DBOpenHelper.CREATE_TABLE, name, fields);
			db.execSQL(query);
		}

	}
}
