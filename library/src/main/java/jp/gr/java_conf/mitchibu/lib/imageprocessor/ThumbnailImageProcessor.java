package jp.gr.java_conf.mitchibu.lib.imageprocessor;

import java.util.HashMap;
import java.util.Locale;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

public class ThumbnailImageProcessor extends ImageProcessor<String> {
	private static String ext(String name) {
		if(name == null) return null;
		int index = name.lastIndexOf('.');
		if(index < 0 || index == name.length() - 1) return null;

		String ext = name.substring(index + 1).toLowerCase(Locale.getDefault());
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
	}

	@SuppressWarnings("serial")
	private static HashMap<String, ImageDecoder> map = new HashMap<String, ImageDecoder>() {
		{
			put("image", new ImageDecoder() {
				@Override
				public Bitmap decode(Context context, String uri, int width, int height) {
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(uri, opts);

					opts.inSampleSize = Math.min(opts.outWidth / width, opts.outHeight / height) + 1;
					opts.inJustDecodeBounds = false;

					Bitmap bm = BitmapFactory.decodeFile(uri, opts);
					if(bm == null) return null;
					Bitmap dest = ThumbnailUtils.extractThumbnail(bm, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
					if(!bm.isRecycled()) bm.recycle();
					return dest;
				}
			});
			put("video", new ImageDecoder() {
				@Override
				public Bitmap decode(Context context, String uri, int width, int height) {
					Bitmap bm = ThumbnailUtils.createVideoThumbnail(uri, MediaStore.Images.Thumbnails.MINI_KIND);
					if(bm == null) return null;
					Bitmap dest = ThumbnailUtils.extractThumbnail(bm, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
					if(!bm.isRecycled()) bm.recycle();
					return dest;
				}
			});
		}
	};

	private final int width;
	private final int height;

	public ThumbnailImageProcessor(Context context, int maxSize, int width, int height) {
		super(context, maxSize);
		this.width = width;
		this.height = height;
	}

	public ThumbnailImageProcessor(Context context, BitmapCache<String> cache, int width, int height) {
		super(context, cache);
		this.width = width;
		this.height = height;
	}

	@Override
	protected RecyclingBitmapDrawable process(String key) {
		String mime = ext(Uri.parse(key).getLastPathSegment());
		if(mime == null) return null;

		ImageDecoder decoder = map.get(mime.split("/")[0]);
		if(decoder == null) return null;

		Bitmap bm = decoder.decode(getContext(), key, width, height);
		return bm == null ? null : new RecyclingBitmapDrawable(getContext().getResources(), bm);
	}

	private interface ImageDecoder {
		Bitmap decode(Context context, String uri, int width, int height);
	}
}
