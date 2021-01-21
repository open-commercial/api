package sic.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import sic.service.IPhotoVideoUploader;
import sic.exception.ServiceException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PhotoVideoUploaderImpl implements IPhotoVideoUploader {

  @Value("${CLOUDINARY_URL}")
  private String cloudinaryUrl;
  private static final String PATTERN = "^https:\\/\\/res.cloudinary.com\\/.*";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  
  @Autowired
  public PhotoVideoUploaderImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public String subirImagen(String nombreImagen, byte[] imagen) {
    String urlImagen;
    try {
      Path path = Files.createTempFile("imagen-file", ".jpg");
      File file = path.toFile();
      Files.write(path, imagen);
      Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
      Map uploadResult =
          cloudinary
              .uploader()
              .upload(
                  file,
                  ObjectUtils.asMap(
                      "public_id",
                      nombreImagen,
                      "transformation",
                      new Transformation().crop("fit").width(800).height(600)));
      urlImagen = uploadResult.get("secure_url").toString();
      logger.warn("La imagen {} se guardó correctamente.", nombreImagen);
      Files.delete(path);
      return urlImagen;
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_subir_imagen", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public void borrarImagen(String publicId) {
    try {
      Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
      logger.warn("La imagen {} se eliminó correctamente.", publicId);
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_borrar_imagen", null, Locale.getDefault()),
          ex);
    }
  }

  @Override
  public void isUrlValida(String url) {
    if (url != null) {
      if (url.isEmpty()) {
        throw new ServiceException(
            messageSource.getMessage("mensaje_url_imagen_no_valida", null, Locale.getDefault()));
      }
      Pattern pattern = Pattern.compile(PATTERN);
      Matcher matcher = pattern.matcher(url);
      if (matcher.matches()) {
        try {
          ImageIO.read(new URL(url));
        } catch (IOException | NullPointerException ex) {
          throw new ServiceException(
              messageSource.getMessage("mensaje_recurso_no_encontrado", null, Locale.getDefault()),
              ex);
        }
      } else {
        throw new ServiceException(
            messageSource.getMessage("mensaje_url_imagen_no_valida", null, Locale.getDefault()));
      }
    }
  }
}
