// Test code from https://forum.arduino.cc/index.php?topic=466483.0

#include <Wire.h>
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
  Wire.begin(MASTER_ADR);
  data.value1 = 1;
  data.value2 = 4;
  data.value3 = 12;
  data.value4 = 30000;
  data.value5 = -10000;

  Wire.onRequest(requestEvent);
}

void loop() {}

void requestEvent(){
 Wire.write(data.bytes, sizeof(data));
}
