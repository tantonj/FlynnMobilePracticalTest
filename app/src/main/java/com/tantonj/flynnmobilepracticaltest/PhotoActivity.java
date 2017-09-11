package com.tantonj.flynnmobilepracticaltest;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by tantonj on 9/10/2017.
 */

public class PhotoActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    JSONArray data; //data recieved from AlbumActivity
    int currentImage = 0; //imageFocused
    int imageLoaded = 1; //number of Images loaded so far. Images load pretty quick from url source. I could have been deloading images as they are no longer a distance of 1 from
                        //currently focused image to save memory. The albums are pretty small, Instead I keep any image in memory once it loads, images only load if they are right
                        //of currently focused image
    String albumTitle = "";
    int albumId = 0;
    private GestureDetectorCompat mDetector; //gestureDetector for flick detection
    AddDialogFragment addPhotoDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_slide_layout);
        Bundle bundle = getIntent().getExtras();
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);
        albumTitle = bundle.getString("title");
        albumId = bundle.getInt("id");
        ((TextView) findViewById(R.id.albumTitle)).setText("From Album: " + albumTitle);
        try {
            data = new JSONArray(bundle.getString("data"));
            new getBitmapFromUrl().execute(data.getJSONObject(currentImage).getString("url")); //executes getBitmapFromUrl AsyncTask to load first image
            ((TextView) findViewById(R.id.imageTitle)).setText(data.getJSONObject(currentImage).getString("title"));
            new getBitmapFromUrl().execute(data.getJSONObject(currentImage+1).getString("url")); //executes getBitmapFromUrl AsyncTask to load second image
        }catch(JSONException e) {

        }

        addPhotoDialog = AddDialogFragment.newInstance(R.string.add_photo, AddDialogFragment.ADD_PHOTO); //add custom DialogFragment
        ImageButton addBut = (ImageButton)findViewById(R.id.addPhotoBut); //find addButton and set it's onclickable to show DialogFragment
        addBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                addPhotoDialog.show(fm, "dialog");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) { //a series of overridden methods necessary for gesture detector
        this.mDetector.onTouchEvent(motionEvent);
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent me0, MotionEvent me1, float vx, float vy) { //detects fling, animates transition between children in ViewFlipper
        ViewFlipper flipper = ((ViewFlipper)findViewById(R.id.imageFlipper));
        if(me1.getX() - me0.getX() > 200) //swipe left
        {
            if(currentImage > 0) { //if the current image is not the first one and you flick right go back
                currentImage--;
                flipper.setOutAnimation(this, R.anim.slide_out_right);
                flipper.setInAnimation(this, R.anim.slide_in_right);
                flipper.showPrevious();
            }
            else
                Toast.makeText(getBaseContext(), "Beginning of Album", Toast.LENGTH_SHORT).show(); //otherwise tell user
        }if(me0.getX() - me1.getX() > 200) //swipe right
        {
            if(currentImage < data.length()) { //if the current image isn't the last image and they flick left go forward
                currentImage++;
                flipper.setOutAnimation(this, R.anim.slide_out_left);
                flipper.setInAnimation(this, R.anim.slide_in_left);
                flipper.showNext();
                if(currentImage >= imageLoaded && currentImage < data.length()-1) { //if the image to the next of you after fling hasn't been loaded yet, load it.
                    imageLoaded++;
                    try {
                        new getBitmapFromUrl().execute(data.getJSONObject(imageLoaded).getString("url"));
                    } catch (JSONException e) {

                    }
                }
            }
            else
                Toast.makeText(getBaseContext(), "End of Album", Toast.LENGTH_SHORT).show();
        }
        try {
            ((TextView) findViewById(R.id.imageTitle)).setText(data.getJSONObject(currentImage).getString("title"));
        }catch(JSONException ex) {

        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(addPhotoDialog.getDialog() != null && addPhotoDialog.getDialog().isShowing())
            addPhotoDialog.dismissAllowingStateLoss();
    }

    public void addPhoto(String url, String title) { //this adds the recently added photo to the data object so it can be displayed to the user, called from fragment
        JSONObject photoJson = new JSONObject();
        try {
            photoJson.put("albumId", albumId);
            photoJson.put("title", title);
            photoJson.put("url", url);
            photoJson.put("thumbnailUrl", url);
            if(data.length() == imageLoaded || data.length() < 2) {
                if(data.length() == 0)
                    ((TextView) findViewById(R.id.imageTitle)).setText(title);
                imageLoaded++;
                new getBitmapFromUrl().execute(url);
            }
            data.put(photoJson);
            new newPhoto().execute(photoJson); //executes AsyncTask newPhoto to upload photo to server.
        }catch(JSONException ex) {

        }
    }

    private class newPhoto extends AsyncTask<JSONObject, Void, JSONObject> { //This Task posts data for a new image to the server.
        @Override
        protected JSONObject doInBackground(JSONObject... params) {

            BufferedReader rd  = null;
            StringBuilder sb = null;
            String line = null;
            JSONObject json = null;
            try {
                JSONObject post = new JSONObject();
                post.put("method", "POST");
                post.put("data", params[0]);
                String address = "http://jsonplaceholder.typicode.com/albums";
                String requestBody = post.toString();
                URL url = new URL(address);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                writer.write(requestBody);
                writer.flush();
                writer.close();
                outputStream.close();

                InputStream inputStream;
                // get stream
                if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = urlConnection.getInputStream();
                } else {
                    inputStream = urlConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp, response = "";
                while ((temp = bufferedReader.readLine()) != null) {
                    response += temp;
                }
                json = new JSONObject(response.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        protected void onPostExecute(JSONObject result) {
            Toast.makeText(getBaseContext(), "Photo Uploaded!", Toast.LENGTH_LONG).show();
        }
    }

    private class getBitmapFromUrl extends AsyncTask<String, Void, Bitmap> { //this AsyncTask is needed to fetch images from urls
        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap bitmap = null;
            try {
                URL bURL = new URL(url[0]);
                bitmap = BitmapFactory.decodeStream(bURL.openConnection().getInputStream());
            } catch (IOException e) {

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {//afterwards finding the imageview and loading bitmap
            LayoutInflater inflater = (LayoutInflater) getSystemService(getBaseContext().LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.image_view_layout, null);
            ViewFlipper flipper = ((ViewFlipper)findViewById(R.id.imageFlipper));
            flipper.addView(view);
            ImageView iv = (ImageView) view.findViewById(R.id.imageView);
            iv.setImageBitmap(result);
        }
    }
}
