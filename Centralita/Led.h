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
 
 #ifndef Led_h
#define Led_h

#include "Arduino.h"
#include "EstadosAlarma.h"

#define PARPADEO_LENTO 1000 // milisegundos
#define PARPADEO_RAPIDO 200 // milisegundos

class Led {
  public:
    Led(byte nPin);
    void check(EstadoAlarma estadoAlarma);
    void setVerde();
    bool isVerde();
    void setRojo();
    bool isRojo();
    void cambia();

  private:
    byte nPin;
    unsigned long tsParpadeo;
};
#endif

/*
 * Estado alarma   Comportamiento LED
 * -------------   -------------------------------
 * desarmada       rojo fijo
 * armada          parpadeo cada segundo
 * disparada       parpadeo cada 200 milisegundos
 */
