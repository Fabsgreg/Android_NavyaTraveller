package navya.tech.navyatraveller.Fragments;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Vibrator;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import java.util.List;


import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.General;
import navya.tech.navyatraveller.HttpConnection;
import navya.tech.navyatraveller.MainActivity;
import navya.tech.navyatraveller.GoogleMapsJSONParser;
import navya.tech.navyatraveller.R;
import navya.tech.navyatraveller.SavingLine;

import io.socket.emitter.Emitter;
import navya.tech.navyatraveller.SavingResult;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */

public class GmapFragment extends Fragment implements OnMapReadyCallback, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener, General, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Google map
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Hashtable<String, Marker> mNavyaMarkers;
    private Hashtable<String, Marker> mStationMarkers;
    private Hashtable<String, Polyline> mLines;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    
    // Layout
    private TextView mArrivalTime;
    private TextView mWaitingTime;
    private TextView mDuration;
    private TextView mDistance;
    private TextView mEndResult;
    private TextView mStartResult;
    private LinearLayout mStartLayout;
    private LinearLayout mEndLayout;
    private Button mStartBouton;
    private Button mEndBouton;
    private FloatingActionButton mFloatingButton;
    private SlidingUpPanelLayout mLayout;

    // Database
    private MyDBHandler mDBHandler;

    // Data saving
    private List<SavingLine> mSavedLine;

    // Bluetooth
    private static final int REQUEST_DISCOVERABLE_CODE = 42;


    //
    ////////////////////////////////////////////////////  View Override /////////////////////////////////////////////////////////
    //


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        MainActivity.getSocket().on("Position", onNewShuttlePosition);
        MainActivity.getSocket().on("shuttleDisconnected", onShuttleDisconnected);
        MainActivity.getSocket().on("journeyCompleted", onJourneyCompleted);
        MainActivity.getSocket().on("journeyRefused", onJourneyRefused);
        MainActivity.getSocket().on("journeyAborted", onJourneyAborted);
        MainActivity.getSocket().on("shuttleUnavailable", onShuttleUnavailable);
        MainActivity.getSocket().on("shuttleArrived", onShuttleArrived);
        MainActivity.getSocket().on("AndroidMission", onAndroidMission);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gmap_fragment, container, false);

        mSavedLine = new ArrayList<>();
        mDBHandler = new MyDBHandler(this.getActivity());
        mNavyaMarkers = new Hashtable<>();
        mStationMarkers = new Hashtable<>();
        mLines = new Hashtable<>();

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        mStartBouton = (Button) v.findViewById(R.id.start_button);
        mStartBouton.setOnClickListener(this);

        mEndBouton = (Button) v.findViewById(R.id.end_button);
        mEndBouton.setOnClickListener(this);

        mEndResult = (TextView) v.findViewById(R.id.result_end_textView);
        mStartResult = (TextView) v.findViewById(R.id.result_start_textView);

        mArrivalTime = (TextView) v.findViewById(R.id.arrival_time);
        mWaitingTime = (TextView) v.findViewById(R.id.waiting_time);
        mDistance = (TextView) v.findViewById(R.id.distance);
        mDuration = (TextView) v.findViewById(R.id.duration);

        mStartLayout = (LinearLayout) v.findViewById(R.id.start_layout);
        mEndLayout = (LinearLayout) v.findViewById(R.id.end_layout);

        mFloatingButton = (FloatingActionButton) v.findViewById(R.id.fab);
        mFloatingButton.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        return v;
    }

    @Override
    public void onClick(View v) {
        // Fire click event on Start button
        if (v.getId() == R.id.start_button){
            if (!MainActivity.getSavingResult().getTravelling()) {
                mStartBouton.setBackgroundColor(0xff547192);
                mStartLayout.setBackgroundColor(0xff547192);
                mEndBouton.setBackgroundColor(0xff6687ae);
                mEndLayout.setBackgroundColor(0xff6687ae);
                MainActivity.getSavingResult().setStartSelected(true);
                // Check if a station has been selected before
                if (MainActivity.getSavingResult().getStartStation().getStationName() != null) {
                    // If yes, memorize this station as starting point
                    mStartResult.setText(MainActivity.getSavingResult().getStartStation().getStationName());
                }
                else {
                    mStartResult.setHint("Pick a station");
                }
            }
        }
        // Fire click event on End button
        else if (v.getId() == R.id.end_button){
            if (!MainActivity.getSavingResult().getTravelling()) {
                mStartBouton.setBackgroundColor(0xff6687ae);
                mStartLayout.setBackgroundColor(0xff6687ae);
                mEndBouton.setBackgroundColor(0xff547192);
                mEndLayout.setBackgroundColor(0xff547192);
                MainActivity.getSavingResult().setStartSelected(false);
                // Check if a station has been selected before
                if (MainActivity.getSavingResult().getEndStation().getStationName() != null) {
                    // If yes, memorize this station as ending point
                    mEndResult.setText(MainActivity.getSavingResult().getEndStation().getStationName());
                }
                else {
                    mEndResult.setHint("Pick a station");
                }
            }
        }
        else if (v.getId() == R.id.fab) {
            // If you're already travelling
            if (MainActivity.getSavingResult().getTravelling()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Info");
                builder.setMessage("Are you sure you want to abort this travel ?");

                // Set up the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject request = new JSONObject();
                        try {
                            request.put("phone_number", MainActivity.getSavingAccount().getPhoneNumber());
                            MainActivity.getSocket().emit("journeyAborted", request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JourneyCompleted();
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
                if (MainActivity.getSavingResult().isGood()) {

                    if (!MainActivity.getSavingAccount().getConnected()) {
                        ShowMyDialog("Info","You need to be logged in before to request a journey");
                        return;
                    }

                    if (!MainActivity.getSavingAccount().getInternetAvailable()) {
                        ShowMyDialog("Info","Server unavailable, please check your connection");
                        return;
                    }

                    setBluetooth(true);
                    setBluetoothDiscoverable(true);
                }
                else {
                    ShowMyDialog("Error", "You must pick two different stations on the same line");
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_DISCOVERABLE_CODE) {
            // Bluetooth Discoverable Mode does not return the standard
            // Activity result codes.
            // Instead, the result code is the duration (seconds) of
            // discoverability or a negative number if the user answered "NO".
            if (resultCode > 0) {
                Toast.makeText(getActivity(),"Bluetooth connection will be disabled after your journey",Toast.LENGTH_LONG).show();

                // Show the QRcode camera window if it hasn't been scanned before
                if ( !MainActivity.getSavingResult().getStationScanned().equalsIgnoreCase(MainActivity.getSavingResult().getStartStation().getStationName()) ) {
                    IntentIntegrator.forSupportFragment(this).setPrompt("Please, scan the QR code near you to complete your order").initiateScan();
                }
                else {
                    // Get the index of the line selected by user
                    int index = -1;
                    for (int i=0; i < mSavedLine.size(); i++){
                        if (mSavedLine.get(i).getLineName().equalsIgnoreCase(MainActivity.getSavingResult().getLine().getName())) {
                            index = i;
                            MainActivity.getSavingResult().setCurrentIndexOfSavedLine(index);
                            break;
                        }
                    }
                    createRequestOnServer(mSavedLine.get(index));
                    MainActivity.getSavingResult().setStationScanned("");
                }
            }
            else {
                ShowMyDialog("Warning","You must accept the previous statement if you want to request a shuttle, do not worry, any personal data will be collected");
            }
        }
        else if(requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (resultCode != 0) {

                // get message
                String scanContent = scanningResult.getContents();

                if (scanContent.equalsIgnoreCase(MainActivity.getSavingResult().getStartStation().getStationName())) {

                    // Get the index of the line selected by user
                    int index = -1;
                    for (int i=0; i < mSavedLine.size(); i++){
                        if (mSavedLine.get(i).getLineName().equalsIgnoreCase(MainActivity.getSavingResult().getLine().getName())) {
                            index = i;
                            MainActivity.getSavingResult().setCurrentIndexOfSavedLine(index);
                            break;
                        }
                    }
                    createRequestOnServer(mSavedLine.get(index));
                    //return;
                }
                else {
                    ShowMyDialog("Error","You've scanned the wrong code, please try again");
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
            }
        }
    }


    //
    ////////////////////////////////////////////////////  Google Map Override /////////////////////////////////////////////////////////
    //

    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng coordinate = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraPosition cameraPosition;
            cameraPosition = CameraPosition.builder()
                    .target(coordinate)
                    .zoom(15)
                    .tilt(0)
                    .build();
            // Animate the change in camera view over 2 seconds
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

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

        focusOnPosition();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        if (mNavyaMarkers.containsKey(marker.getTitle())) {
            return true;
        }

        if (!MainActivity.getSavingResult().getTravelling()) {
            if (MainActivity.getSavingResult().getStartSelected()) {
                MainActivity.getSavingResult().setStartStation(mDBHandler.getStationByName(marker.getSnippet()));
                mStartResult.setText(MainActivity.getSavingResult().getStartStation().getStationName());
                // If there is a station previously scanned and different from the new one selected, reset that one
                if (!MainActivity.getSavingResult().getStationScanned().equalsIgnoreCase(MainActivity.getSavingResult().getStartStation().getStationName())) {
                    MainActivity.getSavingResult().setStationScanned("");
                }
                mEndBouton.callOnClick();
            }
            else {
                MainActivity.getSavingResult().setEndStation(mDBHandler.getStationByName(marker.getSnippet()));
                mEndResult.setText(MainActivity.getSavingResult().getEndStation().getStationName());
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        focusOnPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
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
    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

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
        polyLineOptions.addAll(mSavedLine.get(MainActivity.getSavingResult().getIndex()).getPathPoints(startStation, endStation));
        polyLineOptions.width(25);
        polyLineOptions.color(Color.RED);
        mLines.put("travellingPath",mGoogleMap.addPolyline(polyLineOptions));
    }

    private void showEverything() {
        for (Marker o : mStationMarkers.values()) {
            (o).setVisible(true);
        }

        mLines.get("travellingPath").setVisible(false);
        mLines.remove("travellingPath");
        for (Polyline o : mLines.values()) {
            (o).setVisible(true);
        }
    }

    private void showNearStations (Location currentLocation) {
        List<Station> allStations;
        List<Line> allLines;

        float colorMarker = 0.0F;
        allLines = mDBHandler.getAllLines();
        if (allLines != null && !allLines.isEmpty()) {
            for (Line e : allLines) {
                allStations = mDBHandler.getStationsOfLine(e.getName());
                if (allStations != null && !allStations.isEmpty()) {
                    boolean isNearCurrentLocation = false;

                    // Find near stations
                    for (Station s : allStations) {
                        Location loc = new Location("");
                        loc.setLatitude(s.getLat());
                        loc.setLongitude(s.getLng());

                        if (currentLocation.distanceTo(loc) < MainActivity.minDist) {
                            isNearCurrentLocation = true;
                            break;
                        }
                    }

                    for (Station s : allStations) {
                        if (isNearCurrentLocation) {
                            if (mStationMarkers.containsKey(s.getStationName())) {
                                mStationMarkers.get(s.getStationName()).setVisible(true);
                            }
                            else {
                                mStationMarkers.put(s.getStationName(),mGoogleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker(colorMarker))
                                        .title(s.getLine().getName())
                                        .snippet(s.getStationName())
                                        .position(new LatLng(s.getLat(), s.getLng()))));
                            }
                        }
                        else {
                            if (mStationMarkers.containsKey(s.getStationName())) {
                                mStationMarkers.get(s.getStationName()).setVisible(false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void focusOnPosition () {
        if (currentLocation != null) {
            if (MainActivity.getSavingResult().getTravelling()) {
                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();
                LatLng coordinate = new LatLng(lat, lng);
                CameraPosition cameraPosition;

                cameraPosition = CameraPosition.builder()
                        .target(coordinate)
                        .zoom(20)
                        .tilt(70)
                        .build();

                // Animate the change in camera view over 2 seconds
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
            }
            else {
                showNearStations(currentLocation);
            }
        }
    }

    private void DisplayJourneyData(long waitingTime, long duration, double distance) {
        MainActivity.getSavingResult().setTravelling(true);

        // Display the SlidingUpPanel
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // Adapt the Google map component size to the screen with the SlidingUpPanel below
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMapView.getLayoutParams();
        params.bottomMargin = (int)convertDpToPixel(68, this.getActivity());
        mMapView.setLayoutParams(params);
        params = (ViewGroup.MarginLayoutParams) mFloatingButton.getLayoutParams();
        params.bottomMargin = (int)convertDpToPixel(68, this.getActivity());
        mFloatingButton.setLayoutParams(params);
        mFloatingButton.setImageDrawable(ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_cancel));

        // Display informations about the journey
        mWaitingTime.setText("" + waitingTime + " min");
        mDistance.setText("" + distance + " km");
        mDuration.setText("" + duration + " min");

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, (int)(waitingTime + duration));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String DateToStr = format.format(now.getTime());

        mArrivalTime.setText(DateToStr);

        // Display the path associated to the current journey
        //showPath(MainActivity.getSavingResult().getLine().getName(), MainActivity.getSavingResult().getStartStation().getStationName(), MainActivity.getSavingResult().getEndStation().getStationName());

        focusOnPosition();
    }

    //
    ////////////////////////////////////////////////////  Miscellaneous functions   /////////////////////////////////////////////////////////
    //

    private void JourneyCompleted() {
        // Hide the SlidingUpPanel
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // Resize the Google map component
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMapView.getLayoutParams();
        params.bottomMargin = (int)convertDpToPixel(0,this.getActivity());
        mMapView.setLayoutParams(params);

        params = (ViewGroup.MarginLayoutParams) mFloatingButton.getLayoutParams();
        params.bottomMargin = (int) convertDpToPixel(0, this.getActivity());
        mFloatingButton.setLayoutParams(params);
        mFloatingButton.setImageDrawable(ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_directions));

        // Display all the line with their associated stations
        //showEverything();

        MainActivity.getSavingResult().setTravelling(false);

        focusOnPosition();
    }

    public void Update () {
        if (MainActivity.getSavingResult().getQRcode()) {
            mStartResult.setText(MainActivity.getSavingResult().getStartStation().getStationName());
            mEndBouton.callOnClick();
        }
        else if (MainActivity.getSavingResult().getGo()) {
            mEndBouton.callOnClick();
            mEndResult.setText(MainActivity.getSavingResult().getEndStation().getStationName());
            mStartResult.setText(MainActivity.getSavingResult().getStartStation().getStationName());
            mFloatingButton.callOnClick();
        }
        MainActivity.getSavingResult().setPreviousFragment("Map");
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

    private void createRequestOnServer (final SavingLine data) {

        JSONObject request = new JSONObject();
        try {
            String start = MainActivity.getSavingResult().getStartStation().getStationName();
            String end =  MainActivity.getSavingResult().getEndStation().getStationName();

            request.put("start",start);
            request.put("end",end);
            request.put("line", MainActivity.getSavingResult().getLine().getName());
            request.put("phone_number", MainActivity.getSavingAccount().getPhoneNumber());
            request.put("bluetooth_address", MainActivity.getSavingAccount().getBluetoothAddress());

            MainActivity.getSocket().emit("journeyRequest",request);
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

    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MainActivity.getSavingAccount().setBluetoothAddress(bluetoothAdapter.getAddress());

        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    private void setBluetoothDiscoverable(boolean enable) {
        if (enable) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivityForResult(discoverableIntent,REQUEST_DISCOVERABLE_CODE);
        }
        else {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            startActivity(discoverableIntent);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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


    private Emitter.Listener onJourneyAborted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Your journey has been aborted",Toast.LENGTH_LONG).show();
                    MainActivity.UpdateAccountData();
                    setBluetooth(false);
                }
            });
        }
    };

    private Emitter.Listener onJourneyCompleted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.UpdateAccountData();
                    JourneyCompleted();
                    ShowMyDialog("Info","Journey completed");
                    setBluetooth(false);
                }
            });
        }
    };

    private Emitter.Listener onJourneyRefused = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMyDialog("Info","Your account is blocked, please contact us at developer.navya@gmail.com");
                }
            });
        }
    };

    private Emitter.Listener onAndroidMission = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String protoData;

                    try {
                        protoData = data.getString("myData");
                        AndroidMission mission = AndroidMission.parseFrom(hexStringToByteArray(protoData));

                        DisplayJourneyData(mission.waitingTime, mission.duration, mission.distance);
                        Toast.makeText(getActivity(),"Journey accepted",Toast.LENGTH_LONG).show();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
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
                    try {
                        JSONObject data = (JSONObject) args[0];
                        (mNavyaMarkers.get(data.getString("myData"))).remove();
                        mNavyaMarkers.remove(data.getString("myData"));
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewShuttlePosition = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("onNewShuttlePosition","");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name;
                    String protoData;

                    try {
                        name = data.getString("emitter");
                        protoData = data.getString("myData");
                        Position pos = Position.parseFrom(hexStringToByteArray(protoData));

                        if (mNavyaMarkers.containsKey(name)) {
                            (mNavyaMarkers.get(name)).setPosition(new LatLng(pos.lat, pos.lng));
                        }
                        else if (mGoogleMap != null){
                            Marker tmp = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(pos.lat, pos.lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus))
                                    .title(name));
                            mNavyaMarkers.put(name,tmp);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (java.io.IOException e) {
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
                    Toast.makeText(getActivity(),"Your shuttle has arrived, please get on",Toast.LENGTH_LONG).show();

                    Vibrator v = (Vibrator) getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(500);
                }
            });
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


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

    private class ParserTask extends AsyncTask<String, Integer, SavingLine> {

        @Override
        protected SavingLine doInBackground(String... jsonData) {
            JSONObject jObject;
            SavingLine mResult = new SavingLine();
            try {
                jObject = new JSONObject(jsonData[0]);
                GoogleMapsJSONParser parser = new GoogleMapsJSONParser();
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
        protected void onPostExecute(SavingLine line) {
            PolylineOptions polyLineOptions = new PolylineOptions();

            polyLineOptions.addAll(line.getAllPoints());
            polyLineOptions.width(5);
            polyLineOptions.color(Color.BLUE);
            mLines.put(line.getLineName(),mGoogleMap.addPolyline(polyLineOptions));
        }

    }
}
