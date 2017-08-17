package de.hannes.strafenkatalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hannes on 26.07.14.
 */
public class SQLAgent_Spieler extends SQLiteOpenHelper{

    public static final String DB_SPIELER = "SpielerDB";

    public SQLAgent_Spieler(Context context){
        super(context, DB_SPIELER, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase datenbank) {
        Log.w("DEBUG_HANNES", "onCreateSQLAgent");
        datenbank.execSQL("CREATE TABLE IF NOT EXISTS Spieler" + "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        //TODO: IDs anpassen (keine ID oder ID nach Position???)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Spieler");
        db.execSQL("DROP TABLE IF EXISTS Spieler_old");
        onCreate(db);
    }

    public void spielerEinfügen(String name){
        Log.w("DEBUG_HANNES", "EinfügenSQLAgentSpieler");

        SQLiteDatabase datenbank = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);

        datenbank.insert("Spieler", null, contentValues);

    }

    public Integer spielerIdFinden(String name){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        Cursor res =  datenbank.rawQuery("SELECT id, name FROM Spieler WHERE name='"+name+"'", null);
        ArrayList spielerListe = getAllSpielerNamen();
        int id = 0;

        if (res != null && spielerListe.contains(name) && res.getCount()!=0) {
            res.moveToFirst();
            id = res.getInt(res.getColumnIndex("id"));
            res.close();
        }
        return id;
    }


    public void spielerLöschen(Integer id){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        datenbank.execSQL("DELETE FROM Spieler WHERE id ="+String.valueOf(id));
    }

    public void spielerLöschen(String name){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        int id = spielerIdFinden(name);
        datenbank.execSQL("DELETE FROM Spieler WHERE id =" + String.valueOf(id));
    }

    public void spielerÄndern(String nameAlt, String nameNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();

        int id = spielerIdFinden(nameAlt);
        datenbank.execSQL("UPDATE Spieler SET name ='" + nameNeu + "' WHERE id=" + String.valueOf(id));
    }

    public void spielerDBLeeren(){
        SQLiteDatabase datenbank = this.getWritableDatabase();
        datenbank.execSQL("DELETE FROM Spieler");
    }

    public ArrayList getAllSpielerNamen() {
        ArrayList array_list = new ArrayList<String>();
        SQLiteDatabase datenbank = this.getReadableDatabase();

        Cursor res =  datenbank.rawQuery( "SELECT * FROM Spieler GROUP BY name", null );
        //Cursor res =  datenbank.rawQuery( "SELECT * FROM Spieler", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex("name")));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
    public void updateSpieler(String nameAlt, String nameNeu){
        SQLiteDatabase datenbank = this.getWritableDatabase();
        datenbank.execSQL("UPDATE Spieler SET name ='"+nameNeu+"' WHERE name='"+nameAlt+"'");
    }

    public void strafenSpalteHinzufügen(Integer id){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        try {
            datenbank.execSQL("ALTER TABLE Spieler ADD strNo" + String.valueOf(id) + " FLOAT");
        } catch (SQLiteException e) {
            Log.w("SQLSp:SpalteHinzu", e.toString());
        }
        Log.w("DEBUG_HANNES", "SQLAddSpieler strNo" + String.valueOf(id));

    }
    public void strafenSpalteLöschen(Integer id){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        //datenbank.execSQL("ALTER TABLE Spieler DROP COLUMN strNo"+String.valueOf(id));
        Log.w("DEBUG_HANNES", "SQLDrop strNo"+String.valueOf(id));
        String [] colRemove = {"strNo"+String.valueOf(id)};
        try {
            dropColumn(datenbank, "CREATE TABLE IF NOT EXISTS Spieler" + "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)", "Spieler", colRemove);
        }
        catch (SQLException se){
            Log.w("DEBUG_HANNES", "SQLException");
        }
    }

    public void strafeEintragen(String spielerName, Integer strafenId, Float plus){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        String strafenID = String.valueOf(strafenId);
        Cursor res=null;
        try {
            res = datenbank.rawQuery("SELECT name, strNo" + strafenID + " FROM Spieler WHERE name='" + spielerName + "'", null);
        }catch(SQLiteException e){
            Log.w("SQLSp:Eintragen", e.toString());
        }
        ArrayList spielerListe = getAllSpielerNamen();

        if (res != null && spielerListe.contains(spielerName) && res.getCount()!=0) {
            res.moveToFirst();

            float wert = res.getFloat(res.getColumnIndex("strNo" + strafenID));
            wert = wert + plus;
            datenbank.execSQL("UPDATE Spieler SET strNo" + strafenID + "=" + String.valueOf(wert) + " WHERE name='" + spielerName + "'");
            res.close();
        }
    }

    public float strafeAuslesen(String spielerName, Integer strafenId){
        SQLiteDatabase datenbank = this.getReadableDatabase();
        String strafenID = String.valueOf(strafenId);
        Cursor res =  datenbank.rawQuery( "SELECT name, strNo"+strafenID+" FROM Spieler WHERE name='"+spielerName+"'", null );
        ArrayList spielerListe = getAllSpielerNamen();
        float wert = 0f;
        if (res != null && spielerListe.contains(spielerName) && res.getCount()!=0) {
            res.moveToFirst();

            wert = res.getFloat(res.getColumnIndex("strNo" + strafenID));
            res.close();
        }
        return wert;
    }


    //Fast komplett aus www kopiert
    private void dropColumn(SQLiteDatabase db, String createTableCmd, String tableName, String[] colsToRemove)
            throws java.sql.SQLException{

        List<String> updatedTableColumns = getTableColumns(db, tableName);
        // Remove the columns we don't want anymore from the table's list of columns
        updatedTableColumns.removeAll(Arrays.asList(colsToRemove));

        String columnsSeperated = TextUtils.join(",", updatedTableColumns);

        db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");

        // Creating the table on its new format (no redundant columns)
        db.execSQL(createTableCmd);
        for (int i=2; i<updatedTableColumns.size(); i++) {
            if(updatedTableColumns.get(i)!=colsToRemove[0]) {
                Log.w("DEBUG_HANNES", "SQLAdd Neu " + updatedTableColumns.get(i));
            }
            db.execSQL("ALTER TABLE " + tableName + " ADD " + updatedTableColumns.get(i));
        }
        // Populating the table with the data
        db.execSQL("INSERT INTO " + tableName + "(" + columnsSeperated + ") SELECT "
                + columnsSeperated + " FROM " + tableName + "_old;");
        db.execSQL("DROP TABLE " + tableName + "_old;");
    }
    public List<String> getTableColumns(SQLiteDatabase db, String tableName) {
        ArrayList<String> columns = new ArrayList<String>();
        String cmd = "pragma table_info(" + tableName + ");";
        Cursor cur = db.rawQuery(cmd, null);

        while (cur.moveToNext()) {
            columns.add(cur.getString(cur.getColumnIndex("name")));
        }
        cur.close();

        return columns;
    }
}
