package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

public interface IGastoService {
    
    Gasto getGastoPorId(Long id);
    
    void actualizar(Gasto gasto);
    
    void eliminar(long idGasto);

    List<Gasto> getGastosEntreFechasYFormaDePago(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta);
    
    Gasto getGastosPorNroYEmpreas(Long nroPago, Long id_Empresa);
    
    BigDecimal calcularTotalGastos(List<Gasto> gastos);

    Gasto guardar(Gasto gasto);

    void validarGasto(Gasto gasto);

    long getUltimoNumeroDeGasto(long id_empresa);

    BigDecimal getTotalGastosEntreFechasYFormaDePago(long idEmpresa, long idFormaDePago, Date desde, Date hasta);

}
