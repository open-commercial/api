package sic.modelo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pago implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private long idPago;

    private long nroPago;

    private EstadoPago estadoPago;

    @Pattern(
            regexp = "^https:\\/\\/res.cloudinary.com\\/.*",
            message = "{mensaje_url_imagen_no_valida}")
    @JsonView(Views.Comprador.class)
    private String urlImagen;

    @ManyToOne
    @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
    @NotNull//(message = "{mensaje_recibo_forma_de_pago_vacia}")
    private FormaDePago formaDePago;

    @NotNull
    private LocalDateTime fecha;

    @OneToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    @NotNull//(message = "{mensaje_pedido_cliente_vacio}")
    private Cliente cliente;

    @OneToOne
    @JoinColumn(name = "id_Pedido", referencedColumnName = "id_Pedido")
    private Pedido pedido;


    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0")
    //@DecimalMin(value = "0", message = "{mensaje_pedido_total_estimado_negativo}")
    private BigDecimal monto;



}
