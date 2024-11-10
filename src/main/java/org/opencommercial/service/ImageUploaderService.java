package org.opencommercial.service;

public interface ImageUploaderService {

  boolean isServicioConfigurado();

  String subirImagen(String nombreImagen, byte[] imagen);

  void borrarImagen(String publicId);
}
