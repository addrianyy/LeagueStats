package com.example.android.leaguestats.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.leaguestats.BuildConfig;
import com.example.android.leaguestats.Data;
import com.example.android.leaguestats.R;
import com.example.android.leaguestats.interfaces.MasteryTaskCompleted;
import com.example.android.leaguestats.models.Mastery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MasteryAsyncTask extends AsyncTask<String, Void, ArrayList<Mastery>> {

    private static final String LOG_TAG = MasteryAsyncTask.class.getSimpleName();
    private static final int TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int RESPONSE_CODE = 200;
    private static final String ERROR_RETRIEVING_DATA = "Error retrieving data";
    private static final String ERROR_CLOSING_STREAM = "Error closing stream";
    private static final String ERROR_RESPONSE_CODE = "Error response code: ";
    private static String HTTP_ENTRY_URL;

    private Context mContext;
    private MasteryTaskCompleted mListener;

    public MasteryAsyncTask(Context context, MasteryTaskCompleted listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected ArrayList<Mastery> doInBackground(String... strings) {
        BufferedReader reader = null;
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            URL url = createUrl(strings);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setRequestMethod(Data.REQUEST_METHOD_GET);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                StringBuilder output = new StringBuilder();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }

                if (output.length() == 0) {
                    return null;
                }

                jsonResponse = output.toString();
            } else {
                Log.e(LOG_TAG, ERROR_RESPONSE_CODE + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, ERROR_RETRIEVING_DATA, e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, ERROR_CLOSING_STREAM, e);
                }
            }
        }

        try {
            return getJsonData(jsonResponse);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private ArrayList<Mastery> getJsonData(String json) throws JSONException {
        JSONArray root = new JSONArray(json);

        ArrayList<Mastery> masteries = new ArrayList<>();

        for (int i = 0; i < root.length(); i++) {
            JSONObject object = root.getJSONObject(i);

            long playerId = object.getLong("playerId");
            long championId = object.getLong("championId");
            int championLevel = object.getInt("championLevel");
            int championPoints = object.getInt("championPoints");
            long lastPlayTime = object.getLong("lastPlayTime");
            boolean chestGranted = object.getBoolean("chestGranted");

            Mastery mastery = new Mastery(playerId, championId, championLevel, championPoints, lastPlayTime, chestGranted);
            masteries.add(mastery);
        }

        return masteries;
    }

    //sort_param[0] - sort by
    //sort_param[1] - summoner id
    private URL createUrl(String[] sort_param) {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(
                R.string.shared_preferences_name), Context.MODE_PRIVATE);
        int summonerRegionId = sharedPreferences.getInt(mContext.getString(
                R.string.summoner_region_key), mContext.getResources().getInteger(R.integer.region_eune));

        HTTP_ENTRY_URL = Data.ENTRY_URL_MASTERY_ARRAY[summonerRegionId];

        Uri builtUri = Uri.parse(HTTP_ENTRY_URL).buildUpon()
                .appendPath(sort_param[0])
                .appendPath(sort_param[1])
                .appendQueryParameter(Data.API_KEY, Data.PERSONAL_API_KEY)
                .build();
        try {
            URL url = new URL(builtUri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Mastery> masteries) {
        super.onPostExecute(masteries);

        mListener.masteryTaskCompleted(masteries);
    }
}

