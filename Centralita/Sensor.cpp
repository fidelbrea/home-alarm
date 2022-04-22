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
 
 #include "Sensor.h"

Sensor::Sensor(byte nPin){
  this->nPin = nPin;
  this->predisparo = false;
  this->habilitado = true;
  pinMode(nPin, INPUT_PULLUP);
}

void Sensor::setPin(byte nPin){
  this->nPin = nPin;
}

byte Sensor::getPin(){
  return nPin;
}

void Sensor::setPredisparo(bool predisparo) {
  this->predisparo = predisparo;
}

bool Sensor::isPredisparo() {
  return predisparo;
}

void Sensor::setHabilitado(bool habilitado) {
  this->habilitado = habilitado;
}

bool Sensor::isHabilitado() {
  return habilitado;
}

bool Sensor::getEstado() {
  return (digitalRead(nPin) == HIGH ? true : false);
}
