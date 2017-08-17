package de.hannes.strafenkatalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hannes on 26.07.14.
 */
public class SQLAgent_Strafen extends SQLiteOpenHelper{

    public static final String DB_STRAFEN = "StrafenDB";

    public SQLAgent_Strafen(Context context){
        super(context, DB_STRAFEN, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase datenbank) {
        Log.w("DEBUG_HANNES", "onCreateSQLAgent");
        datenbank.execSQL("CREATE TABLE IF NOT EXISTS Strafen" + "(id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name TEXT, faktor FLOAT)");
        //TODO: IDs anpassen (keine ID oder ID nach Position???)

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Strafen");
        onCreate(db);
    }

    public void strafeEinfügen(String name, Float faktor){
        Log.w("DEBUG_HANNES", "EinfügenSQLAgentStrafe");

        SQLiteDatabase datenbank = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        contentValues.put("faktor", faktor);

        datenbank.insert("Strafen", null, contentValues);

    }

    public Integer strafeIdFinden(String name){
            SQLiteDatabase datenbank = this.getReadableDatabase();

        Cursor res =  datenbank.rawQuery( "SELECT id, name FROM Strafen WHERE name='"+name+"'", null );
        ArrayList strafenListe = getAllStrafenNamen();
        int id = 0;

        if (res != null && strafenListe.contains(name) && res.getCount()!=0) {
            res.moveToFirst();
            id = res.getInt(res.getColumnIndex("id"));
            res.close();
            return id;
        }
        return -1;
    }

    public String strafeNameFinden(Integer id){
        SQLiteDatabase datenbank = this.getReadableDatabase();

        Cursor res =  datenbank.rawQuery("SELECT * FROM Strafen WHERE id="+id, null);
        String name;

        if (res != null && res.getCount()!=0) {
            res.moveToFirst();
            name = res.getString(res.getColumnIndex("name"));
            res.close();
            return name;
        }
        return "";
    }

    public Float strafeFaktorFinden(String name){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT id, name, faktor FROM Strafen WHERE name='"+name+"'", null );
        ArrayList strafenListe = getAllStrafenNamen();
        float faktor = 0f;

        if (res != null && strafenListe.contains(name) && res.getCount()!=0) {
            res.moveToFirst();
            faktor = res.getFloat(res.getColumnIndex("faktor"));
            res.close();
        }
        return faktor;
    }
    public Float strafeFaktorFinden(Integer id){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT faktor FROM Strafen WHERE id="+Integer.toString(id)+"", null );
        float faktor = 0f;

        if (res != null && res.getCount()!=0)  {
            res.moveToFirst();
            faktor = res.getFloat(res.getColumnIndex("faktor"));
            res.close();
        }
        return faktor;
        /*if (res != null)
            res.moveToFirst();

        return res.getFloat(res.getColumnIndex("faktor"));*/
    }


    public void strafeLöschen(Integer id){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("DELETE FROM Strafen WHERE id ="+String.valueOf(id));
    }

    public void strafeLöschen(String name){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        int id = strafeIdFinden(name);
        datenbank.execSQL("DELETE FROM Strafen WHERE id ="+String.valueOf(id));
    }

    public void strafeÄndern(String nameAlt, String nameNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        int id = strafeIdFinden(nameAlt);
        datenbank.execSQL("UPDATE Strafen SET name ='"+nameNeu+"' WHERE id ="+String.valueOf(id));
    }

    public void strafeÄndern(String name, Float faktorNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        int id = strafeIdFinden(name);
        datenbank.execSQL("UPDATE Strafen SET faktor ='"+faktorNeu+"' WHERE id ="+String.valueOf(id));
    }


    public void strafenDBLeeren(){
        SQLiteDatabase datenbank = this.getWritableDatabase();
        datenbank.execSQL("DELETE FROM Strafen");
    }

    public ArrayList getAllStrafenNamen() {
        ArrayList array_list = new ArrayList<String>();
        //hp = new HashMap();
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery( "SELECT * FROM Strafen", null );
        if(res!=null && res.getCount()!=0) {
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
    }
}
