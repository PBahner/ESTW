#ifndef ESTW_h
#define ESTW_h

/*#if Arduino < 100
#include <WProgramm.h>
#else*/
#include <Arduino.h>
#include "KsSignal.h"

// Konstanten
#define StartTag 83 // S
#define EndTag 69 // E
#define BelegtmeldungsTag 66 // B
#define WeichenTag 87 // W
#define UmschaltTag 85 // U
#define FahrstrassenTag 70 // F
#define FestgelegtTag 70 // F
#define AnfrageTag 82 // R (für request)
#define AngenommenTag 65 // A (für accepted)
#define AbgelehntTag 78 // N (für not accepted)
#define UnbesetztTag 85 // U
#define ClearTag 67 // C
#define BefahrenTag 85 // U


#define datenIn 11
#define taktIn 12
#define pLoadIn 13

#define taktOut 8
#define speicherOut 9
#define datenOut 10


// I2C Daten
union i2c_struct{
  struct{
    byte valueSignal1 = 0;
    byte valueSignal2 = 0;
  };
  byte bytes[2];
};


class ESTW{
  public:
    ESTW ();
    
    void weicheSchalten(int, int);
    void weichenSchalten();
    void weichenPosSenden();
    void fahrstrassenPosSenden();
    int fahrstrasseVorhanden(char[20]);
    char getFahrstrasse(int, boolean);
    void fahrwegSichern(int);
    boolean fahrwegFrei(int);
    void signalSchalten(int, boolean);
    void gleisSchalten(int, boolean);
    boolean zugAngekommen(int);
    void fahrstrasseAufloesen(int);
    void output();
    void input();
    boolean signale_weichen = false; // Gleisunterbrechungen nach Signalen Schalten
    //  0 = nicht einstellen, 1 = Fahrweg sichern, 2 = Signal schalten,
    //  3 = Gleis schalten, 4 = Signal/Gleis aus, 5 = FS auflösen
    byte einzustellendeFahrstrasse[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    byte zielGleise[9] = {3, 3, 4, 0, 6, 1, 0, 6, 1};

    KsSignal KS1 = KsSignal(4, 5, 6, 7);
    KsSignal KS2 = KsSignal(14, 15, 16, 17);
    unsigned long verzoegerungGleisfrei[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    unsigned long verzoegerungSignalhalt[9] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    i2c_struct i2c_data;
    
  private:
    boolean weichenSoll[4] = {0, 0, 0, 0}; // gewünschte Weichenposition
    boolean weichen[4] = {0, 0, 0, 0}; // Weichenposition
    boolean weichenSperre[4] = {0, 0, 0, 0}; // sind Weichen gesperrt 0=frei 1=gesperrt
    boolean belegtmeldung[6] = {1, 1, 1, 1, 1, 1};
    boolean shiftIn1[8] = {0, 0, 0, 0, 0, 0, 0, 0}; // Schieberegister-In
    boolean shiftIn2[8] = {0, 0, 0, 0, 0, 0, 0, 0};
    byte wertOut1 = 0b00000000; // Schieberegister-Out
    byte wertOut2 = 0b00000000;
    char fahrstrassen[9][2] = {{'a', 'c'}, {'b', 'c'},              // Ausfahrten aus Bahnhof 
                               {'c', 'd'},                          // Signal C (Berg)
                               {'d', 'n'}, {'d', 'e'}, {'d', 'a'},  // Signal D
                               {'e', 'n'}, {'e', 'e'}, {'e', 'a'}}; // Signal E (Innenkreis)

    // egal = 0, Fahrt = 1
    // egal = 0, Plusstellung (gerade) = 1, Minusstellung (abzweigend) = 2
    // egal = 0, muss frei sein = 1
    //                                     Signale    Weichen  Belegtmeldung
    int fahrstrassenVerschluss[9][15] = {{1,0,0,0,0,  2,0,0,0,  0,0,1,0,0,0}, // AC
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
