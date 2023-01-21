package sic.service;

public interface IImageUploaderService {

  boolean isServicioDeshabilitado();

  String subirImagen(String nombreImagen, byte[] imagen);

  void borrarImagen(String publicId);

  void isUrlValida(String name);
}
