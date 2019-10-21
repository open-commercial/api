package sic.service;

import java.time.LocalDateTime;

public interface IClockService {

  LocalDateTime getFechaActual();

  void cambiarFechaHora(int anio, int mes, int dia, int hora, int minuto, int segundo);
}
