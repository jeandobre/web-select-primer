package controllers;

import models.ArquivoBeta;
import models.Resultado;
import play.*;
import play.mvc.*;
//import play.data.Upload;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import play.data.validation.Valid;
import play.cache.Cache;

//import java.io.FileInputStream;

//import models.*;


public class Application extends Controller {

    public static final String sequenciaParecida =  Play.configuration.getProperty("sequencia.parecida");
    public static final String sequenciaDiferente = Play.configuration.getProperty("sequencia.diferente");
    public static final String heuristica = Play.configuration.getProperty("sequencia.heuristica");

    public static final String localEntrada = Play.configuration.getProperty("diretorio.entrada");
    public static final String localSaida = Play.configuration.getProperty("diretorio.saida");

    public static List<CandidatoPrimer> re;

    public static void index() {
        Cache.clear();
        render();
    }

    public static void erro(){
        validation.keep();
        render();
    }

    public static void result(){

        Resultado resultado = (Resultado) Cache.get( session.getId() );

        for(ArquivoBeta beta: resultado.betas){
            beta.candidatos = CandidatoPrimer.computaResultado(beta.local);
            Integer i = beta.candidatos.get(0).j;
            Integer tamanho = beta.candidatos.size();
            beta.ocr = new Ocorrencia(i, i + tamanho, tamanho);
            beta.setMaiorMenorPorBeta();
        }

        //TODO agora aki gero a lista de resultados
        re = new ArrayList<CandidatoPrimer>();
        Boolean temLinha = true;
        Integer linha = 0;
        while(temLinha){
            CandidatoPrimer cp = resultado.maiorPorLinha(linha);
            if(cp != null){
                re.add(cp);
                linha++;
            } else {
                temLinha = false;
            }
        }
        resultado.candidatos = re;
        Integer i = re.get(0).j;
        Integer tamanho = re.size();
        resultado.ocr = new Ocorrencia(i, i + tamanho, tamanho);
        resultado.j = i;
        resultado.setMaiorMenor();

        render(resultado);
    }

    public static void process(@Valid Parametros parametro){

        if(validation.hasErrors()) {
            validation.keep();
            erro();
        } else {
            Boolean fasta = (ConvertFASTA2txt.converter(parametro.alfa, localEntrada + "/" + parametro.alfa.getName()));
            Arquivo alfa = ValidCharDNA.validar(localEntrada + "/" + parametro.alfa.getName());
            alfa.fasta = fasta;
            List<ArquivoBeta> betas = new ArrayList<ArquivoBeta>();

            if( parametro.beta == null ){
                validation.addError("Erro", "É necessário adicionar uma ou mais sequências para o processamento!");
            } else {
                for(File f: parametro.beta) {
                    System.out.println(f.getName());
                    fasta = (ConvertFASTA2txt.converter(f, localEntrada + "/" + f.getName()));
                    Arquivo bt = ValidCharDNA.validar(localEntrada + "/" + f.getName());
                    ArquivoBeta beta = new ArquivoBeta();
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

                String commandLine;
                String programa;
                if (parametro.tipoKdiference == 1){
                    commandLine = sequenciaDiferente;
                    programa = "k-DifferencePrimer 1 -vs1";
                } else if (parametro.tipoKdiference == 2){
                    commandLine = sequenciaParecida;
                    programa = "k-DifferencePrimer 2 -vs1";
                } else{
                    commandLine = heuristica;
                    programa = "k-DifferencePrimer heuristica";
                }

                commandLine += " -a " + alfa.nome;
                commandLine += " -k " + parametro.k;
                Integer i = 0;

                if (!parametro.tipoProcessamento) {
                    commandLine += " -j " + parametro.j + " " + parametro.distancia;
                    i = parametro.j;
                }

                Integer saida = 1;
                for(Arquivo beta: betas){
                    beta.local = localSaida + "/out_" + session.getId() + "_" + saida;
                    String argumento = " -b " + beta.nome + " -sf " + beta.local;
                    //System.out.println(commandLine + argumento);

                    execCommand(commandLine + argumento);
                    saida++;
                }

                Resultado resultado = new Resultado(alfa, betas, i, programa);
                resultado.k = parametro.k;
                resultado.j = parametro.j;
                resultado.maiorMenor = parametro.maiorMenor;

                Cache.add(session.getId(), resultado);

                result();
            }
        }
    }

    private synchronized static void execCommand(final String commandLine) {
        Process exec;
        try{
           exec = Runtime.getRuntime().exec(commandLine);
           if(exec.waitFor() != 0){
               //TODO trown exception de execução
           }
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void posicao(int j){
        Integer inicio = re.get(0).j;
        inicio = j - inicio;
        CandidatoPrimer r = re.get(inicio);
        render(r);
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