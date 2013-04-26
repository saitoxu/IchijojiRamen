package saitoxu.ichijojiramen;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Coupon extends Activity {
	private DatabaseHelper dbhelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon);

		SharedPreferences prefer = getSharedPreferences("IchijojiRamen",
				MODE_PRIVATE);
		String endStr = prefer.getString("start", "") + "〜"
				+ prefer.getString("end", "");
		String startStr = "【有効期間】";

		TextView start = (TextView) findViewById(R.id.kikan_text);
		start.setText(startStr);
		TextView end = (TextView) findViewById(R.id.days_text);
		end.setText(endStr);

		this.dbhelper = new DatabaseHelper(this);

		// データベースに入ってるラーメン屋を取得して，リストにセット
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = getAll(db);
		ArrayList<OriginalItem2> items = convertCursorToItems(cursor);
		cursor.close();
		db.close();

		ArrayAdapter<OriginalItem2> adapter = new OriginalArrayAdapter2(this,
				R.layout.coupon_row, items);
		ListView listView = (ListView) findViewById(R.id.listview2);

		// アダプターを設定
		listView.setAdapter(adapter);
	}

	// カーソルからOriginalItem2のリストを作る
	// このメソッドでクーポンがない店を弾く
	private ArrayList<OriginalItem2> convertCursorToItems(Cursor cursor) {
		ArrayList<OriginalItem2> items = new ArrayList<OriginalItem2>();

		cursor.moveToFirst();

		for (int i = 0; i < cursor.getCount(); i++) {
			Drawable drawable = getDrawableFromByteArray(cursor.getBlob(1));
			if (!cursor.getString(3).equals("null")
					&& !cursor.getString(3).equals("")) {
				OriginalItem2 item2 = new OriginalItem2(cursor.getInt(0),
						drawable, cursor.getString(2), cursor.getString(3));
				items.add(item2);
			}
			cursor.moveToNext();
		}

		return items;
	}

	// byte配列からDrawableを取得
	public Drawable getDrawableFromByteArray(byte[] b) {
		return new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
	}

	// ラーメン屋のIDと店名，店の写真，クーポンの内容を持ったカーソルを取得
	private Cursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query("ramen", new String[] { "ramen_id",
				"photo_view", "name", "coupon" }, null, null, null, null, null);

		return cursor;
	}
}
