package sic.modelo.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.CategoriaIVA;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ClienteEmbeddable implements Serializable {

  private String nroCliente;
  private String nombreFiscalCliente;
  private String nombreFantasiaCliente;
  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVACliente;
  private Long idFiscalCliente;
  private String emailCliente;
  private String telefonoCliente;
  private String descripcionCliente;
  private Double latitudCliente;
  private Double longitudCliente;
  private String calleCliente;
  private Integer numeroCliente;
  private String pisoCliente;
  private String departamentoCliente;
  private String nombreLocalidadCliente;
  private String codigoPostalCliente;
  private BigDecimal costoEnvioCliente;
  private String nombreProvinciaCliente;
  //private UbicacionEmbeddable ubicacion;
}