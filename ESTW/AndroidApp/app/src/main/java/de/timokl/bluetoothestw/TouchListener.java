package de.timokl.bluetoothestw;


public interface TouchListener {

public void onTouchDown(int moveX, int moveY, int weiche);
public void onTouchDown(int moveX, int moveY, String signal);
public void resetSignals();


}
