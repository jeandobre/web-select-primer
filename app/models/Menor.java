package models;

import play.db.jpa.Model;

import javax.persistence.*;
import javax.xml.transform.Result;

/**
 * Created by jeandobre on 05/11/2016.
 */
@Entity
@Table(name = "menores", schema = "public")
public class Menor extends Model {

    @Id
    @GeneratedValue
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id")
    public Resultado resultado;
}
