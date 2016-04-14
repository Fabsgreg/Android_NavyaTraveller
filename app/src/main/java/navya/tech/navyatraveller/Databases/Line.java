package navya.tech.navyatraveller.Databases;

import java.io.Serializable;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Line implements Serializable {

    private int _id;
    private String _name;

    public Line() {}

    public long getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }
}
