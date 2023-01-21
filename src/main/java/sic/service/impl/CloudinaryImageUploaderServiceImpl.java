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
import sic.service.IImageUploaderService;
import sic.exception.ServiceException;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryImageUploaderServiceImpl implements IImageUploaderService {

  @Value("#{new Boolean('${CLOUDINARY_ENABLED}')}")
  private boolean cloudinaryEnabled;

  @Value("${CLOUDINARY_URL}")
  private String cloudinaryUrl;

  private static final String CLOUDINARY_PATTERN = "^https:\\/\\/res.cloudinary.com\\/.*";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  
  @Autowired
  public CloudinaryImageUploaderServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean isServicioDeshabilitado() {
    if (cloudinaryEnabled && cloudinaryUrl != null && !cloudinaryUrl.equals("")) {
      return false;
    }
    logger.warn("El servicio de Cloudinary se encuentra deshabilitado");
    return true;
  }

  @Override
  public String subirImagen(String nombreImagen, byte[] imagen) {
    if (isServicioDeshabilitado()) return null;
    try {
      var path = Files.createTempFile("imagen-file", ".jpg");
      var file = path.toFile();
      Files.write(path, imagen);
      var cloudinary = new Cloudinary(cloudinaryUrl);
      var uploadResult = cloudinary.uploader()
              .upload(file,
                      ObjectUtils.asMap("public_id", nombreImagen, "transformation",
                              new Transformation<>()
                                      .crop("fit")
                                      .width(800)
                                      .height(600)));
      var urlImagen = uploadResult.get("secure_url").toString();
      logger.info("La imagen {} se guardó correctamente.", nombreImagen);
      Files.delete(path);
      return urlImagen;
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_subir_imagen", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public void borrarImagen(String publicId) {
    if (isServicioDeshabilitado()) return;
    try {
      var cloudinary = new Cloudinary(cloudinaryUrl);
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
      logger.info("La imagen {} se eliminó correctamente.", publicId);
    } catch (IOException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_al_borrar_imagen", null, Locale.getDefault()),
          ex);
    }
  }

  @Override
  public void isUrlValida(String url) {
    if (isServicioDeshabilitado()) return;
    if (url != null) {
      if (url.isEmpty()) {
        throw new ServiceException(
            messageSource.getMessage("mensaje_url_imagen_no_valida", null, Locale.getDefault()));
      }
      Pattern pattern = Pattern.compile(CLOUDINARY_PATTERN);
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
