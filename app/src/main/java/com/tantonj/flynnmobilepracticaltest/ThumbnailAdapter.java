package com.tantonj.flynnmobilepracticaltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by tantonj on 9/10/2017.
 */

public class ThumbnailAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();

    public ThumbnailAdapter(Context c) {
        mContext = c;
    } //Constructs adapter with necessary context

    public void add(String url) { //method called from AlbumActivity, used to add the thumbnail of an album to the gridView
        new getBitmapFromUrl().execute(url);
    }

    public int getCount() {
        return thumbnails.size();
    }

    public Object getItem(int position) {
        return thumbnails.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) { //sets imageview stretching to fit while maintaining aspect ratio
        ImageView imageView = new ImageView(mContext);
        Bitmap bitmap = null;
        imageView.setImageBitmap(thumbnails.get(position));
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    private class getBitmapFromUrl extends AsyncTask<String, Void, Bitmap> { //Async Task for decoding urls into Bitmaps
        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap bitmap = null;
            if(!url.equals("")) {
                try {
                    URL bURL = new URL(url[0]);
                    bitmap = BitmapFactory.decodeStream(bURL.openConnection().getInputStream());
                } catch (IOException e) {

                }

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result==null) { //if the thumbnailurl didn't exist (freshly added album), use default album cover
                result = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.empty);
            }
            thumbnails.add(result); //otherwise add Bitmap result
            notifyDataSetChanged();
        }
    }

}
