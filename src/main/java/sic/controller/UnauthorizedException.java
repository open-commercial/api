package sic.controller;

public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }

    public UnauthorizedException(Throwable causa) {
        super(causa);
    }

    public UnauthorizedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
}
