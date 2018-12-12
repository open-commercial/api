package sic.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sic.service.BusinessServiceException;
import sic.service.IPhotoVideoUploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class PhotoVideoUploaderImpl implements IPhotoVideoUploader {

  @Value("${CLOUDINARY_URL}")
  private String cloudinaryUrl;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Override
  public String subirImagen(String nombreImagen, byte[] imagen) {
    String urlImagen;
    try {
      Path path = Files.createTempFile("imagen-file", ".jpg");
      File file = path.toFile();
      Files.write(path, imagen);
      Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
      Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
              "public_id", nombreImagen,
              "transformation", new Transformation().crop("fit").width(800).height(600)));
      urlImagen = uploadResult.get("secure_url").toString();
      logger.warn("La imagen {} se guardó correctamente.", nombreImagen);
      Files.delete(path);
    } catch (IOException ex) {
      throw new BusinessServiceException(
              RESOURCE_BUNDLE.getString("mensaje_error_al_subir_imagen"), ex);
    }
    return urlImagen;
  }

  @Override
  public void borrarImagen(String publicId) {
    try {
      Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
      logger.warn("La imagen {} se eliminó correctamente.", publicId);
    } catch (IOException ex) {
      throw new BusinessServiceException(
              RESOURCE_BUNDLE.getString("mensaje_error_al_borrar_imagen"), ex);
    }
  }
}
