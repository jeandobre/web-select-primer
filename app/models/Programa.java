package models;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Created by jeandobre on 07/11/2016.
 */
@Entity
@Table(name = "programas", schema = "public")
public class Programa extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column
    @Size(max = 230)
    public String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sequencia")
    public TipoSequencia tipoSequencia;

    @Column
    public String local;

    @Column
    @Size(max = 40)
    public String parametros;

    @Column
    @Size(max = 10)
    public String versao;

    @Column
    public Boolean ativo;

}
