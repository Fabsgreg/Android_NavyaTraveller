package navya.tech.navyatraveller.Fragments;

import android.location.Criteria;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.HttpConnection;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.PathJSONParser;
import navya.tech.navyatraveller.R;
import navya.tech.navyatraveller.SaveLine;
import navya.tech.navyatraveller.SaveResult;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class GmapFragment extends Fragment implements OnMapReadyCallback, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;
    private String mProvider;

    private TextView mArrivalTime;
    private TextView mWaitingTime;
    private TextView mDuration;
    private TextView mDistance;
    private TextView mResult;

    private SlidingUpPanelLayout mLayout;

    private MyDBHandler mDBHandler;

    private Button startBouton;
    private Button endBouton;

    private FloatingActionButton fab;

    private List<SaveLine> mSavedLine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gmap_fragment, container, false);

        mSavedLine = new ArrayList<SaveLine>();

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);

        if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        else {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        mDBHandler = new MyDBHandler(this.getActivity());

        startBouton = (Button) v.findViewById(R.id.start_button);
        startBouton.setOnClickListener(this);

        endBouton = (Button) v.findViewById(R.id.end_button);
        endBouton.setOnClickListener(this);

        mResult = (TextView) v.findViewById(R.id.result_textView);
        mArrivalTime = (TextView) v.findViewById(R.id.arrival_time);
        mWaitingTime = (TextView) v.findViewById(R.id.waiting_time);
        mDistance = (TextView) v.findViewById(R.id.distance);
        mDuration = (TextView) v.findViewById(R.id.duration);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        return v;
    }

    public SaveResult MySaving() {
        return ((MainActivity) getActivity()).saving;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void Update () {
        if (MySaving().getWasQRcode()) {
            endBouton.callOnClick();
        }
        else if (MySaving().getWasGo()) {
            endBouton.callOnClick();
            fab.callOnClick();
        }
        MySaving().setPreviousFragment("Map");
    }

    @Override
    public void onClick(View v) {
        // Fire click event on Start button
        if (v.getId() == R.id.start_button){
            if (!MySaving().getIsTravelling()) {
                startBouton.setBackgroundColor(0xff3464d0);
                endBouton.setBackgroundColor(0xff4977dc);
                MySaving().setIsStartSelected(true);
                MySaving().setIsEndSelected(false);
                // Check if a station has been selected before
                if (MySaving().getStartStation().getStationName() != null) {
                    // If yes, memorize this station as starting point
                    mResult.setText(MySaving().getStartStation().getStationName());
                }
                else {
                    mResult.setText("Pick a station");
                }

            }
        }
        // Fire click event on End button
        else if (v.getId() == R.id.end_button){
            if (!MySaving().getIsTravelling()) {
                startBouton.setBackgroundColor(0xff4977dc);
                endBouton.setBackgroundColor(0xff3464d0);
                MySaving().setIsStartSelected(false);
                MySaving().setIsEndSelected(true);
                // Check if a station has been selected before
                if (MySaving().getEndStation().getStationName() != null) {
                    // If yes, memorize this station as ending point
                    mResult.setText(MySaving().getEndStation().getStationName());
                }
                else {
                    mResult.setText("Pick a station");
                }
            }
        }
        else if (v.getId() == R.id.fab) {
            // If you're already travelling
            if (MySaving().getIsTravelling()) {
                // Hide the SlidingUpPanel
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                // Resize the Google map component
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMapView.getLayoutParams();
                params.bottomMargin = (int)convertDpToPixel(0,this.getActivity());
                mMapView.setLayoutParams(params);

                params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
                params.bottomMargin = (int) convertDpToPixel(0, this.getActivity());
                fab.setLayoutParams(params);
                fab.setImageDrawable(ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_directions));

                // Display all the line with their associated stations
                showEverything();

                MySaving().setIsTravelling(false);
            }
            else {
                if (MySaving().isGood()) {
                    // Show the QRcode camera window if it hasn't been scanned before
                    if ( !MySaving().getStationScanned().equalsIgnoreCase(MySaving().getStartStation().getStationName()) ) {
                        IntentIntegrator.forSupportFragment(this).setPrompt("Please, scan the QR code near you to complete your order").initiateScan();
                    }
                    else {
                        DisplayTravelData();
                        MySaving().setStationScanned("");
                    }
                }
                else {
                    ShowMyDialog("Error", "You must pick two different stations on the same line");
                }
            }
            focusOnPosition();
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

    private BigDecimal truncateDecimal (double x,int numberofDecimals)
    {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }

    public void DisplayTravelData () {
        // Display the SlidingUpPanel
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // Adapt the Google map component size to the screen with the SlidingUpPanel below
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMapView.getLayoutParams();
        params.bottomMargin = (int)convertDpToPixel(68, this.getActivity());
        mMapView.setLayoutParams(params);

        params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        params.bottomMargin = (int)convertDpToPixel(68, this.getActivity());
        fab.setLayoutParams(params);
        fab.setImageDrawable(ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_cancel));

        // Get the index of the line selected by user
        int index = -1;
        for (int i=0; i < mSavedLine.size(); i++){
            if (mSavedLine.get(i).getLineName().equalsIgnoreCase(MySaving().getLine().getName())) {
                index = i;
                MySaving().setIndex(index);
                break;
            }
        }

        // Display informations about the trip
        int waitingTime = 2;

        mWaitingTime.setText("" + waitingTime + " min");
        mDistance.setText("" + truncateDecimal(mSavedLine.get(index).getTotalDistance(MySaving().getStartStation().getStationName(), MySaving().getEndStation().getStationName()), 2) + " km");

        int duration = truncateDouble(mSavedLine.get(index).getTotalDuration(MySaving().getStartStation().getStationName(), MySaving().getEndStation().getStationName()));
        mDuration.setText("" + duration + " min");

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, (waitingTime + duration));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String DateToStr = format.format(now.getTime());

        mArrivalTime.setText(DateToStr);

        ((MainActivity) getActivity()).createRequestOnDB(mSavedLine.get(index));

        // Display the path associated to the current trip
        showPath(MySaving().getLine().getName(), MySaving().getStartStation().getStationName(), MySaving().getEndStation().getStationName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {

            // get message
            String scanContent = scanningResult.getContents();

            // get format
            String scanFormat = scanningResult.getFormatName();

            if (scanContent.equalsIgnoreCase(MySaving().getStartStation().getStationName())) {
                DisplayTravelData();
            }
            else {
                ShowMyDialog("Error","You've scanned the wrong code, please try again");
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                fab.callOnClick();
            }
        }
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
            }
        });
        ad.show();
    }

    private static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.setBuildingsEnabled(true);
        mGoogleMap.setOnMarkerClickListener(this);

