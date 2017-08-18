package de.hannes.strafenkatalog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GesamtListeActivity extends ListActivity implements AdapterView.OnItemClickListener{

    private ArrayList<HashMap<String, String>> strafenListe = new ArrayList<HashMap<String, String>>();
    private ArrayList numberListe = new ArrayList();
    private static String filename = "Strafenkatalog.xls";
    private ArrayList<String> strafenArray = new ArrayList<String>();
    private ArrayList<String> spielerArray = new ArrayList<String>();
    private TextView listText;
    private float strafenSumme, gesamtSumme, overallgesamtSumme=0, overallgesamtGezahlt=0, overallgesamtAbgehoben=0;
    private int[] strafenFaktor;//= {1,3,2,3,7,2,2,20,20,2,5,10,5,-20};
    private String strafenAnzahl, strafenSumm;

    private String teilenListe;

    private SQLAgent_Strafen StrafenDB;
    private SQLAgent_Spieler SpielerDB;
    private SQLAgent_SpStRel SpStRelDB;

    public final Context gesamtListeActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("GesList:Create","GesamtListe onCreate");

        setContentView(R.layout.activity_list);
        listText = (TextView) findViewById(R.id.tv_list);

        StrafenDB = new SQLAgent_Strafen(this);
        SpielerDB = new SQLAgent_Spieler(this);
        SpStRelDB = new SQLAgent_SpStRel(this);


        Bundle extras = getIntent().getExtras();
        spielerArray =  extras.getStringArrayList("SPIELERARR");
        strafenArray = extras.getStringArrayList("STRAFENARR");

        teilenListe = "Strafen:\n";

        //Toast.makeText(this, "Strafenanzahl: " + String.valueOf(strafenArray.size()), Toast.LENGTH_SHORT).show();
        for (int spielerIndex = 0; spielerIndex < spielerArray.size(); spielerIndex++) {
            strafenSumme = 0;
            gesamtSumme = 0;
            String spielerName = String.valueOf(spielerArray.get(spielerIndex));

            for (int strafeIndex = 0; strafeIndex < strafenArray.size(); strafeIndex++) {
                String strafeName = String.valueOf(strafenArray.get(strafeIndex));
                Log.w("GesList:Create", "strafen Name: " + strafeName);


                //int cellval = ExcelAgent.readExCellInt(this, filename, spielerIndex, strafeIndex);
                if (!strafeName.equals(getString(R.string.bt_abheben))) {
                    float cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafeName)));
                    if (cellval > 0) {
                        strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafeName); //strafenFaktor[strafeIndex];
                        gesamtSumme = strafenSumme + gesamtSumme;
                        Log.w("GesList:Create", "GesamtListe Spieler "+spielerName+" Summe = "+String.valueOf(gesamtSumme));
                    }
                }
            }
            //Gezahlt ist nicht in StrafenArray
            String strafeGez = getString(R.string.bt_gezahlt);
            if(StrafenDB.getAllStrafenNamen().contains(strafeGez)){
                float cellval = SpielerDB.strafeAuslesen(spielerName, Integer.valueOf(StrafenDB.strafeIdFinden(strafeGez)));
                if (cellval > 0) {
                    strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafeGez); //strafenFaktor[strafeIndex];
                    gesamtSumme = strafenSumme + gesamtSumme;
                    overallgesamtGezahlt = overallgesamtGezahlt + strafenSumme;
                }
            }
            String strafeAbh = getString(R.string.bt_abheben);
            String spielerAbh = getString(R.string.spieler_abheben);
            if(StrafenDB.getAllStrafenNamen().contains(strafeAbh)){
                float cellval = SpielerDB.strafeAuslesen(spielerAbh, Integer.valueOf(StrafenDB.strafeIdFinden(strafeAbh)));
                if (cellval > 0) {
                    strafenSumme = cellval * StrafenDB.strafeFaktorFinden(strafeAbh); //strafenFaktor[strafeIndex];
                    //gesamtSumme = strafenSumme + gesamtSumme;
                    overallgesamtAbgehoben = overallgesamtAbgehoben + strafenSumme;
                }
            }

            HashMap<String, String> strafenMap = new HashMap<String, String>();

            strafenMap.put("spieler", spielerName);
            strafenMap.put("gesamt", "   " + String.format("%.2f", gesamtSumme) + "€");
            strafenListe.add(strafenMap);
            overallgesamtSumme = overallgesamtSumme + gesamtSumme;

            //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann
            teilenListe = teilenListe + spielerName+":\t \t"+String.format("%.2f", gesamtSumme) + "€\n";
        }

        if (strafenListe.isEmpty()) {
            listText.setText("keine Strafen vorhanden.");

            teilenListe = teilenListe + "Keine Strafen vorhanden.";
        }
        else {
            listText.setText("Strafen offen: "+String.format("%.2f", overallgesamtSumme) + "€\n"
                    + "Gezahlt gesamt: "+String.format("%.2f", overallgesamtGezahlt*(-1)) + "€\n"
                    + "Abgehoben: "+String.format("%.2f", overallgesamtAbgehoben*(-1)) + "€");
            teilenListe = teilenListe + "\nStrafen offen: "+String.format("%.2f", overallgesamtSumme) + "€\n"
                    + "Gezahlt gesamt: "+String.format("%.2f", overallgesamtGezahlt*(-1)) + "€\n"
                    + "Abgehoben: "+String.format("%.2f", overallgesamtAbgehoben*(-1)) + "€";
        }
        String[] from = new String[] { "spieler", "gesamt"};
        int[] to = new int[] {R.id.tv_spieler, R.id.tv_gesamt};
        SimpleAdapter listAdapter= new SimpleAdapter(this, strafenListe, R.layout.gesamt_columns, from, to);
        setListAdapter(listAdapter);

        ListView gesamtListView = getListView();
        gesamtListView.setTextFilterEnabled(true);
        gesamtListView.setOnItemClickListener(this);

        /*lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Toast.makeText(gesamtListeActivity, "Click - onlist", Toast.LENGTH_SHORT).show();

            }
        });*/
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (!spielerArray.isEmpty()) {
            //spSpieler = (Spinner) findViewById(R.id.sp_spieler);
            //spielerIndex = Integer.valueOf(spSpieler.getSelectedItemPosition());
            String spielerName = String.valueOf(spielerArray.get(position));

            Intent list = new Intent(this, ListeActivity.class);
            list.putExtra("SPIELER", spielerName);
            list.putExtra("STRAFENARR", strafenArray);
            startActivity(list);
        }

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

            case R.id.abheben:
                final String spielerName = getString(R.string.spieler_abheben);
                final Dialog geldAbheben = new Dialog(this);
                geldAbheben.setContentView(R.layout.dialog_gezahlt);
                geldAbheben.setTitle(getString(R.string.bt_abheben));

                Button btOKAbh = (Button) geldAbheben.findViewById(R.id.bt_gez_ok);
                Button btESCAbh = (Button) geldAbheben.findViewById(R.id.bt_gez_esc);
                final EditText etAbh = (EditText) geldAbheben.findViewById(R.id.et_gezahlt);
                final EditText etAbhGrund = (EditText) geldAbheben.findViewById(R.id.et_grund_bez);
                // Attached listener for Anlegen button
                btOKAbh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!etAbh.getText().toString().equals("")) {
                            String abhName = getString(R.string.bt_abheben);
                            if (!StrafenDB.getAllStrafenNamen().contains(abhName)) {
                                //Wenn Abheben schon existiert, dann hinzufügen sonst neu:
                                StrafenDB.strafeEinfügen(abhName, -1f);
                                SpielerDB.strafenSpalteHinzufügen(StrafenDB.strafeIdFinden(abhName));
                                Log.w("DEBUG_HANNES","Abheben: Name "+spielerName+" id "+StrafenDB.strafeIdFinden(abhName)+ " text "+etAbh.getText().toString());
                            }
                            int strafenId = StrafenDB.strafeIdFinden(abhName);
                            if (!SpielerDB.getAllSpielerNamen().contains(spielerName))
                                SpielerDB.spielerEinfügen(spielerName);
                            SpielerDB.strafeEintragen(spielerName, strafenId, Float.valueOf(etAbh.getText().toString()));
                            Calendar kal = Calendar.getInstance();
                            String grundAbh = etAbhGrund.getText().toString();
                            //if(grundAbh.isEmpty())
                            //    grundAbh = getString(R.string.kein_grund);
                            Log.w("GesList:Abheben", "Spieler: "+spielerName+" Strafen ID:"+strafenId+" Kalender:"+String.valueOf(kal.get(Calendar.DATE))+String.valueOf(kal.get(Calendar.MONTH)+1)+String.valueOf(kal.get(Calendar.YEAR))+" Text: "+etAbh.getText().toString() +" Grund: "+ grundAbh);
                            SpStRelDB.relationEinfügen(spielerName, strafenId,
                                    kal.get(Calendar.DATE), kal.get(Calendar.MONTH)+1, kal.get(Calendar.YEAR), etAbh.getText().toString() +"€ - "+ grundAbh);
                            Toast.makeText(GesamtListeActivity.this, etAbh.getText().toString() + "€ abgehoben.", Toast.LENGTH_SHORT).show();

                            overallgesamtAbgehoben = overallgesamtAbgehoben-Float.valueOf(etAbh.getText().toString());
                            listText.setText("Strafen offen: "+String.format("%.2f", overallgesamtSumme) + "€\n" +
                                    "Gezahlt gesamt: "+String.format("%.2f", overallgesamtGezahlt*(-1)) + "€" +
                                    "\nAbgehoben: "+String.format("%.2f", overallgesamtAbgehoben*(-1)) + "€");

                            geldAbheben.dismiss();
                        }  else
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    new AlertDialog.Builder(gesamtListeActivity)
                                            .setTitle("Warnung!")
                                            .setMessage("Bitte Summe eingeben.")
                                            .setPositiveButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();

                                }
                            });
                    }
                });
                btESCAbh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        geldAbheben.dismiss();
                    }
                });
                geldAbheben.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
