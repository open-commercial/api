package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProveedorCriteria;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.CuentaCorriente;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.Empresa;
import sic.modelo.Proveedor;
import sic.modelo.QProveedor;
import sic.service.IProveedorService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.ProveedorRepository;
import sic.service.ICuentaCorrienteService;

@Service
public class ProveedorServiceImpl implements IProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ProveedorServiceImpl(ProveedorRepository proveedorRepository, ICuentaCorrienteService cuentaCorrienteService) {
        this.proveedorRepository = proveedorRepository;
        this.cuentaCorrienteService = cuentaCorrienteService;
    }

    @Override
    public Proveedor getProveedorPorId(Long idProvedor){
        Proveedor proveedor = proveedorRepository.findById(idProvedor);
        if (proveedor == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_no_existente"));
        }
        return proveedor;
    }
    
    @Override
    public List<Proveedor> getProveedores(Empresa empresa) {
        return proveedorRepository.findAllByAndEmpresaAndEliminadoOrderByRazonSocialAsc(empresa, false);
    }

  @Override
  public Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria) {
    QProveedor qProveedor = QProveedor.proveedor;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorRazonSocial()) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qProveedor.razonSocial.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorId_Fiscal())
      builder.or(qProveedor.idFiscal.containsIgnoreCase(criteria.getIdFiscal()));
    if (criteria.isBuscaPorCodigo())
      builder.or(qProveedor.codigo.containsIgnoreCase(criteria.getCodigo()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(qProveedor.localidad.id_Localidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(qProveedor.localidad.provincia.id_Provincia.eq(criteria.getIdProvincia()));
    if (criteria.isBuscaPorPais())
      builder.and(qProveedor.localidad.provincia.pais.id_Pais.eq(criteria.getIdPais()));
    builder.and(
        qProveedor
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qProveedor.eliminado.eq(false)));
    Page<Proveedor> proveedores = proveedorRepository.findAll(builder, criteria.getPageable());
    if (criteria.isConSaldo()) {
      proveedores
          .getContent()
          .forEach(
              p -> {
                CuentaCorriente cc = cuentaCorrienteService.getCuentaCorrientePorProveedor(p);
                p.setSaldoCuentaCorriente(cc.getSaldo());
                p.setFechaUltimoMovimiento(cc.getFechaUltimoMovimiento());
              });
    }
    return proveedores;
  }

    @Override
    public Proveedor getProveedorPorCodigo(String codigo, Empresa empresa) {
        return proveedorRepository.findByCodigoAndEmpresaAndEliminado(codigo, empresa, false);
    }

    @Override
    public Proveedor getProveedorPorId_Fiscal(String idFiscal, Empresa empresa) {
        return proveedorRepository.findByIdFiscalAndEmpresaAndEliminado(idFiscal, empresa, false);
    }

    @Override
    public Proveedor getProveedorPorRazonSocial(String razonSocial, Empresa empresa) {
        return proveedorRepository.findByRazonSocialAndEmpresaAndEliminado(razonSocial, empresa, false);
    }

    private void validarOperacion(TipoDeOperacion operacion, Proveedor proveedor) {
        //Entrada de Datos
        if (proveedor.getEmail() != null && proveedor.getEmail().equals("") == false) {
            if (Validator.esEmailValido(proveedor.getEmail()) == false) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_email_invalido"));
            }
        }
        //Requeridos
        if (Validator.esVacio(proveedor.getRazonSocial())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_razonSocial_vacia"));
        }
        if (proveedor.getCondicionIVA() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_condicionIVA_vacia"));
        }
        if (proveedor.getLocalidad() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_localidad_vacia"));
        }
        if (proveedor.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_empresa_vacia"));
        }
        //Duplicados
        //Codigo
        if (!proveedor.getCodigo().equals("")) {
            Proveedor proveedorDuplicado = this.getProveedorPorCodigo(proveedor.getCodigo(), proveedor.getEmpresa());
            if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
                    && proveedorDuplicado != null
                    && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_duplicado_codigo"));
            }
            if (operacion.equals(TipoDeOperacion.ALTA)
                    && proveedorDuplicado != null
                    && !proveedor.getCodigo().equals("")) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_duplicado_codigo"));
            }
        }
        //ID Fiscal
        if (!proveedor.getIdFiscal().equals("")) {
            Proveedor proveedorDuplicado = this.getProveedorPorId_Fiscal(proveedor.getIdFiscal(), proveedor.getEmpresa());
            if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
                    && proveedorDuplicado != null
                    && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_duplicado_idFiscal"));
            }
            if (operacion.equals(TipoDeOperacion.ALTA)
                    && proveedorDuplicado != null
                    && !proveedor.getIdFiscal().equals("")) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_duplicado_idFiscal"));
            }
        }
        //Razon social
        Proveedor proveedorDuplicado = this.getProveedorPorRazonSocial(proveedor.getRazonSocial(), proveedor.getEmpresa());
        if (operacion.equals(TipoDeOperacion.ALTA) && proveedorDuplicado != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_duplicado_razonSocial"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (proveedorDuplicado != null && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_duplicado_razonSocial"));
            }
        }
    }

    @Override
    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        if(proveedor.getCodigo() == null) {
            proveedor.setCodigo("");
        }
        if(proveedor.getIdFiscal() == null) {
            proveedor.setIdFiscal("");
        }
        this.validarOperacion(TipoDeOperacion.ALTA, proveedor);
        proveedor = proveedorRepository.save(proveedor);
        CuentaCorrienteProveedor cuentaCorrienteCliente = new CuentaCorrienteProveedor();
        cuentaCorrienteCliente.setProveedor(proveedor);
        cuentaCorrienteCliente.setEmpresa(proveedor.getEmpresa());
        cuentaCorrienteService.guardarCuentaCorrienteProveedor(cuentaCorrienteCliente);
        LOGGER.warn("El Proveedor " + proveedor + " se guardó correctamente.");
        return proveedor;
    }

    @Override
    @Transactional
    public void actualizar(Proveedor proveedor) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, proveedor);
        proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional
    public void eliminar(long idProveedor) {
        Proveedor proveedor = this.getProveedorPorId(idProveedor);
        if (proveedor == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_proveedor_no_existente"));
        }
        proveedor.setEliminado(true);
        proveedorRepository.save(proveedor);
    }
}
