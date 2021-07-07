#include "FormSignal.h"
#include <Wire.h>
#define MASTER_ADR 2
#define SLAVE_ADR 3

// Pins für Schieberegister-In
#define datenIn 4
#define taktIn 5
#define pLoadIn 6

union i2c_from_master{
  struct{
    byte valueSignal1 = 0;
    byte valueSignal2 = 0;
  };
  byte bytes[2];
};
union i2c_for_master{
  struct{
    byte input1 = 0;
    byte input2 = 0;
    byte checksum = 0;
  };
  byte bytes[3];
};

i2c_from_master dataFromMaster;
i2c_for_master dataForMaster;
unsigned long millis_before = 0;

FormSignal fSignal1;
FormSignal fSignal2;


////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  Serial.begin(9600);
  
  //Pins für Schieberegister-IN
  pinMode(pLoadIn, OUTPUT);
  pinMode(taktIn, OUTPUT);
  pinMode(datenIn, INPUT);
  inputShiftRegister();

  // Formsignale starten
  fSignal1.begin(2);
  fSignal2.begin(3);

  // I2C
  Wire.begin(SLAVE_ADR);
  Wire.onRequest(i2cRequestEvent);
}


////////////////////////////////////LOOP////////////////////////////////////
void loop() {
  if(millis() >= millis_before+200){
    getData();
    millis_before = millis();
  }
  inputShiftRegister();
}


void getData(){
  // Daten vom Master-Arduino abfragen
  Serial.println("getData");
  Wire.requestFrom(MASTER_ADR, sizeof(dataFromMaster));
  for (unsigned int i = 0; i < sizeof(dataFromMaster); i++){
    dataFromMaster.bytes[i] = Wire.read();
    // empfangene Daten direkt ausgeben
    Serial.println(dataFromMaster.bytes[i]);
  }
  // Signal 1 stellen
  switch(dataFromMaster.valueSignal1){
    case 1: fSignal1.setSignal(1); break;
    default: fSignal1.setSignal(0); break;
  }
  // Signal 2 stellen
  switch(dataFromMaster.valueSignal2){
    case 1: fSignal2.setSignal(1); break;
    default: fSignal2.setSignal(0); break;
  }
  Serial.println("-------------------");
}


void inputShiftRegister(){
  // Daten vom Schieberegister-IN einlesen
  digitalWrite(taktIn, HIGH);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, LOW);
  delayMicroseconds(20);
  digitalWrite(pLoadIn, HIGH);
  
  int in1 = shiftIn(datenIn, taktIn, MSBFIRST); in1 =~ in1;
  int in2 = shiftIn(datenIn, taktIn, MSBFIRST); in2 =~ in2;
  dataForMaster.input1 = in1;
  dataForMaster.input2 = in2;
}


void i2cRequestEvent(){
  // calculate Checksum
  byte checksumInput[2] = {dataForMaster.input1, dataForMaster.input2};
  dataForMaster.checksum = calculateChecksum(checksumInput);
  // write data including checksum to i2c
  Wire.write(dataForMaster.bytes, sizeof(dataForMaster));
  Serial.println("Request!!");
}


// calculate fletchers checksum
byte calculateChecksum(byte inputData[]) {
  byte sum1 = 0;
  byte sum2 = 0;
  for (int i=0; i<sizeof(inputData); i++) {
    sum1 = (sum1 + inputData[i]) % 255;
    sum2 = (sum2 + sum1) % 255;
  }
  byte checksum = (sum1 + sum2) % 255;
  return checksum;
}
