package models;

import play.db.jpa.GenericModel;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Created by jeandobre on 27/10/16.
 */
@Entity
@Table(name="resultados", schema="public")
public class Resultado extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id")
    public Processamento processamento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id")
    public Ocorrencia ocorrencia;

  /*  public Boolean isMenor(){
       //TODO
        return Boolean.FALSE;
    }

    public Boolean isMaior(){
       //TODO
        return Boolean.FALSE;
    }
*/
}
