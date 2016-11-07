package models;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Created by jeandobre on 20/10/16.
 */
@Entity
@Table(name="ocorrencias", schema="public")
public class Ocorrencia extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beta_id", referencedColumnName="id")
    public ArquivoBeta beta;

    @Column
    public Integer j;

    @Column
    public Integer r;

    @Column
    public String segmento;

}
