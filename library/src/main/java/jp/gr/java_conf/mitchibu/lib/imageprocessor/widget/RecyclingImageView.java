package jp.gr.java_conf.mitchibu.lib.imageprocessor.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;

public class RecyclingImageView extends ImageView {
	public RecyclingImageView(Context context) {
		super(context);
	}

	public RecyclingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public void setImageDrawable(Drawable src) {
		clean();
		if(src != null && src instanceof RecyclingBitmapDrawable) ((RecyclingBitmapDrawable)src).setIsDisplayed(true);
		super.setImageDrawable(src);
	}

	@Override
	public void setImageBitmap(Bitmap src) {
		setImageDrawable(null);
		super.setImageBitmap(src);
	}

	@Override
	public void setImageURI(Uri uri) {
		setImageDrawable(null);
		super.setImageURI(uri);
	}

	@Override
	protected void onDetachedFromWindow() {
		clean();
		super.onDetachedFromWindow();
	}

	private void clean() {
		Drawable old = getDrawable();
		if(old != null && old instanceof RecyclingBitmapDrawable) ((RecyclingBitmapDrawable)old).setIsDisplayed(false);
	}
}
