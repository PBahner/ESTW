#include "ESTW.h"
#include "KsSignal.h"
#include <Wire.h>
ESTW Estw;

// Variablen
char buffer[20]; // Daten Array fuer die einkommenden Seriellen Daten
int bufferCount;
unsigned long previousMillis1 = 0;
unsigned long previousMillis2 = 0;

// I2C Daten
union i2c_struct{
  struct{
    int valueSignal1 = 0;
    int valueSignal2 = 0;
  };
  byte bytes[2];
};
i2c_struct i2c_data;

////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  //Pins für Schieberegister-IN
  pinMode(pLoadIn, OUTPUT);
  pinMode(taktIn, OUTPUT);
  pinMode(datenIn, INPUT);

  //Pins für Schieberegister-OUT
  pinMode(taktOut, OUTPUT);
  pinMode(speicherOut, OUTPUT);
  pinMode(datenOut, OUTPUT);

  //Daten ausgeben
  Estw.output();

  // I2C
  Wire.begin(2);
  Wire.onRequest(i2cRequestEvent);

  Serial.begin(9600); // Serielle Schnittstelle initialisieren
}

////////////////////////////////////LOOP////////////////////////////////////
void loop() {
  for(int i=0; i<sizeof(Estw.einzustellendeFahrstrasse); i++){
    //    Prüfen ob der Fahrweg (Gleise) frei ist
    if(Estw.einzustellendeFahrstrasse[i] == 1){
      if(Estw.fahrwegFrei(i)){        
        Estw.fahrwegSichern(i);   //    Weichen stellen
        Estw.einzustellendeFahrstrasse[i] = 2;
      }
    }
    //    Signal schalten
    if(Estw.einzustellendeFahrstrasse[i] == 2){
      if(Estw.fahrwegFrei(i)){    //    zusätzliche überprüfung ob die Gleise Frei sind   
        Estw.signalSchalten(i, 1);
        Estw.verzoegerungGleisfrei[i] = millis();
        Estw.einzustellendeFahrstrasse[i] = 3;
      }
    }
    //    nach der Verzögerung wird das Gleis geschalten
    if(Estw.einzustellendeFahrstrasse[i] == 3){
      if(Estw.fahrwegFrei(i) and Estw.verzoegerungGleisfrei[i]+2000 <= millis()){
        Estw.gleisSchalten(i, 1);
        Estw.verzoegerungGleisfrei[i] = 0;
        Estw.verzoegerungSignalhalt[i] = millis();
        Estw.einzustellendeFahrstrasse[i] = 4;
      }
    }
    if(Estw.einzustellendeFahrstrasse[i] == 4 and Estw.verzoegerungSignalhalt[i]+2000 <= millis()){
      if(!Estw.fahrwegFrei(i)){
        Estw.gleisSchalten(i, 0);
        Estw.signalSchalten(i, 0);
        Estw.verzoegerungSignalhalt[i] = 0;
        Estw.einzustellendeFahrstrasse[i] = 0;
      }
    }
  }

  if(millis() - previousMillis1 >= 250){
    previousMillis1 = millis();
    Estw.weichenSchalten();
  }

  if(millis() - previousMillis2 >= 100){
    previousMillis2 = millis();
    //Daten Auslesen und Ausgeben
    Estw.input();
    Estw.output();
  
    Estw.weichenPosSenden();
    Estw.fahrstrassenPosSenden();
  }

  Estw.KS1.updateSignalbild();
  Estw.KS2.updateSignalbild();
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
      Estw.weicheSchalten(weiche, pos);

    } else if (buffer[1] == FahrstrassenTag and buffer[2] == AnfrageTag) { // Anfrage für Fahrstraße einstellen erkannt (FR.,.E)
      int numFahrstrasse;
      numFahrstrasse = Estw.fahrstrasseVorhanden(buffer);
      //Serial.print(numFahrstrasse);

      // Wird die Fahrstraße angenommen ?
      if (numFahrstrasse != 100) { // Fahrstrasse muss Vorhanden sein
        Estw.einzustellendeFahrstrasse[numFahrstrasse] = 1;
      } 
    }
    // empfangene Daten zurücksetzen
    for (int i = 0; i < 19; i++) {
      buffer[i] = '.';
    }
  }
}

void i2cRequestEvent(){
  Wire.write(i2c_data.bytes, sizeof(i2c_data));
}
