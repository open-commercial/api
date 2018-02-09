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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.FormaDePago;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeComprobante;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.repository.ReciboRepository;
import sic.service.BusinessServiceException;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICuentaCorrienteService;
import sic.service.IEmpresaService;
import sic.service.IFacturaService;
import sic.service.IFormaDePagoService;
import sic.service.INotaService;
import sic.service.IPagoService;
import sic.service.IReciboService;
import sic.service.IRenglonCuentaCorrienteService;
import sic.service.ServiceException;

@Service
public class ReciboServiceImpl implements IReciboService {
    
    private final ReciboRepository reciboRepository;
    private final IFacturaService facturaService;
    private final IPagoService pagoService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IEmpresaService empresaService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
    private final INotaService notaService;
    private final IFormaDePagoService formaDePagoService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    public ReciboServiceImpl(ReciboRepository reciboRepository, IFacturaService facturaService, IPagoService pagoService,
                             ICuentaCorrienteService cuentaCorrienteService, IEmpresaService empresaService, 
                             IConfiguracionDelSistemaService cds, IRenglonCuentaCorrienteService renglonCuentaCorrienteService,
                             INotaService notaService, IFormaDePagoService formaDePagoService) {
        this.reciboRepository = reciboRepository;
        this.facturaService = facturaService;
        this.pagoService = pagoService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.empresaService = empresaService;
        this.configuracionDelSistemaService = cds;
        this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
        this.notaService = notaService;
        this.formaDePagoService = formaDePagoService;
    }

    @Override
    public Recibo getById(long idRecibo) {
        return reciboRepository.findById(idRecibo);
    }

