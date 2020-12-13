package com.example.myheartportal.patient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PatientDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "patient.db";
    private static final String TABLE_NAME = "patient_table";
    private static final String ID = "ID";
    private static final String FIRST_NAME = "FIRST_NAME";
    private static final String MIDDLE_NAME = "MIDDLE_NAME";
    private static final String LAST_NAME = "LAST_NAME";
    private static final String CONTACT_NUMBER_1 = "CONTACT_NUMBER1";
    private static final String CONTACT_NUMBER_2 = "CONTACT_NUMBER2";
    private static final String CONTACT_NUMBER_3 = "CONTACT_NUMBER3";


    public PatientDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(ID INTEGER UNIQUE, FIRST_NAME TEXT, MIDDLE_NAME TEXT, LAST_NAME TEXT, " +
                "CONTACT_NUMBER1 TEXT, CONTACT_NUMBER2 TEXT, CONTACT_NUMBER3 TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(int id, String first_name, String middle_name, String last_name,
                              String contact_number1, String contact_number2, String contact_number3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(FIRST_NAME, first_name);
        contentValues.put(MIDDLE_NAME, middle_name);
        contentValues.put(LAST_NAME, last_name);
        contentValues.put(CONTACT_NUMBER_1, contact_number1);
        contentValues.put(CONTACT_NUMBER_2, contact_number2);
        contentValues.put(CONTACT_NUMBER_3, contact_number3);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return result;
    }

    public boolean updateData(int id, String first_name, String middle_name, String last_name,
                              String contact_number1, String contact_number2, String contact_number3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(FIRST_NAME, first_name);
        contentValues.put(MIDDLE_NAME, middle_name);
        contentValues.put(LAST_NAME, last_name);
        contentValues.put(CONTACT_NUMBER_1, contact_number1);
        contentValues.put(CONTACT_NUMBER_2, contact_number2);
        contentValues.put(CONTACT_NUMBER_3, contact_number3);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{String.valueOf(id)});

        return true;
    }

    public Integer deleteData (int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
    }
}
