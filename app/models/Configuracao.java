package models;

import play.Play;
import play.db.jpa.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by jeandobre on 07/11/2016.
 */
@Entity
@Table(name = "configuracoes", schema = "public")
public class Configuracao extends GenericModel {

    @Id
    @Column
    public String key;

    @Column
    public String valor;

    public static String getValor(String chave){
        String so = Play.configuration.getProperty("so");
        chave += "." + so;
       return ((Configuracao) find("key = :chave").setParameter("chave", chave).first()).valor;
    }

    public static List<Configuracao> getLista(){
        return find("order by key").fetch();
    }
}
