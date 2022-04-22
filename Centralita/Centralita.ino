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
 
//--------------------------------------------------------------------
// Definiciones
#define CODIGO_ARMAR "159"

//--------------------------------------------------------------------
// Inclusiones
#include <ArduinoJson.h>
#include <avr/wdt.h>
#include "Alarma.h"
#include "Alimentacion.h"
#include "Teclado.h"
#include "EstadosAlarma.h"

//--------------------------------------------------------------------
// Variables estáticas (globales)
static Alarma alarma = Alarma();
static Alimentacion alimentacion = Alimentacion(22, 23);
static Teclado teclado = Teclado(18, 19);
static String bufferEntradaSerial;
static EstadoAlarma estadoAlarmaAnterior = alarma.getEstado();
static uint32_t ultimoContadorLoops;

//--------------------------------------------------------------------
// Declaración de funciones implementadas
void setup();
void loop();
void serialEvent();
void checkRamLevel();
int getFreeMemory();
void softwareReboot();

//--------------------------------------------------------------------
// Configuración inicial
void setup() {
  Serial.begin(9600, SERIAL_8N1);
  pinMode(LED_BUILTIN, OUTPUT);
  bufferEntradaSerial = "";
  ultimoContadorLoops = 0;

  // Enviamos el mensaje de inico
  StaticJsonDocument<256> doc;
  doc[F("tipo")] = F("INICIO");
  doc[F("datos")] = F("v1.0");
  serializeJsonPretty(doc, Serial);
  Serial.flush();
}

//--------------------------------------------------------------------
// Bucle
void loop() {
  // ******************************************************************
  // Guardamos cada 10 segundos los loops realizados
  static uint32_t nContadorLoops;
  static uint32_t tsContadorLoops;
  if( (millis() - tsContadorLoops) > 10000 ){
    ultimoContadorLoops = nContadorLoops;
    nContadorLoops = 0;
    tsContadorLoops = millis();
  }else{
    nContadorLoops++;
  }

  // ******************************************************************
  // Parpadeo del LED de la placa cada nMillisParpadeo
  static uint32_t tsCambioLedBuiltIn;
  static uint8_t nMillisParpadeo = 249;
  if( (millis() - tsCambioLedBuiltIn) > nMillisParpadeo ){
    digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN));
    tsCambioLedBuiltIn = millis();
  }

  // ******************************************************************
  // Vigilancia de la memoria RAM
  // Si la RAM disponible baja de un umbral, reiniciamos la placa
  if(alarma.getEstado() != EstadoAlarma::ARMADA &&
     alarma.getEstado() != EstadoAlarma::PREDISPARO_INICIAL &&
     alarma.getEstado() != EstadoAlarma::PREDISPARO_FINAL &&
     alarma.getEstado() != EstadoAlarma::DISPARADA)
    checkRamLevel();
  
  // ******************************************************************
  // Control de acceso. Código numérico
  if(teclado.isCodigoDisponible()){
    String codigoTeclado = teclado.getCodigo();
    if(codigoTeclado.equals(F(CODIGO_ARMAR))){
      alarma.pita(1, 1000);
      if(alarma.getEstado() == EstadoAlarma::DESARMADA)
        alarma.prearmar();
    }else{
      StaticJsonDocument<256> doc;
      doc[F("tipo")] = F("CODIGO");
      doc[F("datos")] = codigoTeclado;
      serializeJsonPretty(doc, Serial);
      Serial.flush();
    }
  }

  // ******************************************************************
  // Control de acceso. Etiqueta RFID
  if(teclado.isTagDisponible()){
    StaticJsonDocument<256> doc;
    doc[F("tipo")] = F("TAG");
    doc[F("datos")] = teclado.getTag();
    serializeJsonPretty(doc, Serial);
    Serial.flush();
  }
  
  // ******************************************************************
  // Alarma
  alarma.check();

  // ******************************************************************
  // Alimentación
  alimentacion.check();

  // ******************************************************************
  // Cambio del estado de la alarma
  if(estadoAlarmaAnterior != alarma.getEstado()){
    if(alarma.getEstado() == EstadoAlarma::ARMADA ||
       alarma.getEstado() == EstadoAlarma::DESARMADA ||
       alarma.getEstado() == EstadoAlarma::DISPARADA){
      StaticJsonDocument<256> doc;
      doc[F("tipo")] = F("ALARM_STATE");
      doc[F("datos")] = alarma.getEstado();
      serializeJsonPretty(doc, Serial);
      Serial.flush();
      estadoAlarmaAnterior = alarma.getEstado();
    }
  }
}

