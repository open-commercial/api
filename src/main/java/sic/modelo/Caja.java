package sic.modelo;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
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
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caja")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroCaja", "empresa"})
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

    @Column(nullable = true)
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

    private double saldoInicial;

    private double saldoFinal;

    private double saldoReal;

    private boolean eliminada;
    
    @Transient
    private Map<Long, Double> totalesPorFomaDePago;
    
    @Transient
    private double totalAfectaCaja;
            
    @Transient
    private double totalGeneral;

}
