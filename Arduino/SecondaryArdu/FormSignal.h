#include <Arduino.h>
#include <Servo.h>

#ifndef FormSignal_h
#define FormSignal_h

class FormSignal{
  public:
    void begin(int);
    void setSignal(boolean);
  private:
    int steps = 80;
    int servoSpeed = 8;
    boolean pos = 1;
    int servoPin;
    Servo signalServo;
    void openSignal();
    void closeSignal();
};
#endif
