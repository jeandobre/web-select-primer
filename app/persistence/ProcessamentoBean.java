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
 * Updated by jeandobre on 19/06/2017
 */
public class ProcessamentoBean {

    private EntityManager em = JPA.em();
    //private EntityTransaction tx = JPA.em().getTransaction();

    public static final String sequenciaParecida =  Configuracao.getValor("sequencia.parecida");
    public static final String sequenciaDiferente = Configuracao.getValor("sequencia.diferente");

    public static final String localSaida =         Configuracao.getValor("diretorio.saida");

    public static Long processamento(Parametros parametro, Arquivo alfa, List<Arquivo> betas){
        String getId = "";
        Calendar calendar = Calendar.getInstance();
        if(parametro.tipoSequencia == TipoSequencia.PARECIDAS) getId = sequenciaParecida;
        else if(parametro.tipoSequencia == TipoSequencia.DIFERENTES) getId = sequenciaDiferente;
        

        Programa programa = Programa.findById(Long.valueOf( getId ) );
        TipoSequencia tipoSequencia = programa.tipoSequencia;

        String commandLine = programa.local + " " + programa.parametros;
        commandLine += " -a " + alfa.local;
        commandLine += " -k " + parametro.k;
        Integer i = 0;

        
        commandLine += " -j " + (parametro.jInicio - 1) + " " + (parametro.jFim - parametro.jInicio - 1);
        i = parametro.jInicio;
        
        Processamento processamento = new Processamento();
        processamento.inicio = calendar.getTime();
        processamento.alfaNome = alfa.nome;
        processamento.alfaArquivo = alfa.local;
        processamento.alfaTamanho = alfa.quantidadeCaracteres;
        processamento.tipoSequencia = tipoSequencia;
        //processamento.mostrarMaiorMenor = parametro.maiorMenor;
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
            List<CandidatoPrimer> candidatos = CandidatoPrimer.computaResultado(beta.arquivoResultado, alfa.sequencia);
            for (CandidatoPrimer cp : candidatos) {
                Ocorrencia ocr = new Ocorrencia();
                ocr.beta = beta;
                ocr.j = cp.j;
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
    
   // public static String carregarSequencia(String file){
    	/*String seq = "";
    	try{
    		file = "/home/jean/k-difference-prime/dados/" + file; 
            BufferedReader br = new BufferedReader(new FileReader(file));
           
            while(br.ready()){
                seq = br.readLine();                
            }
            br.close();
            // return True;
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    	return seq;
    }
    
    public static void computarLCE(){
    	//int tam[] = {1000, 2000, 3000,4000,5000,6000,7000,8000,9000,
    	//		10000,20000,30000,40000,50000,60000, 70000, 80000, 90000, 100000};
    	
    //	int files[] = {1000, 2000, 3000,4000,5000,6000,7000,8000,9000,
    	//    			10000,20000,30000,40000,50000,60000, 70000, 80000, 90000, 100000};
    	int files[] = {100000};
    	
    	//String files[] = {"hsarhgdig","hsascl2","hsankrd43","hsa1bg","hsalg10b",
    	//		"hsbad","hsankr10","hsasz1","hsaff4"};
    /*	
    	String files[][] = {{"hoxa4hs","hoxa4mm"},{"hoxa4hs","hoxa4pt"},{"hoxa4hs","hoxa4ph"},
    			            {"hoxa4hs","hoxa4pa"},{"hoxa4hs","hoxa4cf"},{"hoxa4mm","hoxa4hs"},
    			            {"hoxa4mm","hoxa4pt"},{"hoxa4mm","hoxa4ph"},{"hoxa4mm","hoxa4pa"},
    			            {"hoxa4mm","hoxa4cf"},{"hoxa4pt","hoxa4mm"},{"hoxa4pt","hoxa4cf"},
    			            {"hoxa4pt","hoxa4pa"},{"hoxa4pt","hoxa4hs"},{"hoxa4pt","hoxa4ph"},
    			            {"hoxa4ph","hoxa4pa"},{"hoxa4ph","hoxa4cf"},{"hoxa4ph","hoxa4mm"},
    			            {"hoxa4ph","hoxa4hs"},{"hoxa4ph","hoxa4pt"},{"hoxa4pa","hoxa4mm"},
    			            {"hoxa4pa","hoxa4hs"},{"hoxa4pa","hoxa4cf"},{"hoxa4pa","hoxa4pt"},
    			            {"hoxa4pa","hoxa4ph"},{"hoxa4cf","hoxa4mm"},{"hoxa4cf","hoxa4hs"},
    			            {"hoxa4cf","hoxa4pa"},{"hoxa4cf","hoxa4pt"},{"hoxa4cf","hoxa4ph"},
    			            {"ctsdhs","ctsdpt"},{"ctsdpt","ctsdhs"},{"ddx18hs","ddx18pt"},
    			            {"ddx18pt","ddx18hs"},{"lrrc4hs","lrrc4mf"},{"lrrc4hs","lrrc4mm"},
    			            {"lrrc4hs","lrrc4bbb"},{"lrrc4hs","lrrc4pa"},{"lrrc4mf","lrrc4hs"},
    			            {"lrrc4mf","lrrc4mm"},{"lrrc4mf","lrrc4bbb"},{"lrrc4mf","lrrc4pa"},
    			            {"lrrc4mm","lrrc4hs"},{"lrrc4mm","lrrc4mf"},{"lrrc4mm","lrrc4bbb"},
    			            {"lrrc4mm","lrrc4pa"},{"lrrc4bbb","lrrc4hs"},{"lrrc4bbb","lrrc4mm"},
    			            {"lrrc4bbb","lrrc4mf"},{"lrrc4bbb","lrrc4pa"},{"lrrc4pa","lrrc4hs"},
    			            {"lrrc4pa","lrrc4mm"},{"lrrc4pa","lrrc4mf"},{"lrrc4pa","lrrc4bbb"},
    			            {"kcnk4hs1","kcnk4ggg"},{"kcnk4hs1","kcnk4pt"},{"kcnk4hs1","kcnk4pa"},
    			            {"kcnk4ggg","kcnk4hs1"},{"kcnk4ggg","kcnk4pt"},{"kcnk4ggg","kcnk4pa"},
    			            {"kcnk4pt","kcnk4hs1"},{"kcnk4pt","kcnk4ggg"},{"kcnk4pt","kcnk4pa"},
    			            {"kcnk4pa","kcnk4hs1"},{"kcnk4pa","kcnk4ggg"},{"kcnk4pa","kcnk4pt"},
    			            {"dnajc4hs","dnajc4mm"},{"dnajc4mm","dnajc4hs"},{"dppa5hs","dppa5mm"},
    			            {"dppa5mm","dppa5hs"}}; 
    	
    	//String files[] = {"hsankr10","hsasz1","hsaff4"};
    	
    	for(int w = 0; w < files.length; w++){
    		//for(int v = 0; v < files.length; v++){
    			
    			//if(w == v) continue;
    			
	    	    int tamanho = files[w];
	    	    String file = "";
	    	    if(tamanho < 10000) file +="0";
	    	    file += String.valueOf(tamanho) + ".txt";
	    	    //System.out.println(file);
	    	    
	    	    String alfa = carregarSequencia("alfa_" + file);
	        	String beta = carregarSequencia("beta_" + file);

	        	double max, media_s, media_c;
	        	double soma;
	        	long tt;
	        	//System.out.println(alfa);
	        	//System.out.println(beta);
	        	int m = alfa.length();
	        	int n = beta.length();
	        	
	        	soma = 0;
	        	max = 0;
	        	long inc = 0;
	        	
		    	for(int i = 0; i < m; i++){
		    		for(int j = 0; j < n; j++){
		    			int row = 0;
		    			while(i+row < m && j+row < n && alfa.charAt(i+row)==beta.charAt(j+row))row++;

		    			if(row > max) max = row;
		    			if(row > 0) inc++;
		    			soma += row;
		    		}
		    	}
		    	tt = (m*n);
		    			    	
		    	System.out.println("Soma: " + soma + ", inc:" + inc + ", tt: " + tt);
		    	
		    	media_c = (soma / (tt < 0 ? tt * -1 : tt));
		    	media_s = (soma / inc);
		    	System.out.print(m + ";");
		    	System.out.print(n + ";");
		    	System.out.println(max + ";" + media_c + ";" +  media_s + ";");
	    	//}
    	}*/
    //}
    
    public static void computarLCEunico(){
    	
    	/*		String alfa = "CGTGTCGTACGTGCACGTGA";
    			String beta = "CCCGGCCCGTCCA";
    			
    			String alfa1 = "CCCGGCCCGTCCA";
    			String beta1 = "CGTGTCGTACGTGCACGTGA";

	        	int m = alfa.length();
	        	int n = beta.length();
	        		        	
	        	System.out.println("==============================");
		    	for(int i = 0; i < m; i++){
		    		for(int j = 0; j < n; j++){
		    			int row1 = 0;
		    			while(i+row1 < m && j+row1 < n && alfa.charAt(i+row1)==beta.charAt(j+row1))row1++;
		    			
		    			int row2 = 0;
		    			while(i+row2 < m && j+row2 < n && alfa1.charAt(j+row2)==beta1.charAt(i+row2))row2++;
		    			
		    			if(row1 != row2)
		    			  System.out.println("LCE(" + i + "," + j + ")=" + row1 + ";" + "LCE(" + j + "," + i + ")=" + row2 + ";");	
		    		}
		    	}
		    	
		    	
    			/*
		    	
		    	System.out.println("==============================");
		    	for(int i = 0; i < m; i++){
		    		for(int j = 0; j < n; j++){
		    			int row = 0;
		    			while(i+row < m && j+row < n && alfa.charAt(i+row)==beta.charAt(j+row))row++;
		    			System.out.println("LCE(" + i + "," + j + ")=" + row + ";");	
		    		}
		    	}*/
		    	
    }

}
