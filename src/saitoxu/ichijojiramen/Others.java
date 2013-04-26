package saitoxu.ichijojiramen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Others extends ListActivity {
	private DatabaseHelper dbhelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.dbhelper = new DatabaseHelper(this);
		setContentView(R.layout.others);

		String[] menuItems = { "ヘルプ", "Facebookページ", "リセット" };
		String[] menuSubItems = { "スタンプラリーの説明です", "アプリのFacebookページに飛びます",
				"記録を消去します" };
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < menuItems.length; i++) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("title", menuItems[i]);
			data.put("comment", menuSubItems[i]);
			dataList.add(data);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, dataList,
				R.layout.my_list_item_2, new String[] { "title", "comment" },
				new int[] { android.R.id.text1, android.R.id.text2 });
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (position == 0) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			// アラートダイアログのタイトルを設定します
			alertDialogBuilder.setTitle("スタンプラリーの説明");
			// アラートダイアログのメッセージを設定します
			alertDialogBuilder.setMessage("京都が誇るラーメン街、一乗寺を回るスタンプラリーです！" + "\n"
					+ "2013年4月8日(月)〜7月31日(水)の間にスタンプラリー対象店舗10店のラーメンを食べ、"
					+ "各店に貼ってあるQRコードをアプリのバーコードリーダーで撮ると、スタンプをGETできます。" + "\n"
					+ "全店のスタンプを集めると、1週間有効なクーポンが発行されます！" + "\n"
					+ "この機会に一乗寺のラーメンを食べに行こう！");
			// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			// アラートダイアログのキャンセルが可能かどうかを設定します
			alertDialogBuilder.setCancelable(true);
			AlertDialog alertDialog = alertDialogBuilder.create();
			// アラートダイアログを表示します
			alertDialog.show();
		} else if (position == 1) {
			Uri uri = Uri.parse("https://www.facebook.com/IchijojiRamen");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} else if (position == 2) {
			// リセットボタンはデータベースのflag消すのと，
			// プリファレンスの開始日と終了日消去
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			// アラートダイアログのタイトルを設定します
			alertDialogBuilder.setTitle("リセットしますか？");
			// アラートダイアログのメッセージを設定します
			alertDialogBuilder.setMessage("今までの記録がなくなってしまいますが、よろしいですか？");
			// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
			alertDialogBuilder.setPositiveButton("はい",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							resetData();
						}

						// データベースのflagを0にして，プリファレンスを消去
						private void resetData() {
							SharedPreferences prefer = getSharedPreferences(
									"IchijojiRamen", MODE_PRIVATE);
							SharedPreferences.Editor editor = prefer.edit();
							editor.clear();
							editor.commit();
							SQLiteDatabase db = dbhelper.getWritableDatabase();
							deleteFlag(db);
							db.close();
						}

						private void deleteFlag(SQLiteDatabase db) {
							ContentValues values = new ContentValues();
							values.put("flag", 0);
							db.update("ramen", values, null, null);
						}
					});
			alertDialogBuilder.setNegativeButton("いいえ",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			// アラートダイアログのキャンセルが可能かどうかを設定します
			alertDialogBuilder.setCancelable(true);
			AlertDialog alertDialog = alertDialogBuilder.create();
			// アラートダイアログを表示します
			alertDialog.show();
		}
	}
}
