package sic.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
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

@Service
public class ReciboServiceImpl implements IReciboService {
    
    private final ReciboRepository reciboRepository;
    private final IFacturaService facturaService;
    private final IPagoService pagoService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IEmpresaService empresaService;
    private final IConfiguracionDelSistemaService cds;
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
        this.cds = cds;
        this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
        this.notaService = notaService;
        this.formaDePagoService = formaDePagoService;
    }

    @Override
    public Recibo getById(long idRecibo) {
        return reciboRepository.findById(idRecibo);
    }

    @Override
    public Double getMontoById(long idRecibo) {
        return reciboRepository.getMontoById(idRecibo);
    }
    
    @Override 
    @Transactional
    public Recibo guardar(Recibo recibo) {
        recibo.setSerie(cds.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip());
        recibo.setNroRecibo(this.getSiguienteNumeroRecibo(recibo.getEmpresa().getId_Empresa(), cds.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip()));
        recibo.setFecha(new Date());
        double monto = recibo.getMonto();
        int i = 0;
        this.validarRecibo(recibo);
        recibo = reciboRepository.save(recibo);
        Pageable pageable = new PageRequest(i, 10);
        Slice<RenglonCuentaCorriente> renglonesCC =
                this.renglonCuentaCorrienteService
                        .getRenglonesVentaYDebitoCuentaCorriente(
                                this.cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente()).getIdCuentaCorriente(), pageable);
        while (renglonesCC.hasContent()) {
            monto = this.pagarMultiplesComprobantes(renglonesCC.getContent(), recibo, monto, recibo.getFormaDePago(), recibo.getObservacion());
            if (renglonesCC.hasNext()) {
                i++;
                pageable = new PageRequest(i, 10);
                renglonesCC = this.renglonCuentaCorrienteService
                        .getRenglonesVentaYDebitoCuentaCorriente(
                                this.cuentaCorrienteService.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente()).getIdCuentaCorriente(), pageable);
            } else {
                break;
            }
        }
        recibo.setSaldoSobrante(monto);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
        LOGGER.warn("El Recibo " + recibo + " se guardó correctamente.");
        return recibo;
    }
    
    @Override
    @Transactional
    public Recibo actualizarMontoRecibo(long idRecibo, double monto) {
        Recibo r = reciboRepository.findById(idRecibo);
        r.setMonto(monto);
        return reciboRepository.save(r);
    }

    private void validarRecibo(Recibo recibo) {
        //Requeridos
        if (recibo.getMonto() <= 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_monto_igual_menor_cero"));
        }
        if (recibo.getSaldoSobrante() < 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_saldo_sobrante_menor_cero"));
        }
        if (recibo.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_empresa_vacia"));
        }
        if (recibo.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_recibo_cliente_vacio"));
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
        Recibo recibo = reciboRepository.findTopByEmpresaAndSerieOrderByNroReciboDesc(empresaService.getEmpresaPorId(idEmpresa), serie);
        if (recibo == null) {
            return 1; // No existe ningun Recibo anterior
        } else {
            return 1 + recibo.getNroRecibo();
        }
    }
    
    @Override 
    public List<Recibo> construirRecibos(String observacion, long[] idsFormaDePago, Empresa empresa, Cliente cliente, Usuario usuario, double[] monto, Date fecha) { 
        List<Recibo> recibos = new ArrayList<>();
        int i = 0;
        if (idsFormaDePago != null) {
            for (long idFormaDePago : idsFormaDePago) {
                Recibo recibo = new Recibo();
                recibo.setCliente(cliente);
                recibo.setUsuario(usuario);
                recibo.setEmpresa(empresa);
                recibo.setFecha(fecha);
                recibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(idFormaDePago));
                recibo.setMonto(monto[i]);
                recibo.setSerie(cds.getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa()).getNroPuntoDeVentaAfip());
                recibo.setNroRecibo(this.getSiguienteNumeroRecibo(empresa.getId_Empresa(), recibo.getSerie()));
                recibo.setObservacion(observacion);
                recibo.setSaldoSobrante(0);
                i++;
            }
        }
        return recibos;
    }
      
    @Override
    public double pagarMultiplesComprobantes(List<RenglonCuentaCorriente> renglonesCC, Recibo recibo, double monto, FormaDePago formaDePago, String nota) {
        for (RenglonCuentaCorriente rcc : renglonesCC) {
            if (monto > 0.0) {
                if (rcc.getTipoDeComprobante() == TipoDeComprobante.FACTURA_A || rcc.getTipoDeComprobante() == TipoDeComprobante.FACTURA_B
                        || rcc.getTipoDeComprobante() == TipoDeComprobante.FACTURA_C || rcc.getTipoDeComprobante() == TipoDeComprobante.FACTURA_X
                        || rcc.getTipoDeComprobante() == TipoDeComprobante.FACTURA_Y || rcc.getTipoDeComprobante() == TipoDeComprobante.PRESUPUESTO) {
                    FacturaVenta fv = (FacturaVenta) facturaService.getFacturaPorId(rcc.getIdMovimiento());
                    if (fv.isPagada() == false) {
                        fv.setPagos(this.pagoService.getPagosDeLaFactura(fv.getId_Factura()));
                        Pago nuevoPago = new Pago();
                        nuevoPago.setFormaDePago(formaDePago);
                        nuevoPago.setFactura(fv);
                        nuevoPago.setEmpresa(fv.getEmpresa());
                        nuevoPago.setNota(nota);
                        double saldoAPagar = this.pagoService.getSaldoAPagarFactura(fv.getId_Factura());
                        if (saldoAPagar <= monto) {
                            monto = monto - saldoAPagar;
                            // Se utiliza round por un problema de presicion de la maquina ej: 828.65 - 614.0 = 214.64999...
                            monto = Math.round(monto * 100.0) / 100.0;
                            nuevoPago.setMonto(saldoAPagar);
                        } else {
                            nuevoPago.setMonto(monto);
                            monto = 0.0;
                        }
                        nuevoPago.setFactura(fv);
                        nuevoPago.setRecibo(recibo);
                        this.pagoService.guardar(nuevoPago);
                    }
                } else if (rcc.getTipoDeComprobante() == TipoDeComprobante.NOTA_DEBITO_A
                        || rcc.getTipoDeComprobante() == TipoDeComprobante.NOTA_DEBITO_B || rcc.getTipoDeComprobante() == TipoDeComprobante.NOTA_DEBITO_X
                        || rcc.getTipoDeComprobante() == TipoDeComprobante.NOTA_DEBITO_Y || rcc.getTipoDeComprobante() == TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO) {
                    NotaDebito nd = (NotaDebito) notaService.getNotaPorId(rcc.getIdMovimiento());
                    nd.setPagos(this.notaService.getPagosNota(nd.getIdNota()));
                    if (nd.isPagada() == false) {
                        Pago nuevoPago = new Pago();
                        nuevoPago.setFormaDePago(formaDePago);
                        nuevoPago.setNotaDebito(nd);
                        nuevoPago.setEmpresa(nd.getEmpresa());
                        nuevoPago.setNota(nota);
                        double saldoAPagar = this.pagoService.getSaldoAPagarNotaDebito(nd.getIdNota());
                        if (saldoAPagar <= monto) {
                            monto = monto - saldoAPagar;
                            // Se utiliza round por un problema de presicion de la maquina ej: 828.65 - 614.0 = 214.64999...
                            monto = Math.round(monto * 100.0) / 100.0;
                            nuevoPago.setMonto(saldoAPagar);
                        } else {
                            nuevoPago.setMonto(monto);
                            monto = 0.0;
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
    public void eliminar(long idRecibo) {
        this.pagoService.getPagosRelacionadosAlRecibo(idRecibo).forEach((p) -> {
            pagoService.eliminar(p.getId_Pago());
        });
        Recibo r = reciboRepository.findById(idRecibo);
        r.setEliminado(true);
        reciboRepository.save(r);
        LOGGER.warn("El Recibo " + r + " se eliminó correctamente.");
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
    public List<Recibo> getRecibosConSaldoSobrante(long idEmpresa, long idCliente) {
        return reciboRepository.getRecibosConMontoSobrante(idEmpresa, idCliente);
    }

}