/*        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(true);*/

        mLocationManager =  (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        /*mProvider = mLocationManager.getBestProvider(criteria, true);*/
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, 0, this);

        focusOnPosition();

        List<Station> myStations = new ArrayList<Station>();
        List<Line> myLines = new ArrayList<Line>();

        float colorMarker = 0.0F;
        myLines = mDBHandler.getAllLines();
        if (myLines != null && !myLines.isEmpty()) {
            for (Line e : myLines) {
                myStations = mDBHandler.getStationsOfLine(e.getName());
                if (myStations != null && !myStations.isEmpty()) {
                    for (Station s : myStations) {
                        mGoogleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(colorMarker))
                                .title(s.getLine().getName())
                                .snippet(s.getStationName())
                                .position(new LatLng(s.getLat(), s.getLng())));
                    }
                    String url = getMapsApiDirectionsUrl(myStations);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(url,e.getName());
                }
                colorMarker += 30;
            }
        }
    }

    private void showPath(String line, String startStation, String endStation) {
        mGoogleMap.clear();
        List<Station> myStations = mDBHandler.getStationsOfLineBetween(line,startStation,endStation);

        float colorMarker = 30.0F;

        for (Station s : myStations) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(colorMarker))
                    .title(s.getLine().getName())
                    .snippet(s.getStationName())
                    .position(new LatLng(s.getLat(), s.getLng())));
        }

        PolylineOptions polyLineOptions = new PolylineOptions();

        polyLineOptions.addAll(mSavedLine.get(MySaving().getIndex()).getPathPoints(startStation, endStation));
        polyLineOptions.width(25);
        polyLineOptions.color(Color.RED);
        mGoogleMap.addPolyline(polyLineOptions);
    }

    private void showEverything() {
        mGoogleMap.clear();

        List<Station> myStations = new ArrayList<Station>();
        List<Line> myLines = new ArrayList<Line>();

        float colorMarker = 0.0F;
        myLines = mDBHandler.getAllLines();
        if (myLines != null && !myLines.isEmpty()) {
            for (Line e : myLines) {
                myStations = mDBHandler.getStationsOfLine(e.getName());
                if (myStations != null && !myStations.isEmpty()) {
                    for (Station s : myStations) {
                        mGoogleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(colorMarker))
                                .title(s.getLine().getName())
                                .snippet(s.getStationName())
                                .position(new LatLng(s.getLat(), s.getLng())));
                    }

                    PolylineOptions polyLineOptions = new PolylineOptions();

                    long index = e.getId();
                    polyLineOptions.addAll(mSavedLine.get(((int)index-1)).getAllPoints());
                    polyLineOptions.width(5);
                    polyLineOptions.color(Color.BLUE);
                    mGoogleMap.addPolyline(polyLineOptions);

                }
                colorMarker += 30;
            }
        }
    }

    private void focusOnPosition () {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);
            CameraPosition cameraPosition;

            if (MySaving().getIsTravelling()) {
                cameraPosition = CameraPosition.builder()
                        .target(coordinate)
                        .zoom(20)
                        .tilt(70)
                        .build();
            }
            else {
                cameraPosition = CameraPosition.builder()
                        .target(coordinate)
                        .zoom(15)
                        .tilt(0)
                        .build();
            }

            // Animate the change in camera view over 2 seconds
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    2000, null);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        if (!MySaving().getIsTravelling()) {
            if (MySaving().getIsStartSelected()) {
                MySaving().setStartStation(mDBHandler.getStationByName(marker.getSnippet()));
                mResult.setText(MySaving().getStartStation().getStationName());
                // If there is a station previously scanned and different from the new one selected, reset that one
                if (!MySaving().getStationScanned().equalsIgnoreCase(MySaving().getStartStation().getStationName())) {
                    MySaving().setStationScanned("");
                }
                endBouton.callOnClick();
            }
            else {
                MySaving().setEndStation(mDBHandler.getStationByName(marker.getSnippet()));
                mResult.setText(MySaving().getEndStation().getStationName());
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        focusOnPosition();
    }

    @Override
    public void onProviderDisabled(String provider){

    }

    @Override
    public void onProviderEnabled(String provider){

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onResume() {
        super.onResume();
        //mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, 0, this);
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private String getMapsApiDirectionsUrl( List<Station> myStations) {
        String waypoints = "waypoints=optimize:true";
        int count = myStations.size();

        for (int i = 0; i < count; i++) {
            if (i == (count-1)) {
                waypoints += "|" + myStations.get(i).getLat() + "," + myStations.get(i).getLng();
            }
            else {
                waypoints += "|" + myStations.get(i).getLat() + "," + myStations.get(i).getLng()  + "|";
            }
        }

        String origin = "origin=" + myStations.get(0).getLat() + "," + myStations.get(0).getLng() + "&";
        String destination = "destination=" + myStations.get(0).getLat() + "," + myStations.get(0).getLng() + "&";
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + origin + destination + params;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        private String lineName;

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
                lineName = url[1];
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String mResult) {
            super.onPostExecute(mResult);
            new ParserTask().execute(mResult, lineName);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, SaveLine> {

        @Override
        protected SaveLine doInBackground(String... jsonData) {
            JSONObject jObject;
            SaveLine mResult = new SaveLine();
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                mResult = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Station> myStations = mDBHandler.getStationsOfLine(jsonData[1]);
            for (Station s : myStations) {
                mResult.addStationName(s.getStationName());
            }
            mResult.setLineName(jsonData[1]);
            mSavedLine.add(mResult);
            return mResult;
        }

        @Override
        protected void onPostExecute(SaveLine line) {
            PolylineOptions polyLineOptions = new PolylineOptions();

            polyLineOptions.addAll(line.getAllPoints());
            polyLineOptions.width(5);
            polyLineOptions.color(Color.BLUE);
            mGoogleMap.addPolyline(polyLineOptions);
        }

    }


}
