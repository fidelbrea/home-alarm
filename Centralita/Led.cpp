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
 
 #include "Led.h"

Led::Led(byte nPin){
  this->nPin = nPin;
  pinMode(nPin, OUTPUT); // LED lector    (HIGH-rojo, LOW-verde)
  tsParpadeo = 0;
}

void Led::check(EstadoAlarma estadoAlarma){
  switch(estadoAlarma){
    case EstadoAlarma::ARMADA:
    case EstadoAlarma::PREDISPARO_INICIAL:
    case EstadoAlarma::PREDISPARO_FINAL:
      if ((millis() - tsParpadeo) >= PARPADEO_LENTO){
        cambia();
        tsParpadeo = millis();
      }
      break;
    case EstadoAlarma::DISPARADA:
      if ((millis() - tsParpadeo) >= PARPADEO_RAPIDO){
        cambia();
        tsParpadeo = millis();
      }
      break;
    default:
      if(!isRojo())
        setRojo();
  }
}

void Led::setVerde(){
  digitalWrite(nPin, LOW);
}

void Led::setRojo(){
  digitalWrite(nPin, HIGH);
}

void Led::cambia(){
  isRojo() ? setVerde() : setRojo();
}

bool Led::isRojo(){
  return (digitalRead(nPin) == HIGH ? true : false);
}

bool Led::isVerde(){
  return (!this->isRojo());
}
