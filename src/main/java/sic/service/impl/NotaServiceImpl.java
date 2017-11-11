package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.BusquedaNotaCriteria;
import sic.modelo.Cliente;
import sic.modelo.ComprobanteAFIP;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FacturaVenta;
import sic.modelo.Movimiento;
import sic.modelo.Nota;
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.service.BusinessServiceException;
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.IFacturaService;
import sic.service.INotaService;
import sic.repository.NotaCreditoRepository;
import sic.repository.NotaDebitoRepository;
import sic.repository.NotaRepository;
import sic.service.IAfipService;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICuentaCorrienteService;
import sic.service.IPagoService;
import sic.service.IProductoService;
import sic.service.IUsuarioService;
import sic.service.ServiceException;
import sic.util.FormatterFechaHora;

@Service
public class NotaServiceImpl implements INotaService {

    private final NotaRepository notaRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaDebitoRepository notaDebitoRepository;
    private final IFacturaService facturaService;
    private final IClienteService clienteService;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IPagoService pagoService;
    private final IProductoService productoService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IAfipService afipService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Lazy
    public NotaServiceImpl(NotaRepository notaRepository, NotaCreditoRepository notaDeCreditoRepository,
            NotaDebitoRepository notaDeDebitoRespository, IFacturaService facturaService,
            IClienteService clienteService, IUsuarioService usuarioService, IProductoService productoService,
            IEmpresaService empresaService, IPagoService pagoService, ICuentaCorrienteService cuentaCorrienteService,
            IConfiguracionDelSistemaService cds, IAfipService afipService) {
        this.notaRepository = notaRepository;
        this.notaCreditoRepository = notaDeCreditoRepository;
        this.notaDebitoRepository = notaDeDebitoRespository;
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.empresaService = empresaService;
        this.pagoService = pagoService;
        this.productoService = productoService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.configuracionDelSistemaService = cds;
        this.afipService = afipService;
    }

    @Override
    public Nota getNotaPorId(Long idNota) {
        return this.notaRepository.findById(idNota);
    }
    
    @Override
    public Long getCAEById(Long idNota) {
        return notaRepository.getCAEById(idNota);
    }
    
    @Override
    public Double getTotalById(Long idNota) { 
        Double total = notaRepository.getTotalById(idNota);
        return (total != null) ? total : 0.0;
    }

    @Override
    public FacturaVenta getFacturaNota(Long idNota) {
        return this.getNotaPorId(idNota).getFacturaVenta();
    }
      
    @Override
    public NotaDebito getNotaDebitoPorPago(Long idPago) {
        return this.notaDebitoRepository.findByPagoIdAndEliminada(idPago, false);
    }
    
    @Override
    public List<Pago> getPagosNota(Long idNota) {
        return pagoService.getPagosDeNotas(idNota);
    }
    
    @Override
    public double getTotalPagado(Long idNota) {
        double pagado = 0;
        List<Pago> pagos = this.getPagosNota(idNota);
        for (Pago pago : pagos) {
            pagado = pagado + pago.getMonto();
        }
        return pagado;        
    }
    
    @Override
    public List<Nota> getNotasPorFactura(Long idFactura) {
        return notaRepository.findAllByFacturaVentaAndEliminada((FacturaVenta) facturaService.getFacturaPorId(idFactura), false);
    }

