package saitoxu.ichijojiramen;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

// スプラッシュ兼DB更新用アクティビティ
public class SplashActivity extends Activity {
	private DatabaseHelper dbhelper;
	private MyTask task;
	private String url = "http://www8350ui.sakura.ne.jp/ramen.json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		this.dbhelper = new DatabaseHelper(this);
		this.task = new MyTask();
		task.execute(this.url);
	}

	// すでにアプリ内データベースに保存されているラーメン屋のIDを取得
	private ArrayList<Integer> getRamenId(SQLiteDatabase db) {
		ArrayList<Integer> ramen_id = new ArrayList<Integer>();
		Cursor cursor = null;

		cursor = db.query("ramen", new String[] { "ramen_id" }, null, null,
				null, null, null);
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			ramen_id.add(Integer.parseInt(cursor.getString(0)));
			cursor.moveToNext();
		}
		cursor.close();

		return ramen_id;
	}

	// ラーメン屋データベースの更新
	private void updateDatabase(String res) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		// アプリ内データベースに存在するramen_idをArrayList<int>で取得
		ArrayList<Integer> already = getRamenId(db);
		ContentValues cv = new ContentValues();

		try {
			JSONObject json = new JSONObject(res);
			JSONArray jsonArray = json.getJSONArray("ichijoji");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				int num = obj.getInt("id");
				// アプリ内にそのラーメン屋がなかったら追加
				if (!already.contains(num) && obj != null) {
					String name = obj.getString("name");
					String pass = obj.getString("password");
					String tel = obj.getString("TEL");
					String dayoff = obj.getString("dayoff");
					String address = obj.getString("address");
					String hp = obj.getString("HP");
					String time = obj.getString("time");
					String coupon = obj.getString("coupon");
					String ido = obj.getString("lat");
					String keido = obj.getString("lon");
					byte[] photo_ramen = httpGetImage(obj
							.getString("photo_ramen"));
					byte[] photo_view = httpGetImage(obj
							.getString("photo_view"));

					cv.put("ramen_id", num);
					cv.put("name", name);
					cv.put("pass", pass);
					cv.put("tel", tel);
					cv.put("dayoff", dayoff);
					cv.put("address", address);
					cv.put("hp", hp);
					cv.put("time", time);
					cv.put("coupon", coupon);
					cv.put("photo_ramen", photo_ramen);
					cv.put("photo_view", photo_view);
					cv.put("ido", ido);
					cv.put("keido", keido);

					db.insert("ramen", null, cv);
					cv.clear();
					// アプリ内にそのラーメン屋があったら画像以外を更新
					// 画像は重いから更新しない
				} else {
					String name = obj.getString("name");
					String pass = obj.getString("password");
					String tel = obj.getString("TEL");
					String dayoff = obj.getString("dayoff");
					String address = obj.getString("address");
					String hp = obj.getString("HP");
					String time = obj.getString("time");
					String coupon = obj.getString("coupon");
					String ido = obj.getString("lat");
					String keido = obj.getString("lon");

					cv.put("name", name);
					cv.put("pass", pass);
					cv.put("tel", tel);
					cv.put("dayoff", dayoff);
					cv.put("address", address);
					cv.put("hp", hp);
					cv.put("time", time);
					cv.put("coupon", coupon);
					cv.put("ido", ido);
					cv.put("keido", keido);

					String[] value = { String.valueOf(num) };
					db.update("ramen", cv, "ramen_id = ?", value);
					cv.clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	// 画像のURLからbyte配列を取得
	public byte[] httpGetImage(String location) {

		byte[] ret = null;
		byte[] buf = null;

		InputStream is = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			// 指定したURLの作成
			url = new URL(location);

			conn = (HttpURLConnection) url.openConnection();
			// HTTP通信のメソッド指定
			conn.setRequestMethod("GET");
			conn.connect();

			// HTTP通信でデータを取得
			is = conn.getInputStream();

			int length = (int) conn.getContentLength();
			if (length < 0) {
				length = 204800;
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream(length);
			buf = new byte[length];

			int n;
			while ((n = is.read(buf)) != -1) {
				out.write(buf, 0, n);
			}
			ret = out.toByteArray();
			out.close();
			out = null;
		} catch (Exception e) {
		}

		try {
			is.close();
			is = null;
		} catch (Exception e) {
		}

		try {
			conn.disconnect();
			conn = null;
		} catch (Exception e) {
		}

		return ret;
	}

	public class MyTask extends AsyncTask<String, Integer, String> {
		// コンストラクタ
		public MyTask() {
			super();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			// jsonを取得
			String _str = getData(params[0]);
			updateDatabase(_str);
			return _str;
		}

		@Override
		protected void onPostExecute(String result) {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startActivity(new Intent(SplashActivity.this, RaMain.class));
					finish();
				}
			}, 500); // 0.5秒後に画面遷移する
		}

		public String getData(String _url) {
			DefaultHttpClient objHttp = new DefaultHttpClient();
			HttpParams params = objHttp.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 3000);
			String _return = "";

			try {
				HttpGet objGet = new HttpGet(_url);
				HttpResponse objResponse = objHttp.execute(objGet);
				if (objResponse.getStatusLine().getStatusCode() < 400) {
					InputStream objStream = objResponse.getEntity()
							.getContent();
					InputStreamReader objReader = new InputStreamReader(
							objStream);
					BufferedReader objBuf = new BufferedReader(objReader);
					StringBuilder objJson = new StringBuilder();
					String sLine;
					while ((sLine = objBuf.readLine()) != null) {
						objJson.append(sLine);
					}
					_return = objJson.toString();
					objStream.close();
				}
			} catch (IOException e) {
				return e.toString();
			}
			return _return;
		}
	}
}
