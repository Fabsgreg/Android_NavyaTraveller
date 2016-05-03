package navya.tech.navyatraveller;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.Station;

/**
 * Created by gregoire.frezet on 08/04/2016.
 */

public class SavingResult {
    private Station startStation;
    private Station endStation;
    private Line line;
    private Boolean isStartSelected;
    private Boolean isTravelling;
    private Integer currentIndexOfSavedLine;
    private Boolean isGmap;
    private Boolean isQRcode;
    private Boolean isGo;
    private String stationScanned;


    public SavingResult(){
        startStation = new Station();
        endStation = new Station();
        line = new Line();
        isStartSelected = true;
        isTravelling = false;
        currentIndexOfSavedLine = 0;
        isGmap = false;
        isQRcode = false;
        isGo = false;
        stationScanned = "";
    }

    public void Reset() {
        startStation = new Station();
        endStation = new Station();
        line = new Line();
        isStartSelected = true;
        isTravelling = false;
        currentIndexOfSavedLine = 0;
        stationScanned = "";
    }

    public boolean isGood () {

        if ((startStation.getStationName() != null) || endStation.getStationName() != null) {
            if (startStation.getLine().getName().equalsIgnoreCase(endStation.getLine().getName())) {
                if (!startStation.getStationName().equalsIgnoreCase(endStation.getStationName())) {
                    line = startStation.getLine();
                    return true;
                }
            }
        }
        return false;
    }

    public void setPreviousFragment (String name) {
        if (name.equalsIgnoreCase("Map")) {
            isGmap = true;
            isQRcode = false;
            isGo = false;
        }
        else if (name.equalsIgnoreCase("QR code")) {
            isGmap = false;
            isQRcode = true;
            isGo = false;
        }
        else if (name.equalsIgnoreCase("Go")) {
            isGmap = false;
            isQRcode = false;
            isGo = true;
        }
    }


    // Getter & Setter
    public void setStartSelected(Boolean startSelected) {
        this.isStartSelected = startSelected;
    }

    public void setStartStation (Station startStation) {
        this.startStation = startStation;
    }

    public void setEndStation (Station endStation) {
        this.endStation = endStation;
    }

    public void setLine (Line line) {
        this.line = line;
    }

    public void setTravelling(Boolean travelling) { this.isTravelling = travelling; }

    public void setCurrentIndexOfSavedLine(Integer currentIndexOfSavedLine) { this.currentIndexOfSavedLine = currentIndexOfSavedLine; }

    public void setStationScanned (String stationScanned) { this.stationScanned = stationScanned; }

    public Line getLine () {
        return line;
    }

    public Boolean getStartSelected() {
        return isStartSelected;
    }

    public Station getStartStation () {
        return startStation;
    }

    public Station getEndStation () {
        return endStation;
    }

    public Boolean getTravelling() { return isTravelling; }

    public Integer getIndex () {
        return  currentIndexOfSavedLine;
    }

    public Boolean getGmap() { return isGmap; }

    public Boolean getQRcode() { return isQRcode; }

    public Boolean getGo() { return isGo; }

    public String getStationScanned () { return stationScanned; }
}
