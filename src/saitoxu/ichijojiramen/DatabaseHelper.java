package saitoxu.ichijojiramen;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DBNAME = "ramen.db";
	private static final int DBVERSION = 1;
	private static final String CREATE_TABLE_SQL = "create table ramen"
			+ "(_id integer primary key autoincrement,"
			+ "ramen_id integer not null," + "name text not null,"
			+ "pass text default ''," + "tel text default '',"
			+ "hp text default ''," + "dayoff text default '',"
			+ "time text default ''," + "address text default '',"
			+ "coupon text default ''," + "photo_ramen blob default null,"
			+ "ido text default ''," + "keido text default '',"
			+ "photo_view blob default null," + "flag integer default 0)";

	public DatabaseHelper(Context context) {
		super(context, DBNAME, null, DBVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