    @Override
    public List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura) {
        HashMap<Long, Double> listaCantidadesProductosUnificados = new HashMap<>();
        this.getNotasPorFactura(idFactura).forEach(n -> {
            for (RenglonNotaCredito rnc : ((NotaCredito) n).getRenglonesNotaCredito()) {
                if (listaCantidadesProductosUnificados.containsKey(rnc.getIdProductoItem())) {
                    listaCantidadesProductosUnificados.put(rnc.getIdProductoItem(),
                            listaCantidadesProductosUnificados.get(rnc.getIdProductoItem()) + rnc.getCantidad());
                } else {
                    listaCantidadesProductosUnificados.put(rnc.getIdProductoItem(), rnc.getCantidad());
                }
            }
        });
        List<RenglonFactura> renglonesFactura = facturaService.getRenglonesDeLaFactura(idFactura);
        if (!listaCantidadesProductosUnificados.isEmpty()) {
            renglonesFactura.forEach(rf -> {
                if (listaCantidadesProductosUnificados.containsKey(rf.getId_ProductoItem())) {
                    rf.setCantidad(rf.getCantidad() - listaCantidadesProductosUnificados.get(rf.getId_ProductoItem()));
                }
            });
        }
        return renglonesFactura;
    }

    @Override
    public Page<Nota> buscarNotasPorClienteYEmpresa(BusquedaNotaCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Cliente
        if (criteria.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
        }
        return notaRepository.findAllByFechaBetweenAndClienteAndEmpresaAndEliminada(criteria.getFechaDesde(),
                criteria.getFechaHasta(), criteria.getCliente(), criteria.getEmpresa(), false, criteria.getPageable());
    }

    @Override
    public List<Nota> getNotasPorClienteYEmpresa(Long idCliente, Long idEmpresa) {
        return this.notaRepository.findAllByClienteAndEmpresaAndEliminada(this.clienteService.getClientePorId(idCliente),
                this.empresaService.getEmpresaPorId(idEmpresa), false);
    }

    @Override
    public long getSiguienteNumeroNotaDebito(Long idCliente, Long idEmpresa) {
        Nota nota = notaDebitoRepository
                .findTopByClienteAndEmpresaAndEliminadaOrderByNroNotaDesc(clienteService.getClientePorId(idCliente),
                        empresaService.getEmpresaPorId(idEmpresa), false);
        return (nota == null) ? 1 : nota.getNroNota() + 1;
    }
    
    @Override
    public long getSiguienteNumeroNotaCredito(Long idCliente, Long idEmpresa) {
        Nota nota = notaCreditoRepository
                .findTopByClienteAndEmpresaAndEliminadaOrderByNroNotaDesc(clienteService.getClientePorId(idCliente),
                        empresaService.getEmpresaPorId(idEmpresa), false);
        return (nota == null) ? 1 : nota.getNroNota() + 1;
    }

    @Override
    public double getSaldoNotas(Date hasta, Long idCliente, Long IdEmpresa) {
        Double totalCredito = notaCreditoRepository.totalNotasCredito(hasta, clienteService.getClientePorId(idCliente), empresaService.getEmpresaPorId(IdEmpresa));
        Double totalDebito = notaDebitoRepository.totalNotasDebito(hasta, clienteService.getClientePorId(idCliente), empresaService.getEmpresaPorId(IdEmpresa));
        totalCredito = (totalCredito == null ? 0 : totalCredito);
        totalDebito  = (totalDebito == null ? 0 : totalDebito);
        return totalCredito - totalDebito;
    }

    @Override
    public TipoDeComprobante[] getTipoNota(Long idCliente, Long idEmpresa) {
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        Cliente cliente = clienteService.getClientePorId(idCliente);
        if (empresa.getCondicionIVA().isDiscriminaIVA() && cliente.getCondicionIVA().isDiscriminaIVA()) {
            TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[6];
            tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_A;
            tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_X;
            tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
            tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_A;
            tiposPermitidos[4] = TipoDeComprobante.NOTA_DEBITO_X;
            tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
            return tiposPermitidos;
        } else if (empresa.getCondicionIVA().isDiscriminaIVA() && !cliente.getCondicionIVA().isDiscriminaIVA()) {
            TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[6];
            tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_B;
            tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_X;
            tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
            tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_B;
            tiposPermitidos[4] = TipoDeComprobante.NOTA_DEBITO_X;
            tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
            return tiposPermitidos;
        } else {
            TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
            tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_X;
            tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
            tiposPermitidos[2] = TipoDeComprobante.NOTA_DEBITO_X;
            tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
            return tiposPermitidos;
        }
    }

    @Override
    public List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota) {
        return this.notaCreditoRepository.getById(idNota).getRenglonesNotaCredito();
    }

    @Override
    public List<RenglonNotaDebito> getRenglonesDeNotaDebito(Long idNota) {
        return this.notaDebitoRepository.getById(idNota).getRenglonesNotaDebito();
    }

    private void validarNota(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idFactura) {
        if (idFactura != null) {
            Factura f = facturaService.getFacturaPorId(idFactura);
            if (f instanceof FacturaVenta) {
                nota.setFacturaVenta((FacturaVenta) f);
                nota.setCliente(((FacturaVenta) f).getCliente());
            } else {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_de_credito_factura_no_valida"));
            }
        }
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        if (empresa == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        nota.setEmpresa(empresa);
        Cliente cliente = clienteService.getClientePorId(idCliente);
        if (cliente == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
        }
        nota.setCliente(cliente);
        Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
        if (usuario == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_no_existente"));
        }
        nota.setUsuario(usuario);
        if (nota.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_de_empresa_vacia"));
        }
        if (nota.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_de_cliente_vacia"));
        }
        if (nota.getFecha() != null) {
            if (nota.getFacturaVenta() != null) {
                if (nota.getFecha().compareTo(nota.getFacturaVenta().getFecha()) <= 0) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_fecha_incorrecta"));
                }
                if (!nota.getCliente().equals(nota.getFacturaVenta().getCliente())) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_cliente_incorrecto"));
                }
                if (!nota.getEmpresa().equals(nota.getFacturaVenta().getEmpresa())) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_empresa_incorrecta"));
                }
            }
        }
        if (nota.getMotivo() == null || nota.getMotivo().isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_de_motivo_vacio"));
        }
        if (nota instanceof NotaCredito) {
            if (((NotaCredito) nota).getRenglonesNotaCredito() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_de_renglones_vacio"));
            }
        } else {
            if (((NotaDebito) nota).getRenglonesNotaDebito() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_de_renglones_vacio"));
            }
        }
    }

    private void validarCalculosCredito(NotaCredito notaCredito) {
        TipoDeComprobante tipoDeComprobanteDeFacturaRelacionada = this.getTipoDeComprobanteFacturaSegunNotaCredito(notaCredito);
        List<RenglonNotaCredito> renglonesNotaCredito = notaCredito.getRenglonesNotaCredito();
        double subTotal = 0;
        double[] importes = new double[renglonesNotaCredito.size()];
        int i = 0;
        int sizeRenglonesCredito = renglonesNotaCredito.size();
        //IVA - subTotal
        double iva21 = 0.0;
        double iva105 = 0.0;
        if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_Y
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
            double[] ivaPorcentajes = new double[sizeRenglonesCredito];
            double[] ivaNetos = new double[sizeRenglonesCredito];
            double[] cantidades = new double[sizeRenglonesCredito];
            for (RenglonNotaCredito r : renglonesNotaCredito) {
                ivaPorcentajes[i] = r.getIvaPorcentaje();
                ivaNetos[i] = r.getIvaNeto();
                cantidades[i] = r.getCantidad();
                importes[i] = r.getImporteBruto();
                i++;
            }
            subTotal = this.calcularSubTotalCredito(importes);
            if (notaCredito.getSubTotal() != subTotal) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_sub_total_no_valido"));
            }
            iva21 = this.calcularIVANetoCredito(tipoDeComprobanteDeFacturaRelacionada, cantidades, ivaPorcentajes, ivaNetos,
                    21, notaCredito.getDescuentoPorcentaje(), notaCredito.getRecargoPorcentaje());
            if (notaCredito.getIva21Neto() != iva21) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva21_no_valido"));
            }
            iva105 = this.calcularIVANetoCredito(tipoDeComprobanteDeFacturaRelacionada, cantidades, ivaPorcentajes, ivaNetos,
                    10.5, notaCredito.getDescuentoPorcentaje(), notaCredito.getRecargoPorcentaje());
            if (notaCredito.getIva105Neto() != iva105) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva105_no_valido"));
            }
        } else if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_X) {
            for (RenglonNotaCredito r : renglonesNotaCredito) {
                importes[i] = r.getImporteNeto();
                i++;
            }
            subTotal = this.calcularSubTotalCredito(importes);
            if (notaCredito.getSubTotal() != subTotal) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_sub_total_no_valido"));
            }
            if (notaCredito.getIva21Neto() != 0.0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva21_no_valido"));
            }
            if (notaCredito.getIva105Neto() != 0.0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva105_no_valido"));
            }
        }
        //DescuentoNeto
        double descuentoNeto = this.calcularDecuentoNetoCredito(subTotal, notaCredito.getDescuentoPorcentaje());
        if (notaCredito.getDescuentoNeto() != descuentoNeto) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_descuento_neto_no_valido"));
        }
        //RecargoNeto
        double recargoNeto = this.calcularRecargoNetoCredito(subTotal, notaCredito.getRecargoPorcentaje());
        if (notaCredito.getRecargoNeto() != recargoNeto) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_recargo_neto_no_valido"));
        }
        //subTotalBruto
        double subTotalBruto = this.calcularSubTotalBrutoCredito(tipoDeComprobanteDeFacturaRelacionada, subTotal, recargoNeto, descuentoNeto, iva105, iva21);
        if (notaCredito.getSubTotalBruto() != subTotalBruto) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_sub_total_bruto_no_valido"));
        }
        //Total
        double total = this.calcularTotalCredito(subTotalBruto, iva105, iva21);
        if (notaCredito.getTotal() != total) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_total_no_valido"));
        }
    }
    
    private void validarCalculosDebito(NotaDebito notaDebito) {
        // monto no gravado
        Double montoPago = pagoService.getPagoPorId(notaDebito.getPagoId()).getMonto();
        if (notaDebito.getMontoNoGravado() != montoPago) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_monto_no_gravado_no_valido"));
        }
        // iva 21
        double iva21 = 0.0;
        switch (notaDebito.getTipoComprobante()) {
            case NOTA_DEBITO_X:
            case NOTA_DEBITO_A:
            case NOTA_DEBITO_B:
            case NOTA_DEBITO_Y:
            case NOTA_DEBITO_PRESUPUESTO:
            case NOTA_CREDITO_X:
            case NOTA_CREDITO_A:
            case NOTA_CREDITO_B:
            case NOTA_CREDITO_PRESUPUESTO:
            iva21 = notaDebito.getSubTotalBruto() * 0.21;
                if (notaDebito.getIva21Neto() != iva21) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva21_no_valido"));
                }
                break;
        }
        // total
        if (notaDebito.getTotal() != this.calcularTotalDebito(notaDebito.getSubTotalBruto(), iva21, montoPago)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_total_no_valido"));
        }
    }

    @Override
    @Transactional
    public Nota guardarNota(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idFactura, Long idPago, boolean modificarStock) {
        this.validarNota(nota, idEmpresa, idCliente, idUsuario, idFactura);
        nota.setSerie(configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(nota.getEmpresa()).getNroPuntoDeVentaAfip());
        if (nota instanceof NotaCredito) {
            NotaCredito notaCredito = (NotaCredito) nota;
            notaCredito.setTipoComprobante(this.getTipoDeNotaCreditoSegunFactura(notaCredito.getFacturaVenta()));
            notaCredito.setNroNota(this.getSiguienteNumeroNotaCredito(idCliente, idEmpresa));
            notaCredito.setModificaStock(modificarStock);
            if (modificarStock) {
                this.actualizarStock(notaCredito.getRenglonesNotaCredito(), TipoDeOperacion.ACTUALIZACION);
            }
            this.validarCalculosCredito(notaCredito);
            notaCredito = notaCreditoRepository.save(notaCredito);
            this.cuentaCorrienteService.asentarEnCuentaCorriente(notaCredito, TipoDeOperacion.ALTA);
            LOGGER.warn("La Nota " + notaCredito + " se guardó correctamente.");
            return notaCredito;
        } else {
            NotaDebito notaDebito = (NotaDebito) nota;
            Pago pago = pagoService.getPagoPorId(idPago);
            notaDebito.setTipoComprobante(this.getTipoDeNotaDebitoSegunPago(pago));
            notaDebito.setNroNota(this.getSiguienteNumeroNotaDebito(idCliente, idEmpresa));
            notaDebito.setPagoId(pago.getId_Pago());
            this.validarCalculosDebito(notaDebito);
            notaDebito = notaDebitoRepository.save(notaDebito);
            this.cuentaCorrienteService.asentarEnCuentaCorriente(notaDebito, TipoDeOperacion.ALTA);
            LOGGER.warn("La Nota " + notaDebito + " se guardó correctamente.");
            return notaDebito;
        }
    }

    @Override
    @Transactional
    public Nota autorizarNota(Nota nota) {
        double montoNoGravado = (nota instanceof NotaDebito) ? ((NotaDebito) nota).getMontoNoGravado() : 0;
        ComprobanteAFIP comprobante = ComprobanteAFIP.builder()
                .fecha(nota.getFecha())
                .tipoComprobante(nota.getTipoComprobante())
                .CAE(nota.getCAE())
                .vencimientoCAE(nota.getVencimientoCAE())
                .numSerieAfip(nota.getNumSerieAfip())
                .numFacturaAfip(nota.getNumNotaAfip())
                .empresa(nota.getEmpresa())
                .cliente(nota.getCliente())
                .subtotalBruto(nota.getSubTotalBruto())
                .iva105neto(nota.getIva105Neto())
                .iva21neto(nota.getIva21Neto())
                .montoNoGravado(montoNoGravado)
                .total(nota.getTotal())
                .build();
        afipService.autorizar(comprobante);
        nota.setCAE(comprobante.getCAE());
        nota.setVencimientoCAE(comprobante.getVencimientoCAE());
        nota.setNumSerieAfip(comprobante.getNumSerieAfip());
        nota.setNumNotaAfip(comprobante.getNumFacturaAfip());
        return nota;
    }
    
    private TipoDeComprobante getTipoDeNotaDebitoSegunPago(Pago pago) {
        TipoDeComprobante tipo = null;
        if (pago.getFactura() != null) {
            switch (pago.getFactura().getTipoComprobante()) {
                case FACTURA_A:
                    tipo = TipoDeComprobante.NOTA_DEBITO_A;
                    break;
                case FACTURA_B:
                    tipo = TipoDeComprobante.NOTA_DEBITO_B;
                    break;
                case FACTURA_X:
                    tipo = TipoDeComprobante.NOTA_DEBITO_X;
                    break;
                case FACTURA_Y:
                    tipo = TipoDeComprobante.NOTA_DEBITO_Y;
                    break;
                case PRESUPUESTO:
                    tipo = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
                    break;
            }
        } else if (pago.getNotaDebito() != null) {
            tipo = pago.getNotaDebito().getTipoComprobante();
        }
        return tipo;
    }
    
    private TipoDeComprobante getTipoDeComprobanteFacturaSegunNotaCredito(NotaCredito notaCredito) {
        TipoDeComprobante tipo = null;
        if (notaCredito.getTipoComprobante() != null) {
            switch (notaCredito.getTipoComprobante()) {
                case NOTA_CREDITO_A:
                    tipo = TipoDeComprobante.FACTURA_A;
                    break;
                case NOTA_CREDITO_B:
                    tipo = TipoDeComprobante.FACTURA_B;
                    break;
                case NOTA_CREDITO_X:
                    tipo = TipoDeComprobante.FACTURA_X;
                    break;
                case NOTA_CREDITO_Y:
                    tipo = TipoDeComprobante.FACTURA_X;
                    break;
                case NOTA_CREDITO_PRESUPUESTO:
                    tipo = TipoDeComprobante.PRESUPUESTO;
                    break;
            }
        }
        return tipo;
    }
    
    private TipoDeComprobante getTipoDeNotaCreditoSegunFactura(Factura factura) {
        TipoDeComprobante tipo = null;
        switch (factura.getTipoComprobante()) {
            case FACTURA_A:
                tipo = TipoDeComprobante.NOTA_CREDITO_A;
                break;
            case FACTURA_B:
                tipo = TipoDeComprobante.NOTA_CREDITO_B;
                break;
            case FACTURA_X:
                tipo = TipoDeComprobante.NOTA_CREDITO_X;
                break;
            case FACTURA_Y:
                tipo = TipoDeComprobante.NOTA_CREDITO_Y;
                break;
            case PRESUPUESTO:
                tipo = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
                break;
        }
        return tipo;
    }
    
    private void actualizarStock(List<RenglonNotaCredito> renglonesNotaCredito, TipoDeOperacion tipoOperacion) {
        HashMap<Long, Double> idsYCantidades = new HashMap<>();
        renglonesNotaCredito.forEach(r -> {
            idsYCantidades.put(r.getIdProductoItem(), r.getCantidad());
        });
        productoService.actualizarStock(idsYCantidades, tipoOperacion, Movimiento.VENTA);
    }

    @Override
    public byte[] getReporteNota(Nota nota) {
        ClassLoader classLoader = NotaServiceImpl.class.getClassLoader();
        InputStream isFileReport;
        JRBeanCollectionDataSource ds;
        Map params = new HashMap();
        if (nota instanceof NotaCredito) {
            isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/NotaCredito.jasper");
            List<RenglonNotaCredito> renglones = this.getRenglonesDeNotaCredito(nota.getIdNota());
            ds = new JRBeanCollectionDataSource(renglones);
            params.put("notaCredito", (NotaCredito) nota);
        } else {
            isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/NotaDebito.jasper");
            List<RenglonNotaDebito> renglones = this.getRenglonesDeNotaDebito(nota.getIdNota());
            ds = new JRBeanCollectionDataSource(renglones);
            params.put("notaDebito", (NotaDebito) nota);
        }
        ConfiguracionDelSistema cds = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(nota.getEmpresa());
        params.put("preImpresa", cds.isUsarFacturaVentaPreImpresa());
        if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X) 
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_X)
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO)) {
            nota.setSubTotalBruto(nota.getTotal());
            nota.setIva105Neto(0);
            nota.setIva21Neto(0);
        }
        if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_A) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B)
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_A) 
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B)) {
            if (nota.getNumSerieAfip() != 0 && nota.getNumNotaAfip() != 0) {
                params.put("serie", nota.getNumSerieAfip());
                params.put("nroNota", nota.getNumNotaAfip());
            } else {
                params.put("serie", null);
                params.put("nroNota", null);
            }
        } else {
            params.put("serie", nota.getSerie());
            params.put("nroNota", nota.getNroNota());
        }
        if (!nota.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(nota.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }

    @Override
    @Transactional
    public void eliminarNota(long[] idsNota) {
        for (long idNota : idsNota) {
            Nota nota = this.getNotaPorId(idNota);
            if (nota != null && nota.getCAE() == 0l) {
                if (!getPagosNota(idNota).isEmpty()) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_no_se_puede_eliminar"));
                }
                if (nota instanceof NotaCredito) {
                    NotaCredito nc = (NotaCredito) nota;
                    if (nc.isModificaStock()) {
                        this.actualizarStock(nc.getRenglonesNotaCredito(), TipoDeOperacion.ALTA);
                    }
                }
                nota.setEliminada(true);
                this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ELIMINACION);
                notaRepository.save(nota);
                LOGGER.warn("La Nota " + nota + " se eliminó correctamente.");
            } else {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_eliminar_factura_aprobada"));
            }
        }
    }

    @Override
    public double calcularTotalNota(List<RenglonNotaCredito> renglonesNota) {
        double total = 0.0;
        total = renglonesNota.stream().map((r) -> r.getImporteNeto()).reduce(total, (accumulator, _item) -> accumulator + _item);
        return total;
    }

    @Override
    public double getIvaNetoNota(Long idNota) {
        Nota nota = this.getNotaPorId(idNota);
        double ivaNeto = 0.0;
        if (nota instanceof NotaCredito) {
            ivaNeto = this.getRenglonesDeNotaCredito(nota.getIdNota()).stream().map((renglonNota) -> (renglonNota.getIvaPorcentaje() / 100) * renglonNota.getImporte()).reduce(ivaNeto, (accumulator, _item) -> accumulator + _item);
        } else {
            ivaNeto = this.getRenglonesDeNotaDebito(idNota).stream().map((r) -> r.getIvaNeto()).reduce(ivaNeto, (accumulator, _item) -> accumulator + _item);
        }
        return ivaNeto;
    }

    @Override
    public List<RenglonNotaCredito> calcularRenglonCredito(TipoDeComprobante tipo, double[] cantidad, long[] idRenglonFactura) {
        List<RenglonNotaCredito> renglonesNota = new ArrayList<>();
        RenglonNotaCredito renglonNota;
        if (cantidad.length == idRenglonFactura.length) {
            for (int i = 0; i < idRenglonFactura.length; i++) {
                RenglonFactura renglonFactura = facturaService.getRenglonFactura(idRenglonFactura[i]);
                if (renglonFactura.getCantidad() < cantidad[i] || cantidad[i] < 0) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_de_credito_cantidad_no_valida") + " " + renglonFactura.getDescripcionItem());
                }
                renglonNota = new RenglonNotaCredito();
                renglonNota.setIdProductoItem(renglonFactura.getId_ProductoItem());
                renglonNota.setCodigoItem(renglonFactura.getCodigoItem());
                renglonNota.setDescripcionItem(renglonFactura.getDescripcionItem());
                renglonNota.setMedidaItem(renglonFactura.getMedidaItem());
                renglonNota.setCantidad(cantidad[i]);
                renglonNota.setPrecioUnitario(renglonFactura.getPrecioUnitario());
                renglonNota.setDescuentoPorcentaje(renglonFactura.getDescuento_porcentaje());
                renglonNota.setDescuentoNeto(renglonNota.getPrecioUnitario() * (renglonFactura.getDescuento_porcentaje() / 100));
                renglonNota.setGananciaPorcentaje(renglonFactura.getGanancia_porcentaje());
                renglonNota.setGananciaNeto(renglonNota.getPrecioUnitario() * (renglonNota.getGananciaPorcentaje() / 100));
                renglonNota.setIvaPorcentaje(renglonFactura.getIva_porcentaje());
                if (tipo.equals(TipoDeComprobante.FACTURA_Y)) {
                    renglonNota.setIvaPorcentaje(renglonFactura.getIva_porcentaje() / 2);
                }
                renglonNota.setIvaNeto((tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) ? renglonFactura.getIva_neto() : 0);
                renglonNota.setImporte(renglonNota.getPrecioUnitario() * cantidad[i]);
                renglonNota.setImporteBruto(renglonNota.getImporte() - (renglonNota.getDescuentoNeto() * cantidad[i]));
                if (tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
                    renglonNota.setImporteNeto(renglonNota.getImporteBruto());
                } else {
                    renglonNota.setImporteNeto(renglonNota.getImporteBruto() + (renglonNota.getIvaNeto() * cantidad[i]));
                }
                renglonesNota.add(renglonNota);
            }
        }
        return renglonesNota;
    }

    @Override
    public List<RenglonNotaDebito> calcularRenglonDebito(Long idPago, double monto, double ivaPorcentaje) {
        List<RenglonNotaDebito> renglonesNota = new ArrayList<>();
        RenglonNotaDebito renglonNota;
        if (idPago != null) {
            Pago pago = pagoService.getPagoPorId(idPago);
            renglonNota = new RenglonNotaDebito(); 
            String descripcion = "Pago Nº " + pago.getNroPago() + " " + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO)).format(pago.getFecha());
            if (pago.getNota() != null && pago.getNota().length() > 0) {
                descripcion += " " + pago.getNota();
            }
            renglonNota.setDescripcion(descripcion);
            renglonNota.setMonto(pago.getMonto());
            renglonNota.setImporteBruto(renglonNota.getMonto());
            renglonNota.setIvaPorcentaje(0);
            renglonNota.setIvaNeto(0);
            renglonNota.setImporteNeto(this.calcularImporteRenglon(0, renglonNota.getImporteBruto(), 1));
            renglonesNota.add(renglonNota);
            renglonNota = new RenglonNotaDebito();
            renglonNota.setDescripcion("Gasto Administrativo");
            renglonNota.setMonto(monto);
            renglonNota.setIvaPorcentaje(ivaPorcentaje);
            renglonNota.setIvaNeto(monto * (ivaPorcentaje / 100));
            renglonNota.setImporteBruto(monto);
            renglonNota.setImporteNeto(this.calcularImporteRenglon(renglonNota.getIvaNeto(), renglonNota.getImporteBruto(), 1));
            renglonesNota.add(renglonNota);
        }
        return renglonesNota;
    }

    private double calcularImporteRenglon(double ivaNeto, double subTotalBrutoRenglon, double cantidad) {
        return subTotalBrutoRenglon + (ivaNeto * cantidad);
    }
    
    @Override
    public double calcularSubTotalCredito(double[] importe) {
        return facturaService.calcularSubTotal(importe);
    }
    
    @Override
    public double calcularDecuentoNetoCredito(double subTotal, double descuentoPorcentaje) {
        return facturaService.calcularDescuentoNeto(subTotal, descuentoPorcentaje);
    }
    
    @Override
    public double calcularRecargoNetoCredito(double subTotal, double recargoPorcentaje) {
        return facturaService.calcularDescuentoNeto(subTotal, recargoPorcentaje);
    }
    
    @Override
    public double calcularIVANetoCredito(TipoDeComprobante tipoDeComprobante,
                                  double[] cantidades,
                                  double[] ivaPorcentajeRenglones,
                                  double[] ivaNetoRenglones,
                                  double ivaPorcentaje,
                                  double descuentoPorcentaje, 
                                  double recargoPorcentaje) {
        return facturaService.calcularIvaNetoFactura(tipoDeComprobante, cantidades, ivaPorcentajeRenglones, ivaNetoRenglones, ivaPorcentaje, descuentoPorcentaje, recargoPorcentaje);
    }
    
    @Override
    public double calcularSubTotalBrutoCredito(TipoDeComprobante tipoDeComprobante, double subTotal, double recargoNeto, double descuentoNeto, double iva105Neto, double iva21Neto) {         
        double resultado = subTotal + recargoNeto - descuentoNeto;
        if (tipoDeComprobante == TipoDeComprobante.FACTURA_B || tipoDeComprobante == TipoDeComprobante.PRESUPUESTO) {
            resultado = resultado - (iva105Neto + iva21Neto);
        }
        return resultado;
    }
    @Override
    public double calcularTotalCredito(double subTotal_bruto, double iva105_neto, double iva21_neto) {
        return facturaService.calcularTotal(subTotal_bruto, iva105_neto, iva21_neto);
    }
    
    @Override
    public double calcularTotalDebito(double subTotal_bruto, double iva21_neto, double montoNoGravado) {
        return subTotal_bruto + iva21_neto + montoNoGravado;
    }

}
