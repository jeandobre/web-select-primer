package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by jeandobre on 29/10/16.
 */
@Entity
@Table(name="betas", schema="public")
public class ArquivoBeta extends Model {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id")
    public Processamento processamento;

    public String nome;

    public String arquivo;

    public Integer tamanho;

    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "beta")
    public List<Ocorrencia> ocorrencias;

    /*

    public Ocorrencia ocr;
    public CandidatoPrimer maior;
    public CandidatoPrimer menor;

    public List<CandidatoPrimer> candidatos;

    public void setMaiorMenorPorBeta(){
        maior = candidatos.get(0);
        menor = candidatos.get(0);
        for (CandidatoPrimer rr : candidatos) {
            if (rr.tamanho > maior.tamanho) maior = rr;
            if (rr.tamanho < menor.tamanho) menor = rr;
        }
    }

    */
}
