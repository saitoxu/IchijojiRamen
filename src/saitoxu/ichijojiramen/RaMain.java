package saitoxu.ichijojiramen;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;

public class RaMain extends Activity {
	private DatabaseHelper dbhelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.dbhelper = new DatabaseHelper(this);

		// データベースに入ってるラーメン屋を取得して，リストにセット
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = getAll(db);
		ArrayList<OriginalItem> items = convertCursorToItems(cursor);
		cursor.close();
		db.close();

		// データベース調べて，クリアしてる∧クーポン発券から1週間以内だったらレイアウト別のにする
		try {
			if (allClear() && isCouponTime()) {
				setContentView(R.layout.main_list_with_coupon);
				Button couponBtn = (Button) findViewById(R.id.coupon_button);
				couponBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(RaMain.this, Coupon.class);
						startActivity(intent);
					}
				});
			} else {
				setContentView(R.layout.main_list);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		ArrayAdapter<OriginalItem> adapter = new OriginalArrayAdapter(this,
				R.layout.row, items);
		ListView listView = (ListView) findViewById(R.id.listview);

		// アダプターを設定
		listView.setAdapter(adapter);

		// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				OriginalItem item = (OriginalItem) listView
						.getItemAtPosition(position);
				int ramen_id = item.getRamenId();

				Intent intent = new Intent(RaMain.this, RamenDetail.class);
				intent.putExtra("ramen_id", ramen_id);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.dbhelper = new DatabaseHelper(this);

		// データベースに入ってるラーメン屋を取得して，リストにセット
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = getAll(db);
		ArrayList<OriginalItem> items = convertCursorToItems(cursor);
		cursor.close();
		db.close();

		// データベース調べて，クリアしてる∧クーポン発券から1週間以内だったらレイアウト別のにする
		try {
			if (allClear() && isCouponTime()) {
				setContentView(R.layout.main_list_with_coupon);
				Button couponBtn = (Button) findViewById(R.id.coupon_button);
				couponBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(RaMain.this, Coupon.class);
						startActivity(intent);
					}
				});
			} else {
				setContentView(R.layout.main_list);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		ArrayAdapter<OriginalItem> adapter = new OriginalArrayAdapter(this,
				R.layout.row, items);
		ListView listView = (ListView) findViewById(R.id.listview);

		// アダプターを設定
		listView.setAdapter(adapter);

		// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				OriginalItem item = (OriginalItem) listView
						.getItemAtPosition(position);
				int ramen_id = item.getRamenId();

				Intent intent = new Intent(RaMain.this, RamenDetail.class);
				intent.putExtra("ramen_id", ramen_id);
				startActivity(intent);
			}
		});
	}

	// 全部のflagが1だったらtrue，それ以外はfalse
	private boolean allClear() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("ramen", new String[] { "flag" }, null, null,
				null, null, null);
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			if (cursor.getInt(0) == 0) {
				cursor.close();
				db.close();
				return false;
			}
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return true;
	}

	// クーポン期間中だったらtrue
	private boolean isCouponTime() throws ParseException {
		// 日付の記録は2013/02/25って感じ
		SharedPreferences prefer = getSharedPreferences("IchijojiRamen",
				MODE_PRIVATE);
		String time = prefer.getString("end", "");

		if (!time.equals("")) {
			// カレンダークラスでクーポン終了日に一日足す
			// それと現在時刻を比較して，過ぎてたらクーポン画面出さない
			Calendar cal = Calendar.getInstance();
			Date endDate = toDate(time);
			cal.setTime(endDate);
			cal.add(Calendar.DATE, +1);
			Date now = new Date();

			if (now.before(cal.getTime())) {
				return true;
			}
		}
		return false;
	}

	// 2013/02/26っていう文字列をDate型に変換
	public static Date toDate(String str) throws ParseException {
		Date date = DateFormat.getDateInstance().parse(str);
		return date;
	}

	// カーソルからOriginalItemのリストを作る
	private ArrayList<OriginalItem> convertCursorToItems(Cursor cursor) {
		ArrayList<OriginalItem> items = new ArrayList<OriginalItem>();

		cursor.moveToFirst();

		for (int i = 0; i < cursor.getCount(); i++) {
			Drawable drawable = getDrawableFromByteArray(cursor.getBlob(1));
			OriginalItem item = new OriginalItem(cursor.getInt(0), drawable,
					cursor.getString(2), cursor.getInt(3));
			items.add(item);
			cursor.moveToNext();
		}

		return items;
	}

	// byte配列からDrawableを取得
	public Drawable getDrawableFromByteArray(byte[] b) {
		return new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
	}

	// ラーメン屋のIDと店名，店の写真を持ったカーソルを取得
	private Cursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query("ramen", new String[] { "ramen_id",
				"photo_view", "name", "flag" }, null, null, null, null, null);

		return cursor;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.qr_code:
			Intent intent = new Intent(RaMain.this, QRCodeReader.class);
			startActivity(intent);
			return true;
		case R.id.menu_others:
			Intent intent2 = new Intent(RaMain.this, Others.class);
			startActivity(intent2);
			return true;
		}
		return false;
	}
}
