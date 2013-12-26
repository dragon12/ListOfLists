package com.android.gers.listoflists.DB;

import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class DbTableListItems extends DbTableBase {
	
	public static final String TABLE_NAME = "items";

	public static final String COL_ID = BaseColumns._ID;
	public static final int COL_IDX_ID = 0;
	
	public static final String COL_LIST_ID = "list_id";
	public static final int COL_IDX_LIST_ID = 1;

	public static final String COL_NAME = "name";
	public static final int COL_IDX_NAME = 2;

	public static final String COL_RATING = "rating";
	public static final int COL_IDX_RATING = 3;

	public static final String[] Columns = {
		COL_ID,
		COL_LIST_ID,
		COL_NAME,
		COL_RATING
	};
	
	//query indices
	public static final int COL_QUERY_STATS_LIST_ID = 0;
	public static final int COL_QUERY_STATS_COUNT_STATUS = 1;
	
	private String foreignTable;

	private String foreignColumn;

	public DbTableListItems(String foreignTable, String foreignColumn) {
		super(TABLE_NAME);
		this.foreignTable = foreignTable;
		this.foreignColumn = foreignColumn;
	}
	
	public void drop(SQLiteDatabase db)
	{
		db.execSQL("drop table if exists " + TABLE_NAME);
	}
	
	@Override
	public void create(SQLiteDatabase db)
	{
		String sql = 
				"CREATE TABLE " + TABLE_NAME + "(" + 
						COL_ID + " integer primary key autoincrement, " +
						COL_LIST_ID + " integer not null REFERENCES " + foreignTable + "(" + foreignColumn + ")," +
						COL_NAME + " text not null, " +
						COL_RATING + " double not null)";
						
		db.execSQL(sql);		
	}
	
	public long insert(SQLiteDatabase db, ContentValues kvps) {
		return db.insert(TABLE_NAME, null, kvps);
	}
	
	public Boolean update(SQLiteDatabase db, long id, ContentValues kvps) {
		return db.update(TABLE_NAME, kvps, COL_ID + " = " + id, null) == 1;
	}
	
	public Boolean updateByListId(SQLiteDatabase db, long listId, ContentValues kvps) {
		return db.update(TABLE_NAME, kvps, COL_LIST_ID + " = " + listId, null) >= 1;
	}

	public Cursor getItemById(SQLiteDatabase db, long id) {
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_ID + " = " + id);
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);		
	}
	
	public Cursor getItemsByListId(SQLiteDatabase db, long listId) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_LIST_ID + " = " + listId);
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);		
	}
	
	public Cursor getItemStatsByListId(SQLiteDatabase db) {

		return db.rawQuery(
				"select " + COL_LIST_ID + ", count(" + COL_LIST_ID + ")" +
						" from " + TABLE_NAME +
						" group by " + COL_LIST_ID, 
						null);
	}
	
	// we don't return a status for this one because there's no guarantee that the list
	//  has any items
	public Boolean deleteItemsByListId(SQLiteDatabase db, long listId) {
		db.delete(TABLE_NAME, COL_LIST_ID + " = " + listId, null);
		return true;
	}
	
	public Boolean deleteItemsByListIdWhere(SQLiteDatabase db, long listId, ContentValues whereKvps) {
		StringBuilder whereClause = new StringBuilder();
		Set<Entry<String, Object>> kvpSet = whereKvps.valueSet();
		for (Entry<String, Object> kvp : kvpSet) {
			if (whereClause.length() != 0) {
				whereClause.append(" and ");
			}
			whereClause = whereClause.append(kvp.getKey()).append(" = ").append(kvp.getValue().toString());
		}
		db.delete(TABLE_NAME, whereClause.toString(), null);
		return true;
	}
	
	public Boolean deleteItemsById(SQLiteDatabase db, long id) {
		return db.delete(TABLE_NAME, COL_ID + " = " + id, null) == 1;
	}
}


