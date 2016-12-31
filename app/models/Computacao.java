package models;

import play.Play;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

/**
 * Created by jeandobre on 05/11/2016.
 */
@Entity
@Table(name = "computacao", schema = "public")
public class Computacao extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    public Integer alfa;
    public Integer beta;
    public Integer k;
    public Integer maior;
    public Integer menor;
    public float media;
    public Integer ocr;
    public Boolean ruim;

    public static List<Computacao> todosRuim(Integer k){

        return find("k = :k AND ruim = TRUE ORDER BY alfa,beta ASC")
                .setParameter("k", k).fetch();
    }
}
