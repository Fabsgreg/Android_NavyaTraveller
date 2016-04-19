package navya.tech.navyatraveller.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

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

        IntentIntegrator.forSupportFragment(this).setPrompt("Please, scan the QR code near you to complete your order").initiateScan();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultCode != 0) {
            // Get back data scanned

            String scanContent = scanningResult.getContents();

            Boolean isStationExisting = false;
            List<Station> allStations;
            allStations = mDBHandler.getAllStations();
            for (Station s : allStations) {
                if (scanContent.equalsIgnoreCase(s.getStationName())) {
                    isStationExisting = true;
                }
            }

            if (isStationExisting) {
                Station stationScanned = mDBHandler.getStationByName(scanContent);
                MySaving().Reset();
                MySaving().setStartStation(stationScanned);
                MySaving().setLine(stationScanned.getLine());
                MySaving().setStationScanned(scanContent);
                MySaving().setPreviousFragment("QR Code");
            }
            else {
                ShowMyDialog("Error","The station scanned doesn't exist");
                return;
            }
        }
        ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true));
    }

    public SaveResult MySaving() {
        return MainActivity.saving;
    }

    public void ShowMyDialog (String title, String text) {
        Context context = getActivity();
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setCancelable(false);
        ad.setTitle(title);
        ad.setMessage(text);
        ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true));
            }
        });
        ad.show();
    }



}
