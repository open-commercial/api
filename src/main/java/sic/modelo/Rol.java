package sic.modelo;

public enum Rol {

    ADMINISTRADOR(1),
    ENCARGADO(2),
    VENDEDOR(3),
    VIAJANTE(4),
    COMPRADOR(5);

    private final int value;

    Rol(int value) {
        this.value = value;
    }

    public int getNivelDeAcceso() {
        return value;
    }
}
