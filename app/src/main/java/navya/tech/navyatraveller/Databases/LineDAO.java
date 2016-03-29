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
public class LineDAO {

    // Database fields
    private SQLiteDatabase _Database;
    private MyDBHandler _MyDBHandler;
    private Context _Context;
    private String[] _AllColumns = { MyDBHandler.COLUMN_LINE_ID, MyDBHandler.COLUMN_LINE_NAME };

    public LineDAO(Context context) {
        this._Context = context;
        _MyDBHandler = new MyDBHandler(context, null, null, 1);

        // open the database
        try {
            open();
        } catch (SQLException e) {
            int a;
            Log.e("LineDAO", "SQLException on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        _Database = _MyDBHandler.getWritableDatabase();
        int a;
    }

    public void close() {
        _MyDBHandler.close();
    }

    public Line createLine(String name) {
        ContentValues values = new ContentValues();
        values.put(MyDBHandler.COLUMN_LINE_NAME, name);

        long insertId = _Database.insert(MyDBHandler.TABLE_LINES, null, values);

        Cursor cursor = _Database.query(MyDBHandler.TABLE_LINES, _AllColumns, MyDBHandler.COLUMN_LINE_ID + " = " + insertId, null, null, null, null);

        cursor.moveToFirst();
        Line newLine = cursorToLine(cursor);
        cursor.close();
        return newLine;
    }

    public void deleteCompany(Line line) {
        String lineName = line.getName();
        // delete all employees of this company
        StationDAO stationDao = new StationDAO(_Context);
        List<Station> listStations = stationDao.getStationsOfLine(lineName);
        if (listStations != null && !listStations.isEmpty()) {
            for (Station e : listStations) {
                stationDao.deleteStation(e);
            }
        }

        _Database.delete(MyDBHandler.TABLE_LINES, MyDBHandler.COLUMN_LINE_NAME + " = " + lineName, null);
    }

    public List<Line> getAllCompanies() {
        List<Line> listLines = new ArrayList<Line>();

        Cursor cursor = _Database.query(MyDBHandler.TABLE_LINES, _AllColumns, null, null, null, null, null);
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
        return listLines;
    }

    public Line getLineByName(String name) {
        Cursor cursor = _Database.query(MyDBHandler.TABLE_LINES, _AllColumns, MyDBHandler.COLUMN_LINE_NAME + " = ?", new String[] { name }, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Line line = cursorToLine(cursor);
        return line;
    }

    protected Line cursorToLine(Cursor cursor) {
        Line line = new Line();
        line.setId(cursor.getInt(0));
        line.setName(cursor.getString(1));
        return line;
    }

}
