package models;

import play.Play;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by jeandobre on 20/10/16.
 */
@Entity
@Table(name="ocorrencias", schema="public")
public class Ocorrencia extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beta_id")
    public ArquivoBeta beta;

    @Column
    public Integer j;

    @Column
    public Integer r;

    @Column
    public String segmento;

    @Column(name = "posicao_tela")
    public Integer posicaoTela;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ocorrencia")
    public Resultado resultado;

    public static List<Ocorrencia>  todasOcorrenciasPorBetaOrdem(Long betaId, Integer pagina){
        final Integer linhas = Integer.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        return find("beta.id = :betaId ORDER BY j ASC")
                .setParameter("betaId", betaId)
                .fetch(pagina, linhas);
    }

    public static int paginasOcorrenciasPorBeta(Long betaId){
        final double linhas = Double.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        Query q1 = em().createQuery("SELECT COUNT(o) FROM Ocorrencia o WHERE o.beta.id = :betaId");
        Long total = (Long) q1.setParameter("betaId", betaId).getSingleResult();

        return (int) Math.ceil( total / linhas );
    }
}
