package sic.respository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import sic.modelo.Medida;
import sic.modelo.dto.MedidaDTO;
import sic.repository.MedidaRepository;
import sic.repository.ProductoRepository;

import static org.junit.Assert.assertEquals;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = MedidaRepository.class)
@SpringBootTest
@RunWith(SpringRunner.class)
//@DataJpaTest
public class MedidaRepositoryTest {

    @Autowired
    private MedidaRepository medidaRepository;

    @Before
    public void setUp() throws Exception {
        Medida medida1 = new Medida();
        medida1.setNombre("Metro");
        medida1.setVersion(0);
//        Medida medida2 = new Medida();
//        medida2.setNombre("Kilo");
//        medida2.setVersion(1);
//        Medida medida3 = new Medida();
//        medida3.setNombre("Gramos");
//        medida3.setVersion(1);
//        Medida medida4 = new Medida();
//        medida4.setNombre("CM");
//        medida4.setVersion(1);
        medidaRepository.save(medida1);
//        medidaRepository.save(medida2);
//        medidaRepository.save(medida3);
//        medidaRepository.save(medida4);
//        movieRepository.save(new Movie("The great movie", 5));
//        movieRepository.save(new Movie("The not so great movie", 4));
//        movieRepository.save(new Movie("Yet another movie", 3));
//        movieRepository.save(new Movie("The big crap", 1));
    }

    @Test(expected = ObjectOptimisticLockingFailureException.class)
    public void shouldTestConcurrencia() {

        assertEquals("Medidas disponibles.", 1, medidaRepository.count());

        Medida medidaUsuarioUno = medidaRepository.findByNombreAndEliminada("Metro", false);
        Medida medidaUsuarioDos = medidaRepository.findByNombreAndEliminada("Metro", false);

        MedidaDTO medidaUsuarioUnoDTO = MedidaDTO.builder().idMedida(medidaUsuarioUno.getIdMedida())
                .nombre(medidaUsuarioUno.getNombre())
                .eliminada(medidaUsuarioUno.isEliminada())
                .version(medidaUsuarioUno.getVersion())
                .idMedida(medidaUsuarioUno.getIdMedida())
                .build();
        MedidaDTO medidaUsuarioDosDTO = MedidaDTO.builder().idMedida(medidaUsuarioDos.getIdMedida())
                .nombre(medidaUsuarioDos.getNombre())
                .eliminada(medidaUsuarioDos.isEliminada())
                .version(medidaUsuarioDos.getVersion())
                .idMedida(medidaUsuarioDos.getIdMedida())
                .build();

        medidaUsuarioUnoDTO.setNombre("Kilometros -");
        medidaUsuarioDosDTO.setNombre("Millas -");

        Medida medidaUnoActualizadaUsuarioUno = new Medida();
        medidaUnoActualizadaUsuarioUno.setNombre(medidaUsuarioUnoDTO.getNombre());
        medidaUnoActualizadaUsuarioUno.setEliminada(medidaUsuarioUnoDTO.isEliminada());
        medidaUnoActualizadaUsuarioUno.setVersion(medidaUsuarioUnoDTO.getVersion());
        medidaUnoActualizadaUsuarioUno.setIdMedida(medidaUsuarioUnoDTO.getIdMedida());
        Medida medidaUnoActualizadaUsuarioDos = new Medida();
        medidaUnoActualizadaUsuarioDos.setNombre(medidaUsuarioDosDTO.getNombre());
        medidaUnoActualizadaUsuarioDos.setEliminada(medidaUsuarioDosDTO.isEliminada());
        medidaUnoActualizadaUsuarioDos.setVersion(medidaUsuarioDosDTO.getVersion());
        medidaUnoActualizadaUsuarioDos.setIdMedida(medidaUsuarioDosDTO.getIdMedida());;

        assertEquals(0, medidaUnoActualizadaUsuarioUno.getVersion().intValue());
        assertEquals(0, medidaUnoActualizadaUsuarioDos.getVersion().intValue());

        medidaRepository.save(medidaUnoActualizadaUsuarioUno);

        medidaRepository.save(medidaUnoActualizadaUsuarioDos);
    }

}
