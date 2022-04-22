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
 
 #include "Buzzer.h"

Buzzer::Buzzer(byte nPin){
  pinMode(nPin, OUTPUT); // Buzzer lector (LOW-pita)
  this->nPin = nPin;
  nPitidos = 0;
  silencio();
}

void Buzzer::check(){
  if (nPitidos > 0) {
    if (isSonando()) {
      if ((millis() - tsPitido) >= nMilisPitido) { // pitando
        silencio();
        tsPitido = millis();
        nPitidos--;
      }
    } else {
      if ((millis() - tsPitido) >= nMilisPausaPitido) { // silencio
        suena();
        tsPitido = millis();
      }
    }
  }  
}

void Buzzer::pita(byte numPitidos, unsigned int nDuracion) {
  pita(numPitidos, nDuracion, nDuracion);
}

void Buzzer::pita(byte numPitidos, unsigned int nDurPitido, unsigned int nDurPausa) {
  nPitidos = numPitidos;
  nMilisPitido = nDurPitido;
  nMilisPausaPitido = nDurPausa;
  tsPitido = millis();
  if (nPitidos > 0)
    suena();
}

void Buzzer::suena() {
  digitalWrite(nPin, LOW);
}

void Buzzer::silencio() {
  digitalWrite(nPin, HIGH);
}

boolean Buzzer::isSonando() {
  return (digitalRead(nPin) == HIGH ? false : true);
}
