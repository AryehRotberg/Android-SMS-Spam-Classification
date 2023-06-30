package com.example.smsspamclassification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private final Context context;
    private static final String DATABASE_NAME = "spam_collection.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "spam_collection";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_BODY = "body";
    private static final String COLUMN_CLASS = "class";
    private static final String COLUMN_CONFIDENCE = "confidence";

    public DatabaseHelper(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_SENDER + " TEXT, " +
                COLUMN_BODY + " TEXT, " +
                COLUMN_CLASS + " TEXT, " +
                COLUMN_CONFIDENCE + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addRecord(String sender, String messageBody, String classType, String confidence)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_SENDER, sender);
        cv.put(COLUMN_BODY, messageBody);
        cv.put(COLUMN_CLASS, classType);
        cv.put(COLUMN_CONFIDENCE, confidence);

        long result = db.insert(TABLE_NAME,null, cv);

        if (result == -1)
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
    }

    public Cursor getData()
    {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null)
            cursor = db.rawQuery(query, null);

        return cursor;
    }

    void deleteRow(String messageBody)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME, COLUMN_BODY + "=?", new String[]{messageBody});

        if (result == -1)
            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
    }
}
