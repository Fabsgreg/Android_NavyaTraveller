package navya.tech.navyatraveller.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.MyDBHandler;
import navya.tech.navyatraveller.Databases.Station;
import navya.tech.navyatraveller.HttpConnection;
import navya.tech.navyatraveller.PathJSONParser;
import navya.tech.navyatraveller.R;

/**
 * Created by gregoire.frezet on 24/03/2016.
 */
public class GmapFragment extends Fragment implements OnMapReadyCallback, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;

    private SlidingUpPanelLayout mLayout;

    private MyDBHandler mDBHandler;

    private Button startBouton;
    private Button endBouton;

    private FloatingActionButton fab;

    private TextView result;

    private saveResult mSaveResult;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gmap_fragment, container, false);

        mSaveResult = new saveResult();

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

        result = (TextView) v.findViewById(R.id.result_textView);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_button){
            startBouton.setBackgroundColor(0xff3464d0);
            endBouton.setBackgroundColor(0xff4977dc);
            mSaveResult.setIsStartSelected(true);
            mSaveResult.setIsEndSelected(false);
            if (mSaveResult.getStartStation().getStationName() != null) {
                result.setText(mSaveResult.getStartStation().getStationName());
            }
            else {
                result.setText("Pick a station");
            }
        }
        else if (v.getId() == R.id.end_button){
            startBouton.setBackgroundColor(0xff4977dc);
            endBouton.setBackgroundColor(0xff3464d0);
            mSaveResult.setIsStartSelected(false);
            mSaveResult.setIsEndSelected(true);
            if (mSaveResult.getEndStation().getStationName() != null) {
                result.setText(mSaveResult.getEndStation().getStationName());
            }
            else {
                result.setText("Pick a station");
            }
        }
        else if (v.getId() == R.id.fab) {
            if (mSaveResult.isGood()) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
            else {
                Context context = getActivity();
                AlertDialog ad = new AlertDialog.Builder(context).create();
                ad.setCancelable(false);
                ad.setTitle("Error");
                ad.setMessage("You must pick two different stations on the same line");
                ad.setButton(-1, "OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        }
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

        mLocationManager =  (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng coordinate = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
        mGoogleMap.animateCamera(cameraUpdate);

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
                    downloadTask.execute(url);
                }
                colorMarker += 30;
            }
        }



/*        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13));

        // Flat markers will rotate when the map is rotated,
        // and change perspective when the map is tilted.
        mGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_direction_arrow))
                .position(coordinate)
                .flat(true));
                //.rotation(245));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(coordinate)
                .zoom(13)
                .bearing(90)
                .build();

        // Animate the change in camera view over 2 seconds
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                2000, null);*/

        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));

        //googleMap.addMarker(new MarkerOptions().title("Hello Google Maps!").position(marker));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        if (mSaveResult.getIsStartSelected()) {
            mSaveResult.setStartStation(mDBHandler.getStationByName(marker.getSnippet()));
            result.setText(mSaveResult.getStartStation().getStationName());
        }
        else {
            mSaveResult.setEndStation(mDBHandler.getStationByName(marker.getSnippet()));
            result.setText(mSaveResult.getEndStation().getStationName());
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng coordinate = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onProviderDisabled(String provider){

    }

    @Override
    public void onProviderEnabled(String provider){

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }


    @Override
    public void onResume() {
        super.onResume();
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
        //mMapView.onDestroy();
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
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(5);
                polyLineOptions.color(Color.BLUE);
            }

            mGoogleMap.addPolyline(polyLineOptions);

        }
    }

    private class saveResult
    {
        private Station startStation;
        private Station endStation;
        private Line line;
        private boolean isStartSelected;
        private boolean isEndSelected;

        saveResult(){
            startStation = new Station();
            endStation = new Station();
            line = new Line();
            isStartSelected = true;
            isEndSelected = false;
        }

        boolean isGood () {
            if ((startStation.getStationName() == null) || endStation.getStationName() == null) {
                return false;
            }
            else {
                if (startStation.getLine().getName().equalsIgnoreCase(endStation.getLine().getName())) {
                    if (!startStation.getStationName().equalsIgnoreCase(endStation.getStationName())) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        }

        void setIsStartSelected (boolean _state) {
            isStartSelected = _state;
        }

        boolean getIsStartSelected () {
            return isStartSelected;
        }

        void setIsEndSelected (boolean _state) {
            isEndSelected = _state;
        }

        boolean getIsEndSelected () {
            return isEndSelected;
        }

        void setStartStation (Station _station) {
            startStation = _station;
        }

        Station getStartStation () {
            return startStation;
        }

        void setEndStation (Station _station) {
            endStation = _station;
        }

        Station getEndStation () {
            return endStation;
        }

        void setLine (Line _line) {
            line = _line;
        }
    }
}
