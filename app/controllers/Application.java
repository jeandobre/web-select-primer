package controllers;

import models.Processamento;
import models.Resultado;
import persistence.ConvertFASTA2txt;
import persistence.ProcessamentoBean;
import persistence.ValidCharDNA;
import play.*;
import play.mvc.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import play.data.validation.Valid;
import play.cache.Cache;

public class Application extends Controller {

    public static final String localEntrada = Play.configuration.getProperty("diretorio.entrada");

    public static void index() {
        Cache.clear();
        render();
    }

    public static void erro(){
        validation.keep();
        render();
    }

    public static void result(Long id){

        Processamento processamento = Processamento.findById(id);

        //montar o componente
        Integer i = processamento.resultados.get(0).ocorrencia.j;
        Integer tamanho = processamento.resultados.size();
        ComponenteOcorrencia ocr = new ComponenteOcorrencia(i, i + tamanho, tamanho);

        render(processamento, ocr);
    }

    public static void process(@Valid Parametros parametro){

        if(validation.hasErrors()) {
            validation.keep();
            erro();
        } else {
            Boolean fasta = (ConvertFASTA2txt.converter(parametro.alfa, localEntrada + "/" + parametro.alfa.getName()));
            Arquivo alfa = ValidCharDNA.validar(localEntrada + "/" + parametro.alfa.getName());
            alfa.fasta = fasta;
            List<Arquivo> betas = new ArrayList<Arquivo>();

            if( parametro.beta == null ){
                validation.addError("Erro", "É necessário adicionar uma ou mais sequências para o processamento!");
            } else {
                for(File f: parametro.beta) {
                    System.out.println(f.getName());
                    fasta = (ConvertFASTA2txt.converter(f, localEntrada + "/" + f.getName()));
                    Arquivo bt = ValidCharDNA.validar(localEntrada + "/" + f.getName());
                    Arquivo beta = new Arquivo();
                    beta.nome = bt.nome;
                    beta.quantidadeCaracteres = bt.quantidadeCaracteres;
                    beta.sequencia = bt.sequencia;
                    beta.fasta = fasta;
                    betas.add( beta );

                    bt = null;
                }
            }

            if (alfa.quantidadeCaracteres < parametro.k) {
                validation.addError("Erro", "A quantidade de diferenças (k) não pode ser maior que a quantidade de caracteres da sequência alvo");
            }

            if (!parametro.tipoProcessamento) {
                if (parametro.j == parametro.distancia) { //se o j for igual a distancia esta errado
                     validation.addError("Erro", "O valor de j não pode ser igual à distância!");
                }
                if (parametro.j > alfa.quantidadeCaracteres) { //se
                     validation.addError("Erro", "O valor de j deve estar no intervalo entre 0 e a quantidade de caracteres da sequência alvo");
                }
            }

            if (validation.hasErrors()) {
                validation.keep();
                erro();
            } else {

                Long id = ProcessamentoBean.processamento(parametro, alfa, betas);
                result(id);
            }
        }
    }

    public static void posicao(int j){
        render(Resultado.findById(j));
    }

    public static void diagonal(){
        List<LCE> lista = new ArrayList<LCE>();
        String alfa = "CCCGGCCC";
        String beta = "CCCGTGCCC";

        for(Integer i = 0; i < alfa.length() - 1; i++){
            for(Integer j = 0; j < beta.length() - 1; j++){
                Integer lce = LCE.lce(alfa.toCharArray(), beta.toCharArray(), i, j);
                if(lce > 1){
                    lista.add(new LCE(i, j, lce));
                }
            }
        }

        render(lista);
    }

}