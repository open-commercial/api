package sic.modelo;

/**
 * Describe las distintas operaciones que se pueden realizar sobre una entidad.
 */
public enum TipoDeOperacion {

    /**
     * Para el caso en que se este realizando un alta.
     */
    ALTA,
    /**
     * Para el caso en que se este realizando una actualizacion.
     */
    ACTUALIZACION,
    /**
     * Para el caso en que se este realizando una baja.
     */
    ELIMINACION;
}