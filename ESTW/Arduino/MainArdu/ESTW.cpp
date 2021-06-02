#include "ESTW.h"

ESTW::ESTW(){/*initialisieren*/}

void ESTW::setSwitch(int weiche, int pos){
  targetSwitchState[weiche] = pos;
}

void ESTW::setAllSwitches(){
  for(int Switch=0; Switch<4; Switch++){
    if(lockedSwitches[Switch] == 0){  // ist Weiche nicht gesperrt kann sie gestellt werden
      if(currentSwitchState[Switch] != targetSwitchState[Switch]){  // Weichen Rückmeldung entsprich nicht dem Soll-data
        /*Serial.print("Setze Weiche ");
        Serial.print(Switch);
        Serial.print(" auf ");
        Serial.println(targetSwitchState[Switch]);
        //currentSwitchState[Switch] = targetSwitchState[Switch];*/    //  position in currentSwitchState array eintragen
        
        bitWrite(dataOut1, ((Switch+1)*2)-targetSwitchState[Switch]-1, 1);  //Weichen Relais anziehen
      }else{
        bitWrite(dataOut1, ((Switch+1)*2)-targetSwitchState[Switch]-1, 0);  //Weichen Relais abfallen
      }
    }
  }
}

void ESTW::uartSendSwitchStates(){
  //Weichenposition an App senden
  Serial.print("SWP");
  for(int i=0; i<sizeof(currentSwitchState); i++){
    Serial.print(currentSwitchState[i]);
  }
  Serial.println("\n");
}

void ESTW::uartSendRouteStates(){
  Serial.print("SFP");
  for(int i=0; i<sizeof(statusOfRoutes); i++){
    Serial.print(statusOfRoutes[i]);
  }
  Serial.println("\n");
}

int ESTW::isRouteAvailable(char buffer[20]){
  char start = buffer[3];  // Start-Signal
  char ziel = buffer[5];  // Ziel-Signal
  boolean vorhanden = false;
  if(buffer[4] == ','){  //zusätzliche überprüfung ob ein komma vorhanden ist
    for(int i=0; i<sizeof(routes); i++){
      if(routes[i][0] == start and routes[i][1] == ziel){
        return i;
        vorhanden = true;
      }
    }
    if(!vorhanden){
      return 100;
    }
  }
}

boolean ESTW::isRouteClear(int route){
  for(int i=9; i<15; i++){
    //      wird dieses Gleis gebraucht            und       ist es Frei?
    if(routesLockTable[route][i] == 1 and isTrackOccupied[i-9] == 1){
      return false; // Falls es nicht Frei ist -> return false
    }
  }
  return true;
}

void ESTW::secureRoute(int route){
  boolean richtigeWPos = false;
  while(!richtigeWPos){  // Solange nicht alle Weichen richtig stehen: Weichen schalten
  for(int i=5; i<9; i++){  // 5,6,7,8 (alle Weichen durchgehen)
    switch(routesLockTable[route][i]){  // Weichen schalten
      case 1: setSwitch(i-5, 1); break;  // abzweigend
      case 2: setSwitch(i-5, 0); break;  // gerade
    }
    setAllSwitches();
    // prüfen ob alle Weichenpositionen richtig sind
    // Weichenposition = Weichenposition aus Verschlussplan      oder Weichenposition ist egal
    if(currentSwitchState[0] == routesLockTable[route][i]-1 or routesLockTable[route][i] == 0 and
       currentSwitchState[1] == routesLockTable[route][i]-1 or routesLockTable[route][i] == 0 and
       currentSwitchState[2] == routesLockTable[route][i]-1 or routesLockTable[route][i] == 0 and
       currentSwitchState[3] == routesLockTable[route][i]-1 or routesLockTable[route][i] == 0){
      richtigeWPos = true;
    }
  }
  }
  for(int i=0; i<4; i++){
    // richtige Weichen sperren
    if(currentSwitchState[i] == routesLockTable[route][i+5]-1 and routesLockTable[route][i+5] != 0){
      lockedSwitches[i] = true;
      Serial.println("weiche gesperrt");
    }
  }
}

void ESTW::setSignal(int route, boolean pos){
  for(int i=0; i<5; i++){

    //  Signal einschalten
    if((routesLockTable[route][i] == 1) and (pos == 1)){
      turnOnSignalOfRoute(route);

    //  Signal ausschalten
    }else if((routesLockTable[route][i] == 1) and (pos == 0)){
      turnOffSignalOfRoute(route);
    }
  }
}

