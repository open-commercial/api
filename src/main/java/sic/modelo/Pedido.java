package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "pedido")
@Data
@ToString(exclude = {"facturas", "renglones"})
@EqualsAndHashCode(of = {"nroPedido", "empresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Pedido", scope = Pedido.class)
public class Pedido implements Serializable {

    @Id
    @GeneratedValue
    private long id_Pedido;

    private long nroPedido;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @Column(nullable = false)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    private boolean eliminado;

    @ManyToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonProperty(access = Access.WRITE_ONLY)    
    private List<Factura> facturas;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_Pedido")
    @JsonProperty(access = Access.WRITE_ONLY)
    private List<RenglonPedido> renglones;

    private double totalEstimado;

    private double totalActual;
    
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    public Pedido() {
    }

    public Pedido(long id_Pedido, long nroPedido, Date fecha, Date fechaVencimiento, 
            String observaciones, Empresa empresa, boolean eliminado, Cliente cliente, 
            Usuario usuario, List<Factura> facturas, List<RenglonPedido> renglones, 
            double totalEstimado, double totalActual, EstadoPedido estado) {
        this.id_Pedido = id_Pedido;
        this.nroPedido = nroPedido;
        this.fecha = fecha;
        this.fechaVencimiento = fechaVencimiento;
        this.observaciones = observaciones;
        this.empresa = empresa;
        this.eliminado = eliminado;
        this.cliente = cliente;
        this.usuario = usuario;
        this.facturas = facturas;
        this.renglones = renglones;
        this.totalEstimado = totalEstimado;
        this.totalActual = totalActual;
        this.estado = estado;
    }

}
