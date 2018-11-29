package sic.service;

import java.util.List;
import sic.modelo.Empresa;

public interface IEmpresaService {

  Empresa getEmpresaPorId(Long id_Empresa);

  void actualizar(Empresa empresa);

  void eliminar(Long idEmpresa);

  Empresa getEmpresaPorIdFiscal(Long idFiscal);

  Empresa getEmpresaPorNombre(String nombre);

  List<Empresa> getEmpresas();

  Empresa guardar(Empresa empresa);

  String guardarLogo(long idEmpresa, byte[] imagen);
}
