package com.weiwend.fooldelivery.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//******************************该类由于需求问题，暂时已经作废*******************************
public class AreasDatabaseHelper extends SQLiteOpenHelper{

	private static final String DB_NAME = "areas.db";
	private static final int DB_VERSION = 1;

	private static final String CREATE_AREA_TABLE = "create table areas_table ("
																	+"area_id String,"
														            +"parent_id String,"
														            +"area_name String,"
														            +"first_letters String,"
																	+"sort String);";
	private static final String CREATE_ADDRESS_SENDER_TABLE = "create table address_sender_table ("
			                                                        +"senderId integer primary key autoincrement,"
			                                                        +"userId String,"
																	+"name String,"
														            +"phone String,"
														            +"area String,"
																	+"detailAddress String);";
	
	
	public AreasDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_AREA_TABLE);
		db.execSQL(CREATE_ADDRESS_SENDER_TABLE);
		Log.e("zyf:", "areas_table onCreate...");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		
		db.execSQL("drop table areas_table if exists");	
		db.execSQL(CREATE_AREA_TABLE);
		
		db.execSQL("drop table address_sender_table if exists");	
		db.execSQL(CREATE_ADDRESS_SENDER_TABLE);
		Log.e("zyf:", "areas_table onUpgrade...");
	}

}
