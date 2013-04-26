package saitoxu.ichijojiramen;

import java.io.IOException;
import java.util.ArrayList;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.view.KeyEvent;

public class QRCodeReader extends Activity implements SurfaceHolder.Callback,
		Camera.PreviewCallback, Camera.AutoFocusCallback {
	private static final String TAG = "ZXingBase";

	private static final int MIN_PREVIEW_PIXCELS = 320 * 240;
	private static final int MAX_PREVIEW_PIXCELS = 800 * 480;
	private Camera myCamera;
	private SurfaceView surfaceView;

	private Boolean hasSurface;
	private Boolean initialized;

	private Point screenPoint;
	private Point previewPoint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		hasSurface = false;
		initialized = false;

		setContentView(R.layout.qr_code_reader);

	}

	@Override
	protected void onResume() {
		super.onResume();

		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder holder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(holder);
		} else {
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@Override
	protected void onPause() {
		closeCamera();
		if (!hasSurface) {
			SurfaceHolder holder = surfaceView.getHolder();
			holder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success)
			camera.setOneShotPreviewCallback(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {

		switch (keyCode) {

		case android.view.KeyEvent.KEYCODE_MENU:
		case android.view.KeyEvent.KEYCODE_CAMERA:

			if (myCamera != null) {

				Camera.Parameters parameters = myCamera.getParameters();
				if (!parameters.getFocusMode().equals(
						Camera.Parameters.FOCUS_MODE_FIXED)) {
					myCamera.autoFocus(this);
				}

			}

			return true;

		case android.view.KeyEvent.KEYCODE_BACK:
			finish();
			return true;
		}
		return false;
	}

	/** Camera.PreviewCallback */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		View finderView = (View) findViewById(R.id.viewfinder_view);

		int left = finderView.getLeft() * previewPoint.x / screenPoint.x;
		int top = finderView.getTop() * previewPoint.y / screenPoint.y;
		int width = finderView.getWidth() * previewPoint.x / screenPoint.x;
		int height = finderView.getHeight() * previewPoint.y / screenPoint.y;

		PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,
				previewPoint.x, previewPoint.y, left, top, width, height, false);

		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultiFormatReader reader = new MultiFormatReader();
		try {
			// 読取りが成功した場合
			Result result = reader.decode(bitmap);
			String str = result.getText();
			DatabaseHelper dbhelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			ArrayList<String> passwords = getPasswords(db);

			if (passwords.contains(str)) {
				int ramen_id = getRamenId(db, str);

				// すでに行ってるかどうか
				if (setFlag(db, ramen_id)) {
					db.close();
					Intent intent = new Intent(QRCodeReader.this,
							RamenDetail.class);
					intent.putExtra("ramen_id", ramen_id);
					intent.putExtra("fromQRCode", 1);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(this, "訪問済みです", Toast.LENGTH_SHORT).show();
					db.close();
				}
			} else {
				Toast.makeText(this, "ラーメン屋ではありません", Toast.LENGTH_SHORT).show();
				db.close();
			}
		} catch (Exception e) {
			Toast.makeText(this, "読み取れませんでした", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean setFlag(SQLiteDatabase db, int ramen_id) {
		ContentValues values = new ContentValues();

		Cursor cursor = db.query("ramen", new String[] { "flag" },
				"ramen_id like ?", new String[] { Integer.toString(ramen_id) },
				null, null, null);
		cursor.moveToFirst();
		int flag = cursor.getInt(0);
		if (flag == 1) {
			// もうすでに行ってたらfalseを返す
			return false;
		} else {
			// 行ってなかったらflag=1にしてtrueを返す
			values.put("flag", 1);
			db.update("ramen", values, "ramen_id = ?",
					new String[] { Integer.toString(ramen_id) });
			return true;
		}
	}

	private int getRamenId(SQLiteDatabase db, String str) {
		int ramen_id = 0;
		Cursor cursor = db.query("ramen", new String[] { "ramen_id" },
				"pass like ?", new String[] { str }, null, null, null);
		cursor.moveToFirst();
		ramen_id = cursor.getInt(0);
		cursor.close();
		return ramen_id;
	}

	private ArrayList<String> getPasswords(SQLiteDatabase db) {
		ArrayList<String> passes = new ArrayList<String>();
		Cursor cursor = db.query("ramen", new String[] { "pass" }, null, null,
				null, null, null);

		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			passes.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		return passes;
	}

	private void initCamera(SurfaceHolder holder) {
		try {
			openCamera(holder);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	private void openCamera(SurfaceHolder holder) throws IOException {
		if (myCamera == null) {
			myCamera = Camera.open();
			if (myCamera == null) {
				throw new IOException();
			}
		}
		myCamera.setPreviewDisplay(holder);

		if (!initialized) {
			initialized = true;
			initFromCameraParameters(myCamera);
		}

		setCameraParameters(myCamera);
		myCamera.startPreview();
	}

	private void closeCamera() {
		if (myCamera != null) {
			myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
	}

	private void setCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();

		parameters.setPreviewSize(previewPoint.x, previewPoint.y);
		camera.setParameters(parameters);
	}

	private void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) getApplication()
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		if (width < height) {
			int tmp = width;
			width = height;
			height = tmp;
		}

		screenPoint = new Point(width, height);

		Log.d(TAG, "screenPoint = " + screenPoint);
		previewPoint = findPreviewPoint(parameters, screenPoint, false);
		Log.d(TAG, "previewPoint = " + previewPoint);
	}

	private Point findPreviewPoint(Camera.Parameters parameters,
			Point screenPoint, boolean portrait) {
		Point previewPoint = null;
		int diff = Integer.MAX_VALUE;

		for (Camera.Size supportPreviewSize : parameters
				.getSupportedPreviewSizes()) {
			int pixels = supportPreviewSize.width * supportPreviewSize.height;
			if (pixels < MIN_PREVIEW_PIXCELS || pixels > MAX_PREVIEW_PIXCELS) {
				continue;
			}

			int supportedWidth = portrait ? supportPreviewSize.height
					: supportPreviewSize.width;
			int supportedHeight = portrait ? supportPreviewSize.width
					: supportPreviewSize.height;
			int newDiff = Math.abs(screenPoint.x * supportedHeight
					- supportedWidth * screenPoint.y);

			if (newDiff == 0) {
				previewPoint = new Point(supportedWidth, supportedHeight);
				break;
			}

			if (newDiff < diff) {
				previewPoint = new Point(supportedWidth, supportedHeight);
				diff = newDiff;
			}
		}
		if (previewPoint == null) {
			Camera.Size defaultPreviewSize = parameters.getPreviewSize();
			previewPoint = new Point(defaultPreviewSize.width,
					defaultPreviewSize.height);
		}

		return previewPoint;
	}
}
