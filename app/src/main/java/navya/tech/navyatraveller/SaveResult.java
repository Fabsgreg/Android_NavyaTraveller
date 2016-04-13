package navya.tech.navyatraveller;

import navya.tech.navyatraveller.Databases.Line;
import navya.tech.navyatraveller.Databases.Station;

/**
 * Created by gregoire.frezet on 08/04/2016.
 */
public class SaveResult {
    private Station startStation;
    private Station endStation;
    private Line line;
    private boolean isStartSelected;
    private boolean isEndSelected;
    private boolean isTravelling;
    private int currentIndexOfSavedLine;

    private boolean wasGmap;
    private boolean wasQRcode;
    private boolean wasGo;

    private String stationScanned;
    private String phoneNumber;

    public SaveResult(){
        startStation = new Station();
        endStation = new Station();
        line = new Line();
        isStartSelected = true;
        isEndSelected = false;
        isTravelling = false;
        currentIndexOfSavedLine = 0;
        wasGmap = false;
        wasQRcode = false;
        wasGo = false;
        stationScanned = "";
        phoneNumber = "";
    }

    public void Reset() {
        startStation = new Station();
        endStation = new Station();
        line = new Line();
        isStartSelected = true;
        isEndSelected = false;
        isTravelling = false;
        currentIndexOfSavedLine = 0;
        stationScanned = "";
    }

    public boolean isGood () {
        if ((startStation.getStationName() == null) || endStation.getStationName() == null) {
            return false;
        }
        else {
            if (startStation.getLine().getName().equalsIgnoreCase(endStation.getLine().getName())) {
                if (!startStation.getStationName().equalsIgnoreCase(endStation.getStationName())) {
                    isTravelling = true;
                    line = startStation.getLine();
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

    public void setPreviousFragment (String name) {
        if (name.equalsIgnoreCase("Map")) {
            wasGmap = true;
            wasQRcode = false;
            wasGo = false;
        }
        else if (name.equalsIgnoreCase("QR code")) {
            wasGmap = false;
            wasQRcode = true;
            wasGo = false;
        }
        else if (name.equalsIgnoreCase("Go")) {
            wasGmap = false;
            wasQRcode = false;
            wasGo = true;
        }
    }

    public Line getLine () {
        return line;
    }

    public void setIsStartSelected (boolean _state) {
        isStartSelected = _state;
    }

    public boolean getIsStartSelected () {
        return isStartSelected;
    }

    public void setIsEndSelected (boolean _state) {
        isEndSelected = _state;
    }

    public boolean getIsEndSelected () {
        return isEndSelected;
    }

    public void setStartStation (Station _station) {
        startStation = _station;
    }

    public Station getStartStation () {
        return startStation;
    }

    public void setEndStation (Station _station) {
        endStation = _station;
    }

    public Station getEndStation () {
        return endStation;
    }

    public void setLine (Line _line) {
        line = _line;
    }

    public void setIsTravelling (boolean _state) { isTravelling = _state; }

    public boolean getIsTravelling () { return isTravelling; }

    public void setIndex (Integer _index) {
        currentIndexOfSavedLine = _index;
    }

    public Integer getIndex () {
        return  currentIndexOfSavedLine;
    }

    public void setWasGmap (boolean _state) { wasGmap = _state; }

    public boolean getWasGmap () { return wasGmap; }

    public void setWasQRcode (boolean _state) { wasQRcode = _state; }

    public boolean getWasQRcode () { return wasQRcode; }

    public void setWasGo (boolean _state) { wasGo = _state; }

    public boolean getWasGo () { return wasGo; }

    public void setStationScanned (String _name) { stationScanned = _name; }

    public String getStationScanned () { return stationScanned; }

    public void setPhoneNumber (String _number) { phoneNumber = _number; }

    public String getPhoneNumber () { return phoneNumber; }
}
