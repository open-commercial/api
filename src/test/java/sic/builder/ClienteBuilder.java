package sic.builder;

import java.util.Date;
import sic.modelo.Cliente;
import sic.modelo.CondicionIVA;
import sic.modelo.Empresa;
import sic.modelo.Localidad;
import sic.modelo.Usuario;

public class ClienteBuilder {

    private long id_Cliente = 0L;
    private String razonSocial = "Construcciones S.A.";
    private String nombreFantasia = "Servimetal";
    private String direccion = "Perugorria 2421";
    private CondicionIVA condicionIVA = new CondicionIVABuilder().build();
    private String idFiscal = "23248527419";
    private String email = "servimetal@hotmail.com";
    private String telPrimario = "379 4587114";
    private String telSecundario = "379 4852498";
    private Localidad localidad = new LocalidadBuilder().build();
    private String contacto = "Facundo Pastore";
    private Date fechaAlta = new Date(1342580400000L); // 18/07/2012
    private Empresa empresa = new EmpresaBuilder().build();
    private Usuario viajante = new UsuarioBuilder().build();
    private Usuario credencial = new UsuarioBuilder().build();
    private boolean eliminado = false;
    private boolean predeterminado = false;
    private double saldoCuentaCorriente = 0;
    private Date fechaUltimoMovimiento = new Date();
    
    public Cliente build() {
        return new Cliente(id_Cliente, razonSocial, nombreFantasia, direccion, condicionIVA,
                idFiscal, email, telPrimario, telSecundario, localidad, contacto, fechaAlta,
                empresa, viajante, credencial, eliminado, predeterminado, saldoCuentaCorriente, fechaUltimoMovimiento);
    }
    
    public ClienteBuilder withId_Cliente(long id_Cliente) {
        this.id_Cliente = id_Cliente;
        return this;
    }
    
    public ClienteBuilder withRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
        return this;
    }
    
    public ClienteBuilder withNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
        return this;
    }
    
    public ClienteBuilder withDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }
    
    public ClienteBuilder withCondicionIVA(CondicionIVA condicionIVA) {
        this.condicionIVA = condicionIVA;
        return this;
    }
    
    public ClienteBuilder withIdFiscal(String idFiscal) {
        this.idFiscal = idFiscal;
        return this;
    }
    
    public ClienteBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public ClienteBuilder withTelPrimario(String telPrimario) {
        this.telPrimario = telPrimario;
        return this;
    }
    
    public ClienteBuilder withTelSecundario(String telSecundario) {
        this.telSecundario = telSecundario;
        return this;
    }
    
    public ClienteBuilder withLocalidad(Localidad localidad) {
        this.localidad = localidad;
        return this;
    }
    
    public ClienteBuilder withContacto(String contacto) {
        this.contacto = contacto;
        return this;
    }
    
    public ClienteBuilder withFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
        return this;
    }
    
    public ClienteBuilder withEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }
    
    public ClienteBuilder withViajante(Usuario viajante) {
        this.viajante = viajante;
        return this;
    }
    
    public ClienteBuilder withCredencial(Usuario credencial) {
        this.credencial = credencial;
        return this;
    }
    
    public ClienteBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
    
    public ClienteBuilder withPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
        return this;
    }
    
    public ClienteBuilder whitSaldoCuentaCorriente(double saldoCuentaCorriente) {
        this.saldoCuentaCorriente = saldoCuentaCorriente;
        return this;
    }
}
