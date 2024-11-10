package org.opencommercial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "tokenaccesoexcluido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenAccesoExcluido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idTokenAccesoExcluido;

    @Column(length = 300)
    private String token;
}
