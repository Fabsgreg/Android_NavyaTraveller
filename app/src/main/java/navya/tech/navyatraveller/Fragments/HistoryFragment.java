package navya.tech.navyatraveller.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class HistoryFragment extends Fragment {

    private TextView mTravel;
    //private TextView mLine;
    private TextView mDuration;
    private TextView mDistance;
    private TextView mResult;
    private TextView mTripAborted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.history_fragment, container, false);

        mTravel = (TextView) v.findViewById(R.id.travel);
        //mLine = (TextView) v.findViewById(R.id.line);
        mDuration = (TextView) v.findViewById(R.id.duration);
        mDistance = (TextView) v.findViewById(R.id.distance);
        mResult = (TextView) v.findViewById(R.id.result);
        mTripAborted = (TextView) v.findViewById(R.id.aborted_text);

/*        MainActivity.mSocket.on("historyReceived", onHistoryReceived);

        JSONObject request = new JSONObject();
        try {
            request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
            MainActivity.mSocket.emit("historyRequest",request);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        double distance = MainActivity.savingAccount.getDistance();
        double duration = MainActivity.savingAccount.getDuration();

        Calendar time = Calendar.getInstance();
        time.clear();
        time.add(Calendar.MINUTE, truncateDouble(duration));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        mTravel.setText(MainActivity.savingAccount.getNbrTravel().toString());
        mDuration.setText("" + format.format(time.getTime()) +"");
        mDistance.setText("" + truncateDecimal(distance,2) +" km");
        mResult.setText("Congratulations, you saved " + truncateDecimal(distance * 0.070,3) + " kg of CO2 by travelling with Navya");
        mTripAborted.setText(MainActivity.savingAccount.getTripAborted().toString());
    }

    private BigDecimal truncateDecimal (double x, int numberOfDecimals) {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberOfDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberOfDecimals, BigDecimal.ROUND_CEILING);
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

/*    private Emitter.Listener onHistoryReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<String> myLines = new ArrayList<>();
                    double duration = 0;
                    double distance = 0;

                    try {
                        JSONArray requests = (JSONArray) args[0];
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
            });
        }
    };*/

/*    private String findMostRecursiveItem (List<String> myString) {
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
    }*/
}



