package controllers;

import java.util.List;

/**
 * Created by jeandobre on 29/10/16.
 */
public class ArquivoBeta extends Arquivo {

    public Ocorrencia ocr;
    public CandidatoPrimer maior;
    public CandidatoPrimer menor;

    public List<CandidatoPrimer> candidatos;

    public void setMaiorMenorPorBeta(){
        maior = candidatos.get(0);
        menor = candidatos.get(0);
        for (CandidatoPrimer rr : candidatos) {
            if (rr.tamanho > maior.tamanho) maior = rr;
            if (rr.tamanho < menor.tamanho) menor = rr;
        }
    }
}
