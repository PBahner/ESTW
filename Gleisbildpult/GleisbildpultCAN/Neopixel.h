#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

#ifndef Neopixel_h
#define Neopixel_h

#define BRIGHTNESS_GLEIS 255
#define BRIGHTNESS_LED 20
#define BRIGHTNESS_WS 1

class Neopixel{
  public:
    Neopixel(Adafruit_NeoPixel& neopixels, int, byte, byte[2], byte[2]);
    Neopixel(Adafruit_NeoPixel& neopixels, int, byte, byte[2]);
    void updateleds(byte[4]);
    
  private:
    Adafruit_NeoPixel& neopixels;
    int id;
    byte first_index[2] = {};
    byte second_index[2] = {};
    byte type;           // 0=Gleis 1=Weiche 2=Signal 3=Rangier
};
#endif
