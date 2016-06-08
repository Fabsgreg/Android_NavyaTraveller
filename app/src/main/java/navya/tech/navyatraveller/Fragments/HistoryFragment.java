package navya.tech.navyatraveller.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class HistoryFragment extends Fragment {

    private TextView mTravel;
    private TextView mDuration;
    private TextView mDistance;
    private TextView mResult;
    private TextView mJourneyAborted;

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.history_fragment, container, false);

        mTravel = (TextView) v.findViewById(R.id.travel);
        mDuration = (TextView) v.findViewById(R.id.duration);
        mDistance = (TextView) v.findViewById(R.id.distance);
        mResult = (TextView) v.findViewById(R.id.result);
        mJourneyAborted = (TextView) v.findViewById(R.id.aborted_text);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        double distance = MainActivity.getSavingAccount().getDistance();
        double duration = MainActivity.getSavingAccount().getDuration();

        Calendar time = Calendar.getInstance();
        time.clear();
        time.add(Calendar.MINUTE, truncateDouble(duration));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        mTravel.setText(MainActivity.getSavingAccount().getNbrTravel().toString());
        mDuration.setText("" + format.format(time.getTime()) +"");
        mDistance.setText("" + truncateDecimal(distance,2) +" km");
        mResult.setText("Congratulations, you saved " + truncateDecimal(distance * 0.070,3) + " kg of CO2 by travelling with Navya");
        mJourneyAborted.setText(MainActivity.getSavingAccount().getJourneyAborted().toString());
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

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
}



