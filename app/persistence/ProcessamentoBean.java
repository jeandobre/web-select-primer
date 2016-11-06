package persistence;

import controllers.Arquivo;
import controllers.CandidatoPrimer;
import controllers.Parametros;
import models.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import play.db.jpa.JPA;
import play.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jeandobre on 05/11/2016.
 */
public class ProcessamentoBean {

    private EntityManager em = JPA.em();
    //private EntityTransaction tx = JPA.em().getTransaction();

    public static final String sequenciaParecida =  Play.configuration.getProperty("sequencia.parecida");
    public static final String sequenciaDiferente = Play.configuration.getProperty("sequencia.diferente");
    public static final String heuristica = Play.configuration.getProperty("sequencia.heuristica");

    public static final String localSaida = Play.configuration.getProperty("diretorio.saida");

    public static Long processamento(Parametros parametro, Arquivo alfa, List<Arquivo> betas){

        String commandLine;
        String programa;
        TipoSequencia tipoSequencia;
        if (parametro.tipoKdiference == 1){
            commandLine = sequenciaDiferente;
            programa = "k-DifferencePrimer 1 -vs1";
            tipoSequencia = TipoSequencia.DIFERENTES;
        } else if (parametro.tipoKdiference == 2){
            commandLine = sequenciaParecida;
            programa = "k-DifferencePrimer 2 -vs1";
            tipoSequencia = TipoSequencia.PARECIDAS;
        } else{
            commandLine = heuristica;
            programa = "k-DifferencePrimer heuristica";
            tipoSequencia = TipoSequencia.HEURISTICA;
        }

        commandLine += " -a " + alfa.nome;
        commandLine += " -k " + parametro.k;
        Integer i = 0;

        if (!parametro.tipoProcessamento) {
            commandLine += " -j " + parametro.j + " " + parametro.distancia;
            i = parametro.j;
        }

        Processamento processamento = new Processamento();
        processamento.inicio = new Date();
        processamento.alfaNome = alfa.nome;
        processamento.alfaArquivo = alfa.local;
        processamento.alfaTamanho = alfa.quantidadeCaracteres;
        processamento.tipoSequencia = tipoSequencia;
        processamento.mostrarMaiorMenor = parametro.maiorMenor;
        processamento.mostrarEntreMilDoisMil = parametro.regioesTamanhoFixo;
        processamento.posicao = parametro.j;
        processamento.distancia = parametro.distancia;
        processamento.betas = new ArrayList<ArquivoBeta>(betas.size());

        Integer saida = 1;
        String comando = "";
        for(Arquivo beta: betas){
            beta.local = localSaida + "/out_"; // + session.getId() + "_" + saida;
            String argumento = " -b " + beta.nome + " -sf " + beta.local;
            //System.out.println(commandLine + argumento);
            comando += commandLine + argumento + ";";
            execCommand(commandLine + argumento);
            saida++;

            ArquivoBeta b = new ArquivoBeta();
            b.nome = beta.nome;
            b.arquivo = beta.local;
            b.tamanho = beta.quantidadeCaracteres;
            b.processamento = processamento;
            processamento.betas.add(b);
        }
        processamento.fim = new Date();
        //processamento.tempoGasto = processamento.fim - processamento.inicio;
        processamento.processamento = comando;

        //recupera os resultados por beta;
        for(ArquivoBeta beta : processamento.betas){
            beta.ocorrencias = new ArrayList<Ocorrencia>();
            List<CandidatoPrimer> candidatos = CandidatoPrimer.computaResultado(beta.arquivo);
            for (CandidatoPrimer cp : candidatos) {
                Ocorrencia ocr = new Ocorrencia();
                ocr.beta = beta;
                ocr.j = cp.j;
                ocr.r = cp.tamanho;
                ocr.segmento = cp.sequencia;
                beta.ocorrencias.add(ocr);
            }
            candidatos = null;
        }

        //agora aki gero a lista de resultados
        processamento.resultados = new ArrayList<Resultado>();
        Boolean temLinha = true;
        Integer linha = 0;
        while(temLinha){
            Ocorrencia ocr = processamento.maiorPorPosicao(linha);
            if(ocr != null){
                Resultado result = new Resultado();
                result.ocorrencia = ocr;
                result.processamento = processamento;
                processamento.resultados.add(result);
                linha++;
            } else {
                temLinha = false;
            }
        }

        //encontrar o maior e o menor
        //primeiro tenho que encontrar o valor maior e menor
        Integer maior = 0;
        Integer menor = processamento.alfaTamanho + 1; //pois não haverá nenhum maior que este tamanho
        for (Resultado re: processamento.resultados){
            if(re.ocorrencia.r > maior) maior = re.ocorrencia.r;
            if(re.ocorrencia.r < menor) menor = re.ocorrencia.r;
        }
        //depois encontramos todos os resultados maiores e menores;
        for (Resultado re: processamento.resultados){
            if(re.ocorrencia.r == maior){
                Maior big = new Maior();
                big.resultado = re;
                big.save();
            }
            if(re.ocorrencia.r == menor){
                Menor small = new Menor();
                small.resultado = re;
                small.save();
            }
        }

        processamento.save();
        return processamento.getId();
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
}
