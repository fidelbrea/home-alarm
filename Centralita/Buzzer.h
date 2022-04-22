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
 
 #ifndef Buzzer_h
#define Buzzer_h

#include "Arduino.h"

class Buzzer {
  public:
    Buzzer(byte nPin);
    void check();
    void pita(byte numPitidos, unsigned int nDuracion);
    void pita(byte numPitidos, unsigned int nDurPitido, unsigned int nDurPausa);
    void silencio();

  private:
    byte nPin;
    byte nPitidos;
    unsigned long tsPitido;
    unsigned long nMilisPitido;
    unsigned long nMilisPausaPitido;
    void suena();
    boolean isSonando();

};
#endif

/*
  * nPitidos indica los ciclos (completos) de pitidos que quedan
  * nMilisPausaPitido indica la duración de la pausa
  * nMilisPitido indica la duración del pitido
  * tsPitido es el TimeStamp para su funcionamiento
  * Son variables establecidas por la funcion pita
  * 
  * Ejemplo:
  *   nPitidos = 2
  *   nMilisPausaPitido = 400
  *   nMilisPitido = 600
  *   
  *       +------+    +------+
  *       |      |    |      |
  * ------+      +----+      +-------------------
  * 
  */
