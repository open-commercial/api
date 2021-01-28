package sic.service;

public interface IPhotoVideoUploader {

  String subirImagen(String nombreImagen, byte[] imagen);

  void borrarImagen(String publicId);

  void isUrlValida(String name);
}
