#include <Arduino.h>
#include "Neopixel.h"

Neopixel::Neopixel(Adafruit_NeoPixel& neopixels, int neo_id, byte neo_type, byte index1[2], byte index2[2]): neopixels(neopixels) {
  id = neo_id;
  type = neo_type;

  first_index[0] = index1[0]; first_index[1] = index1[1];
  second_index[0] = index2[0]; second_index[1] = index2[1];
}

Neopixel::Neopixel(Adafruit_NeoPixel& neopixels, int neo_id, byte neo_type, byte index1[2]): neopixels(neopixels) {
  neopixels = neopixels;
  id = neo_id;
  type = neo_type;

  first_index[0] = index1[0]; first_index[1] = index1[1];
}

void Neopixel::updateleds(byte received_data[4]) {
  if(type == 0){
    byte r, g, b;
    g = 0;
    b = 0;
    switch(bitRead(received_data[first_index[0]], first_index[1])){
      case 0: r=0; break;
      case 1: r=BRIGHTNESS_GLEIS; break;
    }
    neopixels.setPixelColor(id, neopixels.Color(g, r, b));
  }

  if(type == 1){
    byte r, g, b;
    switch(bitRead(received_data[first_index[0]], first_index[1])){
      case 0: g=BRIGHTNESS_GLEIS; r=0; break;
      case 1: g=0; r=BRIGHTNESS_GLEIS; break;
    }
    switch(bitRead(received_data[second_index[0]], second_index[1])){
      case 0: b=0; break;
      case 1: b=BRIGHTNESS_LED; break;
    }
    neopixels.setPixelColor(id, neopixels.Color(g, r, b));
  }

  if(type == 2){
    byte r, g, b;
    switch(bitRead(received_data[first_index[0]], first_index[1])){
      case 0: g=BRIGHTNESS_LED; r=0; break;
      case 1: g=0; r=BRIGHTNESS_LED; break;
    }
    switch(bitRead(received_data[second_index[0]], second_index[1])){
      case 0: b=0; break;
      case 1: b=BRIGHTNESS_LED; break;
    }
    neopixels.setPixelColor(id, neopixels.Color(g, r, b));
  }

  if(type == 3){
    byte r, g, b;
    b = 0;
    switch(bitRead(received_data[first_index[0]], first_index[1])){
      case 0: r=0; break;
      case 1: r=BRIGHTNESS_GLEIS; break;
    }
    switch(bitRead(received_data[second_index[0]], second_index[1])){
      case 0: g=0; break;
      case 1: g=BRIGHTNESS_WS; break;
    }
    neopixels.setPixelColor(id, neopixels.Color(g, r, b));
  }
}
