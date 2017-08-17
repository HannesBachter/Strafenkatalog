package de.hannes.strafenkatalog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class SpielerAnlegen extends ActionBarActivity {

    String spielerAnlegenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spieler_anlegen);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spieler_anlegen, menu);
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
        return super.onOptionsItemSelected(item);
    }

    /*public void dialogSpielerAnlegen(Context contxt, ArrayList playerArray, SQLAgent_Spieler sqlAgentSpieler){
        final ArrayList spielerArray = playerArray;
        final Context context = contxt;
        final SQLAgent_Spieler SpielerDB = sqlAgentSpieler;
        final Dialog spielerAnlegen = new Dialog(context);
        // Set GUI of Popup
        spielerAnlegen.setContentView(R.layout.activity_spieler_anlegen);
        spielerAnlegen.setTitle("Spieler Anlegen");

        // Init button of GUI
        Button btOKSp = (Button) spielerAnlegen.findViewById(R.id.bt_sp_ok);
        Button btESCSp = (Button) spielerAnlegen.findViewById(R.id.bt_sp_esc);
        final EditText etSpName = (EditText)spielerAnlegen.findViewById(R.id.et_spieler);
        // Attached listener for Anlegen button
        btOKSp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spielerName = etSpName.getText().toString();
                if (spielerName.matches("\\w*")) {
                    if (spielerArray.contains(spielerName)) {
                        //spielerAnlegenName="Error: Spieler_existiert";
                        //TODO Dialog: Spieler existiert
                    } else {
                        SpielerDB.spielerEinfügen(spielerName);
                        //spielerAnlegenName=spielerName;

                        Toast.makeText(context, "Spieler '" + spielerName + "' angelegt.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(context, "Nur Buchstaben oder Zahlen", Toast.LENGTH_SHORT).show();
                    //spielerAnlegenName="Error: Nur 0-9, a-z";
                //TODO Dialog: nur Buchstaben a-z, 0-9
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
        //return spielerAnlegenName;
    }
*/
}
