#include <Arduino.h>
#include <Servo.h>

#ifndef FormSignal_h
#define FormSignal_h

class FormSignal{
  public:
    FormSignal(int);
    void setSignal(boolean);
  private:
    int steps = 80;
    int servoSpeed = 10;
    boolean pos = 0;
    int servoPin;
    Servo signalServo;
    void openSignal();
    void closeSignal();
};
#endif
