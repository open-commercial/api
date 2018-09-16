package sic.modelo;

import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "proveedor")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"razonSocial", "empresa"})
@ToString
public class Proveedor implements Serializable {

    @Id
    @GeneratedValue
    private long id_Proveedor;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_CondicionIVA", referencedColumnName = "id_CondicionIVA")
    private CondicionIVA condicionIVA;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaIVA categoriaIVA;

    private String idFiscal;

    @Column(nullable = false)
    private String telPrimario;

    @Column(nullable = false)
    private String telSecundario;

    @Column(nullable = false)
    private String contacto;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String web;

    @ManyToOne
    @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
    @QueryInit("provincia.pais")
    private Localidad localidad;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    private boolean eliminado;
    
    @Transient
    private BigDecimal saldoCuentaCorriente;
    
    @Transient 
    private Date fechaUltimoMovimiento;
    
}