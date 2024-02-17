package sic.config;

public class Views {

  // https://www.baeldung.com/spring-security-role-filter-json

  public static class Comprador {}
  public static class Viajante extends Comprador {}
  public static class Vendedor extends Viajante {}
  public static class Encargado extends Vendedor {}
  public static class Administrador extends Encargado {}

}
