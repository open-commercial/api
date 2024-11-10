package org.opencommercial.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.opencommercial.model.CategoriaIVA;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistracionClienteAndUsuarioDTO implements Serializable {

  @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ ]*$", message = "{mensaje_registracion_nombre}")
  private String nombre;

  @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ ]*$", message = "{mensaje_registracion_apellido}")
  private String apellido;

  @Pattern(regexp = "[0-9]{10}", message = "{mensaje_registracion_telefono}")
  private String telefono;

  @Email(message = "{mensaje_registracion_email}")
  private String email;

  private CategoriaIVA categoriaIVA;

  private String nombreFiscal;

  @Length(min = 6, message = "{mensaje_registracion_contraseña}")
  private String password;

  private String recaptcha;
}
