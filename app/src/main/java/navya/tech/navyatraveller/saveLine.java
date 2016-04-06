package navya.tech.navyatraveller;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregoire.frezet on 05/04/2016.
 */
public class SaveLine {
    private List<LatLng> points;
    private List<Double> distance;           // in meter
    private List<Double> duration;          // in minute
    private List<Integer> waypointOrder;
    private String lineName;
    private List<String> stationsName;

    public SaveLine() {
        points =  new ArrayList<LatLng>();
        distance = new ArrayList<Double>();
        duration = new ArrayList<Double>();
        waypointOrder = new ArrayList<Integer>();
        lineName = null;
        stationsName = new ArrayList<String>();
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
        int index = waypointOrder.indexOf(0);
        distance.remove(0.0);
        duration.remove(0.0);
        return;
    }

    public void addPoint (LatLng _point) {
        points.add(_point);
    }

    public List<LatLng> getAllPoints () {
        return points;
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
