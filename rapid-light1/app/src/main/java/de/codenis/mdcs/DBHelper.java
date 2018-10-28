package de.codenis.mdcs;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mdcs-db2";
	private static final int DATABASE_VERSION = 9;
	private static final String PROJECT_DATABASE_TABLE = "project";
	private static final String POSITION_DATABASE_TABLE = "position";
	private static final String PLAN_DATABASE_TABLE = "plan";
	private static final String USER_DATABASE_TABLE = "user";
	private static final String TOXIC_DATABASE_TABLE = "toxic_substance";
	private static final String DES_DATABASE_TABLE = "preset_preselect_description";
	private static final String MEMBER_DATABASE_TABLE = "team";
	
	private static final String PROJECT_DATABASE_CREATE = "create table "+PROJECT_DATABASE_TABLE+"(_id integer primary key autoincrement, "
	+ "server_project_id integer, "
	+ "date_creation text not null, "
	+ "name_station text not null, "
	+ "number_station text not null, "
	+ "address text, "
	+ "object text, "
	+ "auftraggeber text, "
	+ "date_visit text not null, "
	+ "name_evaluator text, "
	+ "photo text, "
	+ "number_of_plan text, "
	+ "image text,"
	+"status text);";
	
	private static final String POSITION_DATABASE_CREATE = "create table "+POSITION_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+ "server_position_id text, "
			+ "project_id text not null, "
			+ "position_number text, "
			+ "toxic_substance text not null, "
			+ "description_topic text, "
			+ "description text, "
			+ "degree text not null, "
			+ "investigation text not null, "
			+ "priority text not null, "
			+ "comment text, "
			+ "photo1 text, "
			+ "photo2 text, "
			+ "plan_id integer, "
			+ "position_xo float, "
			+ "position_yo float, "
			+"status text);";

	private static final String PLAN_DATABASE_CREATE = "create table "+PLAN_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+ "server_plan_id text, "
			+ "project_id text not null, "
			+ "plan_url text not null, "
			+ "plan_name text, "
			+ "number text, "
			+"status text);";

	private static final String USER_DATABASE_CREATE = "create table "+USER_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+"loginname text, "
			+"password text);";
	
	
	private static final String TOXIC_DATABASE_CREATE = "create table "+TOXIC_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+"toxic_substance text);";
	
	private static final String DES_DATABASE_CREATE = "create table "+DES_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+"preselect_description text,"
			+"priority text);";
	
	private static final String MEMBER_DATABASE_CREATE = "create table "+MEMBER_DATABASE_TABLE+"(_id integer primary key autoincrement, "
			+ "loginname text, "
			+ "password text, "
			+"member text);";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("my", "table created");
		try
		{
		db.execSQL(PROJECT_DATABASE_CREATE);
		db.execSQL(POSITION_DATABASE_CREATE);
		db.execSQL(PLAN_DATABASE_CREATE);
		db.execSQL(USER_DATABASE_CREATE);
		db.execSQL(TOXIC_DATABASE_CREATE);
		db.execSQL(DES_DATABASE_CREATE);
		db.execSQL(MEMBER_DATABASE_CREATE);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("my", "Error"+e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS members");
		onCreate(db);
	}

}
