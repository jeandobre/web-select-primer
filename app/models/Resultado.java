package models;

import controllers.Application;
import play.Play;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Created by jeandobre on 27/10/16.
 */
@Entity
@Table(name="resultados", schema="public")
public class Resultado extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processamento_id", referencedColumnName="id")
    public Processamento processamento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", referencedColumnName="id")
    @OrderBy("j")
    public Ocorrencia ocorrencia;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "resultado", cascade = CascadeType.ALL)
    public Maior maior;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "resultado", cascade = CascadeType.ALL)
    public Menor menor;

    public Integer distancia;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distancia_ocorrencia_id", referencedColumnName="id")
    public Ocorrencia distanciaOcorrencia;

    public static Resultado getResultadoPorProcessamento(Long processamentoId, Integer j){
        return find("processamento.id = :processamentoId and ocorrencia.j = :j")
                .setParameter("processamentoId", processamentoId)
                .setParameter("j", j).first();
    }

    public static List<Resultado> todosResultadosPorProcessamentoOrdem(Long processamentoId){
        return  find("SELECT r FROM Resultado r JOIN FETCH r.ocorrencia " +
                "WHERE r.processamento.id = :processamentoId ORDER BY r.ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
              /*  JPA.em().createNamedQuery("SELECT r FROM Resultado r JOIN FETCH r.ocorrencia " +
                        "WHERE r.processamento.id = :processamentoId ORDER BY r.ocorrencia.j ASC")
                        .setParameter("processamentoId", processamentoId)
                .getResultList(); */
    }

    public static List<Resultado> todosResultadosPorProcessamentoOrdem(Long processamentoId, Integer pagina){
        final Integer linhas = Integer.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        return find("processamento.id = :processamentoId ORDER BY ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch(pagina, linhas);
    }

    public static int paginasResultadosPorProcessamento(Long processamentoId){
        final double linhas = Double.valueOf(Play.configuration.getProperty("linhas-por-pagina"));

        Query q1 = em().createQuery("SELECT COUNT(r) FROM Resultado r WHERE r.processamento.id = :processamentoId");
        Long total = (Long) q1.setParameter("processamentoId", processamentoId).getSingleResult();

        return (int) Math.ceil( total / linhas );
    }

    public static List<Resultado> todosResultadosMaiores(Long processamentoId){
        return find("SELECT r FROM Resultado r JOIN FETCH r.ocorrencia " +
                "WHERE r.processamento.id = :processamentoId AND r.maior.id > 0 ORDER BY r.ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
    }

    public static List<Resultado> todosResultadosMenores(Long processamentoId){
        return find("SELECT r FROM Resultado r JOIN FETCH r.ocorrencia " +
                "WHERE r.processamento.id = :processamentoId AND r.menor.id > 0 ORDER BY r.ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
    }
}
