package models;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by jeandobre on 29/10/16.
 */
@Entity
@Table(name="betas", schema="public")
public class ArquivoBeta extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id", referencedColumnName="id")
    public Processamento processamento;

    @Column(nullable = false)
    @NotNull
    public Integer posicao;

    @Column
    public String nome;

    @Column
    public String arquivo;

    @Column(name = "arquivo_resultado")
    public String arquivoResultado;

    @Column
    public Integer tamanho;

    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "beta")
    @OrderBy(value = "j")
    public List<Ocorrencia> ocorrencias;

    public Integer getTotalOcorrencias(){
        return ocorrencias.size();
    }

    public static ArquivoBeta buscarPorProcessamentoPosicao(Long id, Integer posicao){

        return ArquivoBeta.find("processamento.id = :id and posicao = :posicao")
                .setParameter("id", id)
                .setParameter("posicao", posicao)
                .first();
    }

}
