package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sic.service.IMyClockService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.Instant.ofEpochMilli;

@Service
public class MyClockServiceImpl implements IMyClockService {

  private Clock clock;

  @Autowired
  public MyClockServiceImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Date getFechaActual() {
    return Date.from(this.clock.instant());
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
