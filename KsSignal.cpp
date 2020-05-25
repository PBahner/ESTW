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
  
  pinModus(OUTPUT, 0, 0, OUTPUT);
  digitalWrite(pin1, HIGH);
  digitalWrite(pin4, LOW);
}

void KsSignal::setSignalbild(byte sb){
  Signalbild = sb;
  // Hp0
  if (Signalbild == 1){
    pinModus(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, HIGH);
    digitalWrite(pin4, LOW);
  }
  // Ks1
  if (Signalbild == 2){
    pinModus(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
  }
  // Ks2
  if (Signalbild == 3){
    pinModus(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
  }
  // Ks1 + Zs3
  if (Signalbild == 5){
    pinModus(OUTPUT, OUTPUT, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
  }
}

void KsSignal::updateSignalbild(){
  // Ks1 blinkend + Zs3v (weiß)
  if ((millis() % 2000 <= 1000) and (Signalbild == 4 or Signalbild == 9)){
    // Ks1
    pinModus(0, 0, 0, 0);
    pinModus(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
    delay(1);
    // Zs3v
    pinModus(0, 0, 0, 0);
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(1020);
    if (Signalbild == 9){
      pinModus(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  } else if((millis() % 2000 <= 2000) and (Signalbild == 4 or Signalbild == 9)){
    // Zs3v
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delay(2);
    if (Signalbild == 9){
      pinModus(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }
  
  // Ks1 blinkend + Zs3v + Zs3 (weiß)
  if ((millis() % 2000 <= 1000) and (Signalbild == 6 or Signalbild == 10)){
    // Ks1
    pinModus(OUTPUT, 0, 0, OUTPUT);
    digitalWrite(pin1, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(300);
    // Zs3v
    pinModus(0, 0, 0, 0);
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(500);
    // Zs3
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(400);
    
    if (Signalbild == 10){
      pinModus(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
      pinModus(0, 0, 0, 0);
      delay(2);
    }
  }
  else if((millis() % 2000 <= 2000) and (Signalbild == 6 or Signalbild == 10)){
    // Zs3v
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, HIGH);
    digitalWrite(pin4, LOW);
    delayMicroseconds(1500);
    // Zs3
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delayMicroseconds(1400);
    
    pinModus(0, 0, 0, 0);
    if (Signalbild == 10){
      pinModus(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }

  // Ks2 + Zs3 (weiß)
  if (Signalbild == 7 or Signalbild == 11){
    // Zs3
    pinModus(0, OUTPUT, 0, OUTPUT);
    digitalWrite(pin2, LOW);
    digitalWrite(pin4, HIGH);
    delay(1);
    // Ks2
    pinModus(0, 0, 0, 0);
    pinModus(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
    delay(1);
    if (Signalbild == 11){
      pinModus(OUTPUT, 0, OUTPUT, 0);
      digitalWrite(pin1, HIGH);
      digitalWrite(pin3, LOW);
    }
  }
  
  // Ks2 + weiß
  if (Signalbild == 8){
    // Ks2
    pinModus(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, LOW);
    digitalWrite(pin3, HIGH);
    delay(2);
    // weiß
    pinModus(OUTPUT, 0, OUTPUT, 0);
    digitalWrite(pin1, HIGH);
    digitalWrite(pin3, LOW);
  }
}

void KsSignal::pinModus(boolean p1, boolean p2, boolean p3, boolean p4){
  pinMode(pin1, p1);
  pinMode(pin2, p2);
  pinMode(pin3, p3);
  pinMode(pin4, p4);
}
