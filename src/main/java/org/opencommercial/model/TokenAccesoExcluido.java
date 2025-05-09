package org.opencommercial.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tokenaccesoexcluido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"token"})
public class TokenAccesoExcluido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idTokenAccesoExcluido;

    @Column(length = 300)
    private String token;
}
