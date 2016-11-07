package models;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Created by jeandobre on 05/11/2016.
 */
@Entity
@Table(name = "maiores", schema = "public")
public class Maior extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", referencedColumnName="id", nullable=false)
    public Resultado resultado;
}
