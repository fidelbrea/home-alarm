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
#include "Wiegand.h"

#define INTERRUPT_ATTR

volatile unsigned long Wiegand::lecturaMSB = 0;
volatile unsigned long Wiegand::lecturaLSB = 0;
volatile unsigned long Wiegand::tsUltimoBit = 0;
unsigned long Wiegand::codigo = 0;
volatile int Wiegand::bitsLeidos = 0;
int Wiegand::tipoWiegand = 0;

Wiegand::Wiegand() {}

void Wiegand::begin(int pinD0, int pinD1) {
  this->reset();
  pinMode(pinD0, INPUT);
  pinMode(pinD1, INPUT);
  attachInterrupt(digitalPinToInterrupt(pinD0), ReadD0, FALLING);
  attachInterrupt(digitalPinToInterrupt(pinD1), ReadD1, FALLING);
}

INTERRUPT_ATTR void Wiegand::ReadD0 () {
  bitsLeidos++;
  if (bitsLeidos > 31) {
    lecturaMSB <<= 1;
    lecturaMSB |= ((0x80000000 & lecturaLSB) >> 31);
  }
  lecturaLSB <<= 1;
  tsUltimoBit = millis();
}

INTERRUPT_ATTR void Wiegand::ReadD1() {
  bitsLeidos ++;
  if (bitsLeidos > 31){
    lecturaMSB <<= 1;
    lecturaMSB |= ((0x80000000 & lecturaLSB) >> 31);
  }
  lecturaLSB <<= 1;
  lecturaLSB |= 1;
  tsUltimoBit = millis();
}

bool Wiegand::available() {
  unsigned long intervalo = millis() - tsUltimoBit;
  // permitimos analizar los bits recibidos en una ventana que asegure
  // que ya nos e van a recibir mas bits (>30ms) y que ha dado tiempo a
  // leer el dato pendiente (<1s)
  if (intervalo > 30 && intervalo < 1000) {
    noInterrupts();
    boolean res = analizaWiegand();
    interrupts();
    return res;
  }
  return false;
}

unsigned long Wiegand::getCodigo() {
  unsigned long res = codigo;
  this->reset();
  return res;
}

int Wiegand::getTipoWiegand() {
  return tipoWiegand;
}

unsigned long Wiegand::getNumeroEtiqueta (volatile unsigned long *parteAlta, volatile unsigned long *parteBaja, char numBits) {
  if (numBits == 26)
    return ((*parteBaja & 0xFFFFFE) >> 1);

  if (numBits == 34){
    *parteAlta = *parteAlta & 0x01;
    *parteAlta <<= 31;
    *parteBaja >>= 1;
    return (*parteAlta | *parteBaja);
  }
  return *parteBaja;
}

bool Wiegand::analizaWiegand () {
  if((bitsLeidos == 24) || (bitsLeidos == 26) || (bitsLeidos == 32) || (bitsLeidos == 34) || (bitsLeidos == 8) || (bitsLeidos == 4)){

    if(bitsLeidos == 8){
      char highNibble = (lecturaLSB & 0xF0) >> 4;
      char lowNibble = (lecturaLSB & 0x0F);
      tipoWiegand = bitsLeidos;
      bitsLeidos = 0;
      lecturaLSB = 0;
      lecturaMSB = 0;
      if (lowNibble == (~highNibble & 0x0F)){
        codigo = (int)tecladoToAscii(lowNibble);
        return true;
      }else{
        tsUltimoBit = millis();
        return false;
      }
    }else if(4 == bitsLeidos){
      codigo = (int)tecladoToAscii(lecturaLSB & 0x0000000F);
      tipoWiegand = bitsLeidos;
      bitsLeidos = 0;
      lecturaLSB = 0;
      lecturaMSB = 0;
      return true;
    }else{
      tipoWiegand = bitsLeidos;
      codigo = getNumeroEtiqueta (&lecturaMSB, &lecturaLSB, bitsLeidos);
      bitsLeidos = 0;
      lecturaLSB = 0;
      lecturaMSB = 0;
      return true;
    }
  }else{
    tsUltimoBit = millis();
    bitsLeidos = 0;
    lecturaLSB = 0;
    lecturaMSB = 0;
  }
}

char Wiegand::tecladoToAscii(char nTeclaPulsada) {
  switch (nTeclaPulsada) {
    case 0x0b: // tecla * --> Enter
      return 0x0d;
    case 0x0a: // tecla # --> Escape
      return 0x1b;
    default:
      return (48 + nTeclaPulsada);
  }
}

void Wiegand::reset(){
  tsUltimoBit = 0;
  lecturaMSB = 0;
  lecturaLSB = 0;
  codigo = 0;
  tipoWiegand = 0;
  bitsLeidos = 0;
}
