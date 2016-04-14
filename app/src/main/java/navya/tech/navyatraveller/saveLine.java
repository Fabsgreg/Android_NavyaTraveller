package navya.tech.navyatraveller;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregoire.frezet on 05/04/2016.
 */
public class SaveLine {
    private List<Double> distance;          // in meter
    private List<Double> duration;          // in minute
    private String lineName;
    private List<String> stationsName;
    private List<List<LatLng>> route;

    public SaveLine() {
        distance = new ArrayList<>();
        duration = new ArrayList<>();
        lineName = null;
        stationsName = new ArrayList<>();
        route = new ArrayList<>();
    }

    public List<LatLng> getPathPoints (String startStation, String endStation) {
        List<LatLng> tmp = new ArrayList<>();
        int indexStart = stationsName.indexOf(startStation);
        int indexEnd = stationsName.indexOf(endStation);

        if (indexStart < indexEnd) {
            for (int i=indexStart; i<indexEnd; i++) {
                tmp.addAll(route.get(i));
            }
        }
        else {
            for (int i=indexEnd; i<indexStart; i++) {
                tmp.addAll(route.get(i));
            }
        }
        return tmp;
    }

    public void addPointOnRoute (Integer step, LatLng point) {
        try  {
            route.get(step).add(point);
        }
        catch (Exception e) {
            route.add(new ArrayList<LatLng>());
            route.get(step).add(point);
        }
    }

    public double getTotalDistance(String startStation, String endStation) {
        double result = 0.0;
        int indexStart = stationsName.indexOf(startStation);
        int indexEnd = stationsName.indexOf(endStation);

        if (indexStart < indexEnd) {
            for (int i=indexStart; i<indexEnd; i++) {
                result += distance.get(i);
            }
        }
        else {
            for (int i=indexEnd; i<indexStart; i++) {
                result += distance.get(i);
            }
        }
        return result;
    }

    public double getTotalDuration(String startStation, String endStation) {
        double result = 0.0;
        int indexStart = stationsName.indexOf(startStation);
        int indexEnd = stationsName.indexOf(endStation);

        if (indexStart < indexEnd) {
            for (int i=indexStart; i<indexEnd; i++) {
                result += duration.get(i);
            }
        }
        else {
            for (int i=indexEnd; i<indexStart; i++) {
                result += duration.get(i);
            }
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
        List<LatLng> tmp = new ArrayList<>();
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
}
