package sic.service;

public interface IImageUploaderService {

  boolean isServicioConfigurado();

  String subirImagen(String nombreImagen, byte[] imagen);

  void borrarImagen(String publicId);

  void isUrlValida(String name);
}