void ESTW::turnOnSignalOfRoute(int route){
  //  Fahrstraße AC
  if(route == 0){
    i2c_data.valueSignal1 = 1;  // Formsignal
  //  Fahrstraße BC
  }else if(route == 1){
    i2c_data.valueSignal2 = 1;  // Formsignal
  //  Fahrstraße DN
  }else if(route == 3){
    KS1.setSignalPattern(2);
  //  Fahrstraße DE      DA, und AC nicht auf "fahrt"
  }else if(route == 4 or (route == 5 and statusOfRoutes[0] != 4)){
    KS1.setSignalPattern(7);
  //  Fahrstraße DA, und AC auf "fahrt"       AC, und DA auf "fahrt"
  }else if((route == 5 and statusOfRoutes[0] == 4) or (statusOfRoutes[5] == 4 and route == 0)){
    KS1.setSignalPattern(5);
  //  Fahrstraße EN
  }else if(route == 6){
    KS2.setSignalPattern(2);
  //  Fahrstraße EE        EA, und AC nicht auf "fahrt"
  }else if(route == 7 or (route == 8 and statusOfRoutes[0] != 4)){
    KS2.setSignalPattern(7);
  //  Fahrstraße EA, und AC auf "fahrt"       AC, und EA auf "fahrt"
  }else if((route == 8 and statusOfRoutes[0] == 4) or (statusOfRoutes[8] == 4 and route == 0)){
    KS2.setSignalPattern(5);
  }
}

void ESTW::turnOffSignalOfRoute(int route){
  if(routesLockTable[route][0] == 1){
    i2c_data.valueSignal1 = 0;
  }
  if(routesLockTable[route][1] == 1){
    i2c_data.valueSignal2 = 0;
  }
  if(routesLockTable[route][3] == 1){
    KS1.setSignalPattern(1);
  }
  if(routesLockTable[route][4] == 1){
    KS2.setSignalPattern(1);
  }
}

void ESTW::setPowerOfTrack(int route, boolean pos){
  for(int i=0; i<5; i++){
    if((routesLockTable[route][i] == 1) and (pos == 1)){
      bitWrite(dataOut2, i, 1); // evtl. 0 ?
    }else if((routesLockTable[route][i] == 1) and (pos == 0)){
      bitWrite(dataOut2, i, 0);
    }
  }
}

boolean ESTW::isTrainArrived(int route) {
  byte count = 0;
  byte sum = 0;
  for(int b=0; b<6; b++) {  // alle Gleise durchgehen
    //  alle gebrauchten Fahrstraßen sind Frei
    if(routesLockTable[route][b+9] == 1) {
      sum++;
    }
    if(routesLockTable[route][b+9] == 1 and isTrackOccupied[b] == 0) {
      count++;
    //  gebrauchte Fahrstraße ist belegt und Zielgleis
    }else if(routesLockTable[route][b+9] == 1 and isTrackOccupied[b] == 1 and b+1 == destinationTrack[route]) {
      count++;
    }
  }
  if(count==sum){
    return true;    
  }
  return false;
}

void ESTW::cancelRoute(int route) {  
  for(int i=5; i<9; i++){
    //  Weiche wurde benutzt
    if(routesLockTable[route][i] != 0) {
      lockedSwitches[i-5] = 0;
    }
  }
}

void ESTW::outputShiftRegister(){
  // Daten am Schieberegister-OUT ausgeben
  digitalWrite(speicherOut, LOW);
  shiftOut(datenOut, taktOut, MSBFIRST, dataOut2);
  shiftOut(datenOut, taktOut, MSBFIRST, dataOut1);
  digitalWrite(speicherOut, HIGH);
}
void ESTW::outputPCF8574(){
  PCFOutputBoard1.write8(~dataOut1);
  PCFOutputBoard2.write8(~dataOut2);
}

void ESTW::inputShiftRegister(){
  // Daten vom Schieberegister-IN einlesen
  digitalWrite(taktIn, HIGH);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, LOW);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, HIGH);
  
  int in1 = shiftIn(datenIn, taktIn, MSBFIRST); in1 =~ in1;
  int in2 = shiftIn(datenIn, taktIn, MSBFIRST); in2 =~ in2;

  //  Gleisbelegtmeldung an App senden
  Serial.print(char(StartTag)); Serial.print(char(BelegtmeldungsTag));
  for(int i=0; i<8; i++){
    shiftIn1[i] = bitRead(in1, i); //Rückmeldungen in arrays eintragen
    shiftIn2[i] = bitRead(in2, i);
    Serial.print(shiftIn1[i]);
    if(i < 4){
      currentSwitchState[i] = shiftIn2[i];
    }
    if(i < 6){
      isTrackOccupied[i] = shiftIn1[i];
    }
    if(i == 0 and controlMode){ // Gleisunterbrechungen nach Signalen Schalten
      bitWrite(dataOut2, 0, not currentSwitchState[i]);
      bitWrite(dataOut2, 1, currentSwitchState[i]);
    }else if(i == 2 and controlMode){
      bitWrite(dataOut2, 3, not currentSwitchState[i]);
      bitWrite(dataOut2, 4, currentSwitchState[i]);
    }
  }
  Serial.println();
  /*for(int i=0; i<8; i++){
    Serial.print(shiftIn2[i]);
  }
  Serial.println();*/
}
