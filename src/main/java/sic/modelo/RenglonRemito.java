package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(name = "renglonremito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenglonRemito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idRenglonRemito;

    @Column(nullable = false)
    private String codigoItem;

    @Column(nullable = false)
    private String descripcionItem;

    @Column(nullable = false)
    private String medidaItem;//TipoDeBulto

    @Column(precision = 25, scale = 15)
    @NotNull
    @Positive(message = "{mensaje_renglon_cantidad_mayor_uno}")
    private BigDecimal cantidad;
}
