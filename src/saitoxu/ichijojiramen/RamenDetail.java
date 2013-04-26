package saitoxu.ichijojiramen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class RamenDetail extends Activity {
	private DatabaseHelper dbhelper;
	private String ramen_name;
	private String phone_number;
	private String url;
	private String ido = "0.0";
	private String keido = "0.0";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ramen_detail);
		dbhelper = new DatabaseHelper(this);
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		Intent i = getIntent();
		int ramen_id = i.getIntExtra("ramen_id", 0);
		int fromQRCode = i.getIntExtra("fromQRCode", 0);
		if (fromQRCode == 1) {
			// QRコードリーダーから遷移してきたらなんかスタンプ押すみたいなアクション入れたかった

			// ラーメン屋コンプしたら
			if (allClear()) {
				// プリファレンスにクーポン開始日と終了日を記録
				SharedPreferences prefer = getSharedPreferences(
						"IchijojiRamen", MODE_PRIVATE);
				SharedPreferences.Editor editor = prefer.edit();

				Date now = new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				Calendar deadLine = Calendar.getInstance();
				deadLine.set(2013, 6, 31); // キャンペーン終了
				cal.add(Calendar.DATE, 7);
				Date end_coupon;

				if (cal.after(deadLine)) { // クーポンの終了日が2013/07/31越してたら2013/07/31に修正
					end_coupon = deadLine.getTime();
				} else {
					end_coupon = cal.getTime();
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");

				String nowStr = sdf.format(now);
				String endStr = sdf.format(end_coupon);

				editor.putString("start", nowStr);
				editor.putString("end", endStr);
				editor.commit();

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				// アラートダイアログのタイトルを設定します
				alertDialogBuilder.setTitle("おめでとうございます！");
				// アラートダイアログのメッセージを設定します
				alertDialogBuilder.setMessage("クーポンが発行されました！");
				// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
				alertDialogBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				// アラートダイアログのキャンセルが可能かどうかを設定します
				alertDialogBuilder.setCancelable(true);
				AlertDialog alertDialog = alertDialogBuilder.create();
				// アラートダイアログを表示します
				alertDialog.show();
			}
		}

		// ラーメン屋の情報をGET
		Cursor cursor = getRamenInfo(db, ramen_id);
		cursor.moveToFirst();

		ido = cursor.getString(9);
		keido = cursor.getString(10);

		TextView tel = (TextView) findViewById(R.id.tel);
		TextView name = (TextView) findViewById(R.id.name);
		TextView hp = (TextView) findViewById(R.id.hp);
		TextView dayoff = (TextView) findViewById(R.id.dayoff);
		TextView time = (TextView) findViewById(R.id.time);
		TextView address = (TextView) findViewById(R.id.address);
		ImageView photo_view = (ImageView) findViewById(R.id.ramen_view);
		ImageView sumi = (ImageView) findViewById(R.id.sumi);

		phone_number = cursor.getString(5);
		tel.setText(phone_number);
		ramen_name = cursor.getString(0);
		name.setText(ramen_name);
		url = cursor.getString(6);
		hp.setText(url);
		dayoff.setText(cursor.getString(4));
		time.setText(cursor.getString(3));
		address.setText(cursor.getString(7));
		photo_view
				.setImageDrawable(getDrawableFromByteArray(cursor.getBlob(1)));

		if (cursor.getInt(8) == 1) {
			sumi.setImageResource(R.drawable.sumi);
		} else {
			sumi.setImageDrawable(null);
		}

		ImageView btnTweet = (ImageView) findViewById(R.id.twitter_button);
		btnTweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://twitter.com/intent/tweet?text="
									+ ramen_name
									+ "なう - 一乗寺のラーメン食べたい http://bit.ly/XlQiHQ"));
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ImageView btnPhone = (ImageView) findViewById(R.id.phone_button);

		btnPhone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
							+ phone_number));
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ImageView btnBrowser = (ImageView) findViewById(R.id.hp_button);
		if (url.equals("")) {
			btnBrowser.setImageResource(R.drawable.pressed_hp_button);
		}
		btnBrowser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		ImageView btnMap = (ImageView) findViewById(R.id.map_button);
		btnMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"
							+ ido + "," + keido));
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		cursor.close();
		db.close();
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
				return false;
			}
			cursor.moveToNext();
		}
		cursor.close();
		return true;
	}

	// ByteArrayをDrawableに変換
	public Drawable getDrawableFromByteArray(byte[] b) {
		return new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
	}

	private Cursor getRamenInfo(SQLiteDatabase db, int id) {
		Cursor cursor = null;
		cursor = db.query("ramen", new String[] { "name", "photo_view",
				"photo_ramen", "time", "dayoff", "tel", "hp", "address",
				"flag", "ido", "keido" }, "ramen_id like ?",
				new String[] { Integer.toString(id) }, null, null, null);

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
			Intent intent = new Intent(RamenDetail.this, QRCodeReader.class);
			startActivity(intent);
			return true;
		case R.id.menu_others:
			Intent intent2 = new Intent(RamenDetail.this, Others.class);
			startActivity(intent2);
			return true;
		}
		return false;
	}
}
