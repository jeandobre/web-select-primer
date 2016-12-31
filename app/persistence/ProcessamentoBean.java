package persistence;

import controllers.Arquivo;
import controllers.CandidatoPrimer;
import controllers.Parametros;
import models.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import play.db.jpa.JPA;
import play.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
            commandLine += " -j " + parametro.jInicio + " " + (parametro.jFim - parametro.jInicio);
            i = parametro.jInicio;
        }

        Processamento processamento = new Processamento();
        processamento.inicio = calendar.getTime();
        processamento.alfaNome = alfa.nome;
        processamento.alfaArquivo = alfa.local;
        processamento.alfaTamanho = alfa.quantidadeCaracteres;
        processamento.tipoSequencia = tipoSequencia;
        processamento.mostrarMaiorMenor = parametro.maiorMenor;
        processamento.mostrarDistancia = parametro.mostrarDistancia;
        processamento.mostrarLimiteCaracteres = parametro.mostrarLimiteCaracteres;
        processamento.jInicio = parametro.jInicio;
        processamento.jFim = parametro.jFim;
        processamento.distancia = parametro.distancia;
        processamento.limiteCaracteres = parametro.limiteCaracteres;
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
            System.out.println(commandLine + argumento);
            comando += commandLine + argumento + ";";
            execCommand(commandLine + argumento);
            saida++;

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
                if(parametro.tipoProcessamento) ocr.j = cp.j + 1; //para não iniciar em zero
                else ocr.j = cp.j;
                ocr.r = cp.tamanho;
                ocr.segmento = cp.sequencia;
                ocr.letra = String.valueOf(cp.sequencia.charAt(0));
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

        //por ultimo, vamos agora fazer os filtros
        List<Resultado> remover = new ArrayList<Resultado>();
        if(processamento.mostrarLimiteCaracteres){
            for(Resultado re: processamento.resultados){
                if(re.ocorrencia.segmento.length() > processamento.limiteCaracteres)
                    remover.add(re);
                //removemos todas as ocorrências que tiveram o tamanho maior que o limite fixado
            }
            processamento.resultados.removeAll(remover);
        }

        remover.clear();
        if(processamento.mostrarDistancia){
            for(Resultado re: processamento.resultados) {
                if(re.distancia != null && re.distancia > 0) continue;
                Integer vv = re.ocorrencia.j;
                Resultado dRe = processamento.buscarResultadoPorPosicaoJ(vv + processamento.distancia);
                if (dRe == null){
                    remover.add(re);
                } else{
                    dRe.distancia = vv;
                    re.distancia = vv + processamento.distancia;
                }
            }
            processamento.resultados.removeAll(remover);
        }

        //encontrar o maior e o menor
        //primeiro tenho que encontrar o valor maior e menor
        processamento.maiorTamanho = 0;
        processamento.menorTamanho = processamento.alfaTamanho + 1; //pois não haverá nenhum maior que este tamanho
        Integer posicaoTela = 1;
        for (Resultado re: processamento.resultados){
            if(re.ocorrencia.r > processamento.maiorTamanho) processamento.maiorTamanho = re.ocorrencia.r;
            if(re.ocorrencia.r < processamento.menorTamanho) processamento.menorTamanho = re.ocorrencia.r;
            if(re.ocorrencia.j != posicaoTela + 1){
                if(posicaoTela == 1) //isso é necessário por conta dos pixels na tela
                  re.ocorrencia.posicaoTela = re.ocorrencia.j - posicaoTela;
                else
                    re.ocorrencia.posicaoTela = re.ocorrencia.j - posicaoTela - 1;
                posicaoTela = re.ocorrencia.j;
            } else posicaoTela++;
            //se o valor for de 1 em 1 não guardo a posicao da tela,
        }

        //depois encontramos todos os resultados maiores e menores;
        if(processamento.mostrarMaiorMenor) {
            for (Resultado re : processamento.resultados) {
                if (re.ocorrencia.r == processamento.maiorTamanho) {
                    Maior big = new Maior();
                    big.resultado = re;
                    re.maior = big;
                }
                if (re.ocorrencia.r == processamento.menorTamanho) {
                    Menor small = new Menor();
                    small.resultado = re;
                    re.menor = small;
                }
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

    public static String randomIdentifier() {
        final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
        final java.util.Random rand = new java.util.Random();
        final Set<String> identifiers = new HashSet<String>();

        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = rand.nextInt(5)+5;
            for(int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if(identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

    public static Arquivo uploadText(String sequencia){
        final String localEntrada = Configuracao.getValor("diretorio.entrada");

        Arquivo arquivo = new Arquivo();
        arquivo.nome = randomIdentifier();
        arquivo.local = localEntrada + "/" + arquivo.nome;
        arquivo.quantidadeCaracteres = sequencia.length();
        arquivo.fasta = false;
        arquivo.sequencia = sequencia;

        try{
            PrintWriter writer = new PrintWriter(arquivo.local, "UTF-8");
            writer.println(sequencia);
            writer.close();
        } catch (IOException e) {
            // do something
        }
        return arquivo;
    }


    public static void computarResultado(){

        //String local = "C:/Users/jeandobre/Documents/Mestrado/Bioinformática/dadosQualificados/";
        String local = "C:/Users/jeandobre/Documents/Mestrado/Bioinformática/ResultadoServidor/";
        List<Integer> tamanhos = new ArrayList<Integer>();
      /*  tamanhos.add(1000);
        tamanhos.add(2000);
        tamanhos.add(3000);
        tamanhos.add(4000);
        tamanhos.add(5000);
        tamanhos.add(6000);
        tamanhos.add(7000);
        tamanhos.add(8000);
        tamanhos.add(9000);
        tamanhos.add(10000);
        tamanhos.add(20000);
        tamanhos.add(30000);
        tamanhos.add(40000);
        tamanhos.add(50000);
        tamanhos.add(60000);
        tamanhos.add(70000);
        tamanhos.add(80000);
        tamanhos.add(90000);
        tamanhos.add(100000); */

       // tamanhos.add(4398);
        tamanhos.add(4455);
        tamanhos.add(5457);
        tamanhos.add(8694);
        tamanhos.add(14972);
        tamanhos.add(16877);
        tamanhos.add(38530);
        tamanhos.add(51655);
        tamanhos.add(66302);
      //  tamanhos.add(90284);

        Integer k = 30;
        List<Integer> lista;

        String file = "";
        //for(Integer i : tamanhos) {
         Integer i = 16877;
            for (Integer j : tamanhos) {

                file = "a" + i + "_b" + j + "_k" + k + "_K1v1";
                try {
                    Computacao cmp = new Computacao();
                    cmp.k = k;
                    cmp.alfa = i;
                    cmp.beta = j;

                    BufferedReader br = new BufferedReader(new FileReader(local + file));
                    lista = new ArrayList<Integer>();
                    int count = 0;
                    while (br.ready()) {
                        String linha = br.readLine();
                        if (count == 0) {
                            count++;
                            continue;
                        }

                        //System.out.println(linha);
                        String gg[] = linha.split(";");
                        //System.out.println(gg[0] + " - " + gg[1]);
                        lista.add(Integer.valueOf(gg[1]));
                    }
                    br.close();

                    //encontrar o maior e menor e média
                    int maior = -1, menor = 10000000;
                    float soma = 0;
                    for (Integer ii : lista) {
                        if (ii.intValue() > maior) maior = ii;
                        if (ii.intValue() < menor) menor = ii;
                        soma += ii;
                    }
                    cmp.maior = maior;
                    cmp.menor = menor;
                    cmp.media = soma / lista.size();
                    cmp.ocr = lista.size();
                    cmp.ruim = ((cmp.maior.intValue() == cmp.menor.intValue()) && (cmp.menor.intValue() == cmp.media));
                    cmp.save();

                    // return True;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        //}
    }

    public static void computarResultado2(){

        //String local = "C:/Users/jeandobre/Documents/Mestrado/Bioinformática/dadosQualificados/";
        String local = "C:/Users/jeandobre/Documents/Mestrado/Bioinformática/ResultadoServidor/";

        Integer k = 30;
        List<Computacao> tamanhos = Computacao.todosRuim(30);
        List<Integer> lista;

        String file = "";
        for(Computacao  cmp : tamanhos) {
            Integer i = cmp.alfa;
            Integer j = cmp.beta;
            file = "a" + i + "_b" + j + "_k" + k + "_K1v1";
            try {
                BufferedReader br = new BufferedReader(new FileReader(local + file));
                lista = new ArrayList<Integer>();
                int count = 0;
                while (br.ready()) {
                    String linha = br.readLine();
                    if (count == 0) {
                        count++;
                        continue;
                    }

                    //System.out.println(linha);
                    String gg[] = linha.split(";");
                    //System.out.println(gg[0] + " - " + gg[1]);
                    lista.add(Integer.valueOf(gg[1]) - Integer.valueOf(gg[0]));
                }
                br.close();

                //encontrar o maior e menor e média
                int maior = -1, menor = 10000000;
                float soma = 0;
                for (Integer ii : lista) {
                    if (ii.intValue() > maior) maior = ii;
                    if (ii.intValue() < menor) menor = ii;
                    soma += ii;
                }
                cmp.maior = maior;
                cmp.menor = menor;
                cmp.media = soma / lista.size();
                cmp.ocr = lista.size();
                cmp.ruim = ((cmp.maior.intValue() == cmp.menor.intValue()) && (cmp.menor.intValue() == cmp.media));
                cmp.save();

                // return True;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }

}
