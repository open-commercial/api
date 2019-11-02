package sic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaGastoCriteria;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

import javax.validation.Valid;

public interface IGastoService {
    
    Gasto getGastoNoEliminadoPorId(Long id);
    
    void eliminar(long idGasto);

    List<Gasto> getGastosEntreFechasYFormaDePago(Empresa empresa, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta);

    Gasto guardar(@Valid Gasto gasto);

    void validarOperacion(Gasto gasto);

    Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria);

    long getUltimoNumeroDeGasto(long idEmpresa);

    BigDecimal getTotalGastosEntreFechasYFormaDePago(long idEmpresa, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

    BigDecimal getTotalGastosQueAfectanCajaEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta);

    BigDecimal getTotalGastosEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta);

    BigDecimal getTotalGastos(BusquedaGastoCriteria criteria);

}
