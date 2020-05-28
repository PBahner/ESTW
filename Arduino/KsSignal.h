#include <Arduino.h>

#ifndef KsSignal_h
#define KsSignal_h


class KsSignal{
  public:
    KsSignal(byte, byte, byte, byte);
    void updateSignalbild();
    void setSignalbild(byte);
  private:
    byte Signalbild = 0;
    byte pin1;
    byte pin2;
    byte pin3;
    byte pin4;
    void pinModus(boolean, boolean, boolean, boolean);
};
#endif
