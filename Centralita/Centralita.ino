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
#define VERSION "v1.0"

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
static String jsonArmado;
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
  doc[F("datos")] = F(VERSION);
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
  if(alarma.getEstado() == EstadoAlarma::DESARMADA)
    checkRamLevel();
  
  // ******************************************************************
  // Control de acceso. Código numérico
  if(teclado.isCodigoDisponible()){
    String codigoTeclado = teclado.getCodigo();
    if(alarma.getEstado() == EstadoAlarma::DESARMADA){
      StaticJsonDocument<256> doc;
      doc[F("tipo")] = F("CODIGO");
      doc[F("datos")] = codigoTeclado;
      serializeJsonPretty(doc, Serial);
      Serial.flush();
    }else{
      StaticJsonDocument<1024> doc;
      DeserializationError error = deserializeJson(doc, jsonArmado);
      if (!error) {
        if (doc.containsKey(F("tipo")) && doc.containsKey(F("datos"))) {
          const char* tipo = doc[F("tipo")];
          for(int i=0; i<doc[F("datos")][F("codigos")].size(); i++){
            String codigo = doc[F("datos")]["codigos"][i];
            if(codigoTeclado.equals(codigo)){
              jsonArmado = "";
              alarma.desarmar();
              break;
            }
          }
        }
      }
    }
  }

  // ******************************************************************
  // Control de acceso. Etiqueta RFID
  if(teclado.isTagDisponible()){
    unsigned long etiquetaLeida =  teclado.getTag();
    if(alarma.getEstado() == EstadoAlarma::DESARMADA){
      StaticJsonDocument<256> doc;
      doc[F("tipo")] = F("TAG");
      doc[F("datos")] = etiquetaLeida;
      serializeJsonPretty(doc, Serial);
      Serial.flush();
    }else{
      StaticJsonDocument<1024> doc;
      DeserializationError error = deserializeJson(doc, jsonArmado);
      if (!error) {
        if (doc.containsKey(F("tipo")) && doc.containsKey(F("datos"))) {
          const char* tipo = doc[F("tipo")];
          for(int i=0; i<doc[F("datos")][F("etiquetas")].size(); i++){
            if(etiquetaLeida == doc[F("datos")]["etiquetas"][i]){
              jsonArmado = "";
              alarma.desarmar();
              break;
            }
          }
        }
      }
    }
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
      Serial.println("borrado 1"); Serial.flush();
      // Se borra el buffer por no encontrar el signo de inicio '{'
    }else if(bufferEntradaSerial.length()>1024){
      bufferEntradaSerial="";
      Serial.println("borrado 2"); Serial.flush();
      // Se borra por ser demasiado grande para manejarlo
    }else if(bufferEntradaSerial.indexOf(F("}"))>0){
      StaticJsonDocument<1024> doc;
      DeserializationError error = deserializeJson(doc, bufferEntradaSerial);
      if (!error) {
        if (doc.containsKey(F("tipo")) && doc.containsKey(F("datos"))) {
          const char* tipo = doc[F("tipo")];
  
          // Interpretamos el mensaje recibido
          if(strcmp(tipo, "armar")==0){
            jsonArmado = bufferEntradaSerial;
            for(int i=0; i<doc[F("datos")][F("sensores")].size(); i++){
              JsonObject sensor = doc[F("datos")][F("sensores")][i];
              alarma.getSensor(((int)sensor["id"])-1)->setHabilitado(sensor["habilitado"]);
              alarma.getSensor(((int)sensor["id"])-1)->setPredisparo(sensor["retardado"]);
            }
            alarma.armar();
          }else if(strcmp(tipo, "prearmar")==0){
            jsonArmado = bufferEntradaSerial;
            for(int i=0; i<doc[F("datos")][F("sensores")].size(); i++){
              JsonObject sensor = doc[F("datos")][F("sensores")][i];
              alarma.getSensor(((int)sensor["id"])-1)->setHabilitado(sensor["habilitado"]);
              alarma.getSensor(((int)sensor["id"])-1)->setPredisparo(sensor["retardado"]);
            }
            alarma.prearmar();
          }else if(strcmp(tipo, "desarmar")==0){
            jsonArmado = "";
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
extern unsigned int __bss_end;
extern unsigned int __heap_start;
extern void *__brkval;
int getFreeMemory(){
  int free_memory;
  return ((int)__brkval == 0)?((int)&free_memory) - ((int)&__bss_end):((int)&free_memory) - ((int)__brkval);
}
