#include "FormSignal.h"

FormSignal::FormSignal(int pin){
  servoPin = pin;
  signalServo.attach(servoPin);
}

void FormSignal::setSignal(boolean pos){
  switch(pos){
    case 1: openSignal(); break;
    default: closeSignal(); break;
  }
}

void FormSignal::openSignal(){
  for (int servoStep = 0; servoStep <= steps; servoStep++) {
    // in steps of 1 degree
    signalServo.write(servoStep);
    delay(servoSpeed);
  }
}

void FormSignal::closeSignal(){
  for (int servoStep = steps; servoStep >= 0; servoStep--) {
    // in steps of 1 degree
    signalServo.write(servoStep);
    delay(servoSpeed);
  }
}
