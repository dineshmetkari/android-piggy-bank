package com.hangman.piggybank.Models;

import java.util.Hashtable;

import com.hangman.piggybank.PiggyBankApplication;
import com.hangman.piggybank.Utils.Formatter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Model class for saving data about user wishes, resources and etc.
 * It's a singletone class.
 * @author pavel.todorov
 */
public class PiggyBank extends SQLiteOpenHelper {
	
	/**
	 * Simple record for saving data about wish in model class.
	 * @author pavel.todorov
	 */
	public class WishRecord {
		public String name = "";
		public String note = "";
		public int priority = 0;
		public double amount = 0.0;
	}
	
	final static String TAG = "PiggyBank";
	final static String ResourcesTableName = "resources_table";
	final static String WishiesTableName = "wishies_table";
	final static int TableVersion = 4;
	final static int PrioritiesCount = 6;

	private Hashtable<Integer, WishRecord> _wishes = null;
	
	/**
	 * Private singleton's constructor.
	 */
	private PiggyBank() {
		super(PiggyBankApplication.getContext(), "PiggyBankTable", null, TableVersion);
	}

	/**
	 * Some construction which hold singleton's instance.
	 */
	private static class PiggyBankHolder {
		private static final PiggyBank INSTANCE = new PiggyBank();
	}

	/**
	 * Get piggy's bank instance.
	 */
	public static PiggyBank getInstance() {
		return PiggyBankHolder.INSTANCE;
	}

	/**
	 * Get resource value (amount) for specified resource.
	 * @param id Id for resource.
	 * @return Resource value (amount). If it doesn't exists 0.0 returned.
	 */
	public double getResourceValue(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(ResourcesTableName, 
				new String[]{"id", "value"}, 
				String.format("id=%d", id), 
				null, null, null, null);
		if(cursor.getCount() == 0)
			return 0.0;
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex("value");
		double res = cursor.getDouble(idx);
		db.close();
		return res;
	}
	
	/**
	 * Set resource value (amount) for specified resource.
	 * If id doesn't exist it created.
	 * Progress value for amount has not changed.
	 * @param id Id for resource.
	 */
	public void setResourceValueWithoutProgressChanging(int id, double value) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(ResourcesTableName, 
				new String[]{"id", "value"}, 
				String.format("id=%d", id), 
				null, null, null, null);
		boolean isFirst = (cursor.getCount() == 0);
		db.close();
		
