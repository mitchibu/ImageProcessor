package jp.gr.java_conf.mitchibu.lib.imageprocessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.drawable.RecyclingBitmapDrawable;

import android.content.Context;
import android.os.Handler;

public abstract class ImageProcessor<E> {
	private static final ExecutorService defaultExecutorService = Executors.newFixedThreadPool(5);

	private final Context context;
	private final BitmapCache<E> cache;
	private final Map<E, RootTask> taskMap = new HashMap<E, RootTask>();
	private final Handler handler = new Handler();
	private final ExecutorService executorService;

	/**
	 * コンストラクタ
	 *
	 * @param maxSize キャッシュサイズ
	 */
	public ImageProcessor(Context context, int maxSize) {
		this(context, maxSize, defaultExecutorService);
	}

	/**
	 * コンストラクタ
	 *
	 * @param maxSize キャッシュサイズ
	 * @param executorService スレッドプール
	 */
	public ImageProcessor(Context context, int maxSize, ExecutorService executorService) {
		this(context, maxSize == 0 ? null : new BitmapCache<E>(maxSize), executorService);
	}

	/**
	 * コンストラクタ
	 *
	 * @param cache キャッシュ
	 */
	public ImageProcessor(Context context, BitmapCache<E> cache) {
		this(context, cache, defaultExecutorService);
	}

	/**
	 * コンストラクタ
	 *
	 * @param cache キャッシュ
	 * @param executorService スレッドプール
	 */
	public ImageProcessor(Context context, BitmapCache<E> cache, ExecutorService executorService) {
		this.context = context;
		this.cache = cache;
		this.executorService = executorService;
	}

	/**
	 * コンテキストを返す
	 *
	 * @return　コンストラクタで渡されたコンテキストを返す。
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * キャッシュされているかどうかを返す。
	 *
	 * @param key 画像のキー
	 * @return　画像がキャッシュされている場合はtrueを返す。それ以外はfalseを返す。
	 */
	public boolean hasCache(E key) {
		return cache != null && cache.get(key) != null;
	}

	/**
	 * 画像をロードする。
	 *
	 * @param key 画像のキー
	 * @param listener ロード完了時のリスナ
	 * @return　画像がキャッシュされている場合はnullを返す。それ以外はTaskオブジェクトを返す。
	 */
	public Task loadBitmap(E key, OnLoadDoneListener<E> listener) {
		RecyclingBitmapDrawable bm = hasCache(key) ? cache.get(key) : null;
		if(bm == null) {
			RootTask rootTask = taskMap.get(key);
			if(rootTask == null) {
				rootTask = new RootTask(key, new TaskExecutor(key));
				taskMap.put(key, rootTask);
				executorService.submit(rootTask);
			}
			rootTask.addListener(listener);
			return new Task(rootTask, listener);
		} else {
			listener.onLoadDone(key, bm);
			return null;
		}
	}

	/**
	 * 全てのロード処理を中断する。
	 *
	 */
	public void cancelAll() {
		Set<Map.Entry<E, RootTask>> entrySet = taskMap.entrySet();
		for(Map.Entry<E, RootTask> entry : entrySet) {
			entry.getValue().cancel(true);
		}
	}

	/**
	 * 画像をキャッシュする。
	 *
	 * @param key 画像のキー
	 * @param bm 画像
	 */
	public void cache(E key, RecyclingBitmapDrawable bm) {
		cache.put(key, bm);
	}
	/**
	 * 実際に画像の読み込みを行う。
	 *
	 * @param key 画像のキー
	 * @return 画像の読み込みが正常に完了した場合はBitmapインスタンスを返す。異常の場合はnullを返す。
	 */
	protected abstract RecyclingBitmapDrawable process(E key);

	public class Task {
		private final RootTask task;
		private final OnLoadDoneListener<E> listener;

		public Task(RootTask task, OnLoadDoneListener<E> listener) {
			this.task = task;
			this.listener = listener;
		}

		public void cancel(boolean mayInterruptIfRunning) {
			task.listenerSet.remove(listener);
			task.cancel(mayInterruptIfRunning);
		}
	}

	private class RootTask extends FutureTask<RecyclingBitmapDrawable> {
		private final Set<OnLoadDoneListener<E>> listenerSet = new HashSet<OnLoadDoneListener<E>>();
		private final E key;

		public RootTask(E key, Callable<RecyclingBitmapDrawable> callable) {
			super(callable);
			this.key = key;
		}

		public void addListener(OnLoadDoneListener<E> listener) {
			listenerSet.add(listener);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if(!listenerSet.isEmpty()) return true; 
			taskMap.remove(key);
			return super.cancel(mayInterruptIfRunning);
		}

		@Override
		public void done() {
			if(isCancelled()) return;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(!taskMap.containsKey(key)) return;
					taskMap.remove(key);
					try {
						RecyclingBitmapDrawable bm = get();
						for(OnLoadDoneListener<E> listener : listenerSet) {
							listener.onLoadDone(key, bm);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private class TaskExecutor implements Callable<RecyclingBitmapDrawable> {
		private final E key;

		public TaskExecutor(E key) {
			this.key = key;
		}

		@Override
		public RecyclingBitmapDrawable call() throws Exception {
			RecyclingBitmapDrawable bm = process(key);
			if(bm != null && cache != null) {
				cache(key, bm);
			}
			return bm;
		}
	}

	public interface OnLoadDoneListener<E> {
		/**
		 * 画像の読み込み完了を通知する。
		 *
		 * @param key 画像のキー
		 * @param bm 読み込んだBitmap
		 */
		void onLoadDone(E key, RecyclingBitmapDrawable bm);
	}
}