//--------------------------------------------------------------------
// Manejo de llegada de información a través del puerto serie
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    bufferEntradaSerial += inChar;
  }

  if(bufferEntradaSerial.length()>0){
    if(bufferEntradaSerial.indexOf(F("{"))==-1){
      bufferEntradaSerial = "";
      // Se borra el buffer por no encontrar el signo de inicio '{'
    }else if(bufferEntradaSerial.length()>512){
      bufferEntradaSerial="";
      // Se borra por ser demasiado grande para manejarlo
    }else if(bufferEntradaSerial.indexOf(F("}"))>0){
      StaticJsonDocument<512> doc;
      DeserializationError error = deserializeJson(doc, bufferEntradaSerial);
      if (!error) {
        if (doc.containsKey(F("tipo")) && doc.containsKey(F("datos"))) {
          const char* tipo = doc[F("tipo")];
  
          // Interpretamos el mensaje recibido
          if(strcmp(tipo, "armar")==0){
            for(int nSensor = 0; nSensor<alarma.getNumSensores(); nSensor++){
              alarma.getSensor(nSensor)->setHabilitado(doc[F("datos")][nSensor*2]);
              alarma.getSensor(nSensor)->setPredisparo(doc[F("datos")][(nSensor*2)+1]);
            }
            alarma.armar();
          }else if(strcmp(tipo, "desarmar")==0){
            alarma.desarmar();
          }else if(strcmp(tipo, "getAlarmState")==0){
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("ALARM_STATE");
            doc[F("datos")] = alarma.getEstado();
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }else if(strcmp(tipo, "setSensors")==0){
            for(int nSensor = 0; nSensor<alarma.getNumSensores(); nSensor++){
              alarma.getSensor(nSensor)->setHabilitado(doc[F("datos")][nSensor*2]);
              alarma.getSensor(nSensor)->setPredisparo(doc[F("datos")][(nSensor*2)+1]);
            }
          }else if(strcmp(tipo, "getSensors")==0){
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("SENSORS");
            JsonArray data = doc.createNestedArray("datos");
            for(int nSensor = 0; nSensor<alarma.getNumSensores(); nSensor++){
              data.add(alarma.getSensor(nSensor)->isHabilitado());
              data.add(alarma.getSensor(nSensor)->isPredisparo());
            }
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }else if(strcmp(tipo, "getRam")==0){
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("RAM");
            doc[F("datos")] = getFreeMemory();
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }else if(strcmp(tipo, "getLoops")==0){
            StaticJsonDocument<256> doc;
            doc[F("tipo")] = F("LOOPS");
            doc[F("datos")] = ultimoContadorLoops;
            serializeJsonPretty(doc, Serial);
            Serial.flush();
          }
        }
        bufferEntradaSerial = "";
      }
    }
  }
}

//--------------------------------------------------------------------
// Vigilancia del nivel de RAM
void checkRamLevel(){
  if(getFreeMemory() < 512){
    wdt_enable(WDTO_15MS); // reinicio del microcontrolador
    delay(5000);
  }
}

//--------------------------------------------------------------------
// Obtiene la cantidad (bytes) de memoria RAM libre
//
// variables externas
extern unsigned int __bss_end;
extern unsigned int __heap_start;
extern void *__brkval;

int getFreeMemory(){
  int free_memory;
  if((int)__brkval == 0)
    free_memory = ((int)&free_memory) - ((int)&__bss_end);
  else
    free_memory = ((int)&free_memory) - ((int)__brkval);
  return free_memory;
}
