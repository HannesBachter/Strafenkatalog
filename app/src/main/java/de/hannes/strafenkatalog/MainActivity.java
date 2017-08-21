package de.hannes.strafenkatalog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spSpieler;
    private Spinner spStrafe;
    private int spielerIndex, strafeIndex;
    private static String filename = "Strafenkatalog.xls";
    public ArrayList spielerArray = new ArrayList<String>();
    public ArrayList strafenArray = new ArrayList<String>();

    private SQLAgent_Strafen StrafenDB;
    private SQLAgent_Spieler SpielerDB;
    private SQLAgent_SpStRel SpStRelDB;

    private boolean block = false;

    public final Context mainActivity = this;

    private ProgressDialog processdialog;
    private Vibrator vibrator;



    //private AdView adView;
    private String MY_AD_UNIT_ID = "ca-app-pub-8747134995439387/1424422154";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processdialog = ProgressDialog.show(mainActivity, "", "Importieren, Bitte warten...", true);

        StrafenDB = new SQLAgent_Strafen(this);
        SpielerDB = new SQLAgent_Spieler(this);
        SpStRelDB = new SQLAgent_SpStRel(this);

        //Spieler/Strafen ArrayList aus Datenbank aktualisieren, dann Spinner damit füllen
        updateSpielerArray();
        updateStrafenArray();
        updateSpinner("SPIELER");
        updateSpinner("STRAFE");

        spSpieler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && spielerArray.size() == 0) {
                    makeDialogSpielerAnlegen();
                    Toast.makeText(MainActivity.this, "Keine Spieler vorhanden", Toast.LENGTH_SHORT).show();
                    spSpieler.setPressed(false); //closes the spinner
                } else
                    spSpieler.performClick();
                return true;
            }
        });

        spStrafe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && strafenArray.size() == 0) {
                    makeDialogStrafeAnlegen();
                    Toast.makeText(MainActivity.this, "Keine Strafen vorhanden", Toast.LENGTH_SHORT).show();
                    spStrafe.setPressed(false); //closes the spinner.
                } else
                    spStrafe.performClick();

                return true;
            }
        });

        vibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        //adView.setAdUnitId(MY_AD_UNIT_ID);
        //adView.setAdSize(AdSize.SMART_BANNER);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* updateSpielerArray();
        updateStrafenArray();
        updateSpinner("SPIELER");
        updateSpinner("STRAFE");*/
    }

    @Override
    protected void onDestroy() {
        Log.w("DEBUG_HANNES", "Main - onDestroy");
        SpielerDB.close();
        StrafenDB.close();
        SpStRelDB.close();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            /*case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                break;*/
            case R.id.reset:
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String dialogMessage = getResources().getString(R.string.dialog_reset);
                builder.setMessage(dialogMessage);
                builder.setTitle(R.string.dialog_del_title);
                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        for(int i=0; i<spielerArray.size(); i++) {
                            String spieler = spielerArray.get(i).toString();
                            for(int j=0; j<strafenArray.size(); j++) {
                                String strafe = strafenArray.get(j).toString();
                                Integer strafeID = StrafenDB.strafeIdFinden(strafe);
                                Float anzahl = SpielerDB.strafeAuslesen(spieler, strafeID);
                                SpielerDB.strafeEintragen(spieler, strafeID, -anzahl);
                            }
                            Integer strafeID = StrafenDB.strafeIdFinden("Gezahlt");
                            if(strafeID!=-1) {
                                Float anzahl = SpielerDB.strafeAuslesen(spieler, strafeID);
                                SpielerDB.strafeEintragen(spieler, strafeID, -anzahl);
                            }
                        }
                        SpStRelDB.strafenDBLeeren();
                        Toast.makeText(MainActivity.this, "Strafenkatalog zurückgesetzt.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            //Beim Auswählen wird Dialog Box geöffnet, in der neue Spieler angelegt werden können,
            //Dialog bekommt View von activity_spieler_anlegen zugewiesen.
            case R.id.spieler_anlegen:
                if (!block) {
                    makeDialogSpielerAnlegen();
                } else
                    makeAlertDialog(getString(R.string.dialog_block));
                break;

            //Beim Auswählen wird Dialog Box geöffnet, in der neue Strafen angelegt werden können
            //Dialog bekommt View von activity_strafe_anlegen zugewiesen.
            case R.id.strafe_anlegen:
                if (!block) {
                    makeDialogStrafeAnlegen();
                } else
                    makeAlertDialog(getString(R.string.dialog_block));
                break;

            //Beim Auswählen wird Dialog Box geöffnet, in der Spieler gelöscht werden können
            case R.id.spieler_löschen:
                if (!block) {
                    makeDialogSpielerLöschen();

                } else
                    makeAlertDialog(getString(R.string.dialog_block));
                break;

            //In Dialog-Box wird abgefragt, welcher Spieler gelöscht werden soll
            case R.id.strafe_löschen:
                if (!block) {
                    makeDialogStrafeLöschen();
                } else
                    makeAlertDialog(getString(R.string.dialog_block));
                break;
            case R.id.strafenkatalog_import:

                if (spielerArray.isEmpty() && strafenArray.isEmpty() && !block) {
                    /*String umgebung = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    int idx1 = umgebung.lastIndexOf("/");
                    umgebung = umgebung.substring(0, idx1 + 1) + "Strafenkatalog/";
                    makeAlertDialog("Umgebung: "+umgebung);*/
                //Wenn in Verzeichnis kein File liegt
                // if (this.getExternalFilesDir(null).listFiles().length == 0) {
                //if (Environment.getExternalStoragePublicDirectory(umgebung).listFiles().length == 0) {
                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.setType("application/excel");    //("file/*");
                    final PackageManager packageManager = mainActivity.getPackageManager();
                    List<ResolveInfo> activityList = packageManager.queryIntentActivities(fileIntent, PackageManager.GET_ACTIVITIES);
                    if(activityList.size() > 0) {
                        startActivityForResult(fileIntent, 2);
                    }
                    else
                        makeAlertDialog("Kein Dateimanager installiert.");
                    //strafenkatalogImportieren();
                /*} else {
                    //Fragen ob vorhandenes File genommen werden soll, wenn nicht - ActionGetContent
                    new AlertDialog.Builder(this).setMessage(getString(R.string.dialog_importieren))
                            .setIcon(android.R.drawable.ic_dialog_info).setTitle("Import")
                            .setNegativeButton(getString(R.string.bt_datei_select), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                    fileIntent.setType("application/excel");    //("file/*");
                                    final PackageManager packageManager = mainActivity.getPackageManager();
                                    List<ResolveInfo> activityList = packageManager.queryIntentActivities(fileIntent, PackageManager.GET_ACTIVITIES);
                                    if(activityList.size() > 0) {
                                        startActivityForResult(fileIntent, 2);
                                    }
                                    else
                                        makeAlertDialog("Kein Dateimanager installiert.");
                                    //strafenkatalogImportieren();
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Laden-Anzeige
                                    //processdialog.show();
                                    if (spielerArray.isEmpty() && strafenArray.isEmpty() && !block) {
                                        strafenkatalogImportieren();

                                    } else if (block)
                                        showToast("Strafenkatalog wird gerade exportiert, bitte warten.");
                                }
                                }).show();
                }*/
                }
                else if(block) {
                    makeAlertDialog(getString(R.string.dialog_block));
                    processdialog.dismiss();
                }

                else {
                    makeAlertDialog("Strafen- und/oder Spieler-Liste nicht leer");
                    processdialog.dismiss();
                }
                break;

            case R.id.strafenkatalog_export:
                String umgebung;
                if(spielerArray.isEmpty())
                    makeAlertDialog("Keine Spieler vorhanden, Bitte Spieler anlegen");
                else if (strafenArray.isEmpty())
                    makeAlertDialog("Keine Strafen vorhanden, Bitte Strafen anlegen");
                else {
                    //Thread zum Exportieren der Daten
                    umgebung = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    int idx1 = umgebung.lastIndexOf("/");
                    umgebung = umgebung.substring(0, idx1 + 1) + "Strafenkatalog";

                    ExcelAgent.setUmgebung(umgebung);


                    new AlertDialog.Builder(this).setMessage(getString(R.string.dialog_exportieren1) + umgebung + getString(R.string.dialog_exportieren2))
                            .setIcon(android.R.drawable.ic_dialog_info).setTitle("Export").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            strafenkatalogExportieren();
                        }
                    }).show();
                }
                break;
            case R.id.strafenkatalog_teilen:
                String teilenListe = "Strafen:";
                /*Iterator strafenArrayIterator = strafenArray.iterator();
                while(strafenArrayIterator.hasNext()){
                    teilenListe += "\n"+ strafenArrayIterator.next().toString() +"\t Fakor: "+String.format("%.2f", StrafenDB.strafeFaktorFinden(strafenArrayIterator.next().toString()))+"€";
                    Log.w("DEBUG_HANNES", "teilenListe: " + strafenArray.iterator().next().toString());
                }*/
                for(int i=0; i<strafenArray.size(); i++){
                    teilenListe += "\n"+ strafenArray.get(i).toString() +"\t (kostet "+ String.format("%.2f", StrafenDB.strafeFaktorFinden(strafenArray.get(i).toString()))+"€)";
                }

                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Strafenkatalog");
                shareIntent.putExtra(Intent.EXTRA_TEXT, teilenListe);
                final PackageManager packageManager = mainActivity.getPackageManager();
                List<ResolveInfo> activityList = packageManager.queryIntentActivities(shareIntent, PackageManager.GET_ACTIVITIES);
                if(activityList.size() > 0) {
                    startActivity(Intent.createChooser(shareIntent, "Strafenliste senden"));
                }
                else
                    makeAlertDialog("Kein passendes Programm installiert.");

                //startActivity(Intent.createChooser(i, "Strafenliste senden"));
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
                                Log.w("MainAct:Abheben","Abheben: Name "+spielerName+" id "+StrafenDB.strafeIdFinden(abhName)+ " text "+etAbh.getText().toString());
                            }
                            int strafenId = StrafenDB.strafeIdFinden(abhName);
                            if (!SpielerDB.getAllSpielerNamen().contains(spielerName))
                                SpielerDB.spielerEinfügen(spielerName);
                            SpielerDB.strafeEintragen(spielerName, strafenId, Float.valueOf(etAbh.getText().toString()));
                            Calendar kal = Calendar.getInstance();
                            String grundAbh = etAbhGrund.getText().toString();
                            //if(grundAbh.isEmpty())
                            //    grundAbh = getString(R.string.kein_grund);
                            Log.w("MainAct:Abheben", "Spieler: "+spielerName+" Strafen ID:"+strafenId+" Kalender:"+String.valueOf(kal.get(Calendar.DATE))+String.valueOf(kal.get(Calendar.MONTH)+1)+String.valueOf(kal.get(Calendar.YEAR))+" Text: "+etAbh.getText().toString() +" Grund: "+ grundAbh);
                            SpStRelDB.relationEinfügen(spielerName, strafenId,
                                    kal.get(Calendar.DATE), kal.get(Calendar.MONTH)+1, kal.get(Calendar.YEAR), etAbh.getText().toString() +"€ - "+ grundAbh);
                            showToast(etAbh.getText().toString() + "€ abgehoben.");

                            geldAbheben.dismiss();
                        }  else
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    new AlertDialog.Builder(mainActivity)
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

    public void strafenkatalogImportieren(){

        //Laden-Anzeige
        processdialog.show();

        final Runnable importieren = new Runnable() {
            // Message importMSG = new Message();
            //Bundle importBNDL = new Bundle();
            ArrayList<String> strafenBuffer = new ArrayList<String>();
            ArrayList<String> spielerBuffer = new ArrayList<String>();

            @Override
            public void run() {
                //Spieler aus erster Zeile lesen (ab Zeile 2, da 0. und 1. Zeile die Strafen mit Faktoren sind)
                int i = 2;
                while (!(ExcelAgent.readExCellString(mainActivity, filename, i, 0) == "Error reading Cell!" || ExcelAgent.readExCellString(mainActivity, filename, i, 0) == "" || ExcelAgent.readExCellString(mainActivity, filename, i, 0) == " ")) {

                    String SpielerName = ExcelAgent.readExCellString(mainActivity, filename, i, 0);
                    if(SpielerName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                        if (!spielerArray.contains(SpielerName)) {
                            SpielerDB.spielerEinfügen(SpielerName);
                        } else {
                            showToast(getString(R.string.spieler) + " '" + SpielerName + "' " + getString(R.string.vorhanden));
                            spielerBuffer.add(SpielerName);
                        }
                    } else {
                        showToast(getString(R.string.spieler) + " '" + SpielerName + "' " + getString(R.string.falsches_zeichen));
                        spielerBuffer.add(SpielerName);
                    }
                    i++;
                    updateSpielerArray();
                }
                updateSpielerArray();
                //updateSpinner("SPIELER");

                //Strafen in oberste Zeile anlegen (ab Spalte 1, da 0. Spalte die Spielernamen beinhaltet)
                int j = 1;
                while (!(ExcelAgent.readExCellString(mainActivity, filename, 0, j) == "Error reading Cell!" || ExcelAgent.readExCellString(mainActivity, filename, 0, j) == "" || ExcelAgent.readExCellString(mainActivity, filename, 0, j) == " ")) {
                    String StrafenName = ExcelAgent.readExCellString(mainActivity, filename, 0, j);
                    float StrafenFaktor = ExcelAgent.readExCellFloat(mainActivity, filename, 1, j);
                    if(StrafenName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                        if (!strafenArray.contains(StrafenName)) {
                        StrafenDB.strafeEinfügen(StrafenName, StrafenFaktor);

                        int strafenId = StrafenDB.strafeIdFinden(StrafenName);
                        SpielerDB.strafenSpalteHinzufügen(strafenId);
                        } else {
                            showToast(getString(R.string.strafe) + " '" + StrafenName + "' " + getString(R.string.vorhanden));
                            strafenBuffer.add(StrafenName);
                        }
                    } else {
                        showToast(getString(R.string.strafe) + " '" + StrafenName + "' " + getString(R.string.falsches_zeichen));
                        strafenBuffer.add(StrafenName);
                    }

                    //Toast.makeText(MainActivity.this,ExcelAgent.readExCellString(this,filename,i,0) , Toast.LENGTH_SHORT).show();
                    j++;
                    updateStrafenArray();
                }
                //updateStrafenArray();
                //updateSpinner("STRAFE");
                //Strafen zu den jeweiligen Spielern hinzufügen
                for (int k = 0; k < i/*spielerArray.size()*/; k++) {
                    for (int l = 0; l < j/*strafenArray.size()*/; l++) {
                        //String spielerName = spielerArray.get(k).toString();
                        //String strafenName = strafenArray.get(l).toString();
                        String spielerName = ExcelAgent.readExCellString(mainActivity, filename, k + 2, 0);
                        String strafenName = ExcelAgent.readExCellString(mainActivity, filename, 0, l + 1);
                        float plus = ExcelAgent.readExCellFloat(mainActivity, filename, k + 2, l + 1);

                        int strafenId = StrafenDB.strafeIdFinden(strafenName);
                        if (strafenId != -1) {
                            SpielerDB.strafeEintragen(spielerName, strafenId, plus);
                            Calendar kal = Calendar.getInstance();
                            if(!strafenName.equals(getString(R.string.bt_gezahlt))) {
                                for (int plusCount = 0; plusCount < plus; plusCount++) {
                                    SpStRelDB.relationEinfügen(spielerName, StrafenDB.strafeIdFinden(strafenName), kal.get(Calendar.DATE), kal.get(Calendar.MONTH) + 1, kal.get(Calendar.YEAR), getString(R.string.tv_import_grund));
                                }
                            }else if (plus > 0.0) {
                                SpStRelDB.relationEinfügen(spielerName, StrafenDB.strafeIdFinden(strafenName), kal.get(Calendar.DATE), kal.get(Calendar.MONTH) + 1, kal.get(Calendar.YEAR),
                                        String.format("%.2f",plus)+"€ - "+getString(R.string.tv_import_grund));
                            }

                        }
                    }
                }
                //Ausgabe gesammelt, welche Spieler/Strafen nicht angelegt werden konnten
                String spBuffer="";
                for(int iSP=0; iSP<spielerBuffer.size(); iSP++){
                    spBuffer = spBuffer +"'"+spielerBuffer.get(iSP)+"', ";
                }

                String stBuffer="";
                for(int iST=0; iST<strafenBuffer.size(); iST++){
                    stBuffer = stBuffer +"'"+strafenBuffer.get(iST)+"', ";
                }

                if(!spBuffer.equals("") && !stBuffer.equals("")) {
                    stBuffer = stBuffer.substring(0, stBuffer.length()-2);
                    spBuffer = spBuffer.substring(0, spBuffer.length()-2);
                    makeAlertDialog(getString(R.string.spieler)+" "+spBuffer+" "+getString(R.string.und)+" "+getString(R.string.strafe)+" "+stBuffer+" "+getString(R.string.nicht_angelegt));
                } else if(!spBuffer.equals("") && stBuffer.equals("")) {
                    spBuffer = spBuffer.substring(0, spBuffer.length()-2);
                    makeAlertDialog(getString(R.string.spieler)+" "+spBuffer+" "+getString(R.string.nicht_angelegt));
                }else if(spBuffer.equals("") && !stBuffer.equals("")) {
                    stBuffer = stBuffer.substring(0, stBuffer.length()-2);
                    makeAlertDialog(getString(R.string.strafe)+" "+stBuffer+" "+getString(R.string.nicht_angelegt));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSpielerArray();
                        updateSpinner("SPIELER");
                        updateStrafenArray();
                        updateSpinner("STRAFE");
                    }
                });
                vibrator.vibrate(500);
             }
        };
        Thread importThread = new Thread(importieren);
        importThread.start();
    }

    public void strafenkatalogExportieren() {
        Runnable exportieren = new Runnable() {
            @Override
            public void run() {
                block = true;
                ExcelAgent.saveExcelFile(mainActivity, filename);
                ExcelAgent.writeExCell(mainActivity, filename, 0, 0, "Strafe:");
                ExcelAgent.writeExCell(mainActivity, filename, 1, 0, "Faktor:");

                //Gezahlt wird nicht im Strafen-Spinner angezeigt, soll aber in Excel eingetragen werden
                if (!strafenArray.contains(getString(R.string.bt_gezahlt))) {
                    strafenArray.add(getString(R.string.bt_gezahlt));
                }
                if (!strafenArray.contains(getString(R.string.bt_abheben))) {
                    strafenArray.add(getString(R.string.bt_abheben));
                }
                if (!spielerArray.contains(getString(R.string.spieler_abheben))) {
                    spielerArray.add(getString(R.string.spieler_abheben));
                }
                for (int i = 0; i < spielerArray.size(); i++) {
                    for (int j = 0; j < strafenArray.size(); j++) {
                        String spielerName = spielerArray.get(i).toString();
                        String strafenName = strafenArray.get(j).toString();
                        //Spielernamen in erste Spalte schreiben
                        ExcelAgent.writeExCell(mainActivity, filename, i + 2, 0, spielerName);
                        //Strafennamen in erste Zeile schreiben
                        ExcelAgent.writeExCell(mainActivity, filename, 0, j + 1, strafenName);
                        //Strafenfaktoren in zweite Zeile schreiben
                        ExcelAgent.writeExCell(mainActivity, filename, 1, j + 1, StrafenDB.strafeFaktorFinden(strafenName));
                        //Strafen in Tabelle schreiben
                        if(!(StrafenDB.strafeIdFinden(strafenName) == -1)) //Abfrage, ob Strafe vorhanden
                        ExcelAgent.writeExCell(mainActivity, filename, i + 2, j + 1, SpielerDB.strafeAuslesen(spielerName, StrafenDB.strafeIdFinden(strafenName)));
                    }
                }
                showToast(getString(R.string.t_exportende));
                vibrator.vibrate(500);
                showToast(getString(R.string.t_exportende));

                block = false;
                //Gezahlt löschen, damit es nicht in Strafen-Spinner angezeigt wird
                if (strafenArray.contains(getString(R.string.bt_gezahlt))) {
                    strafenArray.remove(getString(R.string.bt_gezahlt));
                }
            }
        };
        Thread exportThread = new Thread(exportieren);
        exportThread.start();
    }
    //Werte von Spinner
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        makeAlertDialog("Nichts ausgewählt.");
    }

    //strafeAnlegen wird bei Drücken von bt_anlegen aufgerufen
    public void strafeAnlegen(View view) {
        EditText etStrafeGrund = (EditText) findViewById(R.id.et_grund_strafe);

        spSpieler = (Spinner) findViewById(R.id.sp_spieler);
        spStrafe = (Spinner) findViewById(R.id.sp_strafe);
        if (spielerArray.isEmpty()) {
            makeAlertDialog(getString(R.string.dialog_spielerleer));
        } else if (strafenArray.isEmpty()) {
            makeAlertDialog(getString(R.string.dialog_strafeleer));
        } else if (block) {
            makeAlertDialog(getString(R.string.dialog_block));
        } else {
            spielerIndex = Integer.valueOf(spSpieler.getSelectedItemPosition());
            strafeIndex = Integer.valueOf(spStrafe.getSelectedItemPosition());

            String spielerName = String.valueOf(spSpieler.getSelectedItem());
            String strafenName = String.valueOf(spStrafe.getSelectedItem());
            float plus = 1.0f;

            int strafenId = StrafenDB.strafeIdFinden(strafenName);
            Log.w("MainAct:StrafeAnlegen","Eintragen: Name "+spielerName+" id "+strafenId+ " plus "+plus);
            SpielerDB.strafeEintragen(spielerName, strafenId, plus);
            Calendar kal = Calendar.getInstance();
            SpStRelDB.relationEinfügen(spielerName, StrafenDB.strafeIdFinden(strafenName), kal.get(Calendar.DATE), kal.get(Calendar.MONTH)+1, kal.get(Calendar.YEAR), etStrafeGrund.getText().toString());

            Toast.makeText(MainActivity.this, "Strafe '" + strafenName + "' für " + spielerName +" angelegt.", Toast.LENGTH_SHORT).show();

            etStrafeGrund.setText("");
            hide_keyboard_from(this, view);
            ///TODO: Strafengrund anlegen
            /*/////////DEBUG////////////////////
            SpStRelDB.relationEinfügen(spielerName, strafenName, 30, 9, 1989, etStrafeGrund.getText().toString());
            Cursor res = SpStRelDB.getAllStrafen(spielerName);
            if(res!=null) {
                String testString="";
                res.moveToFirst();
                int ccount=0;
                while(ccount<res.getCount()) {
                    testString = testString+res.getString(SpStRelDB.getAllStrafen(spielerName).getColumnIndex("grund"));
                    res.moveToNext();
                    //testString = testString + res.getString(res.getColumnIndex("grund"));
                    ccount++;
                }
                showToast(testString);

            }
            *//////////////DEBUG/////////////////////////
        }
    }

    public void strafeLoeschen(View view) {

        spSpieler = (Spinner) findViewById(R.id.sp_spieler);
        spStrafe = (Spinner) findViewById(R.id.sp_strafe);
        if (spielerArray.isEmpty()) {
            makeAlertDialog(getString(R.string.dialog_spielerleer));
        } else if (strafenArray.isEmpty()) {
            makeAlertDialog(getString(R.string.dialog_strafeleer));
        } else if (block) {
            makeAlertDialog(getString(R.string.dialog_block));
        } else {
            spielerIndex = Integer.valueOf(spSpieler.getSelectedItemPosition());
            strafeIndex = Integer.valueOf(spStrafe.getSelectedItemPosition());

            String spielerName = String.valueOf(spSpieler.getSelectedItem());
            String strafenName = String.valueOf(spStrafe.getSelectedItem());
            float plus = -1.0f;

            int strafenId = StrafenDB.strafeIdFinden(strafenName);
            if (SpielerDB.strafeAuslesen(spielerName, strafenId) > 0) {
                SpielerDB.strafeEintragen(spielerName, strafenId, plus);
                SpStRelDB.relationLöschen(spielerName, StrafenDB.strafeIdFinden(strafenName));

                Toast.makeText(MainActivity.this, "Strafe '" + strafenName + "' für " + spielerName + " gelöscht.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Strafe '" + String.valueOf(spStrafe.getSelectedItem()) + "' bei " + String.valueOf(spSpieler.getSelectedItem()) + " nicht vorhanden.", Toast.LENGTH_SHORT).show();

            }

        /*if (ExcelAgent.readExCellInt(this, filename, spielerIndex, strafeIndex) == 0) {
            Toast.makeText(MainActivity.this, "Strafe '" + String.valueOf(spStrafe.getSelectedItem()) + "' bei " + String.valueOf(spSpieler.getSelectedItem()) + " nicht vorhanden.", Toast.LENGTH_SHORT).show();
        }
        else if (ExcelAgent.readExCellInt(this, filename, spielerIndex, strafeIndex) == -1){
            Toast.makeText(MainActivity.this, "Fehler beim Auslesen von '" + String.valueOf(spStrafe.getSelectedItem()) + "' bei " + String.valueOf(spSpieler.getSelectedItem()) + ".", Toast.LENGTH_SHORT).show();
        }
        else {
            boolean deleteSuccess = ExcelAgent.changeExCell(this, filename, spielerIndex, strafeIndex,-1);
            if (deleteSuccess){
                Toast.makeText(MainActivity.this, "Strafe '" + String.valueOf(spStrafe.getSelectedItem()) + "' von " + String.valueOf(spSpieler.getSelectedItem()) + " gelöscht.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Fehler beim Löschen von '" + String.valueOf(spStrafe.getSelectedItem()) + "' bei " + String.valueOf(spSpieler.getSelectedItem()) + ".", Toast.LENGTH_SHORT).show();
            }
        }*/
        }
    }

    public void strafeBezahlt(View view) {
        if (!spielerArray.isEmpty()) {
            //spSpieler = (Spinner) findViewById(R.id.sp_spieler);
            //spielerIndex = Integer.valueOf(spSpieler.getSelectedItemPosition());
            final String spielerName = String.valueOf(spSpieler.getSelectedItem());
            final Dialog strafeBezahlt = new Dialog(this);
            strafeBezahlt.setContentView(R.layout.dialog_gezahlt);
            strafeBezahlt.setTitle("Strafe bezahlen");

            Button btOKGez = (Button) strafeBezahlt.findViewById(R.id.bt_gez_ok);
            Button btESCGez = (Button) strafeBezahlt.findViewById(R.id.bt_gez_esc);
            final EditText etGez = (EditText) strafeBezahlt.findViewById(R.id.et_gezahlt);
            final EditText etGezGrund = (EditText) strafeBezahlt.findViewById(R.id.et_grund_bez);
            // Attached listener for Anlegen button
            btOKGez.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!etGez.getText().toString().equals("")) {
                        String gezName = getString(R.string.bt_gezahlt); //Muss so bleiben, Abfrage kommt immer von R.string.bt_gezahlt
                        if (!StrafenDB.getAllStrafenNamen().contains(gezName)) {
                            //Wenn Gezahlt schon existiert, dann hinzufügen sonst neu:
                            StrafenDB.strafeEinfügen(gezName, -1f);

                            int strafenId = StrafenDB.strafeIdFinden(gezName);
                            SpielerDB.strafenSpalteHinzufügen(strafenId);
                        }
                        int strafenId = StrafenDB.strafeIdFinden(gezName);

                        SpielerDB.strafeEintragen(spielerName, strafenId, Float.valueOf(etGez.getText().toString()));
                        Calendar kal = Calendar.getInstance();
                        String grundGEZ = etGezGrund.getText().toString();
                        //if(grundGEZ.isEmpty())
                        //    grundGEZ = getString(R.string.kein_grund);

                        SpStRelDB.relationEinfügen(spielerName, StrafenDB.strafeIdFinden(gezName),
                                kal.get(Calendar.DATE), kal.get(Calendar.MONTH)+1, kal.get(Calendar.YEAR), etGez.getText().toString() +"€ - "+ grundGEZ);
                        showToast(etGez.getText().toString() + "€ bei " + spielerName + " gutgeschrieben.");
                        strafeBezahlt.dismiss();
                    } else
                        makeAlertDialog("Bitte Summe der Zahlung eingeben.");
                }
            });
            btESCGez.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strafeBezahlt.dismiss();
                }
            });
            strafeBezahlt.show();
        } else
            makeAlertDialog(getString(R.string.dialog_spielerleer));
    }

    public void strafeAnzeigen(View view) {

        if (!spielerArray.isEmpty()) {
            //spSpieler = (Spinner) findViewById(R.id.sp_spieler);
            //spielerIndex = Integer.valueOf(spSpieler.getSelectedItemPosition());
            String spielerName = String.valueOf(spSpieler.getSelectedItem());

            Intent list = new Intent(this, ListeActivity.class);
            list.putExtra("SPIELER", spielerName);
            list.putExtra("STRAFENARR", strafenArray);
            startActivity(list);
        } else
            makeAlertDialog(getString(R.string.dialog_spielerleer));
    }

    public void gesamtAnzeigen(View view) {
        Intent gesamt = new Intent(this, GesamtListeActivity.class);
        gesamt.putExtra("SPIELERARR", spielerArray);
        gesamt.putExtra("STRAFENARR", strafenArray);
        startActivity(gesamt);
    }

    private void updateSpinner(String spinner) {
        processdialog.dismiss();
        if (spinner.equals("SPIELER")) {
            spSpieler = (Spinner) findViewById(R.id.sp_spieler);
            ArrayAdapter spielerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spielerArray);
            spielerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
            spSpieler.setAdapter(spielerAdapter);
        } else if (spinner.equals("STRAFE")) {
            /*spStrafe = (Spinner) findViewById(R.id.sp_strafe);
            ArrayAdapter strafenAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,strafenArray);
            strafenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spStrafe.setAdapter(strafenAdapter);*/

            spStrafe = (Spinner) findViewById(R.id.sp_strafe);
            ArrayAdapter strafenAdapter = new MultilineAdapter(this, R.layout.spinner_multiline, strafenArray);
            strafenAdapter.setDropDownViewResource(R.layout.spinner_multiline_dropdown);

            spStrafe.setAdapter(strafenAdapter);
        }
    }

    public class MultilineAdapter extends ArrayAdapter<String> {
        ArrayList objects;

        public MultilineAdapter(Context context, int textViewResourceId, ArrayList objects) {
            super(context, textViewResourceId, objects);
            this.objects = objects;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //return getCustomView(position, convertView, parent);
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_multiline_dropdown, parent, false);
            TextView label = (TextView) row.findViewById(R.id.sp_top);
            //label.setText(strafenArray.get(position).toString());
            label.setText(objects.get(position).toString());

            TextView sub = (TextView) row.findViewById(R.id.sp_sub);
            sub.setText(String.format("%.2f",StrafenDB.strafeFaktorFinden(strafenArray.get(position).toString()))+" €");
            return row;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_multiline, parent, false);
            TextView label = (TextView) row.findViewById(R.id.sp_top);
            //label.setText(strafenArray.get(position).toString());
            label.setText(objects.get(position).toString());

            TextView sub = (TextView) row.findViewById(R.id.sp_sub);
            sub.setText(String.format("%.2f",StrafenDB.strafeFaktorFinden(strafenArray.get(position).toString()))+" €");
            return row;
        }
    }

    public void updateSpielerArray() {
        spielerArray = SpielerDB.getAllSpielerNamen();

        if (spielerArray.contains(getString(R.string.spieler_abheben))) {
            spielerArray.remove(getString(R.string.spieler_abheben));
        }
    }

    public void updateStrafenArray() {
        strafenArray = StrafenDB.getAllStrafenNamen();
        //"Gezahlt" soll nicht bei Strafen vorkommen
        if (strafenArray.contains(getString(R.string.bt_gezahlt))) {
            strafenArray.remove(getString(R.string.bt_gezahlt));
        }
        //"Geld abheben" soll auch nicht angezeigt werden
        if (strafenArray.contains(getString(R.string.bt_abheben))) {
            strafenArray.remove(getString(R.string.bt_abheben));
        }
    }

    public void makeDialogSpielerAnlegen() {
        //Dialog aufpoppen
        final Dialog spielerAnlegen = new Dialog(this);
        // Set GUI of Popup
        spielerAnlegen.setContentView(R.layout.activity_spieler_anlegen);
        spielerAnlegen.setTitle("Spieler Anlegen");

        // Init button of GUI
        Button btOKSp = (Button) spielerAnlegen.findViewById(R.id.bt_sp_ok);
        Button btESCSp = (Button) spielerAnlegen.findViewById(R.id.bt_sp_esc);
        final EditText etSpName = (EditText) spielerAnlegen.findViewById(R.id.et_spieler);
        // Attached listener for Anlegen button
        btOKSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spielerName = etSpName.getText().toString();
                if (spielerName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                    if (spielerArray.contains(spielerName)) {
                        makeAlertDialog("Spieler " + spielerName + " existiert bereits.");
                    } else {
                        SpielerDB.spielerEinfügen(spielerName);

                        updateSpielerArray();
                        updateSpinner("SPIELER");

                        Toast.makeText(MainActivity.this, "Spieler '" + etSpName.getText().toString() + "' angelegt.", Toast.LENGTH_SHORT).show();
                        etSpName.setText("");
                        hide_keyboard_from(mainActivity, v);
                    }
                } else
                    makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
                //Toast.makeText(MainActivity.this, "Nur Buchstaben a-z, 0-9.", Toast.LENGTH_SHORT).show();
            }
        });
        //Listener für Abbrechen Button
        btESCSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spielerAnlegen.dismiss();
            }
        });
        // Make dialog box visible.
        spielerAnlegen.show();
    }

    public void spielerÄndern(final String nameAlt) {

        //TODO: Spieler ändern statt Löschen???
        //Dialog aufpoppen
        final Dialog spielerÄndern = new Dialog(this);
        // Set GUI of Popup
        spielerÄndern.setContentView(R.layout.activity_spieler_anlegen);
        spielerÄndern.setTitle("Spieler Anlegen");

        // Init button of GUI
        Button btOKSp = (Button) spielerÄndern.findViewById(R.id.bt_sp_ok);
        Button btESCSp = (Button) spielerÄndern.findViewById(R.id.bt_sp_esc);
        final EditText etSpName = (EditText) spielerÄndern.findViewById(R.id.et_spieler);
        // Attached listener for Anlegen button
        btOKSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spielerName = etSpName.getText().toString();
                if (spielerName.matches("[a-zA-Z0-9 ]+")) {
                    SpielerDB.updateSpieler(nameAlt, spielerName);

                    updateSpielerArray();
                    updateSpinner("SPIELER");

                    Toast.makeText(MainActivity.this, "Spieler '" + nameAlt + "' zu " + etSpName.getText().toString() + "' geändert.", Toast.LENGTH_SHORT).show();
                } else
                    //Toast.makeText(MainActivity.this, "Nur Buchstaben a-z, 0-9.", Toast.LENGTH_SHORT).show();
                    makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
            }
        });
        //Listener für Abbrechen Button
        btESCSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spielerÄndern.dismiss();
            }
        });
        // Make dialog box visible.
        spielerÄndern.show();
    }

    public void makeDialogStrafeAnlegen() {
        final Dialog strafeAnlegen = new Dialog(this);
        // Set GUI of Popup
        strafeAnlegen.setContentView(R.layout.activity_strafe_anlegen);
        strafeAnlegen.setTitle("Strafe Anlegen");

        // Init button of GUI
        Button btOKSt = (Button) strafeAnlegen.findViewById(R.id.bt_st_ok);
        Button btESCSt = (Button) strafeAnlegen.findViewById(R.id.bt_st_esc);
        final EditText etStName = (EditText) strafeAnlegen.findViewById(R.id.et_strafe);
        final EditText etStFaktor = (EditText) strafeAnlegen.findViewById(R.id.et_faktor);
        // Attached listener for Anlegen button
        btOKSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String StrafenName = etStName.getText().toString();
                // Float strafenFaktor = Float.valueOf(etStFaktor.getText().toString());

                if (StrafenName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                    if (strafenArray.contains(StrafenName)) {
                        makeAlertDialog("Strafe " + StrafenName + " existiert bereits!");
                    } else if (StrafenName == null)
                        makeAlertDialog("Bitte Spielername angeben");
                    else if (etStFaktor.getText().toString().matches(""))
                        makeAlertDialog("Bitte Strafenfaktor angeben");
                    else {
                        Float strafenFaktor = Float.valueOf(etStFaktor.getText().toString());

                        StrafenDB.strafeEinfügen(StrafenName, strafenFaktor);

                        updateStrafenArray();
                        updateSpinner("STRAFE");

                        //StrafenDB.updateStrafenIDs();
                        int strafenId = StrafenDB.strafeIdFinden(StrafenName);
                        SpielerDB.strafenSpalteHinzufügen(strafenId);

                        Toast.makeText(MainActivity.this, "Strafe '" + StrafenName + "' angelegt.", Toast.LENGTH_SHORT).show();
                        etStName.setText("");
                        etStFaktor.setText("");
                        hide_keyboard_from(mainActivity, v);
                    }
                } else
                    makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
                //   Toast.makeText(MainActivity.this, "Nur Buchstaben a-z, 0-9.", Toast.LENGTH_SHORT).show();
            }
        });
        //Listener für Abbrechen Button
        btESCSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strafeAnlegen.dismiss();
            }
        });

        // Make dialog box visible.
        strafeAnlegen.show();
    }

    public void strafeÄndern(final String nameAlt) {
        final Dialog strafeÄndern = new Dialog(this);
        // Set GUI of Popup
        strafeÄndern.setContentView(R.layout.activity_strafe_anlegen);
        strafeÄndern.setTitle("Strafe Ändern");

        // Init button of GUI
        Button btOKSt = (Button) strafeÄndern.findViewById(R.id.bt_st_ok);
        Button btESCSt = (Button) strafeÄndern.findViewById(R.id.bt_st_esc);
        final EditText etStName = (EditText) strafeÄndern.findViewById(R.id.et_strafe);
        final EditText etStFaktor = (EditText) strafeÄndern.findViewById(R.id.et_faktor);
        // Attached listener for Anlegen button
        btOKSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String StrafenName = etStName.getText().toString();

                if (StrafenName.matches("[a-zA-Z0-9 ]+")) {
                    if (!strafenArray.contains(StrafenName)) {
                        makeAlertDialog("Strafe existiert nicht!");
                    } else {
                        StrafenDB.updateStrafe(nameAlt, StrafenName, Float.valueOf(etStFaktor.getText().toString()));
                        updateStrafenArray();
                        updateSpinner("STRAFE");

                        Toast.makeText(MainActivity.this, "Strafe '" + nameAlt + "' zu '" + StrafenName + "' geändert.", Toast.LENGTH_SHORT).show();
                    }
                } else
                    makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
                //Toast.makeText(MainActivity.this, "Nur Buchstaben a-z, 0-9.", Toast.LENGTH_SHORT).show();
            }
        });
        //Listener für Abbrechen Button
        btESCSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strafeÄndern.dismiss();
            }
        });

        // Make dialog box visible.
        strafeÄndern.show();
    }

    public void makeDialogSpielerLöschen() {
        final Dialog spielerLöschen = new Dialog(this);
        // Set GUI of Popup
        spielerLöschen.setContentView(R.layout.activity_spieler_loeschen);
        spielerLöschen.setTitle(R.string.title_activity_spieler_loeschen);

        // Init buttons of GUI
        Button btDelSp = (Button) spielerLöschen.findViewById(R.id.bt_sp_del_ok);
        Button btDESCSp = (Button) spielerLöschen.findViewById(R.id.bt_sp_del_esc);
        Button btDelAllSp = (Button) spielerLöschen.findViewById(R.id.bt_sp_delall);
        Button btChangeSp = (Button) spielerLöschen.findViewById(R.id.bt_sp_change);

        final Spinner spDelSpieler = (Spinner) spielerLöschen.findViewById(R.id.sp_spielerdel);
        final ArrayAdapter spielerDelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spielerArray);
        spielerDelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDelSpieler.setAdapter(spielerDelAdapter);

        // Attached listener for Anlegen button
        btDelSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SpielerName = spDelSpieler.getSelectedItem().toString();

                //Alert Dialog zum erneuten Nachfragen, ob Spieler sicher gelöscht werden soll
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String dialogMessage = getResources().getString(R.string.dialog_del_message_sp1) + " '" + SpielerName + "' " + getResources().getString(R.string.dialog_del_message_2);
                builder.setMessage(dialogMessage);
                builder.setTitle(R.string.dialog_del_title);
                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        String SpielerName = spDelSpieler.getSelectedItem().toString();
                        SpielerDB.spielerLöschen(SpielerName);
                        SpStRelDB.spielerLöschen(SpielerName);

                        updateSpielerArray();
                        updateSpinner("SPIELER");
                        spielerDelAdapter.remove(SpielerName);
                        spielerDelAdapter.notifyDataSetChanged();


                        if (spielerArray.isEmpty())
                            spielerLöschen.dismiss();
                        Toast.makeText(MainActivity.this, "Spieler '" + SpielerName + "' gelöscht.", Toast.LENGTH_SHORT).show();
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
        //Listener für Abbrechen Button
        btDESCSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSpielerArray();
                updateSpinner("SPIELER");
                spielerLöschen.dismiss();
            }
        });
        //Button, um alle Spieler aus der Datenbank zu löschen
        btDelAllSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //erneute Nachfrage mit Alert Dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_del_message_spall)
                        .setTitle(R.string.dialog_del_title);

                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        SpielerDB.spielerDBLeeren();
                        SpStRelDB.resetDB();

                        updateSpielerArray();
                        updateSpinner("SPIELER");

                        spielerDelAdapter.clear();
                        spielerDelAdapter.notifyDataSetChanged();

                        spielerLöschen.dismiss();
                        Toast.makeText(MainActivity.this, "Alle Spieler gelöscht.", Toast.LENGTH_SHORT).show();
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

        btChangeSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SpielerName = spDelSpieler.getSelectedItem().toString();

                //Alert Dialog zum erneuten Nachfragen, ob Spieler sicher geändert werden soll
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                //String dialogMessage = getResources().getString(R.string.dialog_del_message_sp1) + " '" + SpielerName + "' " + getResources().getString(R.string.dialog_change_message_2);
                //builder.setMessage(dialogMessage);
                builder.setTitle(R.string.title_activity_spieler_loeschen);
                final EditText etNeuerName = new EditText(mainActivity);
                etNeuerName.setText(SpielerName);
                builder.setView(etNeuerName);

                builder.setPositiveButton(R.string.bt_change, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //EditText etNeuerName = (EditText) findViewById(R.id.et_spieler_aendern);
                        String NeuerName = etNeuerName.getText().toString();
                        if (NeuerName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                            // User clicked OK button
                            String SpielerName = spDelSpieler.getSelectedItem().toString();
                            SpielerDB.spielerÄndern(SpielerName, NeuerName);
                            SpStRelDB.spielerÄndern(SpielerName, NeuerName);

                            spielerDelAdapter.remove(SpielerName);
                            spielerDelAdapter.add(NeuerName);
                            spielerDelAdapter.sort(new Comparator<String>() {
                                @Override
                                public int compare(String object1, String object2) {
                                    return object1.toLowerCase().compareTo(object2.toLowerCase());
                                }
                            });
                            spielerDelAdapter.notifyDataSetChanged();
                            updateSpielerArray();
                            updateSpinner("SPIELER");

                            if (spielerArray.isEmpty())
                                spielerLöschen.dismiss();
                            Toast.makeText(MainActivity.this, "Spieler '" + SpielerName + "' zu '" + NeuerName + "' geändert.", Toast.LENGTH_SHORT).show();
                        } else
                            makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
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
        // Make dialog box visible.
        spielerLöschen.show();
    }

    public void makeDialogStrafeLöschen() {
        final Dialog strafeLöschen = new Dialog(this);
        // Set GUI of Popup
        strafeLöschen.setContentView(R.layout.activity_spieler_loeschen);
        strafeLöschen.setTitle(R.string.title_activity_strafe_loeschen);
        TextView tvStDel = (TextView) strafeLöschen.findViewById(R.id.tv_sp_del);
        tvStDel.setText(getString(R.string.tv_st_del));

        // Init buttons of GUI
        Button btDelSt = (Button) strafeLöschen.findViewById(R.id.bt_sp_del_ok);
        Button btDESCSt = (Button) strafeLöschen.findViewById(R.id.bt_sp_del_esc);
        Button btDelAllSt = (Button) strafeLöschen.findViewById(R.id.bt_sp_delall);
        Button btChangeSt = (Button) strafeLöschen.findViewById(R.id.bt_sp_change);

        final Spinner spDelStrafe = (Spinner) strafeLöschen.findViewById(R.id.sp_spielerdel);
        final ArrayAdapter strafeDelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, strafenArray);
        strafeDelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDelStrafe.setAdapter(strafeDelAdapter);

        // Attached listener for Anlegen button
        btDelSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String StrafenName = spDelStrafe.getSelectedItem().toString();

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String dialogMessage = getResources().getString(R.string.dialog_del_message_st1) + " '" + StrafenName + "' " + getResources().getString(R.string.dialog_del_message_2);
                builder.setMessage(dialogMessage);
                builder.setTitle(R.string.dialog_del_title);
                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        String StrafenName = spDelStrafe.getSelectedItem().toString();
                        SpielerDB.strafenSpalteLöschen(StrafenDB.strafeIdFinden(StrafenName));
                        StrafenDB.strafeLöschen(StrafenName);
                        SpStRelDB.strafenLöschen(StrafenDB.strafeIdFinden(StrafenName));

                        updateStrafenArray();
                        updateSpinner("STRAFE");
                        strafeDelAdapter.remove(StrafenName);
                        strafeDelAdapter.notifyDataSetChanged();

                        //StrafenDB.updateStrafenIDs();
                        if (strafenArray.isEmpty())
                            strafeLöschen.dismiss();
                        Toast.makeText(MainActivity.this, "Strafe '" + StrafenName + "' gelöscht.", Toast.LENGTH_SHORT).show();
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
        //Listener für Abbrechen Button
        btDESCSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStrafenArray();
                updateSpinner("STRAFE");
                strafeLöschen.dismiss();
            }
        });
        //Button, um alle Strafen aus Datenbank zu löschen
        btDelAllSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_del_message_stall)
                        .setTitle(R.string.dialog_del_title);
                builder.setPositiveButton(R.string.bt_del, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        for (int i = 0; i < strafenArray.size(); i++) {
                            SpielerDB.strafenSpalteLöschen(StrafenDB.strafeIdFinden(strafenArray.get(i).toString()));
                        }
                        StrafenDB.strafenDBLeeren();
                        SpStRelDB.resetDB();

                        updateStrafenArray();
                        updateSpinner("STRAFE");

                        strafeDelAdapter.clear();
                        strafeDelAdapter.notifyDataSetChanged();

                        strafeLöschen.dismiss();
                        Toast.makeText(MainActivity.this, "Alle Strafen gelöscht.", Toast.LENGTH_SHORT).show();
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

        btChangeSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String StrafenName = spDelStrafe.getSelectedItem().toString();

                //Alert Dialog zum erneuten Nachfragen, ob Spieler sicher geändert werden soll
                final AlertDialog.Builder builderName = new AlertDialog.Builder(MainActivity.this);
                //String dialogMessage = getResources().getString(R.string.dialog_del_message_sp1) + " '" + SpielerName + "' " + getResources().getString(R.string.dialog_change_message_2);
                //builder.setMessage(dialogMessage);
                builderName.setTitle(R.string.title_activity_strafe_loeschen);
                final EditText etNeuerName = new EditText(mainActivity);
                etNeuerName.setText(StrafenName);
                builderName.setView(etNeuerName);
                /*final EditText etNeuerFaktor = new EditText(mainActivity);
                etNeuerFaktor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builderName.setView(etNeuerFaktor);*/


                builderName.setPositiveButton(R.string.bt_change, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //EditText etNeuerName = (EditText) findViewById(R.id.et_spieler_aendern);
                        final String NeuerName = etNeuerName.getText().toString();
                        final Float AlterFaktor = Float.valueOf(StrafenDB.strafeFaktorFinden(StrafenName).toString());

                        if (NeuerName.matches("[öÖäÄüÜßa-zA-Z0-9 ]+")) {
                            //StrafenArray kopieren, um zu kucken, ob Strafe mit neuem Namen existiert
                            ArrayList strafenArrayCopy = strafenArray;
                            strafenArrayCopy.remove(StrafenName);
                            if (strafenArrayCopy.contains(NeuerName)) {
                                makeAlertDialog("Strafe " + NeuerName + " existiert bereits!");
                            } else if (NeuerName == null)
                                makeAlertDialog("Bitte Strafenname angeben");
                            else {
                                // User clicked OK button
                                //String StrafenName = spDelStrafe.getSelectedItem().toString();
                                StrafenDB.strafeÄndern(StrafenName, NeuerName);

                                strafeDelAdapter.remove(StrafenName);
                                strafeDelAdapter.add(NeuerName);
                                strafeDelAdapter.sort(new Comparator<String>() {
                                    @Override
                                    public int compare(String object1, String object2) {
                                        return object1.toLowerCase().compareTo(object2.toLowerCase());
                                    }
                                });
                                strafeDelAdapter.notifyDataSetChanged();
                                updateStrafenArray();
                                updateSpinner("STRAFE");

                                if (strafenArray.isEmpty())
                                    strafeLöschen.dismiss();
                                if(!StrafenName.equals(NeuerName))
                                Toast.makeText(MainActivity.this, "Strafe '" + StrafenName + "' zu '" + NeuerName + "' geändert.", Toast.LENGTH_SHORT).show();


                                //Alert Dialog zum ändern des Faktors
                                final AlertDialog.Builder builderFaktor = new AlertDialog.Builder(MainActivity.this);
                                //String dialogMessage = getResources().getString(R.string.dialog_del_message_sp1) + " '" + SpielerName + "' " + getResources().getString(R.string.dialog_change_message_2);
                                //builder.setMessage(dialogMessage);
                                builderFaktor.setTitle(R.string.title_activity_strafe_loeschen);
                                final EditText etNeuerFaktor = new EditText(mainActivity);
                                etNeuerFaktor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                etNeuerFaktor.setText(AlterFaktor.toString());
                                builderFaktor.setView(etNeuerFaktor);


                                builderFaktor.setPositiveButton(R.string.bt_change, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //EditText etNeuerName = (EditText) findViewById(R.id.et_spieler_aendern);
                                        Float NeuerFaktor = Float.valueOf(etNeuerFaktor.getText().toString());

                                        // User clicked OK button
                                        StrafenDB.strafeÄndern(NeuerName, NeuerFaktor);
                                        updateStrafenArray();
                                        updateSpinner("STRAFE");

                                        if (strafenArray.isEmpty())
                                            strafeLöschen.dismiss();
                                        if(Math.abs(AlterFaktor-NeuerFaktor)>0.0001)
                                            Toast.makeText(MainActivity.this, "Strafenfaktor von '" + NeuerName + "' zu '" + NeuerFaktor + "' geändert.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builderFaktor.setNegativeButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                                AlertDialog dialogF = builderFaktor.create();
                                dialogF.show();
                            }
                        } else {
                            makeAlertDialog("Bitte nur Zeichen a-z und 0-9 verwenden.");
                            strafeDelAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builderName.setNegativeButton(R.string.bt_esc, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialogN = builderName.create();
                dialogN.show();
            }
        });

        // Make dialog box visible.
        strafeLöschen.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this);

                if (sharedPrefs.getBoolean("reset_checkbox", false)) {
                    SpielerDB.onUpgrade(SpielerDB.getWritableDatabase(), 1, 1);
                    StrafenDB.onUpgrade(StrafenDB.getWritableDatabase(), 1, 1);
                    SpStRelDB.onUpgrade(SpStRelDB.getWritableDatabase(), 1, 1);
                    updateSpielerArray();
                    updateStrafenArray();
                    updateSpinner("SPIELER");
                    updateSpinner("STRAFE");
                }
                if (sharedPrefs.getBoolean("alphabetSP_checkbox", false)) {
                }
                if (sharedPrefs.getBoolean("alphabetST_checkbox", false)) {
                }

                break;
            case 2:
                if(data!=null) {
                    String name = data.getData().toString();
                    //Uri von übergebenem Verzeichnis
                    Uri uri = data.getData();//Uri.parse(data.getData().toString());
                    Pfadfinder pathFinder = new Pfadfinder();
                    String pfad = pathFinder.getPath(this, uri);
                    //Dateiname steht nach letztem '/'
                    //int idx1 = name.lastIndexOf("/");
                    int idx1 = pfad.lastIndexOf("/");
                    //String dateiName = name.substring(idx1+1);
                    String dateiName = pfad.substring(idx1 + 1);
                    if(dateiName.endsWith("xls")) {
                        //'file:' entfernen
                        //int idx2 = name.indexOf("/");
                        //String ordner = name.substring(idx2, idx1 + 1);
                        String ordner = pfad.substring(0, idx1+1);
                        ExcelAgent.setUmgebung(ordner);
                        filename = dateiName;
                        strafenkatalogImportieren();

                        //makeAlertDialog(name+" getPath: "+pfad+" ordner: "+ordner+" dateiname: "+dateiName);
                    }
                    else
                        makeAlertDialog("Bitte Excel-Datei Version 2003 (*.xls) auswählen");

                }
                break;
        }
    }

    public void makeAlertDialog(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(mainActivity)
                        .setTitle("Warnung!")
                        .setMessage(text)
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

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void hide_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
