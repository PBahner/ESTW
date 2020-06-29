//#include "FormSignal.h"
#include <Wire.h>
#define SLAVE_ADR 3
#define MASTER_ADR 2

union data_u{
  struct{
    int value1;
    int value2;
    int value3;
    int value4;
    int value5;
  };
  byte bytes[10];
};

data_u data;

void setup() {
  Serial.begin(9600);
  Serial.println("Slave");
  
  Wire.begin(SLAVE_ADR);
  getData();
}

void loop() {}

void getData(){
  Wire.requestFrom(MASTER_ADR, sizeof(data));
  for (unsigned int i = 0; i < sizeof(data); i++)
    data.bytes[i] = Wire.read();

  Serial.println(data.value1);
  Serial.println(data.value2);
  Serial.println(data.value3);
  Serial.println(data.value4);
  Serial.println(data.value5);
}
