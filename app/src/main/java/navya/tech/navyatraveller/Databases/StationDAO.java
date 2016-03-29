package navya.tech.navyatraveller.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class StationDAO {

    private Context _Context;

    // Database fields
    private SQLiteDatabase _Database;
    private MyDBHandler _MyDBHandler;
    private String[] _AllColumns = {MyDBHandler.COLUMN_STATION_ID, MyDBHandler.COLUMN_STATION_LINE_NAME, MyDBHandler.COLUMN_STATION_NAME};

    public StationDAO(Context context) {
        _MyDBHandler = new MyDBHandler(context);
        this._Context = context;

        // open the database
        try {
            open();
        } catch (SQLException e) {
            // catch code
        }
    }

    public void open() throws SQLException {
        _Database = _MyDBHandler.getWritableDatabase();
    }

    public void close() {
        _MyDBHandler.close();
    }

    public Station createStation(String stationName, String lineName) {
        ContentValues values = new ContentValues();
        values.put(MyDBHandler.COLUMN_STATION_NAME, stationName);
        values.put(MyDBHandler.COLUMN_STATION_LINE_NAME, lineName);

        long insertId = _Database.insert(MyDBHandler.TABLE_STATIONS, null, values);

        Cursor cursor = _Database.query(MyDBHandler.TABLE_STATIONS, _AllColumns, MyDBHandler.COLUMN_STATION_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Station newStation = cursorToStation(cursor);
        cursor.close();
        return newStation;
    }

    public void deleteStation(Station station) {
        long id = station.getId();

        _Database.delete(MyDBHandler.TABLE_STATIONS, MyDBHandler.COLUMN_STATION_ID + " = " + id, null);
    }

    public List<Station> getAllStations() {
        List<Station> listStations = new ArrayList<Station>();

        Cursor cursor = _Database.query(MyDBHandler.TABLE_STATIONS, _AllColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Station station = cursorToStation(cursor);
            listStations.add(station);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listStations;
    }

    public List<Station> getStationsOfLine(String lineName) {
        List<Station> listStations = new ArrayList<Station>();

        Cursor cursor = _Database.query(MyDBHandler.TABLE_STATIONS, _AllColumns, MyDBHandler.COLUMN_STATION_LINE_NAME + " = ?", new String[] { lineName }, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Station station = cursorToStation(cursor);
            listStations.add(station);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listStations;
    }

    private Station cursorToStation(Cursor cursor) {
        Station station = new Station();
        station.setId(cursor.getInt(0));
        station.setSationName(cursor.getString(1));


        // get The line by name
        String stationName = cursor.getString(2);

        LineDAO dao = new LineDAO(_Context);
        Line line = dao.getLineByName(stationName);
        if (line != null)
            station.setLineName(line);

        return station;
    }

}