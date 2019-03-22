package sic.service;

import java.util.Date;

public interface IClockService {

  Date getFechaActual();

  void cambiarFechaHora(int anio, int mes, int dia, int hora, int minuto, int segundo);
}
