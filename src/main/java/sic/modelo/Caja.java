package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "caja")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroCaja", "empresa"})
@ToString
public class Caja implements Serializable {

    @Id
    @GeneratedValue
    private long id_Caja;

    private int nroCaja;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaApertura;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCorteInforme;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCierre;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @OneToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuarioAbreCaja;

    @OneToOne
    @JoinColumn(name = "id_UsuarioCierra", referencedColumnName = "id_Usuario")
    private Usuario usuarioCierraCaja;

    @Column(nullable = false)
    private String observacion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCaja estado;

    @Column(precision = 25, scale = 15)
    private BigDecimal saldoInicial;

    @Column(precision = 25, scale = 15)
    private BigDecimal saldoSistema;

    @Column(precision = 25, scale = 15)
    private BigDecimal saldoReal;

    private boolean eliminada;

}
