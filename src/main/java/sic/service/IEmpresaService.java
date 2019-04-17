package sic.service;

import java.util.List;
import sic.modelo.Empresa;

import javax.validation.Valid;

public interface IEmpresaService {

  Empresa getEmpresaPorId(Long id_Empresa);

  void actualizar(Empresa empresaParaActualizar, Empresa empresaPersistida);

  void eliminar(Long idEmpresa);

  Empresa getEmpresaPorIdFiscal(Long idFiscal);

  Empresa getEmpresaPorNombre(String nombre);

  List<Empresa> getEmpresas();

  Empresa guardar(@Valid Empresa empresa);

  String guardarLogo(long idEmpresa, byte[] imagen);
}
