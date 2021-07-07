#ifndef ESTW_h
#define ESTW_h

/*#if Arduino < 100
#include <WProgramm.h>
#else*/
#include <Arduino.h>
#include <PCF8574.h>
#include "KsSignal.h"
#include "Switch.h"

// Konstanten
#define StartTag 83 // S
#define EndTag 69 // E
#define BelegtmeldungsTag 66 // B
#define WeichenTag 87 // W
#define UmschaltTag 85 // U
#define FahrstrassenTag 70 // F
#define AnfrageTag 82 // R (für request)
//#define FestgelegtTag 70 // F
//#define AngenommenTag 65 // A (für accepted)
//#define AbgelehntTag 78 // N (für not accepted)
//#define UnbesetztTag 85 // U
//#define AufloesenTag 67 // C (für clear)
//#define BefahrenTag 85 // U

#define datenIn 11
#define taktIn 12
#define pLoadIn 13

#define SLAVE_ADR 3

// I2C Daten
union i2c_for_slave{
  struct{
    byte valueSignal1 = 0;
    byte valueSignal2 = 0;
  };
  byte bytes[2];
};
union i2c_from_slave{
  struct{
    byte input1 = 0;
    byte input2 = 0;
    byte checksum = 0;
  };
  byte bytes[3];
};


class ESTW{
  public:
    ESTW ();
    
    void uartSendSwitchStates();
    void uartSendRouteStates();
    boolean isRouteClear(int);
    int isRouteAvailable(char[20]);
    void secureRoute(int);
    void setSignal(int, boolean);
    void setPowerOfTrack(int, boolean);
    boolean isTrainArrived(int);
    void cancelRoute(int);
    void outputPCF8574();
    void inputShiftRegister();

    //  0 = nicht einstellen, 1 = Fahrweg sichern, 2 = Signal schalten,
    //  3 = Gleis schalten, 4 = Signal/Gleis aus, 5 = FS auflösen
    byte statusOfRoutes[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    byte calculateChecksum(byte[2]);

    KsSignal KS1 = KsSignal(4, 5, 6, 7);
    KsSignal KS2 = KsSignal(14, 15, 16, 17);

    Switch switches[4] = {Switch(0, &dataOut1),
                          Switch(1, &dataOut1),
                          Switch(2, &dataOut1),
                          Switch(3, &dataOut1)};

    // define pcfs and adjust addresses
    PCF8574 PCFOutputBoard1 = PCF8574(0x20);  // 32
    PCF8574 PCFOutputBoard2 = PCF8574(0x21);  // 33

    i2c_for_slave i2c_data;
    i2c_from_slave dataFromSlave;
    
  private:
    boolean controlMode = false; // Gleisunterbrechungen nach Signalen Schalten
    boolean isTrackOccupied[6] = {1, 1, 1, 1, 1, 1};
    boolean shiftIn1[8] = {0, 0, 0, 0, 0, 0, 0, 0}; // Schieberegister-In
    boolean shiftIn2[8] = {0, 0, 0, 0, 0, 0, 0, 0};
    byte dataOut1 = 0b00000000; // PCF-Out
    byte dataOut2 = 0b00000000;

    const byte destinationTrack[9] = {3, 3, 4, 0, 6, 1, 0, 6, 1};

    void turnOnSignalOfRoute(int);
    void turnOffSignalOfRoute(int);
    
    const char routes[9][2] = {{'a', 'c'}, {'b', 'c'},              // Ausfahrten aus Bahnhof 
                               {'c', 'd'},                          // Signal C (Berg)
                               {'d', 'n'}, {'d', 'e'}, {'d', 'a'},  // Signal D
                               {'e', 'n'}, {'e', 'e'}, {'e', 'a'}}; // Signal E (Innenkreis)

    // egal = 0, Fahrt = 1
    // egal = 0, Plusstellung (gerade) = 1, Minusstellung (abzweigend) = 2
    // egal = 0, muss frei sein = 1
    //                                     Signale    Weichen  Belegtmeldung
    const int routesLockTable[9][15] = {{1,0,0,0,0,  2,0,0,0,  0,0,1,0,0,0}, // AC
                                               {0,1,0,0,0,  1,0,0,0,  0,0,1,0,0,0}, // BC
                                               {0,0,1,0,0,  0,0,2,0,  0,0,0,1,0,0}, // CD
                                               {0,0,0,1,0,  0,0,2,2,  0,0,0,0,1,0}, // DN
                                               {0,0,0,1,0,  0,1,2,1,  0,0,0,0,1,1}, // DE
                                               {0,0,0,1,0,  2,2,2,1,  1,0,0,0,1,0}, // DA
                                               {0,0,0,0,1,  0,0,1,2,  0,0,0,0,1,0}, // EN
                                               {0,0,0,0,1,  0,1,1,1,  0,0,0,0,1,0}, // EE
                                               {0,0,0,0,1,  2,2,1,1,  1,0,0,0,1,0}};// EA
};
#endif
