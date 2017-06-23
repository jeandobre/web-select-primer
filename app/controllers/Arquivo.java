package controllers;

import java.util.List;

/**
 * Created by jeandobre on 14/10/2016.
 */
public class Arquivo {

    public String nome;
    public Integer quantidadeCaracteres;
    public Boolean fasta;
    public String sequencia;
    public String local;
	
    
    @Override
	public String toString() {
		return "Arquivo [nome=" + nome + ", quantidadeCaracteres=" + quantidadeCaracteres + ", fasta=" + fasta
				+ ", sequencia=" + sequencia + ", local=" + local + "]";
	}
    
    

}
