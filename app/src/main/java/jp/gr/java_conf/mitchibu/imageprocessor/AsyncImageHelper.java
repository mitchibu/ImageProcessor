package jp.gr.java_conf.mitchibu.imageprocessor;

import android.content.Context;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.ImageProcessor;
import jp.gr.java_conf.mitchibu.lib.imageprocessor.UriImageProcessor;
import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;
import jp.gr.java_conf.mitchibu.lib.imageprocessor.widget.RecyclingImageView;

public class AsyncImageHelper {
	private static ImageProcessor<String> processor = null;

	private ImageProcessor<String>.Task task = null;
	private String uri;

	public AsyncImageHelper(Context context) {
		if(processor == null) {
			int cacheSize = 1024 * 1024 * 8;
			processor = new UriImageProcessor(context, cacheSize);
		}
	}

	public void load(final String uri, final RecyclingImageView view) {
		if(uri == null) {
			view.setImageDrawable(null);
		} else {
			task = processor.loadBitmap(uri, new ImageProcessor.OnLoadDoneListener<String>() {
				@Override
				public void onLoadDone(String key, RecyclingBitmapDrawable bm) {
					if(key.equals(uri)) view.setImageDrawable(bm);
				}
			});
		}
	}

	public String getUri() {
		return uri;
	}

	public void cancel() {
		task.cancel(true);
	}
}
