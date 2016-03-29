package navya.tech.navyatraveller.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gregoire.frezet on 25/03/2016.
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
            + COLUMN_STATION_LINE_NAME + " TEXT NOT NULL "
            +");";

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_TABLE_LINES);
        database.execSQL(SQL_CREATE_TABLE_STATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // clear all data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);

        // recreate the tables
        onCreate(db);
    }

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


}
