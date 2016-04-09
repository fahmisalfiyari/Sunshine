package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayList<String> listpressure = new ArrayList<>();

    String[] cuacaArray = {
            "Hari Ini - Sunny - 88/64",
            "Sunday - Cloudy - 70/64",
            "Monday - Rainy - 46/64",
            "Tuesday - Cloudy - 70/64",
            "Wednesday - Storm - 38/64",
            "Thursday - Sunny - 90/64",
            "Friday - Cloudy - 68/64",
            "Today - Sunny - 88/64",
            "Sunday - Cloudy - 70/64",
            "Monday - Rainy - 46/64",
            "Tuesday - Cloudy - 70/64",
            "Wednesday - Storm - 38/64",
            "Thursday - Sunny - 90/64",
            "Friday - Cloudy - 68/64"
    };


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_menu, menu);

    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_refresh:
//                Toast.makeText(getActivity(),"Refresh Button",
//                Toast.LENGTH_SHORT).show();
                FetchWeatherTask task = new FetchWeatherTask();
                task.execute("40132,id");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                cuacaArray);

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);




        return rootView;
    }

    void parseJsonString(String raw) throws JSONException{
        JSONObject object = new JSONObject(raw);
        JSONObject cityObject = object.getJSONObject("city");
        String name = cityObject.getString("name");
        JSONObject coordObj = cityObject.getJSONObject("coord");
        String lon = coordObj.getString("lon");
        JSONArray listArray = object.getJSONArray("list");
        Log.i("ForecastJsonLog","count : "+listArray.length() );
        for(int i=0;i<listArray.length();i++){
            JSONObject listObject = listArray.getJSONObject(i);
            String pressure = listObject.getString("pressure");
            Log.i("ForecastJsonLog","pressure : "+pressure );
        }
        Log.i("ForecastJsonLog","name : "+name );
        Log.i("ForecastJsonLog","lon : "+lon);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void,Void>  {

        public  final  String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String format = "json";
            String units = "metric";
            int numDays = 7;
            String appid = "2d76692ae167d77f0ff8d27728dd4e5f";

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=2d76692ae167d77f0ff8d27728dd4e5f");

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "zip";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, appid)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                parseJsonString(forecastJsonStr);
                Log.i("Forecasting",forecastJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}
