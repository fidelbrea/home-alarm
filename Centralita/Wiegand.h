#ifndef _Wiegand_H
#define _Wiegand_H

#include "Arduino.h"

class Wiegand {
  public:
    Wiegand();
    void begin(int pinD0, int pinD1);
    bool available();
    unsigned long getCode();
    int getWiegandType();

  private:
    static void ReadD0();
    static void ReadD1();
    static bool DoWiegandConversion();
    static unsigned long GetCardId(volatile unsigned long *codehigh, volatile unsigned long *codelow, char bitlength);
    static void reset();
    static volatile unsigned long _cardTempHigh;
    static volatile unsigned long _cardTemp;
    static volatile unsigned long _lastWiegand;
    static volatile int           _bitCount;
    static int                    _wiegandType;
    static unsigned long          _code;
};

#endif
