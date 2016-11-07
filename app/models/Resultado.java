package models;

import play.db.jpa.Model;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Created by jeandobre on 27/10/16.
 */
@Entity
@Table(name="resultados", schema="public")
public class Resultado extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id", referencedColumnName="id")
    public Processamento processamento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", referencedColumnName="id")
    public Ocorrencia ocorrencia;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ocorrencia")
    public Maior maior;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ocorrencia")
    public Menor menor;
}
