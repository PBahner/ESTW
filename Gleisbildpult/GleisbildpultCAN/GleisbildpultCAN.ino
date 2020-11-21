#include <SPI.h>
#include <mcp2515.h>
#include <Adafruit_NeoPixel.h>
#include "Neopixel.h"

#define DLC_MSG_STELLPULT 5
#define PIN 2
#define LENGTH 20

struct can_frame canMsg;
MCP2515 mcp2515(10);

Adafruit_NeoPixel neopixels = Adafruit_NeoPixel(LENGTH, PIN, NEO_GRB + NEO_KHZ800);

byte l0_0[2] = {0,0};
byte l0_1[2] = {0,1};
byte l0_2[2] = {0,2};
byte l0_3[2] = {0,3};
byte l0_4[2] = {0,4};
byte l0_5[2] = {0,5};
byte l0_6[2] = {0,6};
byte l0_7[2] = {0,7};

byte l1_0[2] = {1,0};
byte l1_1[2] = {1,1};
byte l1_2[2] = {1,2};
byte l1_3[2] = {1,3};
byte l1_4[2] = {1,4};
byte l1_5[2] = {1,5};
byte l1_6[2] = {1,6};
byte l1_7[2] = {1,7};

byte l2_0[2] = {2,0};
byte l2_1[2] = {2,1};
byte l2_2[2] = {2,2};
byte l2_3[2] = {2,3};
byte l2_4[2] = {2,4};
byte l2_5[2] = {2,5};
byte l2_6[2] = {2,6};
byte l2_7[2] = {2,7};

byte l3_0[2] = {3,0};
byte l3_1[2] = {3,1};
byte l3_2[2] = {3,2};
byte l3_3[2] = {3,3};
byte l3_4[2] = {3,4};
byte l3_5[2] = {3,5};
byte l3_6[2] = {3,6};
byte l3_7[2] = {3,7};

byte l4_0[2] = {4,0};
byte l4_1[2] = {4,1};
byte l4_2[2] = {4,2};
byte l4_3[2] = {4,3};
byte l4_4[2] = {4,4};
byte l4_5[2] = {4,5};
byte l4_6[2] = {4,6};
byte l4_7[2] = {4,7};

// (neopixels, id, type, index1, index2)
// 0=Gleis 1=Weiche 2=Signal 3=Rangier

// WEICHEN
//Neopixel w1(neopixels,     0, 1, l1_0, l1_6);
Neopixel w2(neopixels,      12, 1, l1_1, l1_7);
Neopixel w3(neopixels,       6, 1, l1_2, l2_0);
Neopixel w4(neopixels,      11, 1, l1_3, l2_1);
Neopixel w5(neopixels,      17, 1, l1_4, l2_2);
Neopixel Gl1CurveAbove(neopixels,   0, 0, l0_0);
Neopixel Gl1CurveBelow(neopixels,   5, 0, l0_4);

// RANGIERSIGNALE
Neopixel RaA(neopixels,     2, 3, l0_0, l3_3);
Neopixel RaB(neopixels,     8, 3, l0_1, l3_4);
Neopixel RaC(neopixels,    14, 3, l0_2, l3_5);

//Gleisstücken dazwischen
Neopixel GlAboveMiddle(neopixels,  18, 0, l4_0);
Neopixel GlBelowMiddle(neopixels,  19, 0, l4_1);


// GLEISE (Felder mit Beschriftung)
Neopixel Gl1(neopixels,     1, 0, l0_0);
Neopixel Gl2(neopixels,     7, 0, l0_1);
Neopixel Gl3(neopixels,    13, 0, l0_2);

// GLEISE am Signal
Neopixel Gl1Sig(neopixels,  3, 0, l0_4);
Neopixel Gl2Sig(neopixels,  9, 0, l0_5);
Neopixel Gl3Sig(neopixels, 15, 0, l0_6);

// SIGNALE
Neopixel SigA(neopixels,    4, 2, l2_5, l3_0);
Neopixel SigB(neopixels,   10, 2, l2_6, l3_1);
Neopixel SigC(neopixels,   16, 2, l2_7, l3_2);

// Anzahl der neopixels umstellen nicht vergessen

void setup() {
  neopixels.begin();

  // setup CAN
  mcp2515.reset();
  mcp2515.setBitrate(CAN_125KBPS);
  mcp2515.setNormalMode();
  
  Serial.begin(115200);
  Serial.println("Ready!");
}

void loop() {
  //Serial.print(w1.getID());
  //Serial.print(w1.getLEDgerade());
  //Serial.print(w1.getLEDabzweigend());
  //Serial.println(w1.getLEDverschluss());
  //delay(50);
  if (mcp2515.readMessage(&canMsg) == MCP2515::ERROR_OK) {
    Serial.print(canMsg.can_id); // print ID
    Serial.print(" "); 
    Serial.print(canMsg.can_dlc); // print DLC
    Serial.print(" ");
    receiveEventCAN();
  }
}

void receiveEventCAN() {
  byte receive_data[DLC_MSG_STELLPULT] = {};
  for(int i=0; i<DLC_MSG_STELLPULT; i++){
    receive_data[i] = canMsg.data[i];
    Serial.print(receive_data[i]); 
  }
  Serial.println(" empfangen");         // print the integer

  //w1.updateleds(receive_data);
  w2.updateleds(receive_data);
  w3.updateleds(receive_data);
  w4.updateleds(receive_data);
  w5.updateleds(receive_data);
  Gl1CurveAbove.updateleds(receive_data);
  Gl1CurveBelow.updateleds(receive_data);
  
  RaA.updateleds(receive_data);
  RaB.updateleds(receive_data);
  RaC.updateleds(receive_data);
  
  Gl1.updateleds(receive_data);
  Gl2.updateleds(receive_data);
  Gl3.updateleds(receive_data);
  
  Gl1Sig.updateleds(receive_data);
  Gl2Sig.updateleds(receive_data);
  Gl3Sig.updateleds(receive_data);

  GlAboveMiddle.updateleds(receive_data);
  GlBelowMiddle.updateleds(receive_data);
  
  SigA.updateleds(receive_data);
  SigB.updateleds(receive_data);
  SigC.updateleds(receive_data);
  
  neopixels.show();
}