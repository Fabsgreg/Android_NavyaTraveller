package navya.tech.navyatraveller.Databases;

import java.io.Serializable;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Station implements Serializable {

    private int _id;
    private String _name;
    private float _lat;
    private float _lng;
    private Line _line;

    public Station() {}

    public Station(String name, float _lat, float _lng) {
        this._name = name;
        this._lat = _lat;
        this._lng = _lng;
    }

    public long getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getStationName() {
        return _name;
    }

    public void setStationName(String _name) {
        this._name = _name;
    }

    public double getLat() {
        return _lat;
    }
    public void setLat(float _lat) {
        this._lat = _lat;
    }

    public double getLng() {
        return _lng;
    }
    public void setLng(float _lng) {
        this._lng = _lng;
    }

    public Line getLine() {
        return _line;
    }

    public void setLineName(Line _line) {
        this._line = _line;
    }

}
