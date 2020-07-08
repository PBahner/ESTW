#include "FormSignal.h"

void FormSignal::begin(int pin){
  servoPin = pin;
  signalServo.attach(servoPin);
  Serial.print("attaching Servo on pin: ");
  Serial.println(servoPin);
}

void FormSignal::setSignal(boolean setPos){
  Serial.print("SetSignal: ");
  Serial.println(setPos);
  switch(setPos){
    case 1: openSignal(); break;
    case 0: closeSignal(); break;
  }
}

void FormSignal::openSignal(){
  if(!pos){
    Serial.println("Signal öffnen");
    for (int servoStep = 0; servoStep <= steps; servoStep++) {
      // in steps of 1 degree
      Serial.println(servoStep);
      signalServo.write(servoStep);
      delay(servoSpeed);
    }
  pos = true;
  }
}

void FormSignal::closeSignal(){
  if(pos){
    Serial.println("Signal schliessen");
    for (int servoStep = steps; servoStep >= 0; servoStep--) {
      // in steps of 1 degree
      Serial.println(servoStep);
      signalServo.write(servoStep);
      delay(servoSpeed);
    }
  pos = false;
  }
}
