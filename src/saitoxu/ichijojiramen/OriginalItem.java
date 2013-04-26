package saitoxu.ichijojiramen;

import android.graphics.drawable.Drawable;

public class OriginalItem {
	private int ramen_id;
	private Drawable photo_view;
	private String name;
	private int flag;

	public OriginalItem(int id, Drawable photo, String namae, int f) {
		this.ramen_id = id;
		this.photo_view = photo;
		this.name = namae;
		this.flag = f;
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

	public int getFlag() {
		return flag;
	}

	public void setFlag(int f) {
		this.flag = f;
	}
}
