package com.sirtts.hcp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class VitalSignsActivityFragment extends Fragment  implements View.OnClickListener, Response.Listener,
        Response.ErrorListener {
    Button blood,temp,heart,resp;
    TextView lastBlood,lastTemp,lastHeart,lastResp,error;
    ProgressBar mProgressbar;
    private RequestQueue mQueue;
    private SharedPreferences sharedPre ;
    public static final String REQUEST_TAG = "vitalVolleyActivity";

    public VitalSignsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_vital_signs, container, false);

        blood = (Button) rootView.findViewById(R.id.vital_bloodPressurebtnid);
        temp = (Button) rootView.findViewById(R.id.vital_bodyTemperaturebtnid);
        heart = (Button) rootView.findViewById(R.id.vital_heartRatebtnid);
        resp = (Button) rootView.findViewById(R.id.vital_respirataryRatebtnid);

        lastBlood = (TextView) rootView.findViewById(R.id.vital_bloodPressuretvid);
        lastTemp = (TextView) rootView.findViewById(R.id.vital_bodyTemperaturetvid);
        lastHeart = (TextView) rootView.findViewById(R.id.vital_heartRatetvid);
        lastResp = (TextView) rootView.findViewById(R.id.vital_respirataryRatetvid);
        error = (TextView) rootView.findViewById(R.id.vital_error);

        mProgressbar = (ProgressBar) rootView.findViewById(R.id.vital_progressBar);

        blood.setOnClickListener(this);
        temp.setOnClickListener(this);
        heart.setOnClickListener(this);
        resp.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == blood){
            startActivity(new Intent(getContext(), BloodVitalSignsActivity.class));
        }
        else if(v == temp){
            startActivity(new Intent(getContext(), TempVitalSignsActivity.class));
        }
        else if(v == heart){
            startActivity(new Intent(getContext(), HeartVitalSignsActivity.class));
        }
        else if(v == resp){
            startActivity(new Intent(getContext(), RespVitalSignsActivity.class));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Instantiate the RequestQueue.
        error.setText("");
        mProgressbar.setVisibility(View.VISIBLE);
        sharedPre = getActivity().getSharedPreferences(getString(R.string.shared_isUserLoged),Context.MODE_PRIVATE);

        mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                .getRequestQueue();
        final JSONObjectRequest jsonRequest = new JSONObjectRequest(Request.Method
                .POST, getString(R.string.api_url_vital),
                sendData(sharedPre.getInt(getString(R.string.shared_userId),0)), this, this);
        jsonRequest.setTag(REQUEST_TAG);
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if (!isNetworkAvailable(getContext()))
            Toast.makeText(getActivity(), "Failed to Connect! Check your Connection", Toast.LENGTH_SHORT).show();

        mQueue.add(jsonRequest);

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
    public void onResponse(Object response) {
        try {
            mProgressbar.setVisibility(View.INVISIBLE);

            JSONArray bloodJsonarr = ((JSONObject) response).optJSONArray(getString(R.string.api_receive_json_vital_bloodPressure));
            JSONArray tempJsonarr = ((JSONObject) response).optJSONArray(getString(R.string.api_receive_json_vital_bodyTemp));
            JSONArray heartJsonarr = ((JSONObject) response).optJSONArray(getString(R.string.api_receive_json_vital_heartRate));
            JSONArray respJsonarr = ((JSONObject) response).optJSONArray(getString(R.string.api_receive_json_vital_RespRate));

            if(bloodJsonarr.length()>0) {
                JSONObject bloodJsonObj = bloodJsonarr.getJSONObject(0);
                lastBlood.setText(bloodJsonObj.optInt(
                        getString(R.string.api_receive_json_vital_bloodPressure_systolic))
                        + "/" + bloodJsonObj.optInt(getString(R.string.api_receive_json_vital_bloodPressure_diastolic))
                        + " on " + bloodJsonObj.optString(getString(R.string.api_receive_json_vital_date))
                );
            }
            else lastBlood.setText("");

            if(tempJsonarr.length()>0) {
                JSONObject tempJsonObj = tempJsonarr.getJSONObject(0);
                lastTemp.setText(tempJsonObj.optDouble(getString(R.string.api_receive_json_vital_bodyTemp_celsius)) +
                        " °C on " + tempJsonObj.optString(getString(R.string.api_receive_json_vital_date))
                );
            }
            else lastTemp.setText("");

            if(heartJsonarr.length()>0) {
                JSONObject heartJsonObj = heartJsonarr.getJSONObject(0);
                lastHeart.setText(heartJsonObj.optInt(getString(R.string.api_receive_json_vital_heartRate_bpm)) +
                        " bpm on " + heartJsonObj.optString(getString(R.string.api_receive_json_vital_date))
                );
            }
            else lastHeart.setText("");

            if(respJsonarr.length()>0) {
                JSONObject respJsonObj = respJsonarr.getJSONObject(0);
                lastResp.setText(respJsonObj.optInt(getString(R.string.api_receive_json_vital_RespRate_bpm)) +
                        " Breath/Min. on " + respJsonObj.optString(getString(R.string.api_receive_json_vital_date))
                );
            }
            else lastResp.setText("");


        }
        catch(Exception e){
            mProgressbar.setVisibility(View.INVISIBLE);
            error.setText("Can't fetch your data!ff");
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mProgressbar.setVisibility(View.INVISIBLE);
        this.error.setText("Can't fetch your data!");
    }

    public JSONObject sendData(int user_id){
        HashMap<String,Integer> m = new HashMap<>();
        m.put(getString(R.string.api_send_json_vital_userId),user_id);
        Log.e(REQUEST_TAG, "sendData: "+(new JSONObject(m)).toString());
        return new JSONObject(m);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
