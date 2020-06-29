#include "FormSignal.h"
#include <Wire.h>
#define MASTER_ADR 2
#define SLAVE_ADR 3

union i2c_data{
  struct{
    int valueSignal1;
    int valueSignal2;
  };
  byte bytes[4];
};

i2c_data data;
unsigned long millis_before = 0;

FormSignal FormSignal1 = FormSignal(2);
FormSignal FormSignal2 = FormSignal(3);

////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  Serial.begin(9600);
  Wire.begin(SLAVE_ADR);
}

////////////////////////////////////LOOP////////////////////////////////////
void loop() {
  if(millis() >= millis_before+10){
    getData();
    millis_before = millis();
  }
}

void getData(){
  Wire.requestFrom(MASTER_ADR, sizeof(data));
  for (unsigned int i = 0; i < sizeof(data); i++)
    data.bytes[i] = Wire.read();

  FormSignal1.setSignal(data.valueSignal1);
  FormSignal2.setSignal(data.valueSignal2);
  Serial.println(data.valueSignal1);
  Serial.println(data.valueSignal2);
}
