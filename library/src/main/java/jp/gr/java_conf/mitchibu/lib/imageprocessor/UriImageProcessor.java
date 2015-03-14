package jp.gr.java_conf.mitchibu.lib.imageprocessor;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class UriImageProcessor extends ImageProcessor<String> {
	@SuppressWarnings("serial")
	private static HashMap<String, ImageDecoder> map = new HashMap<String, ImageDecoder>() {
		{
			put(ContentResolver.SCHEME_CONTENT, new ImageDecoder() {
				@Override
				public Bitmap decode(Context context, String uri) {
					InputStream in = null;
					try {
						in = context.getContentResolver().openInputStream(Uri.parse(uri));
						return BitmapFactory.decodeStream(in);
					} catch(Exception e) {
						e.printStackTrace();
						return null;
					} finally {
						if(in != null) try { in.close(); } catch(Exception e) {}
					}
				}
			});
			put(ContentResolver.SCHEME_FILE, new ImageDecoder() {
				@Override
				public Bitmap decode(Context context, String uri) {
					InputStream in = null;
					try {
						in = new URL(uri).openConnection().getInputStream();
						return BitmapFactory.decodeFile(Uri.parse(uri).getPath());
					} catch(Exception e) {
						e.printStackTrace();
						return null;
					} finally {
						if(in != null) try { in.close(); } catch(Exception e) {}
					}
				}
			});

			ImageDecoder decoder = new ImageDecoder() {
				@Override
				public Bitmap decode(Context context, String uri) {
					InputStream in = null;
					try {
						in = new URL(uri).openConnection().getInputStream();
						return BitmapFactory.decodeStream(in);
					} catch(Exception e) {
						e.printStackTrace();
						return null;
					} finally {
						if(in != null) try { in.close(); } catch(Exception e) {}
					}
				}
			};
			put("http", decoder);
			put("https", decoder);
		}
	};

	public UriImageProcessor(Context context, int maxSize) {
		super(context, maxSize);
	}

	public UriImageProcessor(Context context, BitmapCache<String> cache) {
		super(context, cache);
	}

	@Override
	protected RecyclingBitmapDrawable process(String key) {
		ImageDecoder decoder = map.get(Uri.parse(key).getScheme());
		if(decoder == null) return null;

		Bitmap bm = decoder.decode(getContext(), key);
		return bm == null ? null : new RecyclingBitmapDrawable(getContext().getResources(), bm);
	}

	private interface ImageDecoder {
		Bitmap decode(Context context, String uri);
	}
}
