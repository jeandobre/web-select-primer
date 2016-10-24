package controllers;

/**
 * Created by jeandobre on 20/10/16.
 */
public class Ocorrencia {
    public Integer inicio;
    public Integer fim;
    public Integer quantidade;

    public Ocorrencia(Integer inicio, Integer fim, Integer quantidade) {
        this.inicio = inicio;
        this.fim = fim;
        this.quantidade = quantidade;
    }
}
