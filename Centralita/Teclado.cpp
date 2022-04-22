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
 
 #include "Teclado.h"

Teclado::Teclado(byte pinD0, byte pinD1){
  wiegand.begin(pinD0, pinD1);
  codigo[0] = '\0';
  indiceCodigo = 0;
}

boolean Teclado::isCodigoDisponible(){
  if (wiegand.available()) {
    switch (wiegand.getWiegandType()) {
      case 4: case 8:
        return teclaPulsada(wiegand.getCode());
        break;
    }
  }
  return false;
}

boolean Teclado::isTagDisponible(){
  if (wiegand.available()) {
    switch (wiegand.getWiegandType()) {
      case 24: case 26: case 32: case 34:
        tagPublico = wiegand.getCode();
        return true;
        break;
    }
  }
  return false;
}

boolean Teclado::teclaPulsada(unsigned long tecla) {
  if (tecla == TECLA_INICIO) {
    tsCodigo = millis();
    indiceCodigo = 0;
  } else {
    if ((millis() - tsCodigo) < T_PULSACION) {
      if (tecla == TECLA_FINAL) {
        codigo[indiceCodigo] = '\0';
        tsCodigo -= T_PULSACION;
        codigoPublico = String(codigo);
        return true;
      } else {
        codigo[indiceCodigo] = 48 + tecla; // guardamos el código ASCII
        indiceCodigo++;
        tsCodigo = millis();
      }
    } else {
      codigo[0] = '\0';
    }
  }
  return false;
}

String Teclado::getCodigo() {
  String codigoTemp = codigoPublico;
  codigoPublico = ""; // borramos el código
  return codigoTemp;
}

unsigned long Teclado::getTag() {
  return tagPublico;
}
