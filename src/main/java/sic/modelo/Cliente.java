package sic.modelo;

import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import lombok.ToString;

@Entity
@Table(name = "cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"razonSocial", "idFiscal", "empresa"})
@ToString
public class Cliente implements Serializable {

    @Id
    @GeneratedValue
    private long id_Cliente;

    @Column(nullable = false)
    private String razonSocial;

    private String nombreFantasia;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_CondicionIVA", referencedColumnName = "id_CondicionIVA")
    private CondicionIVA condicionIVA;

    @Column(nullable = false)
    private String idFiscal;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telPrimario;

    @Column(nullable = false)
    private String telSecundario;

    @ManyToOne
    @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
    @QueryInit("provincia.pais")
    private Localidad localidad;

    @Column(nullable = false)
    private String contacto;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAlta;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_Usuario_Viajante", referencedColumnName = "id_Usuario")
    private Usuario viajante;

    @OneToOne
    @JoinColumn(name = "id_Usuario_Credencial", referencedColumnName = "id_Usuario")
    private Usuario credencial;

    private boolean eliminado;

    private boolean predeterminado;

    @Transient
    private Double saldoCuentaCorriente;
    
    @Transient 
    private Date fechaUltimoMovimiento;

}