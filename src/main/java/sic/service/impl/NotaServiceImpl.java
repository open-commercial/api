package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.ComprobanteAFIP;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.Movimiento;
import sic.modelo.Nota;
import sic.modelo.NotaCredito;
import sic.modelo.NotaCreditoCliente;
import sic.modelo.NotaCreditoProveedor;
import sic.modelo.NotaDebito;
import sic.modelo.NotaDebitoCliente;
import sic.modelo.NotaDebitoProveedor;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.repository.NotaCreditoClienteRepository;
import sic.repository.NotaCreditoProveedorRepository;
import sic.service.BusinessServiceException;
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.IFacturaService;
import sic.service.INotaService;
import sic.repository.NotaCreditoRepository;
import sic.repository.NotaDebitoClienteRepository;
import sic.repository.NotaDebitoProveedorRepository;
import sic.repository.NotaDebitoRepository;
import sic.repository.NotaRepository;
import sic.service.IAfipService;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICuentaCorrienteService;
import sic.service.IProductoService;
import sic.service.IProveedorService;
import sic.service.IReciboService;
import sic.service.IRenglonCuentaCorrienteService;
import sic.service.IUsuarioService;
import sic.service.ServiceException;
import sic.util.FormatterFechaHora;

@Service
public class NotaServiceImpl implements INotaService {

    private final NotaRepository notaRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaCreditoClienteRepository notaCreditoClienteRepository;
    private final NotaCreditoProveedorRepository notaCreditoProveedorRepository;
    private final NotaDebitoRepository notaDebitoRepository;
    private final NotaDebitoClienteRepository notaDebitoClienteRepository;
    private final NotaDebitoProveedorRepository notaDebitoProveedorRepository;
    private final IFacturaService facturaService;
    private final IClienteService clienteService;
    private final IProveedorService proveedorService;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IProductoService productoService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IReciboService reciboService;
    private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IAfipService afipService;
    private final static BigDecimal IVA_21 = new BigDecimal("21");
    private final static BigDecimal IVA_105 = new BigDecimal("10.5");
    private final static BigDecimal CIEN = new BigDecimal("100");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Lazy
    public NotaServiceImpl(NotaRepository notaRepository, NotaCreditoRepository notaDeCreditoRepository,
            NotaCreditoClienteRepository notaCreditoClienteRepository, NotaCreditoProveedorRepository notaCreditoProveedorRepository,
            NotaDebitoRepository notaDeDebitoRespository, NotaDebitoClienteRepository notaDebitoClienteRepository, 
            NotaDebitoProveedorRepository notaDebitoProveedorRepository, IFacturaService facturaService,
            IClienteService clienteService, IProveedorService proveedorService, 
            IUsuarioService usuarioService, IProductoService productoService,
            IEmpresaService empresaService, ICuentaCorrienteService cuentaCorrienteService,
            IReciboService reciboService, IConfiguracionDelSistemaService cds, IAfipService afipService,
            IRenglonCuentaCorrienteService renglonCuentaCorrienteService) {

        this.notaRepository = notaRepository;
        this.notaCreditoRepository = notaDeCreditoRepository;
        this.notaCreditoClienteRepository = notaCreditoClienteRepository;
        this.notaCreditoProveedorRepository = notaCreditoProveedorRepository;
        this.notaDebitoRepository = notaDeDebitoRespository;
        this.notaDebitoClienteRepository = notaDebitoClienteRepository;
        this.notaDebitoProveedorRepository = notaDebitoProveedorRepository;
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.proveedorService = proveedorService;
        this.usuarioService = usuarioService;
        this.empresaService = empresaService;
        this.productoService = productoService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.reciboService = reciboService;
        this.configuracionDelSistemaService = cds;
        this.afipService = afipService;
        this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
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
    public BigDecimal getTotalById(Long idNota) { 
        BigDecimal total = notaRepository.getTotalById(idNota);
        return (total != null) ? total : BigDecimal.ZERO;
    }
    
    @Override
    public Factura getFacturaNotaCredito(Long idNota) {
        NotaCredito nota = this.notaCreditoClienteRepository.getById(idNota);
        if (nota instanceof NotaCreditoCliente) {
            return ((NotaCreditoCliente) nota).getFacturaVenta();
        } else {
            return ((NotaCreditoProveedor) nota).getFacturaCompra();
        }
    }

    @Override
    public FacturaVenta getFacturaNotaCreditoCliente(Long idNota) {
        return this.notaCreditoClienteRepository.getById(idNota).getFacturaVenta();
    }
    
    @Override
    public FacturaCompra getFacturaNotaCreditoProveedor(Long idNota) {
        return this.notaCreditoProveedorRepository.getById(idNota).getFacturaCompra();
    }
    
    @Override
    public boolean existeNotaDebitoPorRecibo(Recibo recibo) {
        boolean existeNotaDebito = false;
        if (recibo.getCliente() != null) {
            existeNotaDebito = notaDebitoClienteRepository.existsByReciboAndEliminada(recibo, false);
        } else if (recibo.getProveedor() != null) {
            existeNotaDebito = notaDebitoProveedorRepository.existsByReciboAndEliminada(recibo, false);
        }
        return existeNotaDebito;
    }

    @Override
    public boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta) {
        return notaCreditoClienteRepository.existsByFacturaVentaAndEliminada(facturaVenta, false);
    }
    
