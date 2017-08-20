package de.hannes.strafenkatalog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class RelationListeActivity extends ListActivity implements AdapterView.OnItemClickListener{

    private ArrayList<HashMap<String, String>> strafenListe = new ArrayList<HashMap<String, String>>();
    private ArrayList<Integer> listeArray = new ArrayList<Integer>();
    private SimpleAdapter listAdapter;
    private static String filename = "Strafenkatalog.xls";
    ArrayList strafenArray = new ArrayList<String>();
    private TextView listText;
    private float strafenSumme, gesamtSumme;
    //private float[] strafenFaktor;//= {1,3,2,3,7,2,2,20,20,2,5,10,5,-20};
    private String strafenAnzahl, strafenName, spielerName;
    private String teilenListe, grundOld, sDatumOld;
    private Float anzahlGEZStr;


    private SQLAgent_Strafen StrafenDB;
    private SQLAgent_Spieler SpielerDB;
    private SQLAgent_SpStRel SpStRelDB;

    Context relListeActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);
        listText = (TextView) findViewById(R.id.tv_list);

        StrafenDB = new SQLAgent_Strafen(this);
        SpielerDB = new SQLAgent_Spieler(this);
        SpStRelDB = new SQLAgent_SpStRel(this);

        Bundle extras = getIntent().getExtras();
        spielerName = extras.getString("SPIELER");
        strafenName = extras.getString("STRAFE");

        listText.setText(strafenName);

        teilenListe = spielerName +": "+ strafenName;

        Cursor relCursor = SpStRelDB.getAllStrafen(spielerName);
        String sName = "";
        String sGrund = "";
        String sGrundTxt = "";
        String sDatum = "";

        if(relCursor!=null && relCursor.getCount()!=0) {
            String testString="";
            relCursor.moveToFirst();
            int ccount=0;
            while(ccount<relCursor.getCount()) {
                sName = StrafenDB.strafeNameFinden(relCursor.getInt(relCursor.getColumnIndex("strafe")));
                //Nur betreffende Strafen anzeigen
                if(sName.equals(strafenName)) {
                    sGrund = relCursor.getString(relCursor.getColumnIndex("grund"));
                    if(sName.equals(getString(R.string.bt_gezahlt))) {
                        int idxG = sGrund.indexOf("€");
                        if(sGrund.length()>4)
                            sGrundTxt = sGrund.substring(idxG + 4);
                        else
                            sGrundTxt = sGrund;
                    }
                    if((sGrundTxt.equals("") && sName.equals((getString(R.string.bt_gezahlt)))) || sGrund.equals(""))
                        sGrund = sGrund + getString(R.string.kein_grund);

                    String tag = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("tag")));
                    String monat = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("monat")));
                    String jahr = String.valueOf(relCursor.getInt(relCursor.getColumnIndex("jahr")));
                    sDatum = " "+tag+"."+monat+"."+jahr;

                    HashMap<String, String> strafenMap = new HashMap<String, String>();
                    //strafenMap.put("strafe", sName);
                    strafenMap.put("grund", sGrund);
                    strafenMap.put("datum", sDatum);
                    strafenListe.add(strafenMap);
                    listeArray.add(relCursor.getInt(relCursor.getColumnIndex("id")));

                    //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann (wird in Optionsmenü gemacht)
                    //teilenListe += "\n"+ sGrund +"\t am"+ sDatum;
                }

                relCursor.moveToNext();
                ccount++;
            }
        }

        String[] from = new String[] { "strafe", "grund", "datum"};
        int[] to = new int[] {R.id.tv_anzahl, R.id.tv_strafe, R.id.tv_summe};
        listAdapter= new SimpleAdapter(this, strafenListe, R.layout.list_columns, from, to);
        setListAdapter(listAdapter);

        ListView einzelListView = getListView();
        einzelListView.setTextFilterEnabled(true);
        einzelListView.setOnItemClickListener(this);
        relCursor.close();

    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        //Toast.makeText(this, "CLICK!!!", Toast.LENGTH_SHORT).show();

        final Integer SpStRelID = listeArray.get(position);
        final int pos = position;

        final Dialog relationÄndern = new Dialog(this);
        relationÄndern.setContentView(R.layout.dialog_rel_aendern);
        relationÄndern.setTitle("Strafeintrag bearbeiten");

        Button btOK = (Button) relationÄndern.findViewById(R.id.bt_rel_ok);
        Button btESC = (Button) relationÄndern.findViewById(R.id.bt_rel_esc);
        Button btDEL = (Button) relationÄndern.findViewById(R.id.bt_rel_del);
        //final EditText etDatum = (EditText) relationÄndern.findViewById(R.id.et_rel_datum);
        final EditText etGrund = (EditText) relationÄndern.findViewById(R.id.et_rel_grund);
        final DatePicker dpDatum = (DatePicker) relationÄndern.findViewById(R.id.dp_rel_change);


        final Cursor relCursor = SpStRelDB.relationFinden(SpStRelID);
        relCursor.moveToFirst();
        if(relCursor!=null && relCursor.getCount()!=0){
            final int tag = relCursor.getInt(relCursor.getColumnIndex("tag"));
            final int monat = relCursor.getInt(relCursor.getColumnIndex("monat"));
            final int jahr = relCursor.getInt(relCursor.getColumnIndex("jahr"));
            String sTag = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("tag")));
            String sMonat = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("monat")));
            String sJahr = String.valueOf(relCursor.getInt(relCursor.getColumnIndex("jahr")));
            sDatumOld = " "+sTag+"."+sMonat+"."+sJahr;
            dpDatum.updateDate(jahr, monat-1, tag);

            //String grund = relCursor.getString(relCursor.getColumnIndex("grund"));
            grundOld = relCursor.getString(relCursor.getColumnIndex("grund"));

            if(strafenName.equals(getString(R.string.bt_gezahlt))) {
                int idxG = grundOld.indexOf("€");
                if(idxG>0)
                    anzahlGEZStr = Float.valueOf(grundOld.substring(0, idxG).replace(",","."));
                if(grundOld.length()>3 && idxG<=grundOld.length()-4){
                    grundOld = grundOld.substring(idxG + 4);}

                if(anzahlGEZStr==null){
                    relationÄndern.show();
                    //Dialog um Gezahlte Summe einzutragen, wenn Strafe mit alter App angelegt wurde (kein anzahlGEZ hinterlegt)
                    final Dialog strafeBezahltAendern = new Dialog(this);
                    strafeBezahltAendern.setContentView(R.layout.dialog_gezahlt_aendern);
                    strafeBezahltAendern.setTitle(getString(R.string.bt_gezahlt));

                    Button btOKGezAendern = (Button) strafeBezahltAendern.findViewById(R.id.bt_gez_aendern_ok);
                    Button btESCGezAendern = (Button) strafeBezahltAendern.findViewById(R.id.bt_gez_aendern_esc);
                    final EditText etGezAendern = (EditText) strafeBezahltAendern.findViewById(R.id.et_gezahlt_aendern);
                    // Attached listener for Anlegen button
                    btOKGezAendern.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!etGezAendern.getText().toString().equals("")) {
                                anzahlGEZStr = Float.valueOf(etGezAendern.getText().toString());
                                SpStRelDB.updateRelation(SpStRelID, tag, monat, jahr, grundOld);
                                strafeBezahltAendern.dismiss();
                            } else
                                new AlertDialog.Builder(relListeActivity)
                                        .setTitle("Warnung!")
                                        .setMessage("Bitte Summe der Zahlung eingeben.")
                                        .setPositiveButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                        }
                    });
                    btESCGezAendern.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            strafeBezahltAendern.dismiss();
                        }
                    });
                    strafeBezahltAendern.show();
                }
            }
            etGrund.setText(grundOld);

        } else {
            //etDatum.setText("Fehler");
            etGrund.setText("Fehler");
        }

        //Attached listener for Ändern button
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(relListeActivity, getString(R.string.dialog_rel_aendern), Toast.LENGTH_SHORT).show();
                int tag = dpDatum.getDayOfMonth();
                int monat = dpDatum.getMonth()+1;
                int jahr = dpDatum.getYear();
                String grund = etGrund.getText().toString();

                if(strafenName.equals(getString(R.string.bt_gezahlt)) && anzahlGEZStr!=null)
                    SpStRelDB.updateRelation(SpStRelID, tag, monat, jahr, anzahlGEZStr+"€ - "+grund);
                else
                    SpStRelDB.updateRelation(SpStRelID, tag, monat, jahr, grund);

                HashMap<String, String> strafenMap = new HashMap<String, String>();
                if(grund.equals(""))
                    grund = getString(R.string.kein_grund);

                if(strafenName.equals(getString(R.string.bt_gezahlt)))
                    strafenMap.put("grund", anzahlGEZStr + "€ - " + grund);
                else
                    strafenMap.put("grund", grund);

                strafenMap.put("datum", String.format("%02d", tag) + "." + String.format("%02d", monat) + "." + jahr);
                strafenListe.set(pos, strafenMap);
                /* teilenliste wird unten im Optionsmenü geschrieben
                if (grundOld.equals(""))
                    //teilenListe = teilenListe.replaceFirst(String.format("%02f",anzahlGEZStr)+"€ - "+ "\\*ohne Bemerkung\\*" + "\t am" + sDatumOld, "");
                    teilenListe = teilenListe.replaceFirst("\\*ohne Bemerkung\\*" + "\t am" + String.valueOf(sDatumOld), grund +"\t am "+ strafenMap.get("datum"));
                else
                    teilenListe = teilenListe.replaceFirst(grundOld + "\t am" + sDatumOld, grund +"\t am "+ strafenMap.get("datum"));
                //teilenListe = teilenListe.replace(String.format("%02f",anzahlGEZStr)+"€ - "+ grundOld +"\t am"+ sDatumOld,
                //teilenListe = teilenListe.replace(anzahlGEZStr+"€ - "+ grundOld +"\t am"+ sDatumOld,
                //        grund +"\t am "+ strafenMap.get("datum"));*/

                listAdapter.notifyDataSetChanged();
                anzahlGEZStr = null;

                relationÄndern.dismiss();
            }
        });
        btDEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: ABHEBEN LÖSCHEN!!!
                final AlertDialog.Builder builder = new AlertDialog.Builder(RelationListeActivity.this);
                String dialogMessage = getResources().getString(R.string.dialog_del_message_st1) + " '" + strafenName + "' " + getResources().getString(R.string.dialog_del_message_3);
                builder.setMessage(dialogMessage);
                builder.setTitle(R.string.dialog_del_title);
                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        if(strafenName.equals(getString(R.string.bt_gezahlt)) && anzahlGEZStr!=null) {
                            SpStRelDB.relationLöschen(SpStRelID);
                            SpielerDB.strafeEintragen(spielerName, StrafenDB.strafeIdFinden(strafenName), -anzahlGEZStr);
                            strafenListe.remove(pos);
                            /*teilenliste wird unten im Optionsmenü geschrieben
                            int dataSet = teilenListe.indexOf(grundOld + "\t am" + sDatumOld);
                            Toast.makeText(relListeActivity,"GRUNDOLD:"+grundOld,Toast.LENGTH_LONG).show();
                            if (grundOld.equals(""))
                                //teilenListe = teilenListe.replaceFirst(String.format("%02f",anzahlGEZStr)+"€ - "+ "\\*ohne Bemerkung\\*" + "\t am" + sDatumOld, "");
                                teilenListe = teilenListe.replaceFirst(anzahlGEZStr+"€ - "+ "\\*ohne Bemerkung\\*" + "\t am" + sDatumOld, "");
                            else
                                teilenListe = teilenListe.replaceFirst(anzahlGEZStr+"€ - "+ grundOld + "\t am" + sDatumOld, "");*/

                            listAdapter.notifyDataSetChanged();
                            Toast.makeText(relListeActivity, "Strafe '" + strafenName + "' für " + spielerName + " gelöscht.", Toast.LENGTH_SHORT).show();
                            relationÄndern.dismiss();
                        }
                        else if (strafenName.equals(getString(R.string.bt_gezahlt)) && anzahlGEZStr==null) {
                            new AlertDialog.Builder(relListeActivity)
                                    .setTitle("Warnung!")
                                    .setMessage("Gezahlter Betrag wurde von alter Version angelegt und kann nicht gelöscht werden.")
                                    .setPositiveButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            relationÄndern.dismiss();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        else {
                            SpStRelDB.relationLöschen(SpStRelID);
                            SpielerDB.strafeEintragen(spielerName, StrafenDB.strafeIdFinden(strafenName), -1.0f);
                            strafenListe.remove(pos);

                            /*teilenliste wird unten im Optionsmenü geschrieben
                            int dataSet = teilenListe.indexOf(grundOld + "\t am" + sDatumOld);
                            if (grundOld.equals(""))
                                teilenListe = teilenListe.replaceFirst("\\*ohne Bemerkung\\*" + "\t am" + sDatumOld, "");
                            else
                                teilenListe = teilenListe.replaceFirst(grundOld + "\t am" + sDatumOld, "");*/

                            listAdapter.notifyDataSetChanged();
                            Toast.makeText(relListeActivity, "Strafe '" + strafenName + "' für " + spielerName + " gelöscht.", Toast.LENGTH_SHORT).show();
                            relationÄndern.dismiss();
                        }
                        anzahlGEZStr = null;
                    }
                });
                builder.setNegativeButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });


        btESC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anzahlGEZStr = null;
                relationÄndern.dismiss();
            }
        });
        relationÄndern.show();

        relCursor.close();

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

                teilenListe = spielerName +": "+ strafenName;

                Cursor relCursor = SpStRelDB.getAllStrafen(spielerName);
                String sName = "";
                String sGrund = "";
                String sGrundTxt = "";
                String sDatum = "";

                if(relCursor!=null && relCursor.getCount()!=0) {
                    relCursor.moveToFirst();
                    int ccount = 0;
                    while (ccount < relCursor.getCount()) {
                        sName = StrafenDB.strafeNameFinden(relCursor.getInt(relCursor.getColumnIndex("strafe")));
                        //Nur betreffende Strafen anzeigen
                        if (sName.equals(strafenName)) {
                            sGrund = relCursor.getString(relCursor.getColumnIndex("grund"));
                            if(sName.equals(getString(R.string.bt_gezahlt))) {
                                int idxG = sGrund.indexOf("€");
                                if(sGrund.length()>4)
                                    sGrundTxt = sGrund.substring(idxG + 4);
                                else
                                    sGrundTxt = sGrund;
                            }
                            if((sGrundTxt.equals("") && sName.equals((getString(R.string.bt_gezahlt)))) || sGrund.equals(""))
                                sGrund = sGrund + getString(R.string.kein_grund);

                            String tag = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("tag")));
                            String monat = String.format("%02d", relCursor.getInt(relCursor.getColumnIndex("monat")));
                            String jahr = String.valueOf(relCursor.getInt(relCursor.getColumnIndex("jahr")));
                            sDatum = " " + tag + "." + monat + "." + jahr;

                            //teilenListe ist String, der in WhatsApp o.Ä. versendet werden kann
                            teilenListe += "\n" + sGrund + "\t am" + sDatum;
                        }

                        relCursor.moveToNext();
                        ccount++;
                    }
                }

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
