package sic.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import sic.exception.BusinessServiceException;
import sic.modelo.dto.EntidadMontoDTO;
import sic.modelo.dto.PeriodoMontoDTO;
import sic.repository.FacturaCompraRepository;
import sic.repository.projection.EntidadMontoProjection;
import sic.repository.projection.PeriodoMontoProjection;
import sic.service.IEstadisticaService;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class EstadisticaServiceImpl implements IEstadisticaService {

  private final FacturaCompraRepository facturaCompraRepository;
  private final CacheManager cacheManager;
  private final MessageSource messageSource;

  @Autowired
  public EstadisticaServiceImpl(FacturaCompraRepository facturaCompraRepository,
                                CacheManager cacheManager, MessageSource messageSource) {
    this.facturaCompraRepository = facturaCompraRepository;
    this.cacheManager = cacheManager;
    this.messageSource = messageSource;
  }

  @Scheduled(fixedRate = 300000) // 5 min
  public void limpiarTodasLasCaches() {
    cacheManager.getCacheNames().forEach(i -> Objects.requireNonNull(cacheManager.getCache(i)).clear());
    log.info("Todas las caches fueron limpiadas: " + cacheManager.getCacheNames());
  }

  @Override
  @Cacheable("monto-neto-comprado-por-anio")
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorAnio(long idSucursal) {
    return facturaCompraRepository.getMontoNetoCompradoPorAnio(idSucursal)
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable("monto-neto-comprado-por-mes")
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorMes(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage("mensaje_formato_anio_no_valido", null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorMes(idSucursal, anio)
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable("monto-neto-comprado-por-proveedor-por-anio")
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorAnio(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage("mensaje_formato_anio_no_valido", null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorProveedorPorAnio(idSucursal, anio)
            .stream()
            .map(this::mapEntidadMonto)
            .toList();
  }

  @Override
  @Cacheable("monto-neto-comprado-por-proveedor-por-mes")
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorMes(long idSucursal, int anio, int mes) {
    if (!this.isMesValido(mes)) throw new BusinessServiceException(
            messageSource.getMessage("mensaje_formato_mes_no_valido", null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorProveedorPorMes(idSucursal, anio, mes)
            .stream()
            .map(this::mapEntidadMonto)
            .toList();
  }

  private PeriodoMontoDTO mapPeriodoMonto(PeriodoMontoProjection projection) {
    return PeriodoMontoDTO.builder()
            .periodo(projection.getPeriodo())
            .monto(projection.getMonto())
            .build();
  }

  private EntidadMontoDTO mapEntidadMonto(EntidadMontoProjection projection) {
    return EntidadMontoDTO.builder()
            .entidad(projection.getEntidad())
            .monto(projection.getMonto())
            .build();
  }

  private boolean isAnioValido(int anio) {
    return anio > 0 && anio < 9999;
  }

  private boolean isMesValido(int mes) {
    return mes >= 1 && mes <= 12;
  }
}
