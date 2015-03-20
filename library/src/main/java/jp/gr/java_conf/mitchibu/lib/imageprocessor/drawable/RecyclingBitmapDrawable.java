package jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
 
public class RecyclingBitmapDrawable extends BitmapDrawable {
	private int cacheRefCount = 0;
	private int displayRefCount = 0;
	private boolean hasBeenDisplayed = false;
	private InvalidBitmapCallback invalidBitmapCallback = null;

	public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	public void setIsDisplayed(boolean isDisplayed) {
		synchronized(this) {
			if(isDisplayed) {
				++ displayRefCount;
				hasBeenDisplayed = true;
			} else {
				-- displayRefCount;
			}
		}
		android.util.Log.v("test", String.format("setIsDisplayed:%08x %d", hashCode(), displayRefCount));
		checkState();
	}

	public void setIsCached(boolean isCached) {
		synchronized(this) {
			if(isCached) {
				++ cacheRefCount;
			} else {
				-- cacheRefCount;
			}
		}
		checkState();
	}

	public void setInvalidBitmapCallback(InvalidBitmapCallback callback) {
		invalidBitmapCallback = callback;
	}

	@Override
	public void draw(Canvas canvas) {
		if(hasValidBitmap()) {
			super.draw(canvas);
		} else {
			if(invalidBitmapCallback != null) invalidBitmapCallback.invalidBitmap();
		}
	}

	private synchronized void checkState() {
		if(cacheRefCount <= 0 && displayRefCount <= 0 && hasBeenDisplayed && hasValidBitmap()) {
			android.util.Log.v("test", "recycle");
			getBitmap().recycle();
		}
	}

	private synchronized boolean hasValidBitmap() {
		Bitmap bitmap = getBitmap();
		return bitmap != null && !bitmap.isRecycled();
	}

	public interface InvalidBitmapCallback {
		void invalidBitmap();
	}
}
