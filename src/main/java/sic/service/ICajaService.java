package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaCajaCriteria;
import sic.modelo.Caja;

public interface ICajaService {

    void actualizar(Caja caja);
    
    void eliminar(Long idCaja);
    
    Caja getCajaPorId(Long id);

    List<Caja> getCajas(long id_Empresa, Date desde, Date hasta);

    Page<Caja> getCajasCriteria(BusquedaCajaCriteria criteria);

    Caja getUltimaCaja(long id_Empresa);

    int getUltimoNumeroDeCaja(long id_Empresa);

    Caja guardar(Caja caja);

    void validarCaja(Caja caja);
    
    Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling);
    
    BigDecimal getTotalCaja(Caja caja, boolean afectaCaja);
    
    BigDecimal getSaldoFinalCajas(long idEmpresa, Long idUsuario, Date desde, Date hasta);
    
    BigDecimal getSaldoRealCajas(long idEmpresa, Long idUsuario, Date desde, Date hasta);

}
