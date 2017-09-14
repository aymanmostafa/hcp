package com.sirtts.hcp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class SugarActivityFragment extends Fragment implements View.OnClickListener, Response.Listener,
        Response.ErrorListener {

    Button view,save;
    TextView date,time,error,date2,time2;
    EditText mg;
    ProgressBar mProgressbar;
    SharedPreferences sharedPre;
    DatePickerDialog datePickerDialog;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "savesugarVolleyActivity";

    public SugarActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sugar, container, false);


        view = (Button) rootView.findViewById(R.id.sugar_Viewbtnid);
        save = (Button) rootView.findViewById(R.id.sugar_savebtnid);

        date = (TextView) rootView.findViewById(R.id.sugar_datetvid);
        time = (TextView) rootView.findViewById(R.id.sugar_timetvid);
        date2 = (TextView) rootView.findViewById(R.id.sugar_date2tvid);
        time2 = (TextView) rootView.findViewById(R.id.sugar_time2tvid);
        error = (TextView) rootView.findViewById(R.id.sugar_error);

        mg = (EditText) rootView.findViewById(R.id.sugar_mgtxtid);

        mProgressbar = (ProgressBar) rootView.findViewById(R.id.sugar_progressBar);

        view.setOnClickListener(this);
        save.setOnClickListener(this);

        date.setText(getCurrentDateAndTime("yyyy-MM-dd"));
        time.setText(getCurrentDateAndTime("HH:mm"));

        date.setOnClickListener(this);
        time.setOnClickListener(this);

        date2.setOnClickListener(this);
        time2.setOnClickListener(this);

        return rootView;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Instantiate the RequestQueue.
        mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                .getRequestQueue();
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
    public void onClick(View v) {
        if(v == view){
            startActivity(new Intent(getContext(), ListsugarActivity.class));
        }
        else if(v == date || v == date2){
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            date.setText(year +"-" + (monthOfYear + 1) + "-" +dayOfMonth);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.show();
        }
        else if(v == time || v == time2){
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    time.setText( selectedHour + ":" + selectedMinute);
                }
            }, hour, minute, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        }
        else if(v == save){
            if(date.getText().toString().equals("")){
                error.setText("Enter The Date");
            }
            else if(time.getText().toString().equals("")){
                error.setText("Enter The Time");
            }
            else if(mg.getText().toString().equals("")){
                error.setText("Enter Your mg/dL");
            }
            else{
                error.setText("");
                if (!isNetworkAvailable(getContext()))
                    Toast.makeText(getActivity(), "Failed to Connect! Check your Connection", Toast.LENGTH_SHORT).show();
                else {
                    sharedPre = getActivity().getSharedPreferences(getString(R.string.shared_isUserLoged), Context.MODE_PRIVATE);

                    mProgressbar.setVisibility(View.VISIBLE);
                    final JSONObjectRequest jsonRequest = new JSONObjectRequest(Request.Method
                            .POST, getString(R.string.api_url_sugar),
                            sendData(sharedPre.getInt(getString(R.string.shared_userId),0), date.getText().toString(),time.getText().toString(),
                                    Integer.valueOf(mg.getText().toString())), this, this);
                    jsonRequest.setTag(REQUEST_TAG);
                    jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    mQueue.add(jsonRequest);
                }
            }
        }
    }

    @Override
    public void onResponse(Object response) {
        try {
            mProgressbar.setVisibility(View.INVISIBLE);
            Boolean userStatus = ((JSONObject) response).optBoolean(getString(R.string.api_receive_json_status));

            if (userStatus) {
                Toast.makeText(getActivity(), "Data Saved!", Toast.LENGTH_LONG).show();
            } else {
                error.setText("Unexpected Error happened!");
            }
        }
        catch(Exception e){
            mProgressbar.setVisibility(View.INVISIBLE);
            error.setText("Unexpected Error happened!");
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mProgressbar.setVisibility(View.INVISIBLE);
        this.error.setText("Unexpected Error happened!");
    }

    public JSONObject sendData(int userid,String date,String time,int bpm){
        HashMap m = new HashMap();
        m.put(getString(R.string.api_send_json_sugar_userId),userid);
        m.put(getString(R.string.api_send_json_sugar_date),date);
        m.put(getString(R.string.api_send_json_sugar_time),time);
        m.put(getString(R.string.api_send_json_sugar_mg),bpm);
        Log.e(REQUEST_TAG, "sendData:"+(new JSONObject(m)).toString());
        return new JSONObject(m);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public String getCurrentDateAndTime(String format)
    {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(format);
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

}

