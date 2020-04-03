package sic.respository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import sic.modelo.Medida;
import sic.repository.MedidaRepository;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MedidaRepositoryTest {

  @Autowired private MedidaRepository medidaRepository;

  @Test(expected = ObjectOptimisticLockingFailureException.class)
  public void shouldTestConcurrencia() {
    Medida medida = new Medida();
    medida.setNombre("Metro");
    medida.setVersion(0);
    medida = medidaRepository.save(medida);
    assertEquals("Medidas disponibles.", 1, medidaRepository.count());
    Medida medidaDelUsuarioUno =
        medidaRepository.findById(medida.getIdMedida()).isPresent()
            ? medidaRepository.findById(medida.getIdMedida()).get()
            : null;
    assertNotNull(medidaDelUsuarioUno);
    Medida medidaDelUsuarioDos =
        medidaRepository.findById(medida.getIdMedida()).isPresent()
            ? medidaRepository.findById(medida.getIdMedida()).get()
            : null;
    assertNotNull(medidaDelUsuarioDos);
    medidaDelUsuarioUno.setNombre("Centimetro");
    medidaDelUsuarioDos.setNombre("Kilometro");
    assertEquals(medidaDelUsuarioUno.getVersion(), medidaDelUsuarioDos.getVersion());
    medidaRepository.save(medidaDelUsuarioUno);
    medidaRepository.save(medidaDelUsuarioDos);
  }
}
