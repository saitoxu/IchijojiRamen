package saitoxu.ichijojiramen;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// これもクーポン用
// 時間あれば一個のクラスにまとめたい
public class OriginalArrayAdapter2 extends ArrayAdapter<OriginalItem2> {

	private LayoutInflater inflater;
	private int textViewResourceId;
	private List<OriginalItem2> items;

	public OriginalArrayAdapter2(Context context, int textViewResourceId,
			List<OriginalItem2> items) {
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

		view = inflater.inflate(textViewResourceId, null);
		// 対象のアイテムを取得
		OriginalItem2 item = items.get(position);

		// アイコンを設定
		ImageView imageView = (ImageView) view.findViewById(R.id.ramen_view);
		imageView.setImageDrawable(item.getPhotoView());

		// ラーメン屋の名前を設定
		TextView textView = (TextView) view.findViewById(R.id.name);
		textView.setText(item.getName());

		// クーポンの内容を設定
		TextView textView2 = (TextView) view.findViewById(R.id.coupon_content);
		textView2.setText(item.getCoupon());

		return view;
	}
}