    @Override
    public Recibo getReciboDelPago(long idPago) {
        return reciboRepository.getReciboDelPago(idPago);
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
        BigDecimal monto = recibo.getMonto();
        int i = 0;
        this.validarRecibo(recibo);
        recibo = reciboRepository.save(recibo);
        Pageable pageable = new PageRequest(i, 10);
        if (recibo.getCliente() != null) {
            Slice<RenglonCuentaCorriente> renglonesCC
                    = this.renglonCuentaCorrienteService
                            .getRenglonesFacturasYNotaDebitoCuentaCorriente(
                                    this.cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente()).getIdCuentaCorriente(), pageable);
            while (renglonesCC.hasContent() && monto.compareTo(BigDecimal.ZERO) != 0) {
                monto = this.pagarMultiplesComprobantesCliente(renglonesCC.getContent(), recibo, monto, recibo.getFormaDePago(), recibo.getConcepto());
                if (renglonesCC.hasNext()) {
                    i++;
                    pageable = new PageRequest(i, 10);
                    renglonesCC = this.renglonCuentaCorrienteService
                            .getRenglonesFacturasYNotaDebitoCuentaCorriente(
                                    this.cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente()).getIdCuentaCorriente(), pageable);
                } else {
                    break;
                }
            }
        } else if (recibo.getProveedor() != null) {
            Slice<RenglonCuentaCorriente> renglonesCC
                    = this.renglonCuentaCorrienteService
                            .getRenglonesFacturasYNotaDebitoCuentaCorriente(
                                    this.cuentaCorrienteService.getCuentaCorrientePorProveedor(recibo.getProveedor().getId_Proveedor()).getIdCuentaCorriente(), pageable);
            while (renglonesCC.hasContent() && monto.compareTo(BigDecimal.ZERO) != 0) {
                monto = this.pagarMultiplesComprobantesProveedor(renglonesCC.getContent(), recibo, monto, recibo.getFormaDePago(), recibo.getConcepto());
                if (renglonesCC.hasNext()) {
                    i++;
                    pageable = new PageRequest(i, 10);
                    renglonesCC = this.renglonCuentaCorrienteService
                            .getRenglonesFacturasYNotaDebitoCuentaCorriente(
                                    this.cuentaCorrienteService.getCuentaCorrientePorProveedor(recibo.getProveedor().getId_Proveedor()).getIdCuentaCorriente(), pageable);
                } else {
                    break;
                }
            }
        }
        recibo.setSaldoSobrante(monto);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
        LOGGER.warn("El Recibo " + recibo + " se guardó correctamente.");
        return recibo;
    }

    @Override
    @Transactional
    public Recibo actualizarSaldoSobrante(long idRecibo, BigDecimal saldoSobrante) {
        Recibo r = reciboRepository.findById(idRecibo);
        r.setSaldoSobrante(saldoSobrante);
        return reciboRepository.save(r);
    }

    private void validarRecibo(Recibo recibo) {
        //Requeridos
        if (recibo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_monto_igual_menor_cero"));
        }
        if (recibo.getSaldoSobrante().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_saldo_sobrante_menor_cero"));
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
                recibo.setConcepto("Cancelación parcial o total de su deuda.");
                recibo.setSaldoSobrante(BigDecimal.ZERO);
                recibos.add(recibo);
                i++;
            }
        }
        return recibos;
    }
      
    @Override
    public BigDecimal pagarMultiplesComprobantesCliente(List<RenglonCuentaCorriente> renglonesCC, Recibo recibo, BigDecimal monto, FormaDePago formaDePago, String nota) {
        for (RenglonCuentaCorriente rcc : renglonesCC) {
            if (monto.compareTo(BigDecimal.ZERO) > 0) {
                if (rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_A || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                        || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_C || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_X
                        || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_Y || rcc.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
                    FacturaVenta fv = (FacturaVenta) facturaService.getFacturaPorId(rcc.getIdMovimiento());
                    BigDecimal credito = notaService.calcularTotaCreditoPorFacturaVenta(fv);
                    BigDecimal saldoAPagar = this.pagoService.getSaldoAPagarFactura(fv.getId_Factura());
                    if (fv.isPagada() == false && saldoAPagar.compareTo(credito) > 0) {
                        fv.setPagos(this.pagoService.getPagosDeLaFactura(fv.getId_Factura()));
                        Pago nuevoPago = new Pago();
                        nuevoPago.setFormaDePago(formaDePago);
                        nuevoPago.setFactura(fv);
                        nuevoPago.setEmpresa(fv.getEmpresa());
                        nuevoPago.setNota(nota);
                        saldoAPagar = saldoAPagar.subtract(credito);
                        if (saldoAPagar.compareTo(monto) < 1) {
                            monto = monto.subtract(saldoAPagar);
                            nuevoPago.setMonto(saldoAPagar);
                        } else {
                            nuevoPago.setMonto(monto);
                            monto = BigDecimal.ZERO;
                        }
                        nuevoPago.setFactura(fv);
                        nuevoPago.setRecibo(recibo);
                        this.pagoService.guardar(nuevoPago);
                    }
                } else if (rcc.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_A
                        || rcc.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_B || rcc.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_X
                        || rcc.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_Y || rcc.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO) {
                    NotaDebito nd = (NotaDebito) notaService.getNotaPorId(rcc.getIdMovimiento());
                    nd.setPagos(this.notaService.getPagosNota(nd.getIdNota()));
                    if (nd.isPagada() == false) {
                        Pago nuevoPago = new Pago();
                        nuevoPago.setFormaDePago(formaDePago);
                        nuevoPago.setNotaDebito(nd);
                        nuevoPago.setEmpresa(nd.getEmpresa());
                        nuevoPago.setNota(nota);
                        BigDecimal saldoAPagar = this.pagoService.getSaldoAPagarNotaDebito(nd.getIdNota());
                        if (saldoAPagar.compareTo(monto) < 1) {
                            monto = monto.subtract(saldoAPagar);
                            nuevoPago.setMonto(saldoAPagar);
                        } else {
                            nuevoPago.setMonto(monto);
                            monto = BigDecimal.ZERO;
                        }
                        nuevoPago.setNotaDebito(nd);
                        nuevoPago.setRecibo(recibo);
                        this.pagoService.guardar(nuevoPago);
                    }
                }
            }
        }
        return monto;
    }
    
    @Override
    public BigDecimal pagarMultiplesComprobantesProveedor(List<RenglonCuentaCorriente> renglonesCC, Recibo recibo, BigDecimal monto, FormaDePago formaDePago, String nota) {
        for (RenglonCuentaCorriente rcc : renglonesCC) {
            if (monto.compareTo(BigDecimal.ZERO) > 0) {
                if (rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_A || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                        || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_C || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_X
                        || rcc.getTipoComprobante() == TipoDeComprobante.FACTURA_Y || rcc.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
                    FacturaCompra fc = (FacturaCompra) facturaService.getFacturaPorId(rcc.getIdMovimiento());
                    if (fc.isPagada() == false) {
                        fc.setPagos(this.pagoService.getPagosDeLaFactura(fc.getId_Factura()));
                        Pago nuevoPago = new Pago();
                        nuevoPago.setFormaDePago(formaDePago);
                        nuevoPago.setFactura(fc);
                        nuevoPago.setEmpresa(fc.getEmpresa());
                        nuevoPago.setNota(nota);
                        BigDecimal saldoAPagar = this.pagoService.getSaldoAPagarFactura(fc.getId_Factura());
                        if (saldoAPagar.compareTo(monto) < 1) {
                            monto = monto.subtract(saldoAPagar);
                            nuevoPago.setMonto(saldoAPagar);
                        } else {
                            nuevoPago.setMonto(monto);
                            monto = BigDecimal.ZERO;
                        }
                        nuevoPago.setFactura(fc);
                        nuevoPago.setRecibo(recibo);
                        this.pagoService.guardar(nuevoPago);
                    }
                }
            }
        }
        return monto;
    }
   
    @Override
    public void eliminar(long idRecibo) {
        Recibo r = reciboRepository.findById(idRecibo);
        if (notaService.existeNotaDebitoPorRecibo(r) == false) {
            pagoService.getPagosRelacionadosAlRecibo(idRecibo).forEach((p) -> {
                pagoService.eliminar(p.getId_Pago());
            });           
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
    public List<Recibo> getRecibosConSaldoSobranteCliente(long idEmpresa, long idCliente) {
        return reciboRepository.getRecibosConSaldoSobranteCliente(idEmpresa, idCliente);
    }
    
    @Override
    public List<Recibo> getRecibosConSaldoSobranteProveedor(long idEmpresa, long idProveedor) {
        return reciboRepository.getRecibosConSaldoSobranteProveedor(idEmpresa, idProveedor);
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
