package navya.tech.navyatraveller.Fragments;

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
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
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

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */

public class GmapFragment extends Fragment implements OnMapReadyCallback, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;

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

    private Hashtable<String, Marker> mNavyaMarkers;
    private Hashtable<String, Marker> mStationMarkers;
    private Hashtable<String, Polyline> mLines;

    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gmap_fragment, container, false);

        mSavedLine = new ArrayList<>();

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

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

        mLocationManager =  (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        mNavyaMarkers = new Hashtable<>();

        mStationMarkers = new Hashtable<>();

        mLines = new Hashtable<>();

        MainActivity.mSocket.on("position", onNewShuttlePosition);
        MainActivity.mSocket.on("shuttleDisconnected", onShuttleDisconnected);
        MainActivity.mSocket.on("tripAccepted", onTripAccepted);
        MainActivity.mSocket.on("tripEnded", onTripEnded);
        MainActivity.mSocket.on("tripRefused", onTripRefused);
        MainActivity.mSocket.on("tripAbortedCallback", onTripAborted);
        MainActivity.mSocket.on("shuttleUnavailable", onShuttleUnavailable);
        MainActivity.mSocket.on("shuttleArrived", onShuttleArrived);
        MainActivity.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        // Fire click event on Start button
        if (v.getId() == R.id.start_button){
            if (!MySaving().getIsTravelling()) {
                startBouton.setBackgroundColor(0xff547192);
                endBouton.setBackgroundColor(0xff6687ae);
                MySaving().setIsStartSelected(true);
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
                startBouton.setBackgroundColor(0xff6687ae);
                endBouton.setBackgroundColor(0xff547192);
                MySaving().setIsStartSelected(false);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Info");
                builder.setMessage("Are you sure you want to abort this travel ?");

                // Set up the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject request = new JSONObject();
                        try {
                            request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
                            MainActivity.mSocket.emit("tripAborted", request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TripEnded();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
            else {
                if (MySaving().isGood()) {

                    if (!MainActivity.savingAccount.getConnected()) {
                        ShowMyDialog("Info","You need to be logged in before to request a trip");
                        return;
                    }

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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {

            // get message
            String scanContent = scanningResult.getContents();

            if (scanContent.equalsIgnoreCase(MySaving().getStartStation().getStationName())) {

                // Get the index of the line selected by user
                int index = -1;
                for (int i=0; i < mSavedLine.size(); i++){
                    if (mSavedLine.get(i).getLineName().equalsIgnoreCase(MySaving().getLine().getName())) {
                        index = i;
                        MySaving().setIndex(index);
                        break;
                    }
                }
                createRequestOnServer(mSavedLine.get(index));
                return;
            }
            else {
                ShowMyDialog("Error","You've scanned the wrong code, please try again");
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        }
        fab.callOnClick();
    }

    //
    ////////////////////////////////////////////////////  Google Map Override /////////////////////////////////////////////////////////
    //

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

        focusOnPosition();

        List<Station> myStations;
        List<Line> myLines;

        float colorMarker = 0.0F;
        myLines = mDBHandler.getAllLines();
        if (myLines != null && !myLines.isEmpty()) {
            for (Line e : myLines) {
                myStations = mDBHandler.getStationsOfLine(e.getName());
                if (myStations != null && !myStations.isEmpty()) {
                    for (Station s : myStations) {
                        mStationMarkers.put(s.getStationName(),mGoogleMap.addMarker(new MarkerOptions()
                                                                        .icon(BitmapDescriptorFactory.defaultMarker(colorMarker))
                                                                        .title(s.getLine().getName())
                                                                        .snippet(s.getStationName())
                                                                        .position(new LatLng(s.getLat(), s.getLng()))));
                    }
                    String url = getMapsApiDirectionsUrl(myStations);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(url,e.getName());
                }
                colorMarker += 30;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        if (mNavyaMarkers.containsKey(marker.getTitle())) {
            return true;
        }

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
    public void onProviderDisabled(String provider){}

    @Override
    public void onProviderEnabled(String provider){}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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

    //
    ////////////////////////////////////////////////////  Google Map functions   /////////////////////////////////////////////////////////
    //

    private void hideAllMarkers() {
        for (Marker m : mStationMarkers.values()) {
            m.setVisible(false);
        }
    }

    private void hideAllRoutes() {
        for (Polyline p : mLines.values()) {
            p.setVisible(false);
        }
    }

    private void showPath(String line, String startStation, String endStation) {
        List<Station> myStations;
        try {
            myStations = mDBHandler.getStationsOfLineBetween(line,startStation,endStation);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        hideAllMarkers();
        hideAllRoutes();

        if (myStations != null) {
            for (Station s : myStations) {
                mStationMarkers.get(s.getStationName()).setVisible(true);
            }
        }

        PolylineOptions polyLineOptions = new PolylineOptions();
        polyLineOptions.addAll(mSavedLine.get(MySaving().getIndex()).getPathPoints(startStation, endStation));
        polyLineOptions.width(25);
        polyLineOptions.color(Color.RED);
        mLines.put("travellingPath",mGoogleMap.addPolyline(polyLineOptions));
    }

    private void showEverything() {
        Iterator itValueM = mStationMarkers.values().iterator();
        while(itValueM.hasNext()){
            ((Marker) itValueM.next()).setVisible(true);
        }

        mLines.get("travellingPath").setVisible(false);
        mLines.remove("travellingPath");
        Iterator itValueL = mLines.values().iterator();
        while(itValueL.hasNext()){
            ((Polyline) itValueL.next()).setVisible(true);
        }
    }

    private void focusOnPosition () {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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
        return ("https://maps.googleapis.com/maps/api/directions/" + output + "?" + origin + destination + params);
    }

    public void DisplayTravelData () {
        MySaving().setIsTravelling(true);

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
        int index = MySaving().getIndex();

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

        // Display the path associated to the current trip
        showPath(MySaving().getLine().getName(), MySaving().getStartStation().getStationName(), MySaving().getEndStation().getStationName());

        focusOnPosition();
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

    private void TripEnded () {
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

        focusOnPosition();
    }

    private SaveResult MySaving() {return MainActivity.savingData;}

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

    private Integer truncateDouble (double x) {
        double tmp = x - ((int) x);
        if (tmp > 0.5) {
            return (((int)x) + 1);
        }
        else {
            return (int)x;
        }
    }

    private BigDecimal truncateDecimal (double x,int numberofDecimals) {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }

    private void createRequestOnServer (final SaveLine data) {

        JSONObject request = new JSONObject();
        try {
            String start = MySaving().getStartStation().getStationName();
            String end =  MySaving().getEndStation().getStationName();

            request.put("start",start);
            request.put("end",end);
            request.put("line", MySaving().getLine().getName());
            request.put("duration",String.valueOf(data.getTotalDuration(start,end)));
            request.put("distance",String.valueOf(data.getTotalDistance(start,end)));
            request.put("phone_number", MainActivity.savingAccount.getPhoneNumber());
            request.put("state",String.valueOf(1));

            MainActivity.mSocket.emit("tripRequest",request);
        } catch (JSONException e) {
            e.printStackTrace();
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
        return (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    //
    ////////////////////////////////////////////////////  Socket.IO events   /////////////////////////////////////////////////////////
    //

    private Emitter.Listener onShuttleUnavailable = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Info","There is no shuttle available for the moment, please retry later");
                }
            });
        }
    };


    private Emitter.Listener onTripAborted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Your trip has been aborted",Toast.LENGTH_LONG).show();
                    MainActivity.UpdateAccountData();
                }
            });
        }
    };

    private Emitter.Listener onTripAccepted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DisplayTravelData();
                    Toast.makeText(getActivity(),"Trip accepted",Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onTripEnded = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.UpdateAccountData();
                    TripEnded();
                    ShowMyDialog("Info","Trip ended");
                }
            });
        }
    };

    private Emitter.Listener onTripRefused = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Info","Your accont is blocked, please contact us at developer.navya@gmail.com");
                }
            });
        }
    };

    private Emitter.Listener onShuttleDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    (mNavyaMarkers.get(args[0])).remove();
                    mNavyaMarkers.remove(args[0]);
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };

    private Emitter.Listener onNewShuttlePosition = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name;
                    double lat;
                    double lng;
                    try {
                        name = data.getString("name");
                        lat = data.getDouble("lat");
                        lng= data.getDouble("lng");

                        if (mNavyaMarkers.containsKey(name)) {
                            (mNavyaMarkers.get(name)).setPosition(new LatLng(lat, lng));
                        }
                        else {
                            Marker tmp = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus))
                                    .title(name));
                            mNavyaMarkers.put(name,tmp);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onShuttleArrived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Your shuttle arrived, please get in",Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    //
    ////////////////////////////////////////////////////  Http request classes   /////////////////////////////////////////////////////////
    //

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
            mLines.put(line.getLineName(),mGoogleMap.addPolyline(polyLineOptions));
        }

    }
}
