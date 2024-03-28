package sic.modelo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.config.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "renglonremito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonView(Views.Comprador.class)
public class RenglonRemito implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idRenglonRemito;

    @Column(nullable = false)
    private String tipoBulto;

    @Column(precision = 25, scale = 15)
    @NotNull
    @Positive(message = "{mensaje_renglon_cantidad_mayor_uno}")
    private BigDecimal cantidad;
}
