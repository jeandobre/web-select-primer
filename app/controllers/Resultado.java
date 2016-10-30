package controllers;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jeandobre on 27/10/16.
 */
public class Resultado implements Serializable {

    Arquivo alfa;
    List<ArquivoBeta> betas;

    Integer k;
    Integer i;
    Integer j;

    String programa;

    Ocorrencia ocr;

    List<CandidatoPrimer> candidatos;

    CandidatoPrimer maior;
    CandidatoPrimer menor;

    Boolean maiorMenor;

    public Resultado(Arquivo alfa, List<ArquivoBeta> betas, Integer i, String programa) {
        this.alfa = alfa;
        this.betas = betas;
        this.i = i;
        this.programa = programa;
    }

    public CandidatoPrimer maiorPorLinha(Integer linha){
        Integer valor = 0;
        CandidatoPrimer maior = null;
        for (ArquivoBeta beta : betas){
            if(linha + 1 > beta.candidatos.size()) return null;

            if(beta.candidatos.get(linha).tamanho > valor){
                valor = beta.candidatos.get(linha).tamanho;
                maior = beta.candidatos.get(linha);
            }
        }

        return maior;
    }

    public void setMaiorMenor(){
        maior = candidatos.get(0);
        menor = candidatos.get(0);
        for (CandidatoPrimer rr : candidatos) {
            if (rr.tamanho > maior.tamanho) maior = rr;
            if (rr.tamanho < menor.tamanho) menor = rr;
        }
    }
}
