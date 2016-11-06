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
    @GeneratedValue
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id")
    public Processamento processamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id")
    public Ocorrencia ocorrencia;

    public Boolean isMenor(){
       //TODO
        return Boolean.FALSE;
    }

    public Boolean isMaior(){
       //TODO
        return Boolean.FALSE;
    }

}
