package de.hannes.strafenkatalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hannes on 26.07.14.
 */
public class SQLAgent_SpStRel extends SQLiteOpenHelper{

    public static final String DB_SPST = "SpStRelDB";

    public SQLAgent_SpStRel(Context context){
        super(context, DB_SPST, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase datenbank) {
        Log.w("DEBUG_HANNES", "onCreateSQLAgent");
        datenbank.execSQL("CREATE TABLE IF NOT EXISTS SpStRel" + "(id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, spieler TEXT, strafe INTEGER, tag INTEGER, monat INTEGER, jahr INTEGER, grund TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS SpStRel");
        onCreate(db);
    }

    public void relationEinfügen(String spieler, Integer strafe, Integer tag, Integer monat, Integer jahr, String grund){
        Log.w("DEBUG_HANNES", "EinfügenSQLAgentSpSt");

        SQLiteDatabase datenbank = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("spieler", spieler);
        contentValues.put("strafe", strafe);
        contentValues.put("tag", tag);
        contentValues.put("monat", monat);
        contentValues.put("jahr", jahr);
        contentValues.put("grund", grund);

        datenbank.insert("SpStRel", null, contentValues);

    }

    public Cursor relationFinden(Integer id) {
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT * FROM SpStRel WHERE id="+id, null );
        return res;
    }

    public void updateRelation(Integer id, Integer tag, Integer monat, Integer jahr, String grund) {
        SQLiteDatabase datenbank = this.getWritableDatabase();
        datenbank.execSQL("UPDATE SpStRel SET tag="+tag+", monat ="+monat+", jahr="+jahr+", grund='"+grund+"' WHERE id="+id);
    }

    public void relationLöschen(String spieler, Integer strafe){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        //datenbank.execSQL("DELETE FROM SpStRel WHERE strafe (SELECT strafe FROM SpStRel WHERE spieler='"+spieler+"' AND strafe="+strafe.toString()+" ORDER BY tag ASC LIMIT 1)");
        datenbank.execSQL("DELETE FROM SpStRel WHERE id = (SELECT MAX(id) FROM SpStRel WHERE spieler='"+spieler+"' AND strafe ="+strafe.toString()+")");
        //                "DELETE FROM Autotabelle where _id IN (SELECT _id FROM Autotabelle WHERE AutoID=" + autoid + " and GesehenDatum=" + datum + " LIMIT 1)"

    }
    public void relationLöschen(Integer id){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        //datenbank.execSQL("DELETE FROM SpStRel WHERE strafe (SELECT strafe FROM SpStRel WHERE spieler='"+spieler+"' AND strafe="+strafe.toString()+" ORDER BY tag ASC LIMIT 1)");
        datenbank.execSQL("DELETE FROM SpStRel WHERE id = "+id);

    }

    public void strafenLöschen(Integer strafe){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("DELETE FROM SpStRel WHERE strafe ="+strafe.toString());
    }

    public void spielerLöschen(String spieler){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("DELETE FROM SpStRel WHERE spieler ='"+spieler+"'");
    }

    public void spielerÄndern(String spielerAlt, String spielerNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("UPDATE SpStRel SET spieler = '"+spielerNeu+"' WHERE spieler ='"+spielerAlt+"'");
    }
    public void resetDB(){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        onUpgrade(datenbank, 1, 1);
    }

    public Cursor getAllStrafen(String spieler) {
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT * FROM SpStRel WHERE spieler='"+spieler+"'", null );
        /*if(res!=null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                array_list.add(res.getString(res.getColumnIndex("strafe")));
                res.moveToNext();
            }
            res.close();
        }*/
        return res;
    }

    public void strafenDBLeeren(){
        SQLiteDatabase datenbank = this.getWritableDatabase();
        datenbank.execSQL("DELETE FROM SpStRel");
    }
/*
    public ArrayList getAllStrafenNamen() {
        ArrayList array_list = new ArrayList<String>();
        //hp = new HashMap();
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT * FROM Strafen", null );
        if(res!=null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                array_list.add(res.getString(res.getColumnIndex("name")));
                res.moveToNext();
            }
            res.close();
        }
        return array_list;
    }

    public void updateStrafe(String nameAlt, String nameNeu, Float faktorNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("UPDATE Strafen SET name ='"+nameNeu+"' , faktor ="+faktorNeu.toString()+" WHERE name='"+nameAlt+"'");
    }
    public void updateStrafenIDs(){
        SQLiteDatabase datenbank = this.getWritableDatabase();
        ArrayList arrayList = new ArrayList<String>();
        arrayList = getAllStrafenNamen();
        for (int i=0; i<arrayList.size(); i++){
            String strafe = arrayList.get(i).toString();
            datenbank.execSQL("UPDATE Strafen SET ID=" + String.valueOf(i+1) + " WHERE name='" + strafe + "'");
        }
    }*/
}
