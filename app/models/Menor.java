package models;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import javax.xml.transform.Result;

/**
 * Created by jeandobre on 05/11/2016.
 */
@Entity
@Table(name = "menores", schema = "public")
public class Menor extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", referencedColumnName="id", nullable=false)
    public Resultado resultado;
}
