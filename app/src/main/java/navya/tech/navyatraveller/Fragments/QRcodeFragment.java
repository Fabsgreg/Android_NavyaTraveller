package navya.tech.navyatraveller.Fragments;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;
import navya.tech.navyatraveller.SaveResult;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class QRcodeFragment extends Fragment {

    private MyDBHandler mDBHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.qr_code_fragment, container, false);

        mDBHandler = new MyDBHandler(this.getActivity());

        IntentIntegrator.forSupportFragment(this).initiateScan();

        return v;
    }

    public void LaunchCamera () {
        IntentIntegrator.forSupportFragment(this).initiateScan();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Remember the current text, to restore if we later restart.
        //outState.putCharSequence("text", mTextView.getText());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {

            // Get back data scanned
            String scanContent = scanningResult.getContents();

            Station stationScanned = mDBHandler.getStationByName(scanContent);

            MySaving().Reset();
            MySaving().setStartStation(stationScanned);
            MySaving().setLine(stationScanned.getLine());
            MySaving().setStationScanned(scanContent);

            MySaving().setPreviousFragment("QR Code");

            ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
            ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0));
        }
    }

    public SaveResult MySaving() {
        return ((MainActivity) getActivity()).saving;
    }



}
