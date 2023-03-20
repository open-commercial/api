package sic.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tokenaccesoexcluido")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenAccesoExcluido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idTokenAccesoExcluido;

    @Column(length = 300)
    private String token;
}
