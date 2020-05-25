#include "ESTW.h"

ESTW::ESTW(){/*initialisieren*/}

void ESTW::weicheSchalten(int weiche, int pos){
  weichenSoll[weiche] = pos;
}

void ESTW::weichenSchalten(){
  for(int weiche=0; weiche<4; weiche++){
    if(weichen[weiche] != weichenSoll[weiche]){
      /*Serial.print("Setze Weiche ");
      Serial.print(weiche);
      Serial.print(" auf ");
      Serial.println(weichenSoll[weiche]);
      //weichen[weiche] = weichenSoll[weiche];*/    //position in weichen array eintragen
      
      bitWrite(wertOut1, ((weiche+1)*2)-weichenSoll[weiche]-1, 1);  //Weichen Relais anziehen
    }else{
      bitWrite(wertOut1, ((weiche+1)*2)-weichenSoll[weiche]-1, 0);  //Weichen Relais abfallen
    }
  }
}

void ESTW::weichenPosSenden(){
  //Weichenposition an App senden
  Serial.print("SWP");
  for(int i=0; i<4; i++){
    Serial.print(weichen[i]);
  }
}

int ESTW::fahrstrasseVorhanden(char buffer[20]){
  char start = buffer[3];   // Start-Signal
  char ziel = buffer[5];    // Ziel-Signal
  boolean vorhanden = false;
  if(buffer[4] == ','){     //zusätzliche überprüfung ob ein komma vorhanden ist
    for(int i=0; i<sizeof(fahrstrassen); i++){
      if(fahrstrassen[i][0] == start and fahrstrassen[i][1] == ziel){
        return i;
        vorhanden = true;
      }
    }
    if(!vorhanden){
      return 100;
    }
  }
}

boolean ESTW::fahrwegFrei(int fahrstrasse){
  for(int i=9; i<15; i++){
    //      wird dieses Gleis gebraucht            und       ist es Frei?
    if(fahrstrassenVerschluss[fahrstrasse][i] == 1 and belegtmeldung[i-9] == 1){
      return false; // Falls es nicht Frei ist -> return false
    }
  }
  return true;
}

void ESTW::fahrwegSichern(int fahrstrasse){
  boolean richtigeWPos = false;
  while(!richtigeWPos){
  for(int i=5; i<9; i++){
    switch(fahrstrassenVerschluss[fahrstrasse][i]){
      case 1: weicheSchalten(i-5, 1); break;
      case 2: weicheSchalten(i-5, 0); break;
    }
    if(weichen[0] == fahrstrassenVerschluss[fahrstrasse][i]-1 or weichen[0] == 0 and
       weichen[1] == fahrstrassenVerschluss[fahrstrasse][i]-1 or weichen[1] == 0 and
       weichen[2] == fahrstrassenVerschluss[fahrstrasse][i]-1 or weichen[2] == 0 and
       weichen[3] == fahrstrassenVerschluss[fahrstrasse][i]-1 or weichen[3] == 0){
      richtigeWPos = true;
    }
  }
  }
}

void ESTW::signalSchalten(int fahrstrasse, boolean pos){
  for(int i=0; i<5; i++){
    if((fahrstrassenVerschluss[fahrstrasse][i] == 1) and (pos == 1)){
      if(fahrstrasse == 3){
        KS1.setSignalbild(2);
      }else if(fahrstrasse == 4 or (fahrstrasse == 5 and einzustellendeFahrstrasse[0] != 4)){
        KS1.setSignalbild(7);
      }else if((fahrstrasse == 5 and einzustellendeFahrstrasse[0] == 4) or (einzustellendeFahrstrasse[5] == 4 and fahrstrasse == 0)){
        KS1.setSignalbild(5);
      }
      if(fahrstrasse == 6){
        KS2.setSignalbild(2);
      }else if(fahrstrasse == 7 or (fahrstrasse == 8 and einzustellendeFahrstrasse[0] != 4)){
        KS2.setSignalbild(7);
      }else if((fahrstrasse == 8 and einzustellendeFahrstrasse[0] == 4) or (einzustellendeFahrstrasse[8] == 4 and fahrstrasse == 0)){
        KS2.setSignalbild(5);
      }
    }else if((fahrstrassenVerschluss[fahrstrasse][i] == 1) and (pos == 0)){
      if(fahrstrassenVerschluss[fahrstrasse][3] == 1){
        KS1.setSignalbild(1);
      }
      if(fahrstrassenVerschluss[fahrstrasse][4] == 1){
        KS2.setSignalbild(1);
      }
    }
  }
}

void ESTW::gleisSchalten(int fahrstrasse, boolean pos){
  for(int i=0; i<5; i++){
    if((fahrstrassenVerschluss[fahrstrasse][i] == 1) and (pos == 1)){
      bitWrite(wertOut2, i, 1); // evtl. 0 ?
    }else if((fahrstrassenVerschluss[fahrstrasse][i] == 1) and (pos == 0)){
      bitWrite(wertOut2, i, 0);
    }
  }
}

void ESTW::output(){
  // Daten am Schieberegister-OUT ausgeben
  digitalWrite(speicherOut, LOW);
  shiftOut(datenOut, taktOut, MSBFIRST, wertOut2);
  shiftOut(datenOut, taktOut, MSBFIRST, wertOut1);
  digitalWrite(speicherOut, HIGH);
}

void ESTW::input(){
  // Daten vom Schieberegister-IN einlesen
  digitalWrite(taktIn, HIGH);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, LOW);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, HIGH);
  
  int in1 = shiftIn(datenIn, taktIn, MSBFIRST); in1 =~ in1;
  int in2 = shiftIn(datenIn, taktIn, MSBFIRST); in2 =~ in2;

  //Gleisbelegtmeldung an App senden
  Serial.print(char(StartTag)); Serial.print(char(BelegtmeldungsTag));
  for(int i=0; i<8; i++){
    shiftIn1[i] = bitRead(in1, i); //Rückmeldungen in arrays eintragen
    shiftIn2[i] = bitRead(in2, i);
    Serial.print(shiftIn1[i]);
    if(i < 4){
      weichen[i] = shiftIn2[i];
    }
    if(i < 6){
      belegtmeldung[i] = shiftIn1[i];
    }
    if(i == 0 and signale_weichen){ // Gleisunterbrechungen nach Signalen Schalten
      bitWrite(wertOut2, 0, not weichen[i]);
      bitWrite(wertOut2, 1, weichen[i]);
    }else if(i == 2 and signale_weichen){
      bitWrite(wertOut2, 3, not weichen[i]);
      bitWrite(wertOut2, 4, weichen[i]);
    }
  }
  Serial.println();
  /*for(int i=0; i<8; i++){
    Serial.print(shiftIn2[i]);
  }
  Serial.println();*/
}
