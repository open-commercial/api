package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.dto.UbicacionDTO;
import sic.modelo.embeddable.ClienteEmbeddable;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "remito")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remito implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idRemito;

  private LocalDateTime fecha;

  @Column(nullable = false)
  private long serie;

  @Column(nullable = false)
  private long nroNota;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoDeComprobante tipoComprobante;

  @Embedded private ClienteEmbeddable clienteEmbedded;

  @ManyToOne
  @JoinColumn(name = "idCliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_factura_cliente_vacio}")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
  @NotNull(message = "{mensaje_factura_sucursal_vacia}")
  private Sucursal sucursal;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_factura_usuario_vacio}")
  private Usuario usuario;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idRemito")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotEmpty(message = "{mensaje_factura_renglones_vacio}")
  private List<RenglonRemito> renglones;

  @Embedded private UbicacionDTO detalleEnvio;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_total_negativo}")
  private BigDecimal total;

  private boolean contraEntrega;
}
