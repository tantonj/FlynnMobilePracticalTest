package com.tantonj.flynnmobilepracticaltest;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

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
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {

    private HashMap<Integer, HashMap<String, String>> albumListHash = new HashMap<Integer, HashMap<String, String>>(); //stores albums from user; fields "title" and "id"
    private ThumbnailAdapter albumGridAdapter; //custom Adapter
    AddDialogFragment addAlbumDialog = null;
    int albumClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_grid_layout);

        albumGridAdapter = new ThumbnailAdapter(this); //initiate Adapter
        GridView gv = (GridView)findViewById(R.id.albumGrid); //get gridview, set adapter, and create both click listeners
        gv.setAdapter(albumGridAdapter);
        gv.setOnItemClickListener(new GridView.OnItemClickListener() { //initiates openAlbum AsyncTask
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                albumClicked = position;
                new OpenAlbum().execute(albumListHash.get(position).get("id"));
            }
        });
        gv.setOnItemLongClickListener(new GridView.OnItemLongClickListener() { //displays album title to user
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getBaseContext(), "Album Title: " + albumListHash.get(position).get("title"), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        addAlbumDialog = AddDialogFragment.newInstance(R.string.add_album, AddDialogFragment.ADD_ALBUM); //add's dialog for adding album
        ImageButton addBut = (ImageButton)findViewById(R.id.addAlbumBut); //gets add imagebutton and sets onclick to open dialog
        addBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                addAlbumDialog.show(fm, "dialog");
            }
        });

        new GetAlbumList().execute((Void) null ); //execute GetAlbumList Async Task
    }

    @Override
    public void onResume() {
        super.onResume();
        if(addAlbumDialog.getDialog() != null && addAlbumDialog.getDialog().isShowing()) //important for dialog not to crash activity
            addAlbumDialog.dismissAllowingStateLoss();
    }

    public void addAlbum(String title) { //method called from dialog, adds new album initiated newAlbum AsyncTask
        try {
            JSONObject album = new JSONObject();
            album.put("title", title);
            album.put("userId", 1);
            new newAlbum().execute(album);
        }catch(JSONException ex) {

        }

    }

    private class newAlbum extends AsyncTask<JSONObject, Void, JSONObject> { //this task will post data for a new album to server, afterwards it adds it to adapter
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
            Toast.makeText(getBaseContext(), "New Album Created", Toast.LENGTH_LONG).show();
            try {
                String newId = result.getString("id");
                String newTitle = result.getJSONObject("data").getString("title");
                HashMap<String, String> albumMap = new HashMap<String, String>();
                albumMap.put("id", newId);
                albumMap.put("title", newTitle);
                albumListHash.put(albumListHash.size(), albumMap);
                new GetAlbumCover().execute(newId); //initiates GetAlbumCover AsyncTask
            }catch(JSONException ex) {

            }
        }
    }

    @TargetApi(19)
    private class GetAlbumCover extends AsyncTask<String, Void, String> { //this task gets all photos in specific album, so that it can take the thumbnail from the first image, adding url to adapter
        @Override
        protected String doInBackground(String... id) {

            BufferedReader rd  = null;
            StringBuilder sb = null;
            String line = null;
            JSONArray photoArray = null;
            try {
                URL url = new URL("http://jsonplaceholder.typicode.com/photos?albumId=" + id[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
                rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();

                while ((line = rd.readLine()) != null)
                {
                    sb.append(line);
                }
                photoArray = new JSONArray(sb.toString());
                JSONObject photoCover = photoArray.getJSONObject(0);
                return photoCover.getString("thumbnailUrl");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            albumGridAdapter.add(result);
            //gv.addView(iv);
        }
    }

    @TargetApi(19)
    private class OpenAlbum extends AsyncTask<String, Void, JSONArray> { //this task will get all photos in album, then create an intent for PhotoActivity, passing data
        @Override
        protected JSONArray doInBackground(String... id) {
            BufferedReader rd  = null;
            StringBuilder sb = null;
            String line = null;
            JSONArray photoArray = null;
            try {
                URL url = new URL("http://jsonplaceholder.typicode.com/photos?albumId=" + id[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
                rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();

                while ((line = rd.readLine()) != null)
                {
                    sb.append(line);
                }
                photoArray = new JSONArray(sb.toString());
                return photoArray;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONArray result) {
            Intent i = new Intent(getBaseContext(), PhotoActivity.class);
            i.putExtra("data", result.toString());
            i.putExtra("title", albumListHash.get(albumClicked).get("title"));
            i.putExtra("id", albumListHash.get(albumClicked).get("id"));
            startActivity(i);
        }
    }

    private class GetAlbumList extends AsyncTask<Void, Void, JSONArray> { //This Task Gets all albums from user, initiating loadAlbumGrid when completed
        @Override
        protected JSONArray doInBackground(Void... params) {

            BufferedReader rd  = null;
            StringBuilder sb = null;
            String line = null;
            JSONArray jsonArray = null;
            try {
                URL url = new URL("http://jsonplaceholder.typicode.com/users/1/albums");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
                rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();

                while ((line = rd.readLine()) != null)
                {
                    sb.append(line);
                }
                jsonArray = new JSONArray(sb.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonArray;
        }

        protected void onPostExecute(JSONArray result) {
            loadAlbumGrid(result);
            //((TextView)findViewById(R.id.debugText)).setText(result.toString());
        }
    }

    @TargetApi(19)
    protected void loadAlbumGrid(JSONArray result) { //Iterates through JSON filling album HashMap, initiating GetAlbumCover Async task when completed
        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject album = result.getJSONObject(i);
                HashMap<String, String> albumMap = new HashMap<String, String>();
                albumMap.put("id", album.getString("id"));
                albumMap.put("title", album.getString("title"));
                albumListHash.put(i, albumMap);
                new GetAlbumCover().execute(album.getString("id"));
            }

        }catch(JSONException ex) {

        }catch(Exception ex) {

        }
    }
}
