#include "KsSignal.h"

KsSignal::KsSignal(byte p1, byte p2, byte p3, byte p4){
  pin1 = p1;
  pin2 = p2;
  pin3 = p3;
  pin4 = p4;
  pinMode(pin1, OUTPUT);
  pinMode(pin2, OUTPUT);
  pinMode(pin3, OUTPUT);
  pinMode(pin4, OUTPUT);
  
  setPinMode(OUTPUT, 0, 0, OUTPUT);
  digitalWrite(pin1, HIGH);
  digitalWrite(pin4, LOW);
}

void KsSignal::setSignalPattern(byte sb){
  SignalPattern = sb;
  // Hp0
  if (SignalPattern == 1){
    setPinMode(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, HIGH);
    digitalWrite(pin4, LOW);
  }
  // Ks1
  if (SignalPattern == 2){
    setPinMode(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
  }
  // Ks2
  if (SignalPattern == 3){
    setPinMode(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
  }
  // Ks1 + Zs3
  if (SignalPattern == 5){
    setPinMode(OUTPUT, OUTPUT, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
  }
}

void KsSignal::updateSignalPattern(){
  // Ks1 blinkend + Zs3v (weiß)
  if ((millis() % 2000 <= 1000) and (SignalPattern == 4 or SignalPattern == 9)){
    // Ks1
    setPinMode(0, 0, 0, 0);
    setPinMode(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
    delay(1);
    // Zs3v
    setPinMode(0, 0, 0, 0);
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(1020);
    if (SignalPattern == 9){
      setPinMode(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  } else if((millis() % 2000 <= 2000) and (SignalPattern == 4 or SignalPattern == 9)){
    // Zs3v
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delay(2);
    if (SignalPattern == 9){
      setPinMode(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }
  
  // Ks1 blinkend + Zs3v + Zs3 (weiß)
  if ((millis() % 2000 <= 1000) and (SignalPattern == 6 or SignalPattern == 10)){
    // Ks1
    setPinMode(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(300);
    // Zs3v
    setPinMode(0, 0, 0, 0);
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(500);
    // Zs3
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(400);
    
    if (SignalPattern == 10){
      setPinMode(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
      setPinMode(0, 0, 0, 0);
      delay(2);
    }
  }
  else if((millis() % 2000 <= 2000) and (SignalPattern == 6 or SignalPattern == 10)){
    // Zs3v
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(1500);
    // Zs3
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(1400);
    
    setPinMode(0, 0, 0, 0);
    if (SignalPattern == 10){
      setPinMode(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }

  // Ks2 + Zs3 (weiß)
  if (SignalPattern == 7 or SignalPattern == 11){
    // Zs3
    setPinMode(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delay(1);
    // Ks2
    setPinMode(0, 0, 0, 0);
    setPinMode(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
    delay(1);
    if (SignalPattern == 11){
      setPinMode(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }
  
  // Ks2 + weiß
  if (SignalPattern == 8){
    // Ks2
    setPinMode(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
    delay(2);
    // weiß
    setPinMode(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, HIGH);
    digitalWrite(pin3, LOW);
  }
}

void KsSignal::setPinMode(boolean p1, boolean p2, boolean p3, boolean p4){
  pinMode(pin1, p1);
  pinMode(pin2, p2);
  pinMode(pin3, p3);
  pinMode(pin4, p4);
}