		ContentValues cv = new ContentValues();
		cv.put("id", String.format("%d", id));
		String val = Formatter.getValueSQLiteFormatter(value, 2);
		cv.put("value",  val);
		db = this.getWritableDatabase();
		if(isFirst)
			db.insert(ResourcesTableName, null, cv);
		else
			db.update(ResourcesTableName, cv, String.format("id=%d", id), null);
        db.close();
	}
	
	/**
	 * Set resource value (amount) for specified resource.
	 * If id doesn't exist it created.
	 * Also it change amount progress value. 
	 * @param id Id for resource.
	 */
	public void setResourceValue(int id, double value) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(ResourcesTableName, 
				new String[]{"id", "value", "progress", "lastDate", "firstDate"}, 
				String.format("id=%d", id), 
				null, null, null, null);
		boolean isFirst = (cursor.getCount() == 0);
		
		double lastValue = this.getResourceValue(id);
		
		ContentValues cv = new ContentValues();
		cv.put("id", String.format("%d", id));
		String val = Formatter.getValueSQLiteFormatter(value, 2);
		cv.put("value",  val);
		long currentMoment = System.currentTimeMillis()/1000;
		if(isFirst) {
			cv.put("progress", 0.0);
			cv.put("firstDate", String.format("%d", currentMoment));
		}
		else {
			cursor.moveToFirst();
			String dateString = cursor.getString(3);
			long lastChangingMoment = (new Long(dateString)).longValue();
			String firstDateString = cursor.getString(4);
			long firstChangingMoment = (new Long(firstDateString)).longValue();
			double currentProgress = cursor.getDouble(2);
			double lastPeriod = (double)(lastChangingMoment - firstChangingMoment);
			double period = (double)(currentMoment - firstChangingMoment);
			double progress = (currentProgress*lastPeriod + value - lastValue)/period;
			cv.put("progress", progress);
			
			Log.d(TAG, String.format("New progress computed: %f", progress));
		}
		cv.put("lastDate", String.format("%d", currentMoment));
		db.close();
		
		db = this.getWritableDatabase();
		if(isFirst)
			db.insert(ResourcesTableName, null, cv);
		else
			db.update(ResourcesTableName, cv, String.format("id=%d", id), null);
        db.close();
	}

	/**
	 * Get resource progress for specified resource.
	 * @param id Id for resource.
	 * @return Resource progress in values in second. If it doesn't exists 0.0 returned.
	 */
	public double getResourceProgress(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(ResourcesTableName, 
				new String[]{"id", "progress"}, 
				String.format("id=%d", id), 
				null, null, null, null);
		if(cursor.getCount() == 0)
			return 0.0;
		cursor.moveToFirst();
		double res = cursor.getDouble(1);
		db.close();
		return res;
	}

	/**
	 * Change or add wished stuff.
	 * @param id Stuff id for changing or null for adding.
	 * @param name String with name for stuff.
	 * @param amount Needed for stuff resource value.
	 * @param priority Priority for accumulation.
	 * @param note String with notes for wished stuff.
	 */
	public void changeWishedStuff(int id, String name, double amount, int priority, String note) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(WishiesTableName, 
				new String[]{"key", "name", "amount", "priority", "note"}, 
				String.format("key=%d", id), 
				null, null, null, null);
		boolean isFirst = (cursor.getCount() == 0);
		db.close();
		
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("amount", Formatter.getValueFormatter(amount, 2));
		cv.put("priority", String.format("%d", priority));
		cv.put("note", note);
		db = this.getWritableDatabase();
		if(isFirst)
			db.insert(WishiesTableName, null, cv);
		else
			db.update(WishiesTableName, cv, String.format("key=%d", id), null);
        db.close();
        getAllWishes();
	}

	/**
	 * Delete wish.
	 * @param id Wish id to delete.
	 */
	public void deleteWish(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int res = db.delete(WishiesTableName,  String.format("key=%d", id), null);
		Log.d(TAG, String.format("%d rows deleted from wishes data table.", res));
		db.close();
        getAllWishes();
	}
	
	/**
	 * Read all wishes from database.
	 * @return Hash table with keys and wishes.
	 */
	private void getAllWishes() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(WishiesTableName, 
				new String[]{"key", "name", "amount", "priority", "note"}, 
				null, null, null, null, null);
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			_wishes = new Hashtable<Integer, WishRecord>();
			return;
		}
		Hashtable<Integer, WishRecord> table = new Hashtable<Integer, WishRecord>();
		do {
			WishRecord record = new WishRecord();
			int key = cursor.getInt(0);
			record.name = cursor.getString(1);
			record.amount = cursor.getDouble(2);
			record.priority = cursor.getInt(3);
			record.note = cursor.getString(4);
			table.put(key, record);
		}
		while(cursor.moveToNext());
		_wishes = table;
	}
	
	/**
	 * Get current wishes hash table.
	 * @return Wishes hash table.
	 */
	public Hashtable<Integer, WishRecord> getWishes() {
		if(_wishes == null)
			getAllWishes();
		return _wishes;
	}
	
	@Override
	public void onCreate(SQLiteDatabase dataBase) {
		dataBase.execSQL("create table " + ResourcesTableName + " (id integer, value real, progress real, lastDate text, firstDate text)");
		dataBase.execSQL("create table " + WishiesTableName + " (key integer primary key asc, name text, amount real, priority integer, note text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase dataBase, int oldVersion, int newVersion) {
		/*if(oldVersion < TableVersion) {
			dataBase.execSQL("DROP TABLE IF EXISTS " + ResourcesTableName);
			dataBase.execSQL("create table " + ResourcesTableName + " (id integer, value real)");
		}*/
	}
}
