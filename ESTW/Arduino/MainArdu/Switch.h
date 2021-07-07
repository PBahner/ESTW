#include <Arduino.h>

#ifndef Switch_h
#define Switch_h

class Switch{
  public:
    // id (Welche Weiche)
    byte id;

    // Konstruktor  ------------------------------------------------
    Switch(byte i, byte* out) {
      id = i;
      dataOut = out;
    }

    // wird ausgeführt sobald Weichenposition verändert wird  ------
    void updateCurrentSwitchState(boolean pos) {
      currentSwitchState = pos;
      // Weiche stellen (wenn nötig)
      setSwitch();
    }

    // gewünschte Weichenposition einstellen  ----------------------
    void setTargetSwitchState(boolean pos) {
      targetSwitchState = pos;
      // Weiche stellen (wenn nötig)
      setSwitch();
    }

    // Weiche sperren
    void lock() {
      locked = true;
    }

    // Weiche entsperren
    void unlock() {
      locked = false;
    }

    // gibt aktuelle Position von Weiche zurück --------------------
    boolean getCurrentPos() {
      return currentSwitchState; 
    }
    
  private:
    boolean currentSwitchState = false;
    boolean targetSwitchState = false;
    boolean locked = false;
    byte* dataOut;

    // Weiche wirklich stellen  ------------------------------------
    void setSwitch() {
      // nur schalten wenn Weiche nicht gesperrt)
      if(!locked and targetSwitchState != currentSwitchState) {
        // Relais anziehen
        bitWrite(*dataOut, ((id+1)*2)-targetSwitchState-1, 1);
        //pcfOut->write(((id+1)*2)-targetSwitchState-1, 1);
      } else {
        // Relais abfallen
        bitWrite(*dataOut, ((id+1)*2)-targetSwitchState-1, 0);
        //pcfOut->write(((id+1)*2)-targetSwitchState-1, 0);
      }
    }
};
#endif
