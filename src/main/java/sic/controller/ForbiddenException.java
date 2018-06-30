package sic.controller;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super();
    }

    public ForbiddenException(String mensaje) {
        super(mensaje);
    }

    public ForbiddenException(Throwable causa) {
        super(causa);
    }

    public ForbiddenException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
