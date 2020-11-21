#include <Wire.h>
#include <SPI.h>
#include <mcp2515.h>

#define SLAVE_ADDRESS 0x04
#define DLC_MSG_STELLPULT 5

struct can_frame canMsgStellpult;
MCP2515 mcp2515(10);


void setup() {
  Serial.begin(115200);

  // setup CAN
  mcp2515.reset();
  mcp2515.setBitrate(CAN_125KBPS);
  mcp2515.setNormalMode();

  // setup CAN messages
  canMsgStellpult.can_id = 0;
  canMsgStellpult.can_dlc = DLC_MSG_STELLPULT; // how many bytes (CAN)

  // setup I2C
  Wire.begin(SLAVE_ADDRESS); 
  Wire.onReceive(receiveEventI2C);
}

void loop() {delay(50);}

void receiveEventI2C(int howMany) {
  // GET I2C Data
  uint8_t receive_data[howMany] = {};
  byte c = 0;
  while (Wire.available()) { 
    receive_data[c] = Wire.read();
    Serial.print(receive_data[c]); 
    c++;
  }
  Serial.println(" I2C empfangen"); 

  // Write received Data to CAN
  for(byte dlc=0; dlc<DLC_MSG_STELLPULT; dlc++){
    canMsgStellpult.data[dlc] = receive_data[dlc];
    //Serial.print(canMsgStellpult.data[dlc]); 
  }
  mcp2515.sendMessage(&canMsgStellpult);
  Serial.println("CAN gesendet");
  delay(100);
}
