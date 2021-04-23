package de.timokl.bluetoothestw;

import android.annotation.SuppressLint;
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

    private Thread bluetoothReceiveThread;
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
        final Context EstwActivityContext = this;

        startThread();

        Thread.State status = bluetoothReceiveThread.getState();
        if (Thread.State.NEW.equals(status) && globalVariables.isConnected()) {
            // first start
            Log.d(LOG_TAG, "start");
            bluetoothReceiveThread.start();
        }

        FloatingActionButton fab_bluetooth = findViewById(R.id.fab_show_bluetooth);
        fab_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(globalVariables.isConnected()){
                    disconnectBluetoothConnection();
                } else {
                    Intent intent = new Intent(EstwActivityContext, MainActivity.class);
                    startActivity(intent);
                    stopThread();
                    //finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        //int ce = v.getId();
    }

    @Override
    public void onTouchDown(int moveX, int moveY, final int Switch) {
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
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick (MenuItem item)
            {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.item_wu:
                        Log.i(LOG_TAG, "wu");
                        Log.d(LOG_TAG, "SWU" + Switch + "," + boolToInt(!canvas.currentSwitchStates[Switch]) + "E");
                        bluetoothSend("SWU" + Switch + "," + boolToInt(!canvas.currentSwitchStates[Switch]) + "E");
                        break;
                    case R.id.item_wue:
                        Log.i(LOG_TAG, "wue");
                        break;
                    case R.id.item_wus:
                        Log.i(LOG_TAG, "wus");
                        break;

                }
                return true;
            }
        });

        popupMenu.inflate(R.menu.menu_w1);
        popupMenu.show();
        root.removeView(view);
    }

    @Override
    public void onTouchDown(int moveX, int moveY, final char signal) {
        int sig_id = 0;

        Log.d(LOG_TAG, "onTouchDown Signal");
        Toast.makeText(getApplicationContext(), signal, Toast.LENGTH_SHORT).show();

        switch (signal){
            case 'a': sig_id = 0;
                break;
            case 'b': sig_id = 1;
                break;
            case 'c': sig_id = 2;
                break;
            case 'd': sig_id = 3;
                break;
            case 'e': sig_id = 4;
                break;
            case 'n': sig_id = 5;
                break;
        }
        int c = 0;
        for(Boolean value : canvas.selectedSignals){ // zählen, wie viel schon ausgewählt wurde
            if(value){c++;}
        }
        switch (c){                             // -> höchstens 2 auswählen
            case 0:
                first_selection = signal;
                canvas.selectedSignals[sig_id] = true; break;
            case 1:
                second_selection = signal;
                canvas.selectedSignals[sig_id] = true; break;
        }
        if(c < 2){
            Log.d(LOG_TAG, "Signal Rechtecke" + Arrays.deepToString(canvas.SigRects));
        }
    }

    @Override
    public void resetSignals(){
        first_selection = 'z';
        second_selection = 'z';
        Arrays.fill(canvas.selectedSignals, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemDisconnect = menu.findItem(R.id.item_disconnect);
        MenuItem itemConnect = menu.findItem(R.id.item_connect);

        if (globalVariables.isConnected()) {
            itemDisconnect.setVisible(true);
            itemConnect.setVisible(false);
        } else {
            itemDisconnect.setVisible(false);
            itemConnect.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.item_connect == item.getItemId()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            stopThread();
            finish();
        }

        if(R.id.item_disconnect == item.getItemId()) {
            disconnectBluetoothConnection();
        }

        return true;
    }



    public void disconnectBluetoothConnection() {
        if (globalVariables.isConnected() && globalVariables.getStreamOut() != null) {
            globalVariables.setIsConnected(false);
            Log.d(LOG_TAG, "Trennen: Beende Verbindung");
            try {
                globalVariables.getStreamOut().flush();
                globalVariables.getSocket().close();
                Toast.makeText(this, "Getrennt!", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Log.e(LOG_TAG,
                        "Fehler beim beenden des Streams und schliessen des Sockets: "
                                + e.toString());
            }
        } else
            Log.d(LOG_TAG, "Trennen: Keine Verbindung zum beenden " + globalVariables.isConnected() + " " + globalVariables.getStreamOut());
    }

    public void onExecute(View v) {
        String message;
        if((first_selection == 'a' || first_selection == 'b') && second_selection == 'd'){
            message = "SFR" + first_selection + "," + 'c' + "E";
            bluetoothSend(message);
            message = "SFR" + 'c' + ',' + second_selection + "E";
        }else{
            message = "SFR" + first_selection + "," + second_selection + "E";
            Log.d(LOG_TAG, "Sende Nachricht: " + message);
        }
        bluetoothSend(message);
        resetSignals();
    }

    public void bluetoothSend(String message) {
        byte[] msgBuffer = message.getBytes();
        if (globalVariables.isConnected()) {
            Log.d(LOG_TAG, "Sende Nachricht: " + message);
            try {
                globalVariables.getStreamOut().write(msgBuffer);
            } catch (IOException e) {
                Log.e(LOG_TAG,
                        "Bluetest: Exception beim Senden: " + e.toString());
            }
        }
    }


    public void bluetoothReceive() {
        int length; // Anzahl empf. Bytes
        StringBuilder msg = new StringBuilder();

        try {
            if (globalVariables.getStreamIn().available() > 0) {
                length = globalVariables.getStreamIn().read(buffer);
                Log.d(LOG_TAG,
                        "Anzahl empfangender Bytes: " + length);

                for (int i = 0; i < length; i++) {
                    //  Daten verarbeiten, wenn 1. Buchstabe 'S'
                    splitReceivedData(i);
                    //  Message zusammensetzen:
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

    public void splitReceivedData(int bPos) {
        if ((char) buffer[bPos] == 83){
//83 = "S" //48 = "0"//66 = "B"//69 = "E"//87 = "W"//80 = "P" //70 = "F"//65 = "A"//78 = "N"
            //"S" gefunden
            Log.d(LOG_TAG, "S gefunden");
            if ((char) buffer[bPos+1] == 66){  //Belegtmeldung
                //"B" gefunden
                Log.d(LOG_TAG, "B gefunden");
                receivedTrackOccupied(bPos + 1);
            }
            if ((char) buffer[bPos+1] == 87 && (char) buffer[bPos+2] == 80){  //Weichen Position
                Log.d(LOG_TAG, "WP gefunden"); //man soll z.B.: "SWP2,0" bekommen
                setSwitch(bPos + 2);

            }

            if ((char) buffer[bPos+1] == 70 && (char) buffer[bPos+2] == 80){ //Fahrstrassen Status
                Log.d(LOG_TAG, "FP gefunden"); //man soll z.B.: "SFP000000000" bekommen
                receivedRouteStatus(bPos + 3);
            }
        }
    }

    public void receivedTrackOccupied(int bPos){
        Log.d(LOG_TAG, "Belegtmeldung empfangen");
        for (int i=1; i < 7; i++) {
            //  48 -> Frei  49 -> Belegt
            canvas.isTrackOccupied[i - 1] = buffer[bPos + i] != 48;
        }
    }

    public void setSwitch(int bPos){
        //int weiche = buffer[bPos+1]-48;
        boolean pos = false;
        for(int i=1; i<5; i++){

            switch (buffer[bPos+i]){
                case 48: pos = false;
                    break;  //  gerade
                case 49: pos = true;
                    break;  // abzweigend
            }
            Log.d(LOG_TAG, i-1 +" "+ pos);
            canvas.currentSwitchStates[i-1] = pos;
        }
    }

    public void receivedRouteStatus(int bPos) {
        for(int route = 0; route<canvas.routes.length; route++){
            Log.d(LOG_TAG, "Fahrstrasse: " + (bPos + route) + "\t" + (char) (buffer[bPos + route]));
            switch ((char) buffer[bPos + route]){
                case 49:  // Fahrstrasse angefragt/wird eingestellt
                    canvas.statusOfRoutes[route] = 1;
                    Log.d(LOG_TAG, "Die" + route);
                    break;
                case 50:  // 2: Fahrweg gesichert
                    canvas.statusOfRoutes[route] = 2;  // Fahrstrasse festegelegt
                    break;
                case 51:  // 3: Signal geschaltet
                case 52:  // 4: Gleis geschaltet
                    for(int signal=0; signal<5; signal++){
                        if(canvas.routesLockTable[route][signal] == 1) {
                            canvas.signalStates[signal] = 2;  // Signal auf grün stellen
                            break;
                        }
                    }
                case 53:  // 5: Signal/Gleis aus -> zug schon in Abschnitt
                    canvas.statusOfRoutes[route] = 3;
                    for(int signal=0; signal<5; signal++){
                        if(canvas.routesLockTable[route][signal] == 1) {
                            canvas.signalStates[signal] = 0;  // Signal auf rot stellen
                            break;
                        }
                    }
                    break;
                // Fahrstrasse wird befahren
                // Signal auf rot stellen
                default:
                    canvas.statusOfRoutes[route] = 0;
                    for(int signal=0; signal<5; signal++){
                        if(canvas.routesLockTable[route][signal] == 1) {
                            canvas.signalStates[signal] = 0;  // Signal auf rot stellen
                            break;
                        }
                    }
                    break;     // Fahrstrasse nicht eingestellt
            }
        }
    }

    public int boolToInt(boolean b){
        if(b) return 1;
        else return 0;
    }


    public void startThread() {
        stopThread = false;
        receiveThread receiver = new receiveThread();
        bluetoothReceiveThread = new Thread(receiver);  //neuen Thread erstellen
    }

    public void stopThread() {
        stopThread = true;
    }

    class receiveThread implements Runnable{

        @Override
        public void run() {
            if (stopThread)
                return;
            while (!isFinishing()){
                bluetoothReceive();
                try {
                    if(globalVariables.getStreamIn().available() > 0){ // ToDo
                        bluetoothReceive();
                    } else Thread.sleep(100);  // ToDo: versuchen zu entfernen
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
