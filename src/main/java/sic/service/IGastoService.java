package sic.service;

import java.util.Date;
import java.util.List;
import sic.modelo.Gasto;

public interface IGastoService {
    
    Gasto getGastoPorId(Long id);
    
    void actualizar(Gasto gasto);
    
    void eliminar(long idGasto);

    List<Gasto> getGastosPorFecha(Long id_Empresa, Date desde, Date hasta);

    List<Gasto> getGastosEntreFechasYFormaDePago(Long id_Empresa, Long id_FormaDePago, Date desde, Date hasta);
    
    Gasto getGastosPorNroYEmpreas(Long nroPago, Long id_Empresa);
    
    double calcularTotalGastos(List<Gasto> gastos);

    Gasto guardar(Gasto gasto);

    void validarGasto(Gasto gasto);

    long getUltimoNumeroDeGasto(long id_empresa);

}
