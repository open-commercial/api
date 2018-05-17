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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.ReciboRepository;
import sic.service.*;

@Service
public class ReciboServiceImpl implements IReciboService {
    
    private final ReciboRepository reciboRepository;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IEmpresaService empresaService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final INotaService notaService;
    private final IFormaDePagoService formaDePagoService;
    private final ICajaService cajaService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public ReciboServiceImpl(ReciboRepository reciboRepository, ICuentaCorrienteService cuentaCorrienteService, 
                             IEmpresaService empresaService, IConfiguracionDelSistemaService cds, INotaService notaService,
                             IFormaDePagoService formaDePagoService, ICajaService cajaService) {
        this.reciboRepository = reciboRepository;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.empresaService = empresaService;
        this.configuracionDelSistemaService = cds;
        this.notaService = notaService;
        this.formaDePagoService = formaDePagoService;
        this.cajaService = cajaService;
    }

    @Override
    public Recibo getById(long idRecibo) {
        return reciboRepository.findById(idRecibo);
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
    public List<Recibo> construirRecibos(long[] idsFormaDePago, Empresa empresa, Cliente cliente,
                                         Usuario usuario, BigDecimal[] montos, BigDecimal totalFactura, Date fecha) {
        List<Recibo> recibos = new ArrayList<>();
        if (idsFormaDePago != null && montos != null && idsFormaDePago.length == montos.length) {
            BigDecimal totalMontos = BigDecimal.ZERO;
            for (BigDecimal monto : montos) {
                totalMontos = totalMontos.add(monto);
            }
            if (totalMontos.compareTo(totalFactura) > 0 || totalMontos.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_recibo_superan_total_factura"));
            }
            int i = 0;
            for (long idFormaDePago : idsFormaDePago) {
                Recibo recibo = new Recibo();
                recibo.setCliente(cliente);
                recibo.setUsuario(usuario);
                recibo.setEmpresa(empresa);
                recibo.setFecha(fecha);
                FormaDePago fdp = formaDePagoService.getFormasDePagoPorId(idFormaDePago);
                recibo.setFormaDePago(fdp);
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
    @Transactional
    public void eliminar(long idRecibo) {
        Recibo r = reciboRepository.findById(idRecibo);
        if (!notaService.existeNotaDebitoPorRecibo(r)) {
            r.setEliminado(true);
            this.cuentaCorrienteService.asentarEnCuentaCorriente(r, TipoDeOperacion.ELIMINACION);
            this.actualizarCajaPorEliminacionDeRecibo(r);
            reciboRepository.save(r);
            LOGGER.warn("El Recibo " + r + " se eliminó correctamente.");
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_no_se_puede_eliminar"));
        }
    }

    private void actualizarCajaPorEliminacionDeRecibo(Recibo recibo) {
        Caja caja = this.cajaService.encontrarCajaCerradaQueContengaFecha(recibo.getEmpresa().getId_Empresa(), recibo.getFecha());
        BigDecimal monto = BigDecimal.ZERO;
        if (caja != null && caja.getEstado().equals(EstadoCaja.CERRADA)) {
            if (recibo.getCliente() != null) {
                monto = recibo.getMonto().negate();
            } else if (recibo.getProveedor() != null) {
                monto = recibo.getMonto();
            }
            cajaService.actualizarSaldoSistema(caja, monto);
            LOGGER.warn("El Recibo " + recibo + " modificó la caja " + caja + "debido a una eliminación.");
        }
    }

    @Override
    public List<Recibo> getRecibosEntreFechasPorFormaDePago(Date desde, Date hasta, FormaDePago formaDePago, Empresa empresa) {
        return reciboRepository.getRecibosEntreFechasPorFormaDePago(empresa.getId_Empresa(), formaDePago.getId_FormaDePago(), desde, hasta);
    }
    
    @Override
    public byte[] getReporteRecibo(Recibo recibo) {
        if (recibo.getProveedor() != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_reporte_proveedor"));
        }
        recibo.getCliente().setSaldoCuentaCorriente(cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente()).getSaldo());
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

    @Override
    public BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(long idEmpresa, long idFormaDePago, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosClientesEntreFechasPorFormaDePago(idEmpresa, idFormaDePago, desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(long idEmpresa, long idFormaDePago, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosProveedoresEntreFechasPorFormaDePago(idEmpresa, idFormaDePago,desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(long idEmpresa, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosClientesQueAfectanCajaEntreFechas(idEmpresa, desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(long idEmpresa, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(idEmpresa, desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getTotalRecibosClientesEntreFechas(long idEmpresa, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosClientesEntreFechas(idEmpresa, desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getTotalRecibosProveedoresEntreFechas(long idEmpresa, Date desde, Date hasta) {
        BigDecimal total = reciboRepository.getTotalRecibosProveedoresEntreFechas(idEmpresa, desde, hasta);
        return (total == null) ? BigDecimal.ZERO : total;
    }


}
