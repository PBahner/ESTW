#include "FormSignal.h"
#include <Wire.h>
#define MASTER_ADR 2
#define SLAVE_ADR 3

union i2c_data{
  struct{
    byte valueSignal1 = 0;
    byte valueSignal2 = 0;
  };
  byte bytes[2];
};

i2c_data requestedData;
unsigned long millis_before = 0;

FormSignal fSignal1;
FormSignal fSignal2;

////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  Serial.begin(9600);
  fSignal1.begin(2);
  fSignal2.begin(3);
  Wire.begin(SLAVE_ADR);
}

////////////////////////////////////LOOP////////////////////////////////////
void loop() {
  if(millis() >= millis_before+200){
    getData();
    millis_before = millis();
  }
}

void getData(){
  // Daten vom Master-Arduino Abfragen
  Serial.println("getData");
  Wire.requestFrom(MASTER_ADR, sizeof(requestedData));
  for (unsigned int i = 0; i < sizeof(requestedData); i++){
    requestedData.bytes[i] = Wire.read();
    // empfangene Daten direkt ausgeben
    Serial.println(requestedData.bytes[i]);
  }
  // Signal 1 stellen
  switch(requestedData.valueSignal1){
    case 1: fSignal1.setSignal(1); break;
    default: fSignal1.setSignal(0); break;
  }
  // Signal 2 stellen
  switch(requestedData.valueSignal2){
    case 1: fSignal2.setSignal(1); break;
    default: fSignal2.setSignal(0); break;
  }
  Serial.println("-------------------");
}
