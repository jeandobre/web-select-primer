package models;

import play.db.jpa.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * Created by jeandobre on 04/11/16.
 */
@Entity
@Table(name="processamentos", schema="public")
public class Processamento extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    public Long id;

    @Temporal(TemporalType.DATE)
    public Date inicio;

    @Temporal(TemporalType.DATE)
    public Date fim;

    @Column(name="tempo_gasto_segundos")
    public Integer tempoGasto;

    @Column(name="alfa_nome")
    public String alfaNome;

    @Column(name="alfa_arquivo")
    public String alfaArquivo;

    @Column(name="alfa_tamanho")
    public Integer alfaTamanho;

    @Column(name="tipo_sequencia")
    public TipoSequencia tipoSequencia;

    @Column(name="mostrar_maior_menor")
    public Boolean mostrarMaiorMenor;

    @Column(name="mostrar_entre_mil_dois_mil")
    public Boolean mostrarEntreMilDoisMil;

    @Column
    public Integer posicao;

    @Column
    public Integer distancia;

    @Column
    public String processamento;

    @Column
    public String nome;

    @Column
    public String informacao;

    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "processamento", orphanRemoval=true)
    public List<ArquivoBeta> betas;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processamento")
    public List<Resultado> resultados;


    public Ocorrencia maiorPorPosicao(Integer posicao){
        Ocorrencia maior = null;
        Integer valor = 0;
        for (ArquivoBeta beta : betas){
            if(posicao + 1 > beta.ocorrencias.size()) return null;

            if(beta.ocorrencias.get(posicao).r > valor){
                valor = beta.ocorrencias.get(posicao).r;
                maior = beta.ocorrencias.get(posicao);
            }
        }

        return maior;
    }
}
