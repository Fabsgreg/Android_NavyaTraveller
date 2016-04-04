package navya.tech.navyatraveller.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregoire.frezet on 29/03/2016.
 */
public class MyDBHandler extends SQLiteOpenHelper {

    // columns of the line table
    public static final String TABLE_LINES = "lines";
    public static final String COLUMN_LINE_ID = "_id";
    public static final String COLUMN_LINE_NAME = "line_name";

    // columns of the station table
    public static final String TABLE_STATIONS = "stations";
    public static final String COLUMN_STATION_ID = "_id";
    public static final String COLUMN_STATION_NAME = "station_name";
    public static final String COLUMN_STATION_LAT = "station_lat";
    public static final String COLUMN_STATION_LNG = "station_lng";
    public static final String COLUMN_STATION_LINE_NAME = "line_name";

    private static final String DATABASE_NAME = "NavyaLines.db";
    private static final int DATABASE_VERSION = 1;

    // SQL statement of the employees table creation
    private static final String SQL_CREATE_TABLE_LINES = "CREATE TABLE " + TABLE_LINES + "("
            + COLUMN_LINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LINE_NAME + " TEXT NOT NULL "
            +");";

    // SQL statement of the companies table creation
    private static final String SQL_CREATE_TABLE_STATIONS = "CREATE TABLE " + TABLE_STATIONS + "("
            + COLUMN_STATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_STATION_NAME + " TEXT NOT NULL, "
            + COLUMN_STATION_LAT + " REAL NOT NULL, "
            + COLUMN_STATION_LNG + " REAL NOT NULL, "
            + COLUMN_STATION_LINE_NAME + " TEXT NOT NULL "
            +");";

    private String[] _AllColumnsLine = { COLUMN_LINE_ID, COLUMN_LINE_NAME };
    private String[] _AllColumnsStation = {COLUMN_STATION_ID, COLUMN_STATION_NAME, COLUMN_STATION_LAT, COLUMN_STATION_LNG, COLUMN_STATION_LINE_NAME};




    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_LINES);
        db.execSQL(SQL_CREATE_TABLE_STATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // clear all data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);

        // recreate the tables
        onCreate(db);
    }

    public void Reset() {
        SQLiteDatabase db = this.getWritableDatabase();

        // clear all data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);

        // recreate the tables
        onCreate(db);
    }

    public Line createLine(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LINE_NAME, name);

        long insertId = db.insert(TABLE_LINES, null, values);

        Cursor cursor = db.query(TABLE_LINES, _AllColumnsLine, COLUMN_LINE_ID + " = " + insertId, null, null, null, null);

        cursor.moveToFirst();
        Line newLine = cursorToLine(cursor);
        cursor.close();
        db.close();
        return newLine;
    }

    public void deleteLine(Line line) {
        SQLiteDatabase db = this.getWritableDatabase();
        String lineName = line.getName();

        // delete all stations of this companyline

        List<Station> listStations = this.getStationsOfLine(lineName);
        if (listStations != null && !listStations.isEmpty()) {
            for (Station e : listStations) {
                deleteStation(e);
            }
        }

        db = this.getWritableDatabase();
        db.delete(TABLE_LINES, COLUMN_LINE_ID + " = " + line.getId(), null);
        db.close();
    }


    public List<Line> getAllLines() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Line> listLines = new ArrayList<Line>();

        Cursor cursor = db.query(TABLE_LINES, _AllColumnsLine, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Line line = cursorToLine(cursor);
                listLines.add(line);
                cursor.moveToNext();
            }

            // make sure to close the cursor
            cursor.close();
        }
        db.close();
        return listLines;
    }

    public Line getLineByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_LINES, _AllColumnsLine, COLUMN_LINE_NAME + " = ?", new String[]{name}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Line line = cursorToLine(cursor);
        db.close();
        return line;
    }

    protected Line cursorToLine(Cursor cursor) {
        Line line = new Line();
        line.setId(cursor.getInt(0));
        line.setName(cursor.getString(1));
        return line;
    }

    /////// Station //////////

    public Station createStation(String stationName, float stationLat, float stationLng, String lineName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATION_NAME, stationName);
        values.put(COLUMN_STATION_LAT, stationLat);
        values.put(COLUMN_STATION_LNG, stationLng);
        values.put(COLUMN_STATION_LINE_NAME, lineName);

        long insertId = db.insert(TABLE_STATIONS, null, values);

        Cursor cursor = db.query(TABLE_STATIONS, _AllColumnsStation, COLUMN_STATION_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Station newStation = cursorToStation(cursor);
        cursor.close();
        db.close();
        return newStation;
    }

    public void deleteStation(Station station) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = station.getId();

        db.delete(TABLE_STATIONS, COLUMN_STATION_ID + " = " + id, null);
        db.close();
    }

    public List<Station> getAllStations() {
        List<Station> listStations = new ArrayList<Station>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_STATIONS, _AllColumnsStation, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Station station = cursorToStation(cursor);
            listStations.add(station);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        db.close();
        return listStations;
    }

    public List<Station> getStationsOfLine(String lineName) {
        List<Station> listStations = new ArrayList<Station>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_STATIONS, _AllColumnsStation, COLUMN_STATION_LINE_NAME + " = ?", new String[] { lineName }, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Station station = cursorToStation(cursor);
            listStations.add(station);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        db.close();
        return listStations;
    }

    public Station getStationByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_STATIONS, _AllColumnsStation, COLUMN_STATION_NAME + " = ?", new String[]{name}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Station station = cursorToStation(cursor);
        db.close();
        return station;
    }

    private Station cursorToStation(Cursor cursor) {
        Station station = new Station();
        station.setId(cursor.getInt(0));
        station.setStationName(cursor.getString(1));
        station.setLat(cursor.getFloat(2));
        station.setLng(cursor.getFloat(3));


        // get The line by name
        String lineName = cursor.getString(4);

        Line line = this.getLineByName(lineName);
        if (line != null)
            station.setLineName(line);

        return station;
    }

}

