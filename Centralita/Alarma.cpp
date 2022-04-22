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
 
 #include "Alarma.h"

uint8_t Alarma::nSensores = sizeof(sensor)/sizeof(sensor[0]);

Alarma::Alarma(){
  estadoAlarma = EstadoAlarma::DESARMADA;
}

void Alarma::check(){

  // ******************************************************************
  // Polling de los distintos elementos de la alarma
  buzzer.check();
  led.check(estadoAlarma);
  sirena.check(estadoAlarma);
  
  switch(estadoAlarma){
    case EstadoAlarma::DESARMADA:
      break;

    case EstadoAlarma::PREARMADO_INICIAL:
      if( (millis() - tsPrearmadoInicial) >= RETARDO_INICIAL ){
        estadoAlarma = EstadoAlarma::PREARMADO_FINAL;
        buzzer.pita(RETARDO_FINAL/500, 250);
        tsPrearmadoFinal = millis();
      }
      break;
    
    case EstadoAlarma::PREARMADO_FINAL:
      if( (millis() - tsPrearmadoFinal) >= RETARDO_FINAL ){
        estadoAlarma = EstadoAlarma::ARMADA;
        buzzer.silencio();
      }
      break;
    
    case EstadoAlarma::ARMADA:
      // -- Chequea sensores
      for(int nSensor=0; nSensor<nSensores; nSensor++){
        if(sensor[nSensor].isHabilitado() && sensor[nSensor].getEstado()){
          if(sensor[nSensor].isPredisparo()){
            estadoAlarma = EstadoAlarma::PREDISPARO_INICIAL;
            buzzer.pita(RETARDO_INICIAL/1000, 500);
            tsPredisparoInicial = millis();
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("PREDISPARO");
            doc[F("datos")] = nSensor;
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }else{
            estadoAlarma = EstadoAlarma::DISPARADA;
            buzzer.pita(1, 0);
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("DISPARO");
            doc[F("datos")] = nSensor;
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }
        }
      }
      break;
    
    case EstadoAlarma::PREDISPARO_INICIAL:
      if( (millis() - tsPredisparoInicial) >= RETARDO_INICIAL ){
        estadoAlarma = EstadoAlarma::PREDISPARO_FINAL;
        buzzer.pita(RETARDO_FINAL/500, 250);
        tsPredisparoFinal = millis();
      }
      break;
    
    case EstadoAlarma::PREDISPARO_FINAL:
      if( (millis() - tsPredisparoFinal) >= RETARDO_FINAL ){
        estadoAlarma = EstadoAlarma::DISPARADA;
        buzzer.silencio();
        StaticJsonDocument<256> doc;
        doc[F("tipo")] = F("DISPARO");
        doc[F("datos")] = 999;
        serializeJsonPretty(doc, Serial);
        Serial.flush();
      }
      break;
    
    case EstadoAlarma::DISPARADA:
      break;
    }
}

void Alarma::prearmar(){
  this->estadoAlarma = EstadoAlarma::PREARMADO_INICIAL;
  buzzer.pita(RETARDO_INICIAL/1000, 500);
  this->tsPrearmadoInicial = millis();
}

void Alarma::armar(){
  this->estadoAlarma = EstadoAlarma::ARMADA;
}

void Alarma::desarmar(){
  this->estadoAlarma = EstadoAlarma::DESARMADA;
  buzzer.pita(1, 1000);
}

EstadoAlarma Alarma::getEstado(){
  return estadoAlarma;
}

void Alarma::pita(byte numPitidos, unsigned int nDuracion){
  buzzer.pita(numPitidos, nDuracion);
}

void Alarma::pita(byte numPitidos, unsigned int nDurPitido, unsigned int nDurPausa){
  buzzer.pita(numPitidos, nDurPitido, nDurPausa);
}

Sensor* Alarma::getSensor(int nSensor){
  if(nSensor >= 0 and nSensor < (sizeof(sensor) / sizeof(Sensor))){
    return &sensor[nSensor];
  }
  return NULL;
}

uint8_t Alarma::getNumSensores(){
  return nSensores;
}
