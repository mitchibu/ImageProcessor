package jp.gr.java_conf.mitchibu.lib.imageprocessor;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.util.LruCache;

public class BitmapCache<E> {
	private final LruCache<E, RecyclingBitmapDrawable> cache;

	public BitmapCache(int maxSize) {
		cache = new LruCache<E, RecyclingBitmapDrawable>(maxSize) {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			protected int sizeOf(E key, RecyclingBitmapDrawable value) {
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
					return value.getBitmap().getByteCount() ;
				} else {
					return value.getBitmap().getRowBytes() * value.getBitmap().getHeight();
				}
			}

			@Override
			protected void entryRemoved(boolean evicted, E key, RecyclingBitmapDrawable oldValue, RecyclingBitmapDrawable newValue) {
				((RecyclingBitmapDrawable)oldValue).setIsCached(false);
			}
		};
	}

	public RecyclingBitmapDrawable get(E key) {
		return cache.get(key);
	}

	public void put(E key, RecyclingBitmapDrawable bm) {
		bm.setIsCached(true);
		RecyclingBitmapDrawable old = cache.put(key, bm);
		if(old != null) old.setIsCached(false);
	}
}
