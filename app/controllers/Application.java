package controllers;

import play.*;
import play.mvc.*;
//import play.data.Upload;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
//import java.io.FileInputStream;

//import models.*;


public class Application extends Controller {

    public static final String sequenciaParecida =  Play.configuration.getProperty("sequencia.parecida");
    public static final String sequenciaDiferente = Play.configuration.getProperty("sequencia.diferente");

    public static final String localEntrada = Play.configuration.getProperty("diretorio.entrada");
    public static final String localSaida = Play.configuration.getProperty("diretorio.saida");

    public static List<Resultado> re;

    public static void index() {
        re = null;
        render();
    }

    public static void erro(){
        validation.keep();
        render();
    }

    public static void result(Parametros parametro, Arquivo alfa, Arquivo beta, Integer i, String programa){
        re = Resultado.computaResultado(localSaida + "/out");
        Integer j = re.get(0).j;
        Ocorrencia ocr = new Ocorrencia(i, i + re.size(), re.size());
        Integer k = parametro.k;

        Resultado maior = null;
        Resultado menor = null;

        if(parametro.maiorMenor){
            maior = re.get(0);
            menor = re.get(0);
           for(Resultado rr: re){
             if(rr.tamanho > maior.tamanho) maior = rr;
             if(rr.tamanho < menor.tamanho) menor = rr;
           }
        }

        render(alfa, beta, programa, ocr, k, j, maior, menor);
    }

    public static void process(Parametros parametro){
        if(validation.hasErrors()) {
            validation.valid(parametro);
            erro();
        } else {
            Boolean fasta = (ConvertFASTA2txt.converter(parametro.alfa, localEntrada + parametro.alfa.getName()));
            Arquivo alfa = ValidCharDNA.validar(localEntrada + parametro.alfa.getName());
            alfa.fasta = fasta;

            fasta = (ConvertFASTA2txt.converter(parametro.beta.get(0), localEntrada + parametro.beta.get(0).getName()));
            Arquivo beta = ValidCharDNA.validar(localEntrada + parametro.beta.get(0).getName());
            beta.fasta = fasta;

            if (alfa.quantidadeCaracteres < parametro.k) {

            }

            if (!parametro.tipoProcessamento) {
                if (parametro.j == parametro.distancia) { //se o j for igual a distancia esta errado
                     //validation.error("Erro", "O valor de j não pode ser igual à distância!");
                }
                if (parametro.j > alfa.quantidadeCaracteres) { //se
                     //validation.error("Erro", "O valor de j deve estar no intervalo entre 0 e o tamanho de alfa!");
                }

            }

            String commandLine;
            String programa;
            if (parametro.tipoKdiference == 1){
                commandLine = sequenciaDiferente;
                programa = "k-DifferencePrimer 1 -vs1";
            }
            else{
                commandLine = sequenciaParecida;
                programa = "k-DifferencePrimer 2 -vs1";
            }

            commandLine += " -a " + alfa.nome;
            commandLine += " -b " + beta.nome;
            commandLine += " -k " + parametro.k;
            commandLine += " -sf " + localSaida + "/out";
            Integer i = 0;

            if (!parametro.tipoProcessamento) {
                commandLine += " -j " + parametro.j + " " + parametro.distancia;
                i = parametro.j;
            }

            //
            //System.out.println(commandLine);

            if (validation.hasErrors()) {
                validation.keep();
                erro();
            } else {

                execCommand(commandLine);
                result(parametro, alfa, beta, i, programa);
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
        Resultado r = re.get(inicio);
        render(r);
    }

}