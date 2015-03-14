package jp.gr.java_conf.mitchibu.imageprocessor;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import jp.gr.java_conf.mitchibu.lib.imageprocessor.widget.RecyclingImageView;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private CursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		adapter = new CursorAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return getLayoutInflater().inflate(R.layout.item, parent, false);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
				Uri uri = ContentUris.withAppendedId(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, id);
				AsyncImageHelper helper = (AsyncImageHelper)view.getTag();
				if(helper == null) helper = new AsyncImageHelper(context);
				else helper.cancel();
				helper.load(uri.toString(), (RecyclingImageView)view);
			}
		};
		GridView list = (GridView)findViewById(android.R.id.list);
		list.setAdapter(adapter);

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getSupportLoaderManager().destroyLoader(0);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
