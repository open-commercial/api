package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

import javax.validation.Valid;

public interface IGastoService {
    
    Gasto getGastoPorId(Long id);
    
    void actualizar(Gasto gasto);
    
    void eliminar(long idGasto);

    List<Gasto> getGastosEntreFechasYFormaDePago(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta);

    Gasto guardar(@Valid Gasto gasto);

    void validarOperacion(Gasto gasto);

    long getUltimoNumeroDeGasto(long id_empresa);

    BigDecimal getTotalGastosEntreFechasYFormaDePago(long idEmpresa, long idFormaDePago, Date desde, Date hasta);

    BigDecimal getTotalGastosQueAfectanCajaEntreFechas(long idEmpresa, Date desde, Date hasta);

    BigDecimal getTotalGastosEntreFechas(long idEmpresa, Date desde, Date hasta);

}
