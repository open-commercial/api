package sic.controller;

import sic.modelo.Rol;
import java.util.EnumMap;
import java.util.Map;

public class Views {

  public static final Map<Rol, Class> MAPPING = new EnumMap<>(Rol.class);

  // https://www.baeldung.com/spring-security-role-filter-json
  static {
    MAPPING.put(Rol.COMPRADOR, Comprador.class);
    MAPPING.put(Rol.VIAJANTE, Viajante.class);
    MAPPING.put(Rol.VENDEDOR, Vendedor.class);
    MAPPING.put(Rol.ENCARGADO, Encargado.class);
    MAPPING.put(Rol.ADMINISTRADOR, Administrador.class);
  }

  public static class Comprador {}
  public static class Viajante extends Comprador {}
  public static class Vendedor extends Viajante {}
  public static class Encargado extends Vendedor {}
  public static class Administrador extends Encargado {}

}
