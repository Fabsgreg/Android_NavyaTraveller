package navya.tech.navyatraveller;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import navya.tech.navyatraveller.Databases.Station;

/**
 * Created by gregoire.frezet on 05/04/2016.
 */
public class SaveLine {
    private List<Double> distance;          // in meter
    private List<Double> duration;          // in minute
    private List<Integer> waypointOrder;
    private String lineName;
    private List<String> stationsName;
    private List<List<LatLng>> route;

    public SaveLine() {
        distance = new ArrayList<Double>();
        duration = new ArrayList<Double>();
        waypointOrder = new ArrayList<Integer>();
        lineName = null;
        stationsName = new ArrayList<String>();
        route = new ArrayList<List<LatLng>>();
    }

    public void addPointOnRoute (Integer step, LatLng point) {
        try  {
            route.get(step).add(point);
        }
        catch (Exception e) {
            route.add(new ArrayList<LatLng>());
            route.get(step).add(point);
        }
        return;
    }

    public double getTotalDistance(String startStation, String endStation) {
        double result = 0.0;
        int indexStart = stationsName.indexOf(startStation);
        int indexEnd = stationsName.indexOf(endStation);

        for (int i=indexStart; i<(indexEnd); i++) {
            result += distance.get(i);
        }

        return result;
    }

    public double getTotalDuration(String startStation, String endStation) {
        double result = 0.0;
        int indexStart = stationsName.indexOf(startStation);
        int indexEnd = stationsName.indexOf(endStation);

        for (int i=indexStart; i<(indexEnd); i++) {
            result += duration.get(i);
        }

        return result;
    }

    public void addStationName (String _name) {
        stationsName.add(_name);
    }

    public void setLineName (String _name) {
        lineName = _name;
    }

    public String getLineName () {
        return lineName;
    }

    public void updateResult () {
        distance.remove(0.0);
        duration.remove(0.0);
        for (int i=0; i < route.size(); i++) {
            if (route.get(i).size() == 1) {
                route.remove(i);
            }
        }
    }

    public List<LatLng> getAllPoints () {
        List<LatLng> tmp = new ArrayList<LatLng>();
        for (int i=0; i<route.size(); i++) {
            tmp.addAll(route.get(i));
        }
        return tmp;
    }

    public void addDuration (Integer _duration) {
        duration.add(_duration.doubleValue() / 60.0);
    }

    public void addDistance (Integer _distance) {
        distance.add(_distance.doubleValue() / 1000.0);
    }

    public void addWaypoint (Integer _waypoint) {
        waypointOrder.add(_waypoint);
    }
}
