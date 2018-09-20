package sic.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.repository.NoRepositoryBean;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

import java.io.Serializable;
import java.math.BigDecimal;

@NoRepositoryBean
public interface NotaRepositoryCustom<T extends Nota, ID extends Serializable> {

  BigDecimal calcularTotalNotas(BooleanBuilder builder);

  BigDecimal calcularIVANotas(BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes);
}
