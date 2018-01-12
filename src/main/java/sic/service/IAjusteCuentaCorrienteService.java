package sic.service;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Nota;

public interface IAjusteCuentaCorrienteService {
    
    AjusteCuentaCorriente getById(long idAjusteCuentaCorriente);
    
    AjusteCuentaCorriente getByNroAjuste(long idAjusteCuentaCorriente);
    
    Page<AjusteCuentaCorriente> getAllByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, Pageable page);
    
    AjusteCuentaCorriente findByNotaDebitoAndEliminado(Nota notaDebito);
    
    Double getMontoById(long idRecibo);
    
    AjusteCuentaCorriente guardar(AjusteCuentaCorriente ajusteCuentaCorriente);
    
    long getSiguienteNumeroAjuste(long idEmpresa, long numSerie);
    
    void eliminar(long idAjusteCuentaCorriente);
    
}
