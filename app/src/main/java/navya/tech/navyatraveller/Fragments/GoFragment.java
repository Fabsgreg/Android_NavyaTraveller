package navya.tech.navyatraveller.Fragments;


import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import java.util.ArrayList;
import java.util.List;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class GoFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner mGoSpinnerStart;
    private Spinner mGoSpinnerEnd;
    private ArrayAdapter<String> mGoSpStaAdap;
    private ArrayAdapter<String> mGoSpEndAdap;

    private MyDBHandler mDBHandler;

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.go_fragment, container, false);

        mDBHandler = new MyDBHandler(this.getActivity());

        List<Line> myLines;
        List<String> values = new ArrayList<>();

        myLines = mDBHandler.getAllLines();
        if (myLines != null && !myLines.isEmpty()) {
            for (Line e : myLines) {
                values.add(e.getName());
            }
        }

        Spinner goSpinnerLine = (Spinner) v.findViewById(R.id.spinnerLine);
        ArrayAdapter<String> goSpLinAdap = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        goSpLinAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        goSpinnerLine.setAdapter(goSpLinAdap);
        goSpinnerLine.setOnItemSelectedListener(this);

        mGoSpinnerStart = (Spinner) v.findViewById(R.id.spinnerStart);
        mGoSpStaAdap = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        mGoSpStaAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGoSpinnerStart.setAdapter(mGoSpStaAdap);
        mGoSpinnerStart.setOnItemSelectedListener(this);

        mGoSpinnerEnd = (Spinner) v.findViewById(R.id.spinnerEnd);
        mGoSpEndAdap = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        mGoSpEndAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGoSpinnerEnd.setAdapter(mGoSpEndAdap);
        mGoSpinnerEnd.setOnItemSelectedListener(this);

        Button goBouton = (Button) v.findViewById(R.id.go_button);
        goBouton.setOnClickListener(this);


        return v;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.go_button) {

            Context context = getActivity();
            AlertDialog ad = new AlertDialog.Builder(context).create();
            ad.setCancelable(false);

            if (mGoSpinnerStart.getSelectedItem().toString().equalsIgnoreCase(mGoSpinnerEnd.getSelectedItem().toString())){
                ad.setTitle("Error");
                ad.setMessage("You must pick two different stations");
                ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            else {
                Station startStation = mDBHandler.getStationByName(mGoSpinnerStart.getSelectedItem().toString());
                Station endStation = mDBHandler.getStationByName(mGoSpinnerEnd.getSelectedItem().toString());

                MainActivity.getSavingResult().Reset();
                MainActivity.getSavingResult().setStartStation(startStation);
                MainActivity.getSavingResult().setEndStation(endStation);
                MainActivity.getSavingResult().setLine(startStation.getLine());
                MainActivity.getSavingResult().setPreviousFragment("Go");

                ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).mNavigationView.getMenu().getItem(0).setChecked(true));

                return;
            }

            ad.show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        Spinner spinner = (Spinner) parent;

        if (spinner.getId() == R.id.spinnerLine)
        {
            List<Station> myStations;

            mGoSpStaAdap.clear();
            mGoSpEndAdap.clear();

            myStations = mDBHandler.getStationsOfLine(parent.getItemAtPosition(pos).toString());
            if (myStations != null && !myStations.isEmpty()) {
                for (Station e : myStations) {
                    mGoSpEndAdap.add(e.getStationName());
                    mGoSpStaAdap.add(e.getStationName());
                }
            }
            mGoSpEndAdap.notifyDataSetChanged();
            mGoSpStaAdap.notifyDataSetChanged();
        }

    }

    @Override
    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }

}
