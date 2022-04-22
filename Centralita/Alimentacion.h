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
 
 #ifndef Alimentacion_h
#define Alimentacion_h

#include "Arduino.h"
#include <ArduinoJson.h>

#define TIEMPO_HASTA_MANDAR_AVISO 5000 // milisegundos

class Alimentacion {
  public:
    Alimentacion(byte nPinAlim, byte nPinBateria);
    void check();

  private:
    byte nPinAlim;
    byte nPinBateria;
    unsigned long tsFalloAlim;
    unsigned long tsFalloBateria;
    bool falloAlim;
    bool falloBateria;
    bool avisoAlimEnviado;
    bool avisoBateriaEnviado;
    bool isAlimOk();
    bool isBateriaOk();
};
#endif
