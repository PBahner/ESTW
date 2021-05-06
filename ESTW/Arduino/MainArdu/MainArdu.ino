#include "ESTW.h"
#include "KsSignal.h"
#include <PCF8574.h>
#include <Wire.h>

ESTW Estw;


// Variablen
char buffer[20]; // Daten Array fuer die einkommenden Seriellen Daten
int bufferCount;
unsigned long previousMillis1 = 0;
unsigned long previousMillis2 = 0;
unsigned long delayUntilTrackIsPowered[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
unsigned long delayUntilSignalTurnsRed[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};

////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  // Serielle Schnittstelle initialisieren
  Serial.begin(9600);

  // I2C
  Wire.begin(2); // I2C Adresse 2
  Wire.onRequest(i2cRequestEvent);
  
  //Pins für Schieberegister-IN
  pinMode(pLoadIn, OUTPUT);
  pinMode(taktIn, OUTPUT);
  pinMode(datenIn, INPUT);

  //Pins für Schieberegister-OUT
  // pinMode(taktOut, OUTPUT);
  // pinMode(speicherOut, OUTPUT);
  // pinMode(datenOut, OUTPUT);
  
  // initialize PCF8574
  if (!Estw.PCFOutputBoard1.begin()){
    Serial.println("could not initialize board-1...");
  }
  if (!Estw.PCFOutputBoard2.begin()){
    Serial.println("could not initialize board-2...");
  }

  //Daten ausgeben
  // Estw.outputShiftRegister();
}

////////////////////////////////////LOOP////////////////////////////////////
void loop() {
  for(int i=0; i<sizeof(Estw.statusOfRoutes); i++){
    //    Prüfen ob der Fahrweg (Gleise) frei ist
    if(Estw.statusOfRoutes[i] == 1){
      if(Estw.isRouteClear(i)){        
        Estw.secureRoute(i);   //  Weichen stellen
        Estw.statusOfRoutes[i] = 2;
      }
    }
    //    Signal schalten
    if(Estw.statusOfRoutes[i] == 2){
      if(Estw.isRouteClear(i)){    //    zusätzliche überprüfung ob die Gleise Frei sind   
        Estw.setSignal(i, 1);
        delayUntilTrackIsPowered[i] = millis();
        Estw.statusOfRoutes[i] = 3;
      }
    }
    //    nach der Verzögerung wird das Gleis geschalten
    if(Estw.statusOfRoutes[i] == 3){
      if(Estw.isRouteClear(i) and delayUntilTrackIsPowered[i]+2000 <= millis()){
        Estw.setPowerOfTrack(i, 1);
        delayUntilTrackIsPowered[i] = 0;
        delayUntilSignalTurnsRed[i] = millis();
        Estw.statusOfRoutes[i] = 4;
      }
    }
    if(Estw.statusOfRoutes[i] == 4 and delayUntilSignalTurnsRed[i]+2000 <= millis()){
      if(!Estw.isRouteClear(i)){
        Estw.setPowerOfTrack(i, 0);
        Estw.setSignal(i, 0);
        delayUntilSignalTurnsRed[i] = 0;
        Estw.statusOfRoutes[i] = 5;
      }
    }
    if(Estw.statusOfRoutes[i] == 5) {
      // ToDo
      if(Estw.isTrainArrived(i)){
        Estw.cancelRoute(i);
        Estw.statusOfRoutes[i] = 0;
      }
    }
  }

  if(millis() - previousMillis1 >= 250){
    previousMillis1 = millis();
    Estw.setAllSwitches();
  }

  if(millis() - previousMillis2 >= 100){
    previousMillis2 = millis();
    //Daten Auslesen und Ausgeben
    Estw.inputShiftRegister();
    // Estw.outputShiftRegister();
    Estw.outputPCF8574();
  
    Estw.uartSendSwitchStates();
    Estw.uartSendRouteStates();
  }

  Estw.KS1.updateSignalPattern();
  Estw.KS2.updateSignalPattern();
}

////////////////////////////////////SERIAL////////////////////////////////////
void serialEvent() {
  char ch = Serial.read();
  buffer[bufferCount] = ch;
  bufferCount++;
  //Serial.print(bufferCount);
  //Serial.println(buffer);
  if ((ch == EndTag) and (buffer[0] == StartTag)) { //Wird ein Serieller Code empfangen (S...E)
    bufferCount = 0;

    if (buffer[1] == WeichenTag and buffer[2] == UmschaltTag and buffer[4] == ',') { //Weiche stellen erkennen (WU.,.E)
      int weiche = buffer[3] - 48;  //zu stellende Weiche ermitteln
      int pos = 0;
      switch (buffer[5]) { // zu stellende position ermitteln
        case 48: pos = 0; break;
        case 49: pos = 1; break;
      }
      Estw.setSwitch(weiche, pos);

    } else if (buffer[1] == FahrstrassenTag and buffer[2] == AnfrageTag) { // Anfrage für Fahrstraße einstellen erkannt (FR.,.E)
      int numFahrstrasse;
      numFahrstrasse = Estw.isRouteAvailable(buffer);
      //Serial.print(numFahrstrasse);

      // Wird die Fahrstraße angenommen ?
      if (numFahrstrasse != 100) { // Fahrstrasse muss Vorhanden sein
        Estw.statusOfRoutes[numFahrstrasse] = 1;
      } 
    }
    // empfangene Daten zurücksetzen
    for (int i = 0; i < 19; i++) {
      buffer[i] = '.';
    }
  }
}

void i2cRequestEvent(){
  Wire.write(Estw.i2c_data.bytes, sizeof(Estw.i2c_data));
  Serial.println("Request!!");
}
