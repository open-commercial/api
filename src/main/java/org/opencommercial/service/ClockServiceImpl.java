package org.opencommercial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.time.Instant.ofEpochMilli;

@Service
public class ClockServiceImpl implements ClockService {

  private Clock clock;

  @Autowired
  public ClockServiceImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public LocalDateTime getFechaActual() {
    return LocalDateTime.ofInstant(this.clock.instant(), ZoneId.systemDefault());
  }

  @Override
  public void cambiarFechaHora(int anio, int mes, int dia, int hora, int minuto, int segundo) {
    LocalDateTime fechaNueva = LocalDateTime.now();
    fechaNueva = fechaNueva.withYear(anio);
    fechaNueva = fechaNueva.withMonth(mes);
    fechaNueva = fechaNueva.withDayOfMonth(dia);
    fechaNueva = fechaNueva.withHour(hora);
    fechaNueva = fechaNueva.withMinute(minuto);
    fechaNueva = fechaNueva.withSecond(segundo);
    this.clock = Clock.fixed(ofEpochMilli(fechaNueva.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()), ZoneId.systemDefault());
  }
}
