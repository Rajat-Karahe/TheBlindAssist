package com.example.theblindassist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table History(sNo INTEGER primary key AUTOINCREMENT, Date TEXT,time TEXT,denom TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists History");
    }

    public Boolean insertuserdata(String date, String time, String denom)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("denom", denom);
        long result=DB.insert("History", null, contentValues);
        if(result==-1){
            return false;
        }else{
            return true;
        }
    }


    public Boolean updateuserdata(String name, String contact, String dob) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("contact", contact);
        contentValues.put("dob", dob);
        Cursor cursor = DB.rawQuery("Select * from History where date = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            long result = DB.update("History", contentValues, "date=?", new String[]{name});
            cursor.close();
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }}

//delete this when not required

    public Boolean deletedata(String name)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Userdetails where name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            long result = DB.delete("History", "date=?", new String[]{name});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public Cursor getdata ()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from History ORDER BY sNo DESC", null);
        return cursor;
    }


    public void deleteLastTable(){
        SQLiteDatabase DB = this.getWritableDatabase();

        Cursor cursor = DB.query("History", null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            String rowId = cursor.getString(cursor.getColumnIndex("sNo"));
            DB.delete("History", "sNo" + "=?",  new String[]{rowId});
        }
        cursor.close();
    }
}
