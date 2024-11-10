package org.opencommercial.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.opencommercial.exception.ServiceException;

import java.io.IOException;
import java.util.Locale;

@Service
@Slf4j
public class CloudinaryImageUploaderServiceImpl implements ImageUploaderService {

  @Value("${CLOUDINARY_URL}")
  private String cloudinaryUrl;

  private static final String MENSAJE_SERVICIO_NO_CONFIGURADO = "El servicio de Cloudinary no se encuentra configurado";
  private final MessageSource messageSource;
  
  @Autowired
  public CloudinaryImageUploaderServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean isServicioConfigurado() {
    return cloudinaryUrl != null && !cloudinaryUrl.isEmpty();
  }

  @Override
  public String subirImagen(String nombreImagen, byte[] imagen) {
    if (!isServicioConfigurado()) throw new ServiceException(MENSAJE_SERVICIO_NO_CONFIGURADO);
    try {
      var cloudinary = new Cloudinary(cloudinaryUrl);
      var uploadResult = cloudinary.uploader()
              .upload(imagen, ObjectUtils.asMap(
                      "public_id", nombreImagen,
                      "transformation", new Transformation<>()
                              .crop("fit")
                              .width(800)
                              .height(600)));
      var urlImagen = uploadResult.get("secure_url").toString();
      log.info("La imagen {} se guardó correctamente.", nombreImagen);
      return urlImagen;
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_subir_imagen", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public void borrarImagen(String publicId) {
    if (!isServicioConfigurado()) throw new ServiceException(MENSAJE_SERVICIO_NO_CONFIGURADO);
    try {
      var cloudinary = new Cloudinary(cloudinaryUrl);
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
      log.info("La imagen {} se eliminó correctamente.", publicId);
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_borrar_imagen", null, Locale.getDefault()),
          ex);
    }
  }
}
