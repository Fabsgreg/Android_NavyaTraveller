package navya.tech.navyatraveller.Databases;

import java.io.Serializable;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Station implements Serializable {

    private Long id;
    private String name;
    private Double lat;
    private Double lng;
    private Line line;

    public Station() {}


    // Getter & Setter
    public Long getId() {
        return id;
    }

    public String getStationName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Line getLine() {
        return line;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStationName(String name) {
        this.name = name;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setLineName(Line line) {
        this.line = line;
    }

}
