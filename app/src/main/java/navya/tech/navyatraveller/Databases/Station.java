package navya.tech.navyatraveller.Databases;

import java.io.Serializable;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Station implements Serializable {

    private int _id;
    private String _name;
    private Line _line;

    public Station() {}

    public Station(String name) {
        this._name = name;
    }

    public long getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getSationName() {
        return _name;
    }

    public void setStationName(String _name) {
        this._name = _name;
    }

    public Line getLine() {
        return _line;
    }

    public void setLineName(Line _line) {
        this._line = _line;
    }

}
