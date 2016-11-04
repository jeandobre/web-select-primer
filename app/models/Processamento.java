package models;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by jeandobre on 04/11/16.
 */
@Entity
@Table(name="processamentos", schema="public")
public class Processamento extends Model {

    @Id
    @GeneratedValue
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

    public Integer posicao;

    public Integer distancia;

    public String processamento;

    public String nome;

    public String informacao;
}
