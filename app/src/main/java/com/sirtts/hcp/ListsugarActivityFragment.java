package com.sirtts.hcp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListsugarActivityFragment extends Fragment implements  Response.Listener<JSONArray>,
        Response.ErrorListener {

    private RequestQueue mQueue;
    ListView listview;
    VitalListAdapter adp;
    ArrayList<String> date_ArrayList = new ArrayList<String>();
    ArrayList<String> time_ArrayList = new ArrayList<String>();
    ArrayList<String> val1_ArrayList = new ArrayList<String>();
    ProgressBar mProgressbar;
    public static final String REQUEST_TAG = "ListٍSugarVolley";


    public ListsugarActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listsugar, container, false);

        listview = (ListView) rootView.findViewById(R.id.ListsugarSigns_listView);
        mProgressbar = (ProgressBar) rootView.findViewById(R.id.ListsugarSigns_progressBar);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public  void onResume(){
        if (isNetworkAvailable(getContext())) {
            SharedPreferences sharedPre = getActivity().getSharedPreferences(getString(R.string.shared_isUserLoged), Context.MODE_PRIVATE);

            mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                    .getRequestQueue();
            final JSONArrayRequest jsonRequest = new JSONArrayRequest(Request.Method
                    .POST, getString(R.string.api_url_sugar_list),
                    sendData(sharedPre.getInt(getString(R.string.shared_userId),0)), this, this);
            jsonRequest.setTag(REQUEST_TAG);
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(jsonRequest);
        }
        else Toast.makeText(getActivity(), "Failed to Connect! Check your Connection", Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProgressbar.setVisibility(View.INVISIBLE);
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }

    @Override
    public void onResponse(JSONArray response) {
        try {
            mProgressbar.setVisibility(View.INVISIBLE);

            for(int i=0;i<response.length();i++){
                date_ArrayList.add(String.valueOf(response.optJSONObject(i).optString(getString(R.string.api_receive_json_sugar_date))));
                time_ArrayList.add(String.valueOf(response.optJSONObject(i).optString(getString(R.string.api_receive_json_sugar_time))));
                val1_ArrayList.add(String.valueOf(response.optJSONObject(i).optInt(getString(R.string.api_receive_json_sugar_mg)))
                );
            }

            adp = new VitalListAdapter(getContext(),date_ArrayList,time_ArrayList,val1_ArrayList);

            listview.setAdapter(adp);


        }
        catch(Exception e){
            mProgressbar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Unexpected Error happened!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mProgressbar.setVisibility(View.INVISIBLE);
        Toast.makeText(getActivity(), "Unexpected Error happened!", Toast.LENGTH_SHORT).show();
    }

    public JSONObject sendData(int userid){
        HashMap m = new HashMap();
        m.put(getString(R.string.api_send_json_vital_list_arr_userid),userid);
        Log.e(REQUEST_TAG, "sendData: "+(new JSONObject(m)).toString());
        return new JSONObject(m);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}