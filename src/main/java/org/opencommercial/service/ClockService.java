package org.opencommercial.service;

import java.time.LocalDateTime;

public interface ClockService {

  LocalDateTime getFechaActual();

  void cambiarFechaHora(int anio, int mes, int dia, int hora, int minuto, int segundo);
}
