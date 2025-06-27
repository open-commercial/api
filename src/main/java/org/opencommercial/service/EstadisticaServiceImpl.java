package org.opencommercial.service;

import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.dto.EntidadMontoDTO;
import org.opencommercial.model.dto.PeriodoMontoDTO;
import org.opencommercial.repository.FacturaCompraRepository;
import org.opencommercial.repository.FacturaVentaRepository;
import org.opencommercial.repository.projection.EntidadMontoProjection;
import org.opencommercial.repository.projection.PeriodoMontoProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class EstadisticaServiceImpl implements EstadisticaService {

  private final FacturaCompraRepository facturaCompraRepository;
  private final FacturaVentaRepository facturaVentaRepository;
  private final CacheManager cacheManager;
  private final MessageSource messageSource;
  private static final String MENSAJE_ERROR_FORMATO_ANIO_NO_VALIDO = "mensaje_formato_anio_no_valido";
  private static final String MENSAJE_ERROR_FORMATO_MES_NO_VALIDO = "mensaje_formato_mes_no_valido";
  private static final String MONTO_NETO_COMPRADO_POR_ANIO = "monto-neto-comprado-por-anio";
  private static final String MONTO_NETO_COMPRADO_POR_MES = "monto-neto-comprado-por-mes";
  private static final String MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_ANIO = "monto-neto-comprado-por-proveedor-por-anio";
  private static final String MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_MES = "monto-neto-comprado-por-proveedor-por-mes";
  private static final String MONTO_NETO_VENDIDO_POR_ANIO = "monto-neto-vendido-por-anio";
  private static final String MONTO_NETO_VENDIDO_POR_MES = "monto-neto-vendido-por-mes";
  private static final String MONTO_NETO_VENDIDO_POR_CLIENTE_POR_ANIO = "monto-neto-vendido-por-cliente-por-anio";
  private static final String MONTO_NETO_VENDIDO_POR_CLIENTE_POR_MES = "monto-neto-vendido-por-cliente-por-mes";

  @Autowired
  public EstadisticaServiceImpl(FacturaCompraRepository facturaCompraRepository,
                                FacturaVentaRepository facturaVentaRepository,
                                CacheManager cacheManager, MessageSource messageSource) {
    this.facturaCompraRepository = facturaCompraRepository;
    this.facturaVentaRepository = facturaVentaRepository;
    this.cacheManager = cacheManager;
    this.messageSource = messageSource;
  }

  @Scheduled(fixedRate = 300000) // 5 min
  public void limpiarTodasLasCaches() {
    var caches = Set.of(MONTO_NETO_COMPRADO_POR_ANIO,
                        MONTO_NETO_COMPRADO_POR_MES,
                        MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_ANIO,
                        MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_MES,
                        MONTO_NETO_VENDIDO_POR_ANIO,
                        MONTO_NETO_VENDIDO_POR_MES,
                        MONTO_NETO_VENDIDO_POR_CLIENTE_POR_ANIO,
                        MONTO_NETO_VENDIDO_POR_CLIENTE_POR_MES);
    caches.forEach(i -> Objects.requireNonNull(cacheManager.getCache(i)).clear());
    log.info("Todas las caches fueron limpiadas. {}", caches);
  }

  @Override
  @Cacheable(MONTO_NETO_COMPRADO_POR_ANIO)
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorAnio(long idSucursal, int limite) {
    return facturaCompraRepository.getMontoNetoCompradoPorAnio(idSucursal, PageRequest.ofSize(limite))
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_COMPRADO_POR_MES)
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorMes(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_ANIO_NO_VALIDO, null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorMes(idSucursal, anio)
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_ANIO)
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorAnio(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_ANIO_NO_VALIDO, null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorProveedorPorAnio(idSucursal, anio)
            .stream()
            .map(this::mapEntidadMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_COMPRADO_POR_PROVEEDOR_POR_MES)
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorMes(long idSucursal, int anio, int mes) {
    if (!this.isMesValido(mes)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_MES_NO_VALIDO, null, Locale.getDefault()));
    return facturaCompraRepository.getMontoNetoCompradoPorProveedorPorMes(idSucursal, anio, mes)
            .stream()
            .map(this::mapEntidadMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_VENDIDO_POR_ANIO)
  public List<PeriodoMontoDTO> getMontoNetoVendidoPorAnio(long idSucursal, int limite) {
    return facturaVentaRepository.getMontoNetoVendidoPorAnio(idSucursal, PageRequest.ofSize(limite))
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_VENDIDO_POR_MES)
  public List<PeriodoMontoDTO> getMontoNetoVendidoPorMes(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_ANIO_NO_VALIDO, null, Locale.getDefault()));
    return facturaVentaRepository.getMontoNetoVendidoPorMes(idSucursal, anio)
            .stream()
            .map(this::mapPeriodoMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_VENDIDO_POR_CLIENTE_POR_ANIO)
  public List<EntidadMontoDTO> getMontoNetoVendidoPorClientePorAnio(long idSucursal, int anio) {
    if (!this.isAnioValido(anio)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_ANIO_NO_VALIDO, null, Locale.getDefault()));
    return facturaVentaRepository.getMontoNetoVendidoPorClientePorAnio(idSucursal, anio)
            .stream()
            .map(this::mapEntidadMonto)
            .toList();
  }

  @Override
  @Cacheable(MONTO_NETO_VENDIDO_POR_CLIENTE_POR_MES)
  public List<EntidadMontoDTO> getMontoNetoVendidoPorClientePorMes(long idSucursal, int anio, int mes) {
    if (!this.isMesValido(mes)) throw new BusinessServiceException(
            messageSource.getMessage(MENSAJE_ERROR_FORMATO_MES_NO_VALIDO, null, Locale.getDefault()));
    return facturaVentaRepository.getMontoNetoVendidoPorClientePorMes(idSucursal, anio, mes)
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
