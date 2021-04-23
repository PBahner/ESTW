package de.timokl.bluetoothestw;


public interface TouchListener {

void onTouchDown(int moveX, int moveY, int weiche);
void onTouchDown(int moveX, int moveY, char signal);
void resetSignals();


}
