package saitoxu.ichijojiramen;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OriginalArrayAdapter extends ArrayAdapter<OriginalItem> {

	private LayoutInflater inflater;
	private int textViewResourceId;
	private List<OriginalItem> items;

	public OriginalArrayAdapter(Context context, int textViewResourceId,
			List<OriginalItem> items) {
		super(context, textViewResourceId, items);

		// リソースIDと表示アイテムを保持っておく
		this.textViewResourceId = textViewResourceId;
		this.items = items;

		// ContextからLayoutInflaterを取得
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		// convertViewなんか入ってたら、それを使う
		// if (convertView != null) {
		// view = convertView;
		// }
		// convertViewがnullなら新規作成
		// else {
		// view = inflater.inflate(textViewResourceId, null);
		// }
		view = inflater.inflate(textViewResourceId, null);
		// 対象のアイテムを取得
		OriginalItem item = items.get(position);

		// アイコンを設定
		ImageView imageView = (ImageView) view.findViewById(R.id.ramen_view);
		imageView.setImageDrawable(item.getPhotoView());

		ImageView sumiView = (ImageView) view.findViewById(R.id.sumi);

		if (item.getFlag() == 1) { // 訪問済みだったら「済」マークを重ねる
			sumiView.setImageResource(R.drawable.sumi);
		}

		// テキストを設定
		TextView textView = (TextView) view.findViewById(R.id.name);
		textView.setText(item.getName());

		return view;
	}
}
