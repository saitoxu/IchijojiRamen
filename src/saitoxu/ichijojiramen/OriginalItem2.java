package saitoxu.ichijojiramen;

import android.graphics.drawable.Drawable;

// クーポンのリスト用クラス
public class OriginalItem2 {
	private int ramen_id;
	private Drawable photo_view;
	private String name;
	private String coupon;

	public OriginalItem2(int id, Drawable photo, String namae, String c) {
		this.ramen_id = id;
		this.photo_view = photo;
		this.name = namae;
		this.coupon = c;
	}

	public int getRamenId() {
		return ramen_id;
	}

	public void setRamenId(int id) {
		this.ramen_id = id;
	}

	public Drawable getPhotoView() {
		return photo_view;
	}

	public void setPhotoView(Drawable photo) {
		this.photo_view = photo;
	}

	public String getName() {
		return name;
	}

	public void setName(String text) {
		this.name = text;
	}

	public String getCoupon() {
		return coupon;
	}

	public void setFlag(String c) {
		this.coupon = c;
	}
}
