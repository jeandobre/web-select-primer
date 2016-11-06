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
    @GeneratedValue
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beta_id")
    public ArquivoBeta beta;

    public Integer j;

    public Integer r;

    public String segmento;

}
