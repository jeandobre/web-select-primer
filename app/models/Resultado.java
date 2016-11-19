package models;

import play.db.jpa.GenericModel;
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

    public static Resultado getResultadoPorProcessamento(Long processamentoId, Integer j){
        return find("processamento.id = :processamentoId and ocorrencia.j = :j")
                .setParameter("processamentoId", processamentoId)
                .setParameter("j", j).first();
    }

    public static List<Resultado> todosResultadosPorProcessamentoOrdem(Long processamentoId){
        return find("processamento.id = :processamentoId ORDER BY ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
    }

    public static List<Resultado> todosResultadosMaiores(Long processamentoId){
        return find("processamento.id = :processamentoId AND maior.id > 0 ORDER BY ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
    }

    public static List<Resultado> todosResultadosMenores(Long processamentoId){
        return find("processamento.id = :processamentoId AND menor.id > 0 ORDER BY ocorrencia.j ASC")
                .setParameter("processamentoId", processamentoId)
                .fetch();
    }

}
