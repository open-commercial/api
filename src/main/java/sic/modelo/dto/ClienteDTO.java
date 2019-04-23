package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Cliente", "nroCliente", "idEmpresa", "nombreEmpresa", "fechaAlta", "idCredencial", "nombreCredencial", "ubicacionFacturacion", "ubicacionEnvio"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ClienteDTO implements Serializable {

  private long id_Cliente;
  private BigDecimal bonificacion;
  private String nroCliente;
  private String nombreFiscal;
  private String nombreFantasia;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;

  @AttributeOverrides({
    @AttributeOverride(name = "calle", column = @Column(name = "calleFacturacion")),
    @AttributeOverride(name = "codigoPostal", column = @Column(name = "codigoPostalFacturacion")),
    @AttributeOverride(name = "departamento", column = @Column(name = "departamentoFacturacion")),
    @AttributeOverride(name = "descripcion", column = @Column(name = "descripcionFacturacion")),
    @AttributeOverride(name = "idLocalidad", column = @Column(name = "idLocalidadFacturacion")),
    @AttributeOverride(name = "idProvincia", column = @Column(name = "idProvinciaFacturacion")),
    @AttributeOverride(name = "idUbicacion", column = @Column(name = "idUbicacionFacturacion")),
    @AttributeOverride(name = "latitud", column = @Column(name = "latitudFacturacion")),
    @AttributeOverride(name = "longitud", column = @Column(name = "longitudFacturacion")),
    @AttributeOverride(name = "nombreLocalidad", column = @Column(name = "nombreLocalidadFacturacion")),
    @AttributeOverride(name = "nombreProvincia", column = @Column(name = "nombreProvinciaFacturacion")),
    @AttributeOverride(name = "numero", column = @Column(name = "numeroFacturacion")),
    @AttributeOverride(name = "piso", column = @Column(name = "pisoFacturacion"))
  })
  private UbicacionDTO ubicacionFacturacion;

  @AttributeOverrides({
    @AttributeOverride(name = "calle", column = @Column(name = "calleEnvio")),
    @AttributeOverride(name = "codigoPostal", column = @Column(name = "codigoPostalEnvio")),
    @AttributeOverride(name = "departamento", column = @Column(name = "departamentoEnvio")),
    @AttributeOverride(name = "descripcion", column = @Column(name = "descripcionEnvio")),
    @AttributeOverride(name = "idLocalidad", column = @Column(name = "idLocalidadEnvio")),
    @AttributeOverride(name = "idProvincia", column = @Column(name = "idProvinciaEnvio")),
    @AttributeOverride(name = "idUbicacion", column = @Column(name = "idUbicacionEnvio")),
    @AttributeOverride(name = "latitud", column = @Column(name = "latitudEnvio")),
    @AttributeOverride(name = "longitud", column = @Column(name = "longitudEnvio")),
    @AttributeOverride(name = "nombreLocalidad", column = @Column(name = "nombreLocalidadEnvio")),
    @AttributeOverride(name = "nombreProvincia", column = @Column(name = "nombreProvinciaEnvio")),
    @AttributeOverride(name = "numero", column = @Column(name = "numeroEnvio")),
    @AttributeOverride(name = "piso", column = @Column(name = "pisoEnvio"))
  })
  private UbicacionDTO ubicacionEnvio;

  private String email;
  private String telefono;
  private String contacto;
  private Date fechaAlta;
  private Long idEmpresa;
  private String nombreEmpresa;
  private Long idViajante;
  private String nombreViajante;
  private Long idCredencial;
  private String nombreCredencial;
  private boolean predeterminado;
  private BigDecimal saldoCuentaCorriente;
  private Date fechaUltimoMovimiento;

}
