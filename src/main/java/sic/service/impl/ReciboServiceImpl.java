package sic.service.impl;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.FormaDePago;
import sic.modelo.Pago;
import sic.modelo.Recibo;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.repository.ReciboRepository;
import sic.service.BusinessServiceException;
import sic.service.ICuentaCorrienteService;
import sic.service.IFacturaService;
import sic.service.IPagoService;
import sic.service.IReciboService;

@Service
public class ReciboServiceImpl implements IReciboService {
    
    private final ReciboRepository reciboRepository;
    private final IFacturaService facturaService;
    private final IPagoService pagoService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    
    @Autowired
    public ReciboServiceImpl(ReciboRepository reciboRepository, IFacturaService facturaService, IPagoService pagoService,
                             ICuentaCorrienteService cuentaCorrienteService) {
        this.reciboRepository = reciboRepository;
        this.facturaService = facturaService;
        this.pagoService = pagoService;
        this.cuentaCorrienteService = cuentaCorrienteService;
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
        double monto = recibo.getMonto();
        int i = 0;
        recibo = reciboRepository.save(recibo);
        Pageable pageable = new PageRequest(i, 10, new Sort(Sort.Direction.ASC, "fecha").and(new Sort(Sort.Direction.DESC, "tipoComprobante")));
        Slice<FacturaVenta> facturasVenta = this.facturaService.getFacturasImpagas(recibo.getCliente(), recibo.getEmpresa(), pageable);
        while (facturasVenta.hasContent()) {
            monto -= this.pagarMultiplesFacturasConMontoRecibo(facturasVenta.getContent(), recibo, monto, recibo.getFormaDePago(), recibo.getObservacion());
            if (facturasVenta.hasNext()) {
                i++;
                pageable = new PageRequest(i, 10, new Sort(Sort.Direction.ASC, "fecha").and(new Sort(Sort.Direction.DESC, "tipoComprobante")));
                facturasVenta = this.facturaService.getFacturasImpagas(recibo.getCliente(), recibo.getEmpresa(), pageable);
            } else {
                break;
            }
        }
        recibo.setSaldoSobrante(monto);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
        return recibo;
    }
    
    @Override
    public double pagarMultiplesFacturasConMontoRecibo(List<FacturaVenta> facturasVenta, Recibo recibo, double monto, FormaDePago formaDePago, String nota) {
        for (FacturaVenta fv : facturasVenta) {
            if (monto > 0.0) {
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
                nuevoPago.setRecibo(recibo);
                this.pagoService.guardar(nuevoPago);
            }
        }
        return monto;
    }
   
    @Override
    public void eliminar(long idRecibo) {
        Recibo r = this.getById(idRecibo);
        if (!this.pagoService.getPagosRelacionadosAlRecibo(r).isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_no_se_puede_eliminar"));
        }
        r.setEliminado(true);
        reciboRepository.save(r);
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
    
}
