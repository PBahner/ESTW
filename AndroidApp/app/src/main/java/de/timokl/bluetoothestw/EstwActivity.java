package de.timokl.bluetoothestw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class EstwActivity extends AppCompatActivity implements View.OnClickListener, TouchListener {

    private static final String LOG_TAG = "EstwApp";
    MyCanvas canvas;
    byte[] buffer = new byte[1024]; // Puffer

    private Thread empfangenThread;
    private boolean stopThread = false;

    private char first_selection = 'z';
    private char second_selection = 'z';

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle("MoBa Steuerung");
        canvas = findViewById(R.id.myCanvas);
        canvas.setTouchListener(this);
        globaleVariablen.setContext(this);

        startThread();

        Thread.State status = empfangenThread.getState();
        if (Thread.State.NEW.equals(status) && globaleVariablen.getIs_connected()) {
            // first start
            Log.d(LOG_TAG, "start");
            empfangenThread.start();
        }

        FloatingActionButton fab_bluetooth = findViewById(R.id.fab_show_bluetooth);
        fab_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(globaleVariablen.getIs_connected()){
                    trennen();
                } else {
                    Intent intent = new Intent(globaleVariablen.getContext(), MainActivity.class);
                    startActivity(intent);
                    stopThread();
                    //finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int ce = v.getId();
    }

    @Override
    public void onTouchDown(int moveX, int moveY, final int weiche) {
        Log.d(LOG_TAG, "onTouchDown");
        final LinearLayout root = findViewById(R.id.containerLayout);
        final View view = new View(getApplicationContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        view.setBackgroundColor(Color.TRANSPARENT);

        root.addView(view);
        view.setX(moveX);
        view.setY(moveY);

        Context wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.PopupMenu);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(wrapper, view);
        popupMenu.setOnMenuItemClickListener (new android.widget.PopupMenu.OnMenuItemClickListener ()
        {
            @Override
            public boolean onMenuItemClick (MenuItem item)
            {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.item_wu:
                        Log.i(LOG_TAG, "wu");
                        Log.d(LOG_TAG, "SWU" + weiche + "," + boolToInt(!canvas.Weichen[weiche]) + "E");
                        senden("SWU" + weiche + "," + boolToInt(!canvas.Weichen[weiche]) + "E"); break;
                    case R.id.item_wue:
                        Log.i(LOG_TAG, "wue"); break;
                    case R.id.item_wus:
                        Log.i(LOG_TAG, "wus"); break;

                }
                return true;
            }
        });

        popupMenu.inflate(R.menu.menu_w1);
        popupMenu.show();
        root.removeView(view);
    }

    @Override
    public void onTouchDown(int moveX, int moveY, final String signal) {
        int sig_id = 0;

        Log.d(LOG_TAG, "onTouchDown Signal");
        Toast.makeText(getApplicationContext(), signal, Toast.LENGTH_SHORT).show();

        switch (signal){
            case "a": sig_id = 0; break;
            case "b": sig_id = 1; break;
            case "c": sig_id = 2; break;
            case "d": sig_id = 3; break;
            case "e": sig_id = 4; break;
            case "n": sig_id = 5; break;
        }
        int c = 0;
        for(Boolean value : canvas.SigAuswahl){ // zählen, wie viel schon ausgewählt wurde
            if(value){c++;}
        }
        switch (c){                             // -> höchstens 2 auswählen
            case 0: first_selection = signal.charAt(0); canvas.SigAuswahl[sig_id] = true;
            case 1: second_selection = signal.charAt(0); canvas.SigAuswahl[sig_id] = true;
        }
        if(c < 2){
            Log.d(LOG_TAG, "Signal Rechtecke" + Arrays.deepToString(canvas.SigRects));
        }
    }

    @Override
    public void resetSignals(){
        first_selection = 'z';
        second_selection = 'z';
        for (int i=0; i < canvas.SigAuswahl.length; i++) {
            canvas.SigAuswahl[i] = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemTrennen = menu.findItem(R.id.item_trennen);
        MenuItem itemVerbinden = menu.findItem(R.id.item_verbinden);

        if (globaleVariablen.getIs_connected()) {
            itemTrennen.setVisible(true);
            itemVerbinden.setVisible(false);
        } else {
            itemTrennen.setVisible(false);
            itemVerbinden.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.item_verbinden == item.getItemId()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            stopThread();
            finish();
        }

        if(R.id.item_trennen == item.getItemId()) {
            trennen();
        }

        return true;
    }



    public void trennen() {
        if (globaleVariablen.getIs_connected() && globaleVariablen.getStream_out() != null) {
            globaleVariablen.setIs_connected(false);
            Log.d(LOG_TAG, "Trennen: Beende Verbindung");
            try {
                globaleVariablen.getStream_out().flush();
                globaleVariablen.getSocket().close();
                Toast.makeText(this, "Getrennt!", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Log.e(LOG_TAG,
                        "Fehler beim beenden des Streams und schliessen des Sockets: "
                                + e.toString());
            }
        } else
            Log.d(LOG_TAG, "Trennen: Keine Verbindung zum beenden " + globaleVariablen.getIs_connected() + " " + globaleVariablen.getStream_out());
    }

    public void verarbeiten(View v) {
        if((first_selection == 'a' || first_selection == 'b') && second_selection == 'd'){
            String message = "SFR" + first_selection + "," + 'c' + "E";
            senden(message);
            message = "SFR" + 'c' + ',' + second_selection + "E";
            senden(message);
        }else{
            String message = "SFR" + first_selection + "," + second_selection + "E";
            Log.d(LOG_TAG, "Sende Nachricht: " + message);
            senden(message);
        }
        resetSignals();
    }

    public void senden(String message) {
        byte[] msgBuffer = message.getBytes();
        if (globaleVariablen.getIs_connected()) {
            Log.d(LOG_TAG, "Sende Nachricht: " + message);
            try {
                globaleVariablen.getStream_out().write(msgBuffer);
            } catch (IOException e) {
                Log.e(LOG_TAG,
                        "Bluetest: Exception beim Senden: " + e.toString());
            }
        }
    }


    public void empfangen() {
        int laenge; // Anzahl empf. Bytes
        StringBuilder msg = new StringBuilder();

        try {
            if (globaleVariablen.getStream_in().available() > 0) {
                laenge = globaleVariablen.getStream_in().read(buffer);
                Log.d(LOG_TAG,
                        "Anzahl empfangender Bytes: " + laenge);


                for (int i = 0; i < laenge; i++) {

                    if ((char) buffer[i] == 83){
//83 = "S" //48 = "0"//66 = "B"//69 = "E"//87 = "W"//80 = "P" //70 = "F"//65 = "A"//78 = "N"
                        //"S" gefunden
                        Log.d(LOG_TAG, "S gefunden");
                        if ((char) buffer[i+1] == 66){  //Belegtmeldung
                            //"B" gefunden
                            Log.d(LOG_TAG, "B gefunden");
                            Belegtmeldung(i + 1);
                        }
                        if ((char) buffer[i+1] == 87 && (char) buffer[i+2] == 80){  //Weichen Position
                            Log.d(LOG_TAG, "WP gefunden"); //man soll z.B.: "SWP2,0" bekommen
                            weicheSchalten(i + 2);

                        }

                        if ((char) buffer[i+1] == 70 && (char) buffer[i+2] == 80){ //Fahrstrassen Status
                            Log.d(LOG_TAG, "FP gefunden"); //man soll z.B.: "SFP000000000" bekommen
                            for(int fahrstrasse=0; fahrstrasse<canvas.fahrstrassen.length; fahrstrasse++){
                                Log.d(LOG_TAG, "Fahrstrasse: " + (i + 3 + fahrstrasse) + "\t" + (char) (buffer[i+3+fahrstrasse]));
                                switch ((char) buffer[i+3+fahrstrasse]){
                                    case 49: canvas.einzustellendeFahrstrassen[fahrstrasse] = 1; Log.d(LOG_TAG, "Die" + fahrstrasse); break;    // Fahrstrasse angefragt/wird eingestellt
                                    case 50:
                                        canvas.einzustellendeFahrstrassen[fahrstrasse] = 2;                 // Fahrstrasse festegelegt
                                        for(int signal=0; signal<5; signal++){
                                            if(canvas.fahrstrassenVerschluss[fahrstrasse][signal] == 1) {
                                                canvas.Signale[signal] = 2; break;      // Signal auf grün stellen
                                            }
                                        }
                                        break;
                                    case 51:
                                    case 52:
                                        canvas.einzustellendeFahrstrassen[fahrstrasse] = 3;
                                        for(int signal=0; signal<5; signal++){
                                            if(canvas.fahrstrassenVerschluss[fahrstrasse][signal] == 1) {
                                                canvas.Signale[signal] = 2; break;      // Signal auf grün stellen
                                            }
                                        }
                                        break;
                                    // Fahrstrasse wird befahren
                                    // Signal auf rot stellen
                                    default:
                                        canvas.einzustellendeFahrstrassen[fahrstrasse] = 0;
                                        for(int signal=0; signal<5; signal++){
                                            if(canvas.fahrstrassenVerschluss[fahrstrasse][signal] == 1) {
                                                canvas.Signale[signal] = 0; break;      // Signal auf rot stellen
                                            }
                                        }
                                        break;     // Fahrstrasse nicht eingestellt
                                }
                            }
                        }
                    }

                    // Message zusammensetzen:
                    msg.append((char) buffer[i]);

                }

                Log.d(LOG_TAG, "Message: " + msg);
                //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            } /*else {
                Toast.makeText(this, "Nichts empfangen", Toast.LENGTH_LONG)
                        .show();
            }*/
        } catch (Exception e) {
            Log.e(LOG_TAG, "Fehler beim Empfangen: " + e.toString());
        }
    }

    public void Belegtmeldung(int bPos){
        Log.d(LOG_TAG, "Belegtmeldung empfangen");
        for (int i=1; i < 7; i++) {
            switch (buffer[bPos+i]){
                case 48: canvas.Belegtmeldung[i-1] = false; break;
                case 49: canvas.Belegtmeldung[i-1] = true; break;
            }
        }
    }

    public void weicheSchalten(int bPos){
        //int weiche = buffer[bPos+1]-48;
        boolean pos = false;
        for(int i=1; i<5; i++){

            switch (buffer[bPos+i]){
                case 48: pos = false; break;
                case 49: pos = true; break;
            }
            Log.d(LOG_TAG, i-1 +" "+ pos);
            canvas.Weichen[i-1] = pos;
        }
    }

    public int boolToInt(boolean b){
        if(b) return 1;
        else return 0;
    }


    public void startThread() {
        stopThread = false;
        empfangenRun empfaenger = new empfangenRun();
        empfangenThread = new Thread(empfaenger);//neuen Thread erstellen
    }

    public void stopThread() {
        stopThread = true;
    }

    class empfangenRun implements Runnable{

        @Override
        public void run() {
            if (stopThread)
                return;
            while (!isFinishing()){
                empfangen();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
