#include "FormSignal.h"

FormSignal::FormSignal(int pin){
  servoPin = pin;
  signalServo.attach(servoPin);
}

void FormSignal::setSignal(boolean setPos){
  switch(setPos){
    case 1: openSignal(); break;
    default: closeSignal(); break;
  }
}

void FormSignal::openSignal(){
  if(!pos){
    for (int servoStep = 0; servoStep <= steps; servoStep++) {
      // in steps of 1 degree
      signalServo.write(servoStep);
      delay(servoSpeed);
    }
  pos = true;
  }
}

void FormSignal::closeSignal(){
  if(pos){
    for (int servoStep = steps; servoStep >= 0; servoStep--) {
      // in steps of 1 degree
      signalServo.write(servoStep);
      delay(servoSpeed);
    }
  pos = false;
  }
}
