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
 
 #ifndef Alarma_h
#define Alarma_h

#include "Arduino.h"
#include "Sensor.h"
#include "Led.h"
#include "Buzzer.h"
#include "Sirena.h"
#include "EstadosAlarma.h"
#include <ArduinoJson.h>


#define RETARDO_INICIAL 15000 // milisegundos
#define RETARDO_FINAL    5000 // milisegundos

class Alarma {
  public:
    Alarma();
    void check();
    void prearmar();
    void armar();
    void desarmar();
    EstadoAlarma getEstado();
    void pita(byte numPitidos, unsigned int nDuracion);
    void pita(byte numPitidos, unsigned int nDurPitido, unsigned int nDurPausa);
    Sensor* getSensor(int nSensor);
    uint8_t getNumSensores();
    
  private:
    unsigned long tsPrearmadoInicial;
    unsigned long tsPrearmadoFinal;
    unsigned long tsPredisparoInicial;
    unsigned long tsPredisparoFinal;
    Sensor sensor[11] = {Sensor(2), Sensor(3), Sensor(4), Sensor(5), Sensor(6), Sensor(7), Sensor(8), Sensor(9), Sensor(10), Sensor(11), Sensor(12)};
    Led led = Led(45);
    Buzzer buzzer = Buzzer(46);
    Sirena sirena = Sirena(40);
    EstadoAlarma estadoAlarma;
    static uint8_t nSensores;
};
#endif
