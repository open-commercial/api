package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import javax.persistence.*;
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
  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private UbicacionDTO ubicacionFacturacion;
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

}
