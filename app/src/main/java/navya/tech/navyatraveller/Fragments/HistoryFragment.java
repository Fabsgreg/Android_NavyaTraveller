package navya.tech.navyatraveller.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class HistoryFragment extends Fragment {

    private TextView mTravel;
    private TextView mLine;
    private TextView mDuration;
    private TextView mDistance;
    private TextView mResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.history_fragment, container, false);

        mTravel = (TextView) v.findViewById(R.id.travel);
        mLine = (TextView) v.findViewById(R.id.line);
        mDuration = (TextView) v.findViewById(R.id.duration);
        mDistance = (TextView) v.findViewById(R.id.distance);
        mResult = (TextView) v.findViewById(R.id.result);

        showRequestHistory();

        return v;
    }

    public void showRequestHistory () {
        /// PHP request
        String createRequest = "http://"+ MainActivity.ipAddress +"/navyaTraveller/showRequestHistory.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext().getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, createRequest, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Handle success event

                List<String> myLines = new ArrayList<>();
                double duration = 0;
                double distance = 0;

                try {
                    JSONObject jObject = new JSONObject(response);
                    JSONArray requests = jObject.getJSONArray("requests");
                    int nbr;
                    for (nbr=0; nbr < requests.length(); nbr++) {
                        JSONObject request = requests.getJSONObject(nbr);
                        myLines.add(request.getString("line"));
                        duration += request.getDouble("duration");
                        distance += request.getDouble("distance");
                    }

                    Calendar time = Calendar.getInstance();
                    time.clear();
                    time.add(Calendar.MINUTE, truncateDouble(duration));
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");

                    mTravel.setText(""+nbr+"");
                    mLine.setText(findMostRecursiveItem(myLines));
                    mDuration.setText("" + format.format(time.getTime()) +"");
                    mDistance.setText("" + truncateDecimal(distance,2) +" km");
                    mResult.setText("Congratulations, you saved " + truncateDecimal(distance * 0.070,3) + " kg of CO2 by travelling with Navya");

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters  = new HashMap<>();

                parameters.put("number", MainActivity.saving.getPhoneNumber());

                return parameters;
            }
        };
        requestQueue.add(request);
    }

    private String findMostRecursiveItem (List<String> myString) {
        List<String> tmp = new ArrayList<>();

        for(int i=0; i<myString.size(); i++) {
            if (!tmp.contains(myString.get(i))) {
                tmp.add(myString.get(i));
            }
        }

        int max = 0;
        int nbrItem = 0;
        String result = "";

        for(int a=0; a<tmp.size(); a++) {
            for(int b=0; b<myString.size(); b++) {
                if (myString.get(b).equalsIgnoreCase(tmp.get(a))) {
                    nbrItem ++;
                }
            }
            if (max < nbrItem) {
                max = nbrItem;
                result = tmp.get(a);
            }
            nbrItem = 0;
        }

        return result;
    }

    private BigDecimal truncateDecimal (double x, int numberofDecimals)
    {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }

    private Integer truncateDouble (double x) {
        double tmp = x - ((int) x);
        if (tmp > 0.5) {
            return (((int)x) + 1);
        }
        else {
            return (int)x;
        }
    }

}