    @Override
    public List<NotaCredito> getNotasCreditoPorFactura(Long idFactura) {
        List<NotaCredito> notasCredito = new ArrayList<>();
        Factura factura = facturaService.getFacturaPorId(idFactura);
        if (factura instanceof FacturaVenta) {
            notasCredito = notaCreditoClienteRepository.findAllByFacturaVentaAndEliminada((FacturaVenta) factura, false);
        } else if (factura instanceof FacturaCompra) {
            notasCredito = notaCreditoProveedorRepository.findAllByFacturaCompraAndEliminada((FacturaCompra) factura, false);
        }
        return notasCredito;
    }

    @Override
    public List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura) {
        HashMap<Long, BigDecimal> listaCantidadesProductosUnificados = new HashMap<>();
        this.getNotasCreditoPorFactura(idFactura).forEach(n -> {
            ((NotaCredito) n).getRenglonesNotaCredito().forEach(rnc -> {
                if (listaCantidadesProductosUnificados.containsKey(rnc.getIdProductoItem())) {
                    listaCantidadesProductosUnificados.put(rnc.getIdProductoItem(),
                            listaCantidadesProductosUnificados.get(rnc.getIdProductoItem()).add(rnc.getCantidad()));
                } else {
                    listaCantidadesProductosUnificados.put(rnc.getIdProductoItem(), rnc.getCantidad());
                }
            });
        });
        List<RenglonFactura> renglonesFactura = facturaService.getRenglonesDeLaFactura(idFactura);
        if (!listaCantidadesProductosUnificados.isEmpty()) {
            renglonesFactura.forEach(rf -> {
                if (listaCantidadesProductosUnificados.containsKey(rf.getId_ProductoItem())) {
                    rf.setCantidad(rf.getCantidad().subtract(listaCantidadesProductosUnificados.get(rf.getId_ProductoItem())));
                }
            });
        }
        return renglonesFactura;
    }

    @Override
    public long getSiguienteNumeroNotaDebitoCliente(Long idEmpresa, TipoDeComprobante tipoDeComprobante) {
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        Long numeroNota = notaDebitoClienteRepository.buscarMayorNumNotaDebitoClienteSegunTipo(tipoDeComprobante, configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa).getNroPuntoDeVentaAfip(), idEmpresa);
        return (numeroNota == null) ? 1 : numeroNota + 1;
    }
    
    @Override
    public long getSiguienteNumeroNotaCreditoCliente(Long idEmpresa, TipoDeComprobante tipoDeComprobante) {
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        Long numeroNota = notaCreditoClienteRepository.buscarMayorNumNotaCreditoClienteSegunTipo(tipoDeComprobante, configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa).getNroPuntoDeVentaAfip(), idEmpresa);
        return (numeroNota == null) ? 1 : numeroNota + 1;
    }

    @Override
    public TipoDeComprobante[] getTipoNotaCliente(Long idCliente, Long idEmpresa) {
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
    public List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(Long idNota) {
        return this.notaCreditoClienteRepository.getById(idNota).getRenglonesNotaCredito();
    }

    @Override
    public List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(Long idNota) {
        return this.notaDebitoClienteRepository.getById(idNota).getRenglonesNotaDebito();
    }
    
    @Override
    public List<RenglonNotaCredito> getRenglonesDeNotaCreditoProveedor(Long idNota) {
        return this.notaCreditoProveedorRepository.getById(idNota).getRenglonesNotaCredito();
    }

    @Override
    public List<RenglonNotaDebito> getRenglonesDeNotaDebitoProveedor(Long idNota) {
        return this.notaDebitoProveedorRepository.getById(idNota).getRenglonesNotaDebito();
    }

    private void validarNota(Nota nota, long idEmpresa, long idUsuario) {
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        if (empresa == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        nota.setEmpresa(empresa);
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
        if (nota.getFecha() != null) {
            if (nota instanceof NotaCreditoCliente) {
                if (nota.getFecha().compareTo(((NotaCreditoCliente) nota).getFacturaVenta().getFecha()) <= 0) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_fecha_incorrecta"));
                }
                if (nota.getCAE() != 0l) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_cliente_CAE"));
                }
            } else if (nota instanceof NotaDebitoCliente) {
                if (nota.getCAE() != 0l) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_cliente_CAE"));
                }
            }
        } else if (nota.getFecha() == null) {
            nota.setFecha(new Date());
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
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal[] importes = new BigDecimal[renglonesNotaCredito.size()];
        int i = 0;
        int sizeRenglonesCredito = renglonesNotaCredito.size();
        //IVA - subTotal
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal iva105 = BigDecimal.ZERO;
        if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_Y
                || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
            BigDecimal[] ivaPorcentajes = new BigDecimal[sizeRenglonesCredito];
            BigDecimal[] ivaNetos = new BigDecimal[sizeRenglonesCredito];
            BigDecimal[] cantidades = new BigDecimal[sizeRenglonesCredito];
            for (RenglonNotaCredito r : renglonesNotaCredito) {
                ivaPorcentajes[i] = r.getIvaPorcentaje();
                ivaNetos[i] = r.getIvaNeto();
                cantidades[i] = r.getCantidad();
                importes[i] = r.getImporteBruto();
                i++;
            }
            subTotal = this.calcularSubTotalCredito(importes);
            if (notaCredito.getSubTotal().compareTo(subTotal) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_sub_total_no_valido"));
            }
            iva21 = this.calcularIVANetoCredito(tipoDeComprobanteDeFacturaRelacionada, cantidades, ivaPorcentajes, ivaNetos,
                    IVA_21, notaCredito.getDescuentoPorcentaje(), notaCredito.getRecargoPorcentaje());
            if (notaCredito.getIva21Neto().compareTo(iva21) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva21_no_valido"));
            }
            iva105 = this.calcularIVANetoCredito(tipoDeComprobanteDeFacturaRelacionada, cantidades, ivaPorcentajes, ivaNetos,
                    IVA_105, notaCredito.getDescuentoPorcentaje(), notaCredito.getRecargoPorcentaje());
            if (notaCredito.getIva105Neto().compareTo(iva105) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva105_no_valido"));
            }
        } else if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_X) {
            for (RenglonNotaCredito r : renglonesNotaCredito) {
                importes[i] = r.getImporteNeto();
                i++;
            }
            subTotal = this.calcularSubTotalCredito(importes);
            if (notaCredito.getSubTotal().compareTo(subTotal) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_sub_total_no_valido"));
            }
            if (notaCredito.getIva21Neto().compareTo(BigDecimal.ZERO) != 0.0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva21_no_valido"));
            }
            if (notaCredito.getIva105Neto().compareTo(BigDecimal.ZERO) != 0.0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_iva105_no_valido"));
            }
        }
        //DescuentoNeto
        BigDecimal descuentoNeto = this.calcularDecuentoNetoCredito(subTotal, notaCredito.getDescuentoPorcentaje());
        if (notaCredito.getDescuentoNeto().compareTo(descuentoNeto) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_descuento_neto_no_valido"));
        }
        //RecargoNeto
        BigDecimal recargoNeto = this.calcularRecargoNetoCredito(subTotal, notaCredito.getRecargoPorcentaje());
        if (notaCredito.getRecargoNeto().compareTo(recargoNeto) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_recargo_neto_no_valido"));
        }
        //subTotalBruto
        BigDecimal subTotalBruto = this.calcularSubTotalBrutoCredito(tipoDeComprobanteDeFacturaRelacionada, subTotal, recargoNeto, descuentoNeto, iva105, iva21);
        if (notaCredito.getSubTotalBruto().compareTo(subTotalBruto) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_sub_total_bruto_no_valido"));
        }
        //Total
        if (notaCredito.getTotal().compareTo(this.calcularTotalCredito(subTotalBruto, iva105, iva21)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_total_no_valido"));
        }
    }
    
    private void validarCalculosDebito(NotaDebito notaDebito) {
        // monto no gravado
        BigDecimal montoComprobante = BigDecimal.ZERO;
        if (notaDebito.getRecibo() != null) {
            montoComprobante = notaDebito.getRecibo().getMonto();
        }
        if (notaDebito.getMontoNoGravado().compareTo(montoComprobante) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_monto_no_gravado_no_valido"));
        }
        // iva 21
        BigDecimal iva21 = BigDecimal.ZERO;
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
                iva21 = notaDebito.getSubTotalBruto().multiply(new BigDecimal("0.21"));
                if (notaDebito.getIva21Neto().compareTo(iva21) != 0) {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva21_no_valido"));
                }
                break;
        }
        // total
        if (notaDebito.getTotal().compareTo(this.calcularTotalDebito(notaDebito.getSubTotalBruto(), iva21, montoComprobante)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_nota_total_no_valido"));
        }
    }

    @Override
    @Transactional
    public Nota guardarNotaCliente(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idRecibo, Long idFactura, boolean modificarStock) {
        this.validarNota(nota, idEmpresa, idUsuario);
        nota.setSerie(configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(nota.getEmpresa()).getNroPuntoDeVentaAfip());
        Cliente cliente = clienteService.getClientePorId(idCliente);
        if (cliente == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_no_existente"));
        }
        if (nota instanceof NotaCredito) {
            if (nota instanceof NotaCreditoCliente) {
                Factura factura = facturaService.getFacturaPorId(idFactura);
                NotaCreditoCliente notaCredito = (NotaCreditoCliente) nota;
                if (factura instanceof FacturaVenta) {
                    notaCredito.setFacturaVenta((FacturaVenta) factura);
                } else {
                    throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_nota_de_credito_cliente_error_factura"));
                }
                notaCredito.setTipoComprobante(this.getTipoDeNotaCreditoSegunFactura(notaCredito.getFacturaVenta()));
                notaCredito.setNroNota(this.getSiguienteNumeroNotaCreditoCliente(idEmpresa, nota.getTipoComprobante()));
                notaCredito.setModificaStock(modificarStock);
                notaCredito.setCliente(cliente);
                if (modificarStock) {
                    this.actualizarStock(notaCredito.getRenglonesNotaCredito(), TipoDeOperacion.ACTUALIZACION);
                }
                this.validarCalculosCredito(notaCredito);
                nota = notaCreditoClienteRepository.save(notaCredito);
                this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ALTA);
                LOGGER.warn("La Nota " + notaCredito + " se guardó correctamente.");
                return notaCredito;

            }
        } else if (nota instanceof NotaDebitoCliente) {
            nota = this.guardarNotaDebitoCliente((NotaDebitoCliente) nota, idRecibo, idEmpresa, cliente);
        }
        return nota;
    }

    private NotaDebito guardarNotaDebitoCliente(NotaDebitoCliente notaDebitoCliente, long idRecibo, long idEmpresa, Cliente cliente) {
        notaDebitoCliente.setCliente(cliente);
        notaDebitoCliente.setRecibo(reciboService.getById(idRecibo));
        notaDebitoCliente.setTipoComprobante(
        this.getTipoDeNotaDebito(this.facturaService.getTipoFacturaVenta(notaDebitoCliente.getEmpresa(), notaDebitoCliente.getCliente())[0]));
        notaDebitoCliente.setNroNota(this.getSiguienteNumeroNotaDebitoCliente(idEmpresa, notaDebitoCliente.getTipoComprobante()));
        this.validarCalculosDebito(notaDebitoCliente);
        notaDebitoCliente = notaDebitoClienteRepository.save(notaDebitoCliente);
        cuentaCorrienteService.asentarEnCuentaCorriente(notaDebitoCliente, TipoDeOperacion.ALTA);
        LOGGER.warn("La Nota " + notaDebitoCliente + " se guardó correctamente.");
        return notaDebitoCliente;
    }
    
    @Override
    @Transactional
    public Nota guardarNotaProveedor(Nota nota, long idEmpresa, long idProveedor, long idUsuario, Long idRecibo, Long idFactura, boolean modificarStock) {
        this.validarNota(nota, idEmpresa, idUsuario);
        Proveedor proveedor = proveedorService.getProveedorPorId(idProveedor);
        if (proveedor == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_no_existente"));
        }
        if (nota instanceof NotaCreditoProveedor) {
            Factura factura = facturaService.getFacturaPorId(idFactura);
            NotaCreditoProveedor notaCreditoProveedor = (NotaCreditoProveedor) nota;
            if (factura instanceof FacturaCompra) {
                notaCreditoProveedor.setFacturaCompra((FacturaCompra) factura);
            } else {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_de_credito_cliente_error_factura"));
            }
            notaCreditoProveedor.setTipoComprobante(this.getTipoDeNotaCreditoSegunFactura(notaCreditoProveedor.getFacturaCompra()));
            notaCreditoProveedor.setModificaStock(modificarStock);
            notaCreditoProveedor.setProveedor(proveedor);
            if (modificarStock) { 
                this.actualizarStock(notaCreditoProveedor.getRenglonesNotaCredito(), TipoDeOperacion.ELIMINACION);
            }
            this.validarCalculosCredito(notaCreditoProveedor);
            nota = notaCreditoProveedorRepository.save(notaCreditoProveedor);
            this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ALTA);
            LOGGER.warn("La Nota " + notaCreditoProveedor + " se guardó correctamente.");
            nota = notaCreditoProveedor;
        } else if (nota instanceof NotaDebitoProveedor) {
            this.guardarNotaDebitoProveedor((NotaDebitoProveedor) nota, idRecibo, proveedor);
        }
        return nota;
    }
       
    private NotaDebitoProveedor guardarNotaDebitoProveedor(NotaDebitoProveedor notaDebitoProveedor, Long idRecibo, Proveedor proveedor) {
        notaDebitoProveedor.setProveedor(proveedor);
        notaDebitoProveedor.setRecibo(reciboService.getById(idRecibo));
        notaDebitoProveedor.setTipoComprobante(this.getTipoDeNotaDebito(this.facturaService.getTipoFacturaCompra(notaDebitoProveedor.getEmpresa(), notaDebitoProveedor.getProveedor())[0]));
        this.validarCalculosDebito(notaDebitoProveedor);
        notaDebitoProveedor = notaDebitoProveedorRepository.save(notaDebitoProveedor);
        cuentaCorrienteService.asentarEnCuentaCorriente(notaDebitoProveedor, TipoDeOperacion.ALTA);
        LOGGER.warn("La Nota " + notaDebitoProveedor + " se guardó correctamente.");
        return notaDebitoProveedor;
    } 

    @Override
    @Transactional
    public Nota autorizarNota(Nota nota) {
        BigDecimal montoNoGravado = (nota instanceof NotaDebitoCliente) ? ((NotaDebito) nota).getMontoNoGravado() : BigDecimal.ZERO;
        if (nota instanceof NotaCreditoCliente || nota instanceof NotaDebitoCliente) {
            Cliente cliente = new Cliente();
            if (nota instanceof NotaCreditoCliente) {
                cliente = ((NotaCreditoCliente) nota).getCliente();
            } else if (nota instanceof NotaDebitoCliente) {
                cliente = ((NotaDebitoCliente) nota).getCliente();
            }
            ComprobanteAFIP comprobante = ComprobanteAFIP.builder()
                    .fecha(nota.getFecha())
                    .tipoComprobante(nota.getTipoComprobante())
                    .CAE(nota.getCAE())
                    .vencimientoCAE(nota.getVencimientoCAE())
                    .numSerieAfip(nota.getNumSerieAfip())
                    .numFacturaAfip(nota.getNumNotaAfip())
                    .empresa(nota.getEmpresa())
                    .cliente(cliente)
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
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_comprobanteAFIP_invalido"));
        }
        return nota;
    }

    private TipoDeComprobante getTipoDeNotaDebito(TipoDeComprobante tipo) {
        switch (tipo) {
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
        HashMap<Long, BigDecimal> idsYCantidades = new HashMap<>();
        renglonesNotaCredito.forEach(r -> idsYCantidades.put(r.getIdProductoItem(), r.getCantidad()));
        if (tipoOperacion == TipoDeOperacion.ELIMINACION) {
            tipoOperacion = TipoDeOperacion.ALTA;
        }
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
            List<RenglonNotaCredito> renglones = this.getRenglonesDeNotaCreditoCliente(nota.getIdNota());
            ds = new JRBeanCollectionDataSource(renglones);
            params.put("notaCredito", (NotaCredito) nota);
        } else {
            isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/NotaDebito.jasper");
            List<RenglonNotaDebito> renglones = this.getRenglonesDeNotaDebitoCliente(nota.getIdNota());
            ds = new JRBeanCollectionDataSource(renglones);
            params.put("notaDebito", (NotaDebito) nota);
        }
        ConfiguracionDelSistema cds = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(nota.getEmpresa());
        params.put("preImpresa", cds.isUsarFacturaVentaPreImpresa());
        if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X) 
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_X)
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO)) {
            nota.setSubTotalBruto(nota.getTotal());
            nota.setIva105Neto(BigDecimal.ZERO);
            nota.setIva21Neto(BigDecimal.ZERO);
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
            if (nota != null) {
                if (nota instanceof NotaCreditoCliente || nota instanceof NotaDebitoCliente) {
                    if (nota.getCAE() != 0l) {
                        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                                .getString("mensaje_eliminar_nota_aprobada"));
                    }
                    if (nota instanceof NotaCreditoCliente) {
                        NotaCredito nc = (NotaCredito) nota;
                        if (nc.isModificaStock()) {
                            this.actualizarStock(nc.getRenglonesNotaCredito(), TipoDeOperacion.ALTA);
                        }
                    }
                } else if (nota instanceof NotaCreditoProveedor) {
                    NotaCredito nc = (NotaCredito) nota;
                    if (nc.isModificaStock()) {
                        this.actualizarStock(nc.getRenglonesNotaCredito(), TipoDeOperacion.ACTUALIZACION);
                    }
                }
                nota.setEliminada(true);
                this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ELIMINACION);
                notaRepository.save(nota);
                LOGGER.warn("La Nota " + nota + " se eliminó correctamente.");
            }
        }
    }

    @Override
    public BigDecimal calcularTotalNota(List<RenglonNotaCredito> renglonesNota) {
        BigDecimal total = BigDecimal.ZERO;
        for (RenglonNotaCredito renglon : renglonesNota) {
            total = total.add(renglon.getImporteNeto());
        }
        return total;
    }

    @Override
    public BigDecimal getIvaNetoNota(Long idNota) {
        Nota nota = this.getNotaPorId(idNota);
        BigDecimal ivaNeto = BigDecimal.ZERO;
        if (nota instanceof NotaCredito) {
            for (RenglonNotaCredito r : this.getRenglonesDeNotaCreditoCliente(nota.getIdNota())) {
                ivaNeto =  ivaNeto.add(r.getIvaPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP).multiply(r.getImporte()));
            }
        } else {
            for (RenglonNotaCredito r : this.getRenglonesDeNotaCreditoCliente(nota.getIdNota())) {
                ivaNeto = ivaNeto.add(r.getIvaNeto());
            }
        }
        return ivaNeto;
    }

    @Override
    public List<RenglonNotaCredito> calcularRenglonCredito(TipoDeComprobante tipo, BigDecimal[] cantidad, long[] idRenglonFactura) {
        List<RenglonNotaCredito> renglonesNota = new ArrayList<>();
        RenglonNotaCredito renglonNota;
        if (cantidad.length == idRenglonFactura.length) {
            for (int i = 0; i < idRenglonFactura.length; i++) {
                RenglonFactura renglonFactura = facturaService.getRenglonFactura(idRenglonFactura[i]);
                if (renglonFactura.getCantidad().compareTo(cantidad[i]) < 0 || cantidad[i].compareTo(BigDecimal.ZERO) < 0) {
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
                renglonNota.setDescuentoNeto(renglonFactura.getDescuento_porcentaje().divide(CIEN, 15, RoundingMode.HALF_UP).multiply(renglonNota.getPrecioUnitario()));
                renglonNota.setGananciaPorcentaje(renglonFactura.getGanancia_porcentaje());
                renglonNota.setGananciaNeto(renglonNota.getGananciaPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP).multiply(renglonNota.getPrecioUnitario()));
                renglonNota.setIvaPorcentaje(renglonFactura.getIva_porcentaje());
                if (tipo.equals(TipoDeComprobante.FACTURA_Y)) {
                    renglonNota.setIvaPorcentaje(renglonFactura.getIva_porcentaje().divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP));
                }
                renglonNota.setIvaNeto((tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) ? renglonFactura.getIva_neto() : BigDecimal.ZERO);
                renglonNota.setImporte(renglonNota.getPrecioUnitario().multiply(cantidad[i]));
                renglonNota.setImporteBruto(renglonNota.getImporte().subtract(renglonNota.getDescuentoNeto().multiply(cantidad[i])));
                if (tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
                    renglonNota.setImporteNeto(renglonNota.getImporteBruto());
                } else {
                    renglonNota.setImporteNeto(renglonNota.getImporteBruto().add(renglonNota.getIvaNeto().multiply(cantidad[i])));
                }
                renglonesNota.add(renglonNota);
            }
        }
        return renglonesNota;
    }

    @Override
    public List<RenglonNotaDebito> calcularRenglonDebito(long idRecibo, BigDecimal monto, BigDecimal ivaPorcentaje) {
        List<RenglonNotaDebito> renglonesNota = new ArrayList<>();
        RenglonNotaDebito renglonNota;
        Recibo r = reciboService.getById(idRecibo);
        renglonNota = new RenglonNotaDebito();
        String descripcion = "Recibo Nº " + r.getNumRecibo() + " " + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO)).format(r.getFecha());
        renglonNota.setDescripcion(descripcion);
        renglonNota.setMonto(r.getMonto());
        renglonNota.setImporteBruto(renglonNota.getMonto());
        renglonNota.setIvaPorcentaje(BigDecimal.ZERO);
        renglonNota.setIvaNeto(BigDecimal.ZERO);
        renglonNota.setImporteNeto(this.calcularImporteRenglon(BigDecimal.ZERO, renglonNota.getImporteBruto(), BigDecimal.ONE));
        renglonesNota.add(renglonNota);
        renglonNota = new RenglonNotaDebito();
        renglonNota.setDescripcion("Gasto Administrativo");
        renglonNota.setMonto(monto);
        renglonNota.setIvaPorcentaje(ivaPorcentaje);
        renglonNota.setIvaNeto(monto.multiply(ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)));
        renglonNota.setImporteBruto(monto);
        renglonNota.setImporteNeto(this.calcularImporteRenglon(renglonNota.getIvaNeto(), renglonNota.getImporteBruto(), BigDecimal.ONE));
        renglonesNota.add(renglonNota);
        return renglonesNota;
    }

    private BigDecimal calcularImporteRenglon(BigDecimal ivaNeto, BigDecimal subTotalBrutoRenglon, BigDecimal cantidad) {
        return ivaNeto.multiply(cantidad).add(subTotalBrutoRenglon);
    }
    
    @Override
    public BigDecimal calcularSubTotalCredito(BigDecimal[] importe) {
        return facturaService.calcularSubTotal(importe);
    }
    
    @Override
    public BigDecimal calcularDecuentoNetoCredito(BigDecimal subTotal, BigDecimal descuentoPorcentaje) {
        return facturaService.calcularDescuentoNeto(subTotal, descuentoPorcentaje);
    }
    
    @Override
    public BigDecimal calcularRecargoNetoCredito(BigDecimal subTotal, BigDecimal recargoPorcentaje) {
        return facturaService.calcularDescuentoNeto(subTotal, recargoPorcentaje);
    }
    
    @Override
    public BigDecimal calcularIVANetoCredito(TipoDeComprobante tipoDeComprobante, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones,
            BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal descuentoPorcentaje, BigDecimal recargoPorcentaje) {
        return facturaService.calcularIvaNetoFactura(tipoDeComprobante, cantidades, ivaPorcentajeRenglones, ivaNetoRenglones, ivaPorcentaje, descuentoPorcentaje, recargoPorcentaje);
    }
    
    @Override
    public BigDecimal calcularSubTotalBrutoCredito(TipoDeComprobante tipoDeComprobante, BigDecimal subTotal, BigDecimal recargoNeto,
            BigDecimal descuentoNeto, BigDecimal iva105Neto, BigDecimal iva21Neto) {         
        BigDecimal resultado = subTotal.add(recargoNeto).subtract(descuentoNeto);
        if (tipoDeComprobante == TipoDeComprobante.FACTURA_B || tipoDeComprobante == TipoDeComprobante.PRESUPUESTO) {
            resultado = resultado.subtract(iva105Neto.add(iva21Neto));
        }
        return resultado;
    }
    
    @Override
    public BigDecimal calcularTotalCredito(BigDecimal subTotal_bruto, BigDecimal iva105_neto, BigDecimal iva21_neto) {
        return facturaService.calcularTotal(subTotal_bruto, iva105_neto, iva21_neto);
    }
    
    @Override
    public BigDecimal calcularTotalDebito(BigDecimal subTotal_bruto, BigDecimal iva21_neto, BigDecimal montoNoGravado) {
        return subTotal_bruto.add(iva21_neto).add(montoNoGravado);
    }

    @Override
    public BigDecimal calcularTotalCreditoClientePorFacturaVenta(FacturaVenta facturaVenta) {
        BigDecimal credito = notaCreditoClienteRepository.getTotalNotasCreditoPorFacturaVenta(facturaVenta);
        return (credito == null) ? BigDecimal.ZERO : credito;
    }
   
}
