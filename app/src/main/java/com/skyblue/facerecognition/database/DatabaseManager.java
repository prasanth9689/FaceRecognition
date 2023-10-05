package com.skyblue.facerecognition.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "saneforce_att";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "face_recognition";
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String EMP_ID_COL = "employee_id";
    private static final String EMP_FACE_REGISTER_DATE_COL = "emp_face_register_date";
    private static final String EMP_FACE_REGISTER_TIME_COL = "emp_face_register_time";
    private static final String IMAGE_VECTOR_DATA_COL = "image_vector_data";
    private static final String IMAGE_BASE64_DATA_COL = "image_base64_array";

    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + EMP_ID_COL + " TEXT,"
                + EMP_FACE_REGISTER_DATE_COL + " TEXT,"
                + EMP_FACE_REGISTER_TIME_COL + " TEXT,"
                + IMAGE_VECTOR_DATA_COL + " TEXT,"
                + IMAGE_BASE64_DATA_COL + " TEXT)";
        db.execSQL(query);
    }

    public void registerNewFace(String name,
                                String vector_image_data,
                                String base_64image_data,
                                String employee_id,
                                String face_register_date,
                                String face_register_time){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NAME_COL, name);
        values.put(IMAGE_VECTOR_DATA_COL, vector_image_data);
        values.put(IMAGE_BASE64_DATA_COL, base_64image_data);
        values.put(EMP_ID_COL, employee_id);
        values.put(EMP_FACE_REGISTER_DATE_COL, face_register_date);
        values.put(EMP_FACE_REGISTER_TIME_COL, face_register_time);

        db.insert(TABLE_NAME, null, values);
     //   db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
