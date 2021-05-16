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

i2c_data data;
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
  Serial.println("getData");
  Wire.requestFrom(MASTER_ADR, sizeof(data));
  Serial.println("getData");
  for (unsigned int i = 0; i < sizeof(data); i++){
    data.bytes[i] = Wire.read();
    Serial.println(data.bytes[i]);
  }

  if(data.valueSignal1 == -1 or data.valueSignal1 == -1){
    fSignal1.setSignal(0);
    delay(10);
    fSignal2.setSignal(0);
    Serial.println("nichts empfangen");
  }else{
    fSignal1.setSignal(data.valueSignal1);
    Serial.println("-------------------");
    delay(10);
    fSignal2.setSignal(data.valueSignal2);
    Serial.println("Signale Schalten");
  }
  Serial.println(data.valueSignal1);
  Serial.println(data.valueSignal2);
  //delay(1000);
}
