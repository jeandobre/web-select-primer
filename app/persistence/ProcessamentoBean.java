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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jeandobre on 05/11/2016.
 */
public class ProcessamentoBean {

    private EntityManager em = JPA.em();
    //private EntityTransaction tx = JPA.em().getTransaction();

    public static final String sequenciaParecida =  Configuracao.getValor("sequencia.parecida");
    public static final String sequenciaDiferente = Configuracao.getValor("sequencia.diferente");
    public static final String heuristica =         Configuracao.getValor("sequencia.heuristica");

    public static final String localSaida =         Configuracao.getValor("diretorio.saida");

    public static Long processamento(Parametros parametro, Arquivo alfa, List<Arquivo> betas){
        String getId = "";
        Calendar calendar = Calendar.getInstance();
        if(parametro.tipoSequencia == TipoSequencia.PARECIDAS) getId = sequenciaParecida;
        else if(parametro.tipoSequencia == TipoSequencia.DIFERENTES) getId = sequenciaDiferente;
        else getId = heuristica;

        Programa programa = Programa.findById(Long.valueOf( getId ) );
        TipoSequencia tipoSequencia = programa.tipoSequencia;

        String commandLine = programa.local + " " + programa.parametros;
        commandLine += " -a " + alfa.local;
        commandLine += " -k " + parametro.k;
        Integer i = 0;

        if (!parametro.tipoProcessamento) {
            commandLine += " -j " + parametro.j + " " + parametro.distancia;
            i = parametro.j;
        }

        Processamento processamento = new Processamento();
        processamento.inicio = calendar.getTime();
        processamento.alfaNome = alfa.nome;
        processamento.alfaArquivo = alfa.local;
        processamento.alfaTamanho = alfa.quantidadeCaracteres;
        processamento.tipoSequencia = tipoSequencia;
        processamento.mostrarMaiorMenor = parametro.maiorMenor;
        processamento.mostrarEntreMilDoisMil = parametro.regioesTamanhoFixo;
        processamento.posicao = parametro.j;
        processamento.distancia = parametro.distancia;
        processamento.betas = new ArrayList<ArquivoBeta>(betas.size());
        processamento.quantidadeDiferencas = parametro.k;
        processamento.programa = programa;

        Integer saida = 1;
        String comando = "";
        for(Arquivo beta: betas){
            ArquivoBeta b = new ArquivoBeta();
            b.nome = beta.nome;
            b.arquivo = beta.local;
            b.tamanho = beta.quantidadeCaracteres;
            b.processamento = processamento;
            b.posicao = saida;

            b.arquivoResultado = localSaida + "/out_" + saida;
            String argumento = " -b " + b.arquivo + " -sf " + b.arquivoResultado;
            //System.out.println(commandLine + argumento);
            comando += commandLine + argumento + ";";
            execCommand(commandLine + argumento);
            saida++;

            //b.arquivoResultado = beta.local;
            processamento.betas.add(b);
        }
        processamento.processamento = comando;

        //recupera os resultados por beta;
        for(ArquivoBeta beta : processamento.betas){
            beta.ocorrencias = new ArrayList<Ocorrencia>();
            List<CandidatoPrimer> candidatos = CandidatoPrimer.computaResultado(beta.arquivoResultado);
            for (CandidatoPrimer cp : candidatos) {
                Ocorrencia ocr = new Ocorrencia();
                ocr.beta = beta;
                ocr.j = cp.j + 1; //para não iniciar em zero
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
        processamento.maiorTamanho = 0;
        processamento.menorTamanho = processamento.alfaTamanho + 1; //pois não haverá nenhum maior que este tamanho
        for (Resultado re: processamento.resultados){
            if(re.ocorrencia.r > processamento.maiorTamanho) processamento.maiorTamanho = re.ocorrencia.r;
            if(re.ocorrencia.r < processamento.menorTamanho) processamento.menorTamanho = re.ocorrencia.r;
        }
        //depois encontramos todos os resultados maiores e menores;
        for (Resultado re: processamento.resultados){
            if(re.ocorrencia.r == processamento.maiorTamanho){
                Maior big = new Maior();
                big.resultado = re;
                re.maior = big;
            }
            if(re.ocorrencia.r == processamento.menorTamanho){
                Menor small = new Menor();
                small.resultado = re;
                re.menor = small;
            }
        }
        calendar = Calendar.getInstance();
        processamento.fim = calendar.getTime();
        long diff =  processamento.fim.getTime() - processamento.inicio.getTime();
        long diffSeconds = diff / 1000 % 60;
        processamento.tempoGasto = diffSeconds;
        processamento.save();
        return processamento.id;
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
