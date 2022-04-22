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
 
 #include "Alimentacion.h"

Alimentacion::Alimentacion(byte nPinAlim, byte nPinBateria){
  pinMode(nPinAlim, INPUT_PULLUP);
  pinMode(nPinBateria, INPUT_PULLUP);
  this->nPinAlim = nPinAlim;
  this->nPinBateria = nPinBateria;
  tsFalloAlim = 0;
  tsFalloBateria = 0;
  avisoAlimEnviado = false;
  avisoBateriaEnviado = false;
  falloAlim = false;
  falloBateria = false;
}

void Alimentacion::check(){

  // ******************************************************************
  // Vigilancia de la alimentación 230V AC
  //
  if(!isAlimOk()){
    if(!falloAlim){
      falloAlim = true;
      avisoAlimEnviado = false;
      tsFalloAlim = millis();
    }
  }else{
    if(falloAlim)
      falloAlim = false;
  }

  // ******************************************************************
  // Vigilancia de la alimentación 230V AC
  //
  if(!isBateriaOk()){
    if(!falloBateria){
      falloBateria = true;
      avisoBateriaEnviado = false;
      tsFalloBateria = millis();
    }
  }else{
    if(falloBateria)
      falloBateria = false;
  }

  // ******************************************************************
  // Envío de mensajes
  //
  // Mensaje de activación alimentacióin 230V AC
  if(falloAlim && !avisoAlimEnviado && (millis() - tsFalloAlim) > TIEMPO_HASTA_MANDAR_AVISO){
    StaticJsonDocument<256> doc;
    doc[F("tipo")] = F("FALLO_230");
    doc[F("datos")] = true;
    serializeJsonPretty(doc, Serial);
    Serial.flush();
    avisoAlimEnviado = true;
  }
  // Mensaje de activación batería
  if(falloBateria && !avisoBateriaEnviado && (millis() - tsFalloBateria) > TIEMPO_HASTA_MANDAR_AVISO){
    StaticJsonDocument<256> doc;
    doc[F("tipo")] = F("FALLO_BAT");
    doc[F("datos")] = true;
    serializeJsonPretty(doc, Serial);
    Serial.flush();
    avisoBateriaEnviado = true;
  }
  // Mensaje de desactivación alimentacióin 230V AC
  if(avisoAlimEnviado && !falloAlim){
    StaticJsonDocument<256> doc;
    doc[F("tipo")] = F("FALLO_230");
    doc[F("datos")] = false;
    serializeJsonPretty(doc, Serial);
    Serial.flush();
    avisoAlimEnviado = false;
  }
  // Mensaje de desactivación batería
  if(avisoBateriaEnviado && !falloBateria){
    StaticJsonDocument<256> doc;
    doc[F("tipo")] = F("FALLO_BAT");
    doc[F("datos")] = false;
    serializeJsonPretty(doc, Serial);
    Serial.flush();
    avisoBateriaEnviado = false;
  }  
}


bool Alimentacion::isAlimOk() {
  // El contacto "AC OK" se cierra cuando la alimentación 230VAC está presente
  return (digitalRead(nPinAlim) == HIGH ? false : true);
}

bool Alimentacion::isBateriaOk() {
  // El contacto "Bat." se cierra cuando la tensión de batería es inferior a 11V
  return (digitalRead(nPinBateria) == HIGH ? true : false);
}
