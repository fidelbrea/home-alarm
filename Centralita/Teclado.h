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
 
#ifndef Teclado_h
#define Teclado_h

#include "Arduino.h"
#include "Wiegand.h"

#define T_PULSACION 2000 // Tiempo máximo de espera entre pulsaciones
#define TECLA_INICIO 27  // Tecla ESC
#define TECLA_FINAL  13  // Tecla ENT

class Teclado {
  public:
    Teclado(byte pinD0, byte pinD1);
    boolean isCodigoDisponible();
    boolean isTagDisponible();
    String getCodigo();
    unsigned long getTag();

  private:
    boolean       teclaPulsada();
    Wiegand       wiegand;
    char          codigo[255];
    String        codigoPublico;
    unsigned long tagPublico;
    byte          indiceCodigo;
    unsigned long tsCodigo = 0; // Marca de tiempo de la última pulsación
};
#endif
