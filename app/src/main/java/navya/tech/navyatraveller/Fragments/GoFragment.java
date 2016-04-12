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
import navya.tech.navyatraveller.SaveResult;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class GoFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Button goBouton;
    private Spinner goSpinnerLine ,goSpinnerStart, goSpinnerEnd;
    private ArrayAdapter<String> goSpLinAdap, goSpStaAdap, goSpEndAdap;

    private MyDBHandler mDBHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.go_fragment, container, false);

        mDBHandler = new MyDBHandler(this.getActivity());

        List<Line> myLines = new ArrayList<Line>();
        List<String> values = new ArrayList<String>();

        myLines = mDBHandler.getAllLines();
        if (myLines != null && !myLines.isEmpty()) {
            for (Line e : myLines) {
                values.add(e.getName());
            }
        }

        goSpinnerLine = (Spinner) v.findViewById(R.id.spinnerLine);
        goSpLinAdap = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        goSpLinAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        goSpinnerLine.setAdapter(goSpLinAdap);
        goSpinnerLine.setOnItemSelectedListener(this);

        goSpinnerStart = (Spinner) v.findViewById(R.id.spinnerStart);
        goSpStaAdap = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        goSpStaAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        goSpinnerStart.setAdapter(goSpStaAdap);
        goSpinnerStart.setOnItemSelectedListener(this);

        goSpinnerEnd = (Spinner) v.findViewById(R.id.spinnerEnd);
        goSpEndAdap = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        goSpEndAdap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        goSpinnerEnd.setAdapter(goSpEndAdap);
        goSpinnerEnd.setOnItemSelectedListener(this);

        goBouton = (Button) v.findViewById(R.id.go_button);
        goBouton.setOnClickListener(this);


        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void MyClick(View v) {
        switch(v.getId()) {
            case R.id.go_button:
                int a = 1;
                break;
            // Just like you were doing
        }
    }


    @Override
    public void onClick(View v) {
        //do what you want to do when mybutton is clicked

        if (v.getId() == R.id.go_button) {

            Context context = getActivity();
            AlertDialog ad = new AlertDialog.Builder(context).create();
            ad.setCancelable(false);

            if (goSpinnerStart.getSelectedItem().toString() == goSpinnerEnd.getSelectedItem().toString()){
                ad.setTitle("Error");
                ad.setMessage("You must pick two different stations");
                ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            else {
/*                ad.setTitle("Congratulation");
                ad.setMessage("Your shuttle has been requested");
                ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });*/

                Station startStation = mDBHandler.getStationByName(goSpinnerStart.getSelectedItem().toString());
                Station endStation = mDBHandler.getStationByName(goSpinnerEnd.getSelectedItem().toString());

                MySaving().Reset();
                MySaving().setStartStation(startStation);
                MySaving().setEndStation(endStation);
                MySaving().setLine(startStation.getLine());
                MySaving().setPreviousFragment("Go");

                ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
                ((MainActivity) getActivity()).onNavigationItemSelected(((MainActivity) getActivity()).navigationView.getMenu().getItem(0));

                return;
            }

            ad.show();
        }
    }

    public SaveResult MySaving() {
        return ((MainActivity) getActivity()).saving;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        Spinner spinner = (Spinner) parent;

        if (spinner.getId() == R.id.spinnerLine)
        {
            List<Station> myStations = new ArrayList<Station>();
            List<String> values = new ArrayList<String>();

            goSpStaAdap.clear();
            goSpEndAdap.clear();

            myStations = mDBHandler.getStationsOfLine(parent.getItemAtPosition(pos).toString());
            if (myStations != null && !myStations.isEmpty()) {
                for (Station e : myStations) {
                    goSpEndAdap.add(e.getStationName());
                    goSpStaAdap.add(e.getStationName());
                }
            }
            goSpEndAdap.notifyDataSetChanged();
            goSpStaAdap.notifyDataSetChanged();
        }

    }

    @Override
    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }


}
