package de.hannes.strafenkatalog;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListeActivity extends ListActivity implements AdapterView.OnItemClickListener{

    private ArrayList<HashMap<String, String>> strafenListe = new ArrayList<HashMap<String, String>>();
    private ArrayList numberListe = new ArrayList();
    private static String filename = "Strafenkatalog.xls";
    ArrayList strafenArray = new ArrayList<String>();

    private TextView listText;
    private float strafenSumme, gesamtSumme;
    //private float[] strafenFaktor;//= {1,3,2,3,7,2,2,20,20,2,5,10,5,-20};
    private String strafenAnzahl, strafenSumm, spielerName;
    private String teilenListe;

    private SQLAgent_Strafen StrafenDB;
    private SQLAgent_Spieler SpielerDB;
    private ArrayList listeArray = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("DEBUG_HANNES", "onCreateListeActivity");

        setContentView(R.layout.activity_list);
        /*listText = (TextView) findViewById(R.id.tv_list);

        StrafenDB = new SQLAgent_Strafen(this);
        SpielerDB = new SQLAgent_Spieler(this);

        //strafenArray = this.getResources().getStringArray(R.array.strafen_array);
        //strafenFaktor = this.getResources().getIntArray(R.array.faktoren_array);

        Bundle extras = getIntent().getExtras();
        //int spielerIndex =  extras.getInt("SPIELER");
        spielerName = extras.getString("SPIELER");
        strafenArray = extras.getStringArrayList("STRAFENARR");

        String strafeGez = getString(R.string.bt_gezahlt);
        //HashMap<String, String> strafenMap = new HashMap<String, String>();
        // Toast.makeText(this, "Strafenanzahl: " + String.valueOf(strafenArray.size()) + "Spielerindex: " +String.valueOf(spielerIndex), Toast.LENGTH_SHORT).show();

        teilenListe = spielerName + ":\n";
        //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann


        for (int strafeIndex = 0; strafeIndex < strafenArray.size(); strafeIndex++){

            String strafenName = String.valueOf(strafenArray.get(strafeIndex));

            float cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafenName)));
            if(cellval!=0 && strafenName!=strafeGez) {
                strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafenName);
                HashMap<String, String> strafenMap = new HashMap<String, String>();
                strafenAnzahl = String.valueOf((int)cellval) + "x ";
                strafenSumm = " = " + String.format("%.2f", strafenSumme);
                strafenMap.put("anzahl", strafenAnzahl);
                strafenMap.put("strafe", strafenName);
                strafenMap.put("summe", strafenSumm+"€");
                strafenListe.add(strafenMap);
                gesamtSumme = strafenSumme + gesamtSumme;
                //listeArray nimmt Positionen von normalem strafenArray auf, um RelationListe auszuführen
                listeArray.add(strafeIndex);

                //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann
                teilenListe = teilenListe + strafenAnzahl + strafenName+"\t"+strafenSumm*//*.replace("=","")*//*+"€\n";
            }
        }
        //Gezahlt wird gesondert dargestellt:
        if(StrafenDB.getAllStrafenNamen().contains(strafeGez)){
            float cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafeGez)));
            if (cellval!=0){
                strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafeGez);
                HashMap<String, String> strafenMap = new HashMap<String, String>();
                strafenAnzahl = "";
                strafenSumm = String.format("%.2f", strafenSumme);
                strafenMap.clear();
                strafenMap.put("anzahl", strafenAnzahl);
                strafenMap.put("strafe", strafeGez);
                strafenMap.put("summe", strafenSumm+"€");
                strafenListe.add(strafenMap);
                gesamtSumme = strafenSumme + gesamtSumme;

                teilenListe = teilenListe + strafeGez+":t "+strafenSumm+"€\n";
            }
        }

        if (strafenListe.isEmpty()) {
            listText.setText("keine Strafen vorhanden.");

            teilenListe = teilenListe + "Keine Strafen vorhanden.";
        }
        else {
            listText.setText("Strafen von "+spielerName+" gesamt: " + String.format("%.2f", gesamtSumme) + "€");

            teilenListe = teilenListe +"\nGesamt:\t"+ String.format("%.2f", gesamtSumme) + "€";

            gesamtSumme = 0;
        }
        String[] from = new String[] { "anzahl", "strafe", "summe"};
        int[] to = new int[] {R.id.tv_anzahl, R.id.tv_strafe, R.id.tv_summe};
        SimpleAdapter listAdapter= new SimpleAdapter(this, strafenListe, R.layout.list_columns, from, to);
        setListAdapter(listAdapter);

        ListView einzelListView = getListView();
        einzelListView.setTextFilterEnabled(true);
        einzelListView.setOnItemClickListener(this);*/

    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        String strafenName = "";
        if(position < listeArray.size()) {
            strafenName = String.valueOf(strafenArray.get(Integer.valueOf(listeArray.get(position).toString())));
        } else {
            strafenName = getString(R.string.bt_gezahlt);
        }
        Intent list = new Intent(this, RelationListeActivity.class);
        list.putExtra("SPIELER", spielerName);
        list.putExtra("STRAFE", strafenName);
        startActivity(list);

    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.w("DEBUG_HANNES", "onResumeListeActivity");

        strafenListe.clear();
        listeArray.clear();
        teilenListe = "";

        listText = (TextView) findViewById(R.id.tv_list);

        StrafenDB = new SQLAgent_Strafen(this);
        SpielerDB = new SQLAgent_Spieler(this);

        //strafenArray = this.getResources().getStringArray(R.array.strafen_array);
        //strafenFaktor = this.getResources().getIntArray(R.array.faktoren_array);

        Bundle extras = getIntent().getExtras();
        //int spielerIndex =  extras.getInt("SPIELER");
        spielerName = extras.getString("SPIELER");
        strafenArray = extras.getStringArrayList("STRAFENARR");
        String strafeGez = getString(R.string.bt_gezahlt);
        String strafeAbh = getString(R.string.bt_abheben);
        //HashMap<String, String> strafenMap = new HashMap<String, String>();
        // Toast.makeText(this, "Strafenanzahl: " + String.valueOf(strafenArray.size()) + "Spielerindex: " +String.valueOf(spielerIndex), Toast.LENGTH_SHORT).show();

        teilenListe = spielerName + ":\n";
        //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann

        for (int strafeIndex = 0; strafeIndex < strafenArray.size(); strafeIndex++){
            String strafenName = String.valueOf(strafenArray.get(strafeIndex));
            Log.w("ListAct:Resume", "Strafenname:"+strafenName);
            float cellval = 0;

            //if (!strafenName.equals(getString(R.string.bt_abheben)))
                cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafenName)));

            if(cellval!=0 && !strafenName.equals(strafeGez)) {
                strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafenName);
                HashMap<String, String> strafenMap = new HashMap<String, String>();
                strafenAnzahl = String.valueOf((int)cellval) + "x ";
                strafenSumm = " = " + String.format("%.2f", strafenSumme);
                strafenMap.put("anzahl", strafenAnzahl);
                strafenMap.put("strafe", strafenName);
                strafenMap.put("summe", strafenSumm+"€");
                strafenListe.add(strafenMap);
                gesamtSumme = strafenSumme + gesamtSumme;
                //listeArray nimmt Positionen von normalem strafenArray auf, um RelationListe auszuführen
                listeArray.add(strafeIndex);

                //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann
                teilenListe = teilenListe + strafenAnzahl + strafenName+"\t"+strafenSumm/*.replace("=","")*/+"€\n";
            }
        }
        //Gezahlt wird gesondert dargestellt:
        if(StrafenDB.getAllStrafenNamen().contains(strafeGez)){
            float cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafeGez)));
            if (cellval!=0){
                strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafeGez);
                HashMap<String, String> strafenMap = new HashMap<String, String>();
                strafenAnzahl = "";
                strafenSumm = String.format("%.2f", strafenSumme);
                strafenMap.clear();
                strafenMap.put("anzahl", strafenAnzahl);
                strafenMap.put("strafe", strafeGez);
                strafenMap.put("summe", strafenSumm+"€");
                strafenListe.add(strafenMap);
                gesamtSumme = strafenSumme + gesamtSumme;

                teilenListe = teilenListe + strafeGez+": "+strafenSumm+"€\n";
            }
        }

        if (strafenListe.isEmpty()) {
            listText.setText("keine Strafen vorhanden.");

            teilenListe = teilenListe + "Keine Strafen vorhanden.";
        }
        else {
            if(!spielerName.equals(getString(R.string.spieler_abheben)))
                listText.setText("Strafen von "+spielerName+" gesamt: " + String.format("%.2f", gesamtSumme) + "€");
            else
                listText.setText("Ausgaben gesamt: " + String.format("%.2f", gesamtSumme) + "€");

            teilenListe = teilenListe +"\nGesamt:\t"+ String.format("%.2f", gesamtSumme) + "€";

            gesamtSumme = 0;
        }
        String[] from = new String[] { "anzahl", "strafe", "summe"};
        int[] to = new int[] {R.id.tv_anzahl, R.id.tv_strafe, R.id.tv_summe};
        SimpleAdapter listAdapter= new SimpleAdapter(this, strafenListe, R.layout.list_columns, from, to);
        setListAdapter(listAdapter);

        ListView einzelListView = getListView();
        einzelListView.setTextFilterEnabled(true);
        einzelListView.setOnItemClickListener(this);
    }

    public void strafeGelöscht(String strafenName){
        //TODO: teilenListe ändern!!!

        String[] from = new String[] { "anzahl", "strafe", "summe"};
        int[] to = new int[] {R.id.tv_anzahl, R.id.tv_strafe, R.id.tv_summe};

        SimpleAdapter listAdapter= new SimpleAdapter(this, strafenListe, R.layout.list_columns, from, to);
        setListAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        switch (id)
        {
            case R.id.teilen:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Strafenkatalog");
                shareIntent.putExtra(Intent.EXTRA_TEXT, teilenListe);
                final PackageManager packageManager = this.getPackageManager();
                List<ResolveInfo> activityList = packageManager.queryIntentActivities(shareIntent, PackageManager.GET_ACTIVITIES);
                if(activityList.size() > 0) {
                    startActivity(Intent.createChooser(shareIntent, "Strafenliste senden"));
                }
                else
                    new AlertDialog.Builder(this)
                            .setTitle("Warnung!")
                            .setMessage("Kein passendes Programm installiert.")
                            .setPositiveButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                break;

        }
        //TODO: nach Anzahl sortieren
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("DEBUG_HANNES", "onPauseListeActivity");

    }
}
