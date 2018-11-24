package sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.service.IPhotoVideoUploader;

@RestController
@RequestMapping("/api/v1")
public class PhotoVideoUploaderController {

    private final IPhotoVideoUploader photoVideoUploader;

    @Autowired
    public PhotoVideoUploaderController(IPhotoVideoUploader photoVideoUploader) {
        this.photoVideoUploader = photoVideoUploader;
    }

    @PostMapping("/imagenes")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public String subirImagen(@RequestParam String nombreImagen, @RequestBody byte[] imagen) {
        return photoVideoUploader.subirImagen(nombreImagen, imagen);
    }

    @DeleteMapping("/imagenes")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void borrarImagen(@RequestParam String nombreImagen) {
        photoVideoUploader.borrarImagen(nombreImagen);
    }
}
