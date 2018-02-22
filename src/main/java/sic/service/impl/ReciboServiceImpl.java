package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Recibo;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.repository.ReciboRepository;
import sic.service.BusinessServiceException;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICuentaCorrienteService;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;
import sic.service.INotaService;
import sic.service.IReciboService;
import sic.service.ServiceException;

@Service
public class ReciboServiceImpl implements IReciboService {
    
    private final ReciboRepository reciboRepository;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IEmpresaService empresaService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final INotaService notaService;
    private final IFormaDePagoService formaDePagoService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    public ReciboServiceImpl(ReciboRepository reciboRepository, ICuentaCorrienteService cuentaCorrienteService, 
                             IEmpresaService empresaService, IConfiguracionDelSistemaService cds, INotaService notaService,
                             IFormaDePagoService formaDePagoService) {
        this.reciboRepository = reciboRepository;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.empresaService = empresaService;
        this.configuracionDelSistemaService = cds;
        this.notaService = notaService;
        this.formaDePagoService = formaDePagoService;
    }

    @Override
    public Recibo getById(long idRecibo) {
        return reciboRepository.findById(idRecibo);
    }
    
    @Override
    public BigDecimal getMontoById(long idRecibo) {
        return reciboRepository.getMontoById(idRecibo);
    }
    
    @Override 
    @Transactional
    public Recibo guardar(Recibo recibo) {
        recibo.setNumSerie(configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip());
        recibo.setNumRecibo(this.getSiguienteNumeroRecibo(recibo.getEmpresa().getId_Empresa(), configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip()));
        recibo.setFecha(new Date());
        this.validarRecibo(recibo);
        recibo = reciboRepository.save(recibo);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
        LOGGER.warn("El Recibo " + recibo + " se guardó correctamente.");
        return recibo;
    }

    @Override
    @Transactional
    public Recibo actualizarSaldoSobrante(long idRecibo, BigDecimal saldoSobrante) {
        Recibo r = reciboRepository.findById(idRecibo);
        return reciboRepository.save(r);
    }

    private void validarRecibo(Recibo recibo) {
        //Requeridos
        if (recibo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_monto_igual_menor_cero"));
        }
        if (recibo.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_empresa_vacia"));
        }
        if (recibo.getCliente() == null && recibo.getProveedor() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_cliente_proveedor_vacio"));
        }
        if (recibo.getCliente() != null && recibo.getProveedor() != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_cliente_proveedor_simultaneos"));
        }
        if (recibo.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_usuario_vacio"));
        }
        if (recibo.getFormaDePago() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_forma_de_pago_vacia"));
        }
        if (recibo.getConcepto() == null || recibo.getConcepto().equals("")) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_concepto_vacio"));
        }
    }
    
    @Override
    public long getSiguienteNumeroRecibo(long idEmpresa, long serie) {
        Recibo recibo = reciboRepository.findTopByEmpresaAndNumSerieOrderByNumReciboDesc(empresaService.getEmpresaPorId(idEmpresa), serie);
        if (recibo == null) {
            return 1; // No existe ningun Recibo anterior
        } else {
            return 1 + recibo.getNumRecibo();
        }
    }
    
    @Override 
    public List<Recibo> construirRecibos(long[] idsFormaDePago, Empresa empresa, Cliente cliente, Usuario usuario, BigDecimal[] montos, BigDecimal totalFactura, Date fecha) { 
        List<Recibo> recibos = new ArrayList<>();
        int i = 0;
        if (idsFormaDePago != null && montos != null && idsFormaDePago.length == montos.length) {
            BigDecimal totalMontos = BigDecimal.ZERO;
            for (BigDecimal monto : montos) {
                totalMontos = totalMontos.add(monto);
            }
            if (totalMontos.compareTo(totalFactura) > 0 || totalMontos.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pagos_superan_total_factura"));
            }
            for (long idFormaDePago : idsFormaDePago) {
                Recibo recibo = new Recibo();
                recibo.setCliente(cliente);
                recibo.setUsuario(usuario);
                recibo.setEmpresa(empresa);
                recibo.setFecha(fecha);
                FormaDePago fp = formaDePagoService.getFormasDePagoPorId(idFormaDePago);
                recibo.setFormaDePago(fp);
                recibo.setMonto(montos[i]);
                recibo.setNumSerie(configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip());
                recibo.setNumRecibo(this.getSiguienteNumeroRecibo(empresa.getId_Empresa(), recibo.getNumSerie()));
                recibo.setConcepto("SALDO.");
                recibos.add(recibo);
                i++;
            }
        }
        return recibos;
    }
   
    @Override
    public void eliminar(long idRecibo) {
        Recibo r = reciboRepository.findById(idRecibo);
        if (notaService.existeNotaDebitoPorRecibo(r) == false) {           
            r.setEliminado(true);
            this.cuentaCorrienteService.asentarEnCuentaCorriente(r, TipoDeOperacion.ELIMINACION);
            reciboRepository.save(r);
            LOGGER.warn("El Recibo " + r + " se eliminó correctamente.");
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_no_se_puede_eliminar"));
        }
    }

    @Override
    public List<Recibo> getByClienteAndEmpresaAndEliminado(Cliente cliente, Empresa empresa, boolean eliminado) {
        return reciboRepository.findAllByClienteAndEmpresaAndEliminado(cliente, empresa, eliminado);
    }

    @Override
    public List<Recibo> getByUsuarioAndEmpresaAndEliminado(Usuario usuario, Empresa empresa, boolean eliminado) {
        return reciboRepository.findAllByUsuarioAndEmpresaAndEliminado(usuario, empresa, eliminado);
    }

    @Override
    public Page<Recibo> getByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminado, Pageable page) {
        return reciboRepository.findAllByFechaBetweenAndClienteAndEmpresaAndEliminado(desde, hasta, cliente, empresa, eliminado, page);
    }
    
    @Override
    public List<Recibo> getByFechaBetweenAndFormaDePagoAndEmpresaAndEliminado(Date desde, Date hasta, FormaDePago formaDePago, Empresa empresa) {
        return reciboRepository.findAllByFechaBetweenAndFormaDePagoAndEmpresaAndEliminado(desde, hasta, formaDePago, empresa, false);
    }
    
    @Override
    public byte[] getReporteRecibo(Recibo recibo) {
        if (recibo.getProveedor() != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_reporte_proveedor"));
        }
        recibo.getCliente().setSaldoCuentaCorriente(cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente()).getSaldo());
        ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Recibo.jasper");
        Map params = new HashMap();
        params.put("recibo", recibo);
        if (!recibo.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(recibo.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }
  
}
