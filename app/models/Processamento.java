package models;

import play.Play;
import play.db.jpa.GenericModel;
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
public class Processamento extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    public Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    public Date inicio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    public Date fim;

    @Column(name="tempo_gasto_segundos")
    public Long tempoGasto;

    @Column(name="alfa_nome")
    public String alfaNome;

    @Column(name="alfa_arquivo")
    public String alfaArquivo;

    @Column(name="alfa_tamanho")
    public Integer alfaTamanho;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_sequencia")
    public TipoSequencia tipoSequencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id")
    public Programa programa;

   /* @Column(name="mostrar_maior_menor")
    public Boolean mostrarMaiorMenor; */

   /* @Column(name="mostrar_entre_mil_dois_mil")
    public Boolean mostrarEntreMilDoisMil; */

    @Column(name = "j_inicio")
    public Integer jInicio;

    @Column(name = "j_fim")
    public Integer jFim;

    @Column(name = "mostrar_distancia")
    public Boolean mostrarDistancia; 

    @Column
    public Integer distancia; 

    @Column(name="mostrar_limite_caracteres")
    public Boolean mostrarLimiteCaracteres; 

    @Column(name = "limite_caracteres")
    public Integer limiteCaracteres; 

    @Column
    public String processamento;

    @Column(name = "quantidade_diferencas")
    public Integer quantidadeDiferencas;

    @Column
    public String nome;

    @Column
    public String informacao;

    @Column(name = "maior_tamanho")
    public Integer maiorTamanho;

    @Column(name = "menor_tamanho")
    public Integer menorTamanho;

    @Column(nullable = false)
    public Boolean salvo = false;

    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "processamento", orphanRemoval=true)
    @OrderBy("posicao")
    public List<ArquivoBeta> betas;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processamento")
    @OrderBy("ocorrencia")
    public List<Resultado> resultados;

    /*public Integer getTotalResultado(){
        return resultados.size();
    }*/

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

    public static int paginasProcessamentos(String busca){
        final double linhas = Double.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        Query q1 = em().createQuery("SELECT COUNT(p) FROM Processamento p " +
                "WHERE (alfaNome like :busca OR alfaArquivo like :busca " +
                "OR nome like :busca OR informacao like :busca) " +
                "AND (salvo = true)");
        Long total = (Long) q1.setParameter("busca","%" + busca + "%").getSingleResult();

        return (int) Math.ceil( total / linhas );
    }

    public Ocorrencia buscarResultadoPorPosicaoJ(Integer j){

        for(Resultado re: resultados){    
            if(re.ocorrencia.j.intValue() == j.intValue()) return re.ocorrencia;            
        }
        return null;
    }
}
