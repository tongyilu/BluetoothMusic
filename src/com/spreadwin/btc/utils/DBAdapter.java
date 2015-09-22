package com.spreadwin.btc.utils;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * 数据库相关操作的类
 */
public class DBAdapter {
	/**
	 * 数据库名
	 */
	private static final String DATABASE_NAME = "contacts.db";

	/**
	 * 数据表名
	 */
	private static final String DATABASE_TABLE = "contacts_table";

	/**
	 * 数据库版本
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * key_id ：主键
	 */
	@SuppressWarnings("unused")
	private static final String KEY_ID = "key_id";

	/**
	 * position ：位置信息，表示数据是第几项
	 */
	private static final String POSITION = "position";

	/**
	 * name ：姓名
	 */
	private static final String NAME = "name";

	/**
	 * phone_number ：电话号码
	 */
	private static final String PHONE_NUMBER = "phone_number";

	/**
	 * ip ：是否ip播出
	 */
	private static final String IP = "ip";

	/**
    * 
    */
	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + "  (key_id INTEGER PRIMARY KEY, "
			+ "position INTEGER, " + "name TEXT, " + "phone_number TEXT, "
			+ "ip INTEGER" + ");";

	/**
    * 
    */
	private final Context context;

	/**
    * 
    */
	private DatabaseHelper DBHelper;

	/**
    * 
    */
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		context = ctx;
		DBHelper = DatabaseHelper.getInstance(ctx);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static DatabaseHelper mInstance = null; 
		/**单例模式**/

		public static synchronized DatabaseHelper getInstance(Context context) { 
			if (mInstance == null) { 
				mInstance = new DatabaseHelper(context); 
			} 
			return mInstance; 
		} 
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {			
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
		
	

	}

	/**
	 * 打开数据库
	 * 
	 * @return
	 * @throws SQLException
	 */
	public SQLiteDatabase open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		
//		Cursor cursor = getAll();
//
//		if (cursor.getCount() == 0) {
//
//			Log.e("数据库为空", "插入数据");
//			/**
//			 * 先使用假数据初始化数据库
//			 */
////			for (int i = 0; i < 10; i++) {
////				insert(i, "", "", 0);
////			}
//		} else {
//			Log.e("数据库不为空", "读取数据");
//		}
//		cursor.close();

		return db;
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		DBHelper.close();			
	}

	/**
	 * 向数据库中插入数据
	 */

	public long insert(int position, String name, String phone_number, int ip) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(POSITION, position);
		initialValues.put(NAME, name);
		initialValues.put(PHONE_NUMBER, phone_number);
		initialValues.put(IP, ip);

		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * 删除数据,其实不是真正意义上的删除，而是将name = ""、phone_number = ""、ip = 0
	 */
	public boolean delete(int position) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(POSITION, position);
		initialValues.put(NAME, "");
		initialValues.put(PHONE_NUMBER, "");
		initialValues.put(IP, 0);

		return db.update(DATABASE_TABLE, initialValues, POSITION + "="
				+ position, null) > 0;

		// return db.delete(DATABASE_TABLE, POSITION + "=" + position, null) >
		// 0;
	}

	/**
	 * 更改数据
	 */
	public boolean update(int position, String name, String phone_number, int ip) {

		ContentValues initialValues = new ContentValues();
		// initialValues.put(POSITION, position);
		initialValues.put(NAME, name);
		initialValues.put(PHONE_NUMBER, phone_number);
		initialValues.put(IP, ip);

		return db.update(DATABASE_TABLE, initialValues, POSITION + "="
				+ position, null) > 0;
	}

	/* 删除一个表 */
	public void DeleteTable() {
		Cursor cursor = getAll();
		for (int i = 0; i < cursor.getCount(); i++) {
			db.delete(DATABASE_TABLE, POSITION + "=" + i, null);			
		}
		cursor.close();
	}

	public Cursor getAll() {

		Cursor cur = db.query(DATABASE_TABLE, null, null, null, null, null,
				null);
		return cur;

	}

	public Cursor get(long rowId) throws SQLException {
		Cursor cur = db.query(true, DATABASE_TABLE, new String[] { POSITION,
				NAME, PHONE_NUMBER, IP },

		POSITION + "=" + rowId, null, null, null, null, null);

		if (cur != null) {
			cur.moveToFirst();
		}

		return cur;
	}

}
