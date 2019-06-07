package sic.modelo.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.CategoriaIVA;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ClienteEmbeddable {

  private String nroCliente;
  private String nombreFiscal;
  private String nombreFantasia;
  @Enumerated(EnumType.STRING)
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private String email;
  private String telefono;
  private UbicacionEmbeddable ubicacion;
}
