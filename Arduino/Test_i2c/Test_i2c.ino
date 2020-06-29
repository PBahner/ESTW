// Test code from https://forum.arduino.cc/index.php?topic=466483.0

#include <Wire.h>
#define MASTER_ADR 2

union i2c_data{
  struct{
    int valueSignal1;
    int valueSignal2;
  };
  byte bytes[2];
};

i2c_data data;

////////////////////////////////////SETUP////////////////////////////////////
void setup() {
  Wire.begin(MASTER_ADR);
  data.valueSignal1 = 1;
  data.valueSignal2 = 0;

  Wire.onRequest(requestEvent);
}

////////////////////////////////////LOOP////////////////////////////////////
void loop() {}

void requestEvent(){
 Wire.write(data.bytes, sizeof(data));
}
