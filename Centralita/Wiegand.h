/* 
 * Copyright (C) 2022 Fidel Brea Montilla (fidelbreamontilla@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
#ifndef Wiegand_h
#define Wiegand_h

#include "Arduino.h"

class Wiegand {
  public:
    Wiegand();
    void begin(int pinD0, int pinD1);
    bool available();
    unsigned long getCodigo();
    int getTipoWiegand();

  private:
    static void ReadD0();
    static void ReadD1();
    static bool analizaWiegand();
    static unsigned long getNumeroEtiqueta (volatile unsigned long *parteAlta, volatile unsigned long *parteBaja, char numBits);
    static char tecladoToAscii(char nTeclaPulsada);
    static void reset();
    static volatile unsigned long lecturaMSB;
    static volatile unsigned long lecturaLSB;
    static volatile unsigned long tsUltimoBit;
    static volatile int           bitsLeidos;
    static int                    tipoWiegand;
    static unsigned long          codigo;
};

#endif
