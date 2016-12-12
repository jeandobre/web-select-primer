package models;

import play.Play;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by jeandobre on 12/12/2016.
 */

@Entity
@Table(name="view_processamentos", schema="public")
public class ViewProcessamento extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    public Long id;

    @Column(name="alfa_nome")
    public String alfaNome;

    @Column(name="alfa_tamanho")
    public Integer alfaTamanho;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_sequencia")
    public TipoSequencia tipoSequencia;

    @Column(name = "quantidade_diferencas")
    public Integer quantidadeDiferencas;

    @Column
    public String nome;

    @Column
    public String informacao;

    @Column(name = "ocorrencias")
    public Integer totalResultado;

    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "processamento", orphanRemoval=true)
    @OrderBy("posicao")
    public List<ArquivoBeta> betas;

    public static List<ViewProcessamento> listaProcessamentosSalvos(String busca, Integer pagina){
        final Integer linhas = Integer.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        return ViewProcessamento.find("(alfaNome like :busca " +
                " OR nome like :busca OR informacao like :busca) " +
                " ORDER BY id DESC")
                .setParameter("busca","%" + busca + "%")
                .fetch(pagina, linhas);
    }
}
