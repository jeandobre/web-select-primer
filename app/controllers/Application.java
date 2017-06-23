package controllers;

import models.*;
import persistence.ConvertFASTA2txt;
import persistence.ProcessamentoBean;
import persistence.ValidCharDNA;
import play.*;
import play.mvc.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Valid;
import play.cache.Cache;

import javax.xml.transform.Result;

public class Application extends Controller {
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
        if(processamento != null) {
            //montar o componente
            Integer i = 0;
            List<Resultado> resultados = Resultado.todosResultadosPorProcessamentoOrdem(id);
            Integer tamanho = resultados.size();
            if(tamanho > 0) i = resultados.get(0).ocorrencia.j;
            ComponenteOcorrencia ocr = new ComponenteOcorrencia(i, i + tamanho, tamanho);

            List<Resultado> maiores = Resultado.todosResultadosMaiores(id);
            List<Resultado> menores = Resultado.todosResultadosMenores(id);

            render(processamento, resultados, maiores, menores, ocr);
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um processamento no banco de dados com id " + id + "!");
            validation.keep();
            erro();
        }
    }

    public static void process(@Valid Parametros parametro){
        final String localEntrada = Configuracao.getValor("diretorio.entrada");
        Arquivo alfa = null;
        List<Arquivo> betas = new ArrayList<Arquivo>();
                
        //parametro.tipoSequencia = TipoSequencia.DIFERENTES;
        
        System.out.println(parametro.toString());
              
        if(validation.hasErrors()) {
            validation.keep();
            erro();
        } else {
            if(parametro.alfa == null) validation.required("parametro.alfa", parametro.textoAlfa);
            else validation.required("parametro.alfa", parametro.alfa);
            
            if(parametro.beta == null || parametro.beta.length == 0) 
            	validation.required("parametro.beta", parametro.textoBeta);
            else if (parametro.textoBeta == null)
                validation.addError("Erro", "É necessário adicionar uma ou mais sequências para o processamento!");

            
            if(parametro.mostrarDistancia){
            	validation.required("parametro.distancia", parametro.distancia);
            }
            
            if(parametro.mostrarLimiteCaracteres){
            	validation.required("parametro.mostrarLimiteCaracteres", parametro.mostrarLimiteCaracteres);
            }
         
            if(validation.hasErrors()) {
                validation.keep();
                erro();
            } else {
            	Integer tipoSequenciaAlfa = 0;
            	Integer tipoSequenciaBeta = 0;
            	            	          	
            	if(!StringUtils.isBlank(parametro.textoAlfa)) tipoSequenciaAlfa = 1;
            	if(!StringUtils.isBlank(parametro.textoBeta)) tipoSequenciaBeta = 1;
            	
                if (tipoSequenciaAlfa == 1) {
                    //entao é texto, gera o arquivo
                    alfa = ProcessamentoBean.uploadText(ConvertFASTA2txt.converter(parametro.textoAlfa));
                } else {
                    Boolean fasta = (ConvertFASTA2txt.converter(parametro.alfa, localEntrada + "/" + parametro.alfa.getName()));
                    alfa = ValidCharDNA.validar(localEntrada + "/" + parametro.alfa.getName());
                    alfa.nome = parametro.alfa.getName();
                    alfa.fasta = fasta;
                    alfa.quantidadeCaracteres = alfa.sequencia.length();
                }

                if (tipoSequenciaBeta == 1) {
                    //então é texto, gera o arquivo
                    Arquivo beta = ProcessamentoBean.uploadText(ConvertFASTA2txt.converter(parametro.textoBeta));
                    betas.add(beta);
                } else {
                    for (File f : parametro.beta) {
                        System.out.println(f.getName());
                        Boolean fasta = (ConvertFASTA2txt.converter(f, localEntrada + "/" + f.getName()));
                        Arquivo bt = ValidCharDNA.validar(localEntrada + "/" + f.getName());
                        Arquivo beta = new Arquivo();
                        beta.nome = f.getName();
                        beta.local = bt.local;
                        beta.quantidadeCaracteres = bt.quantidadeCaracteres;
                        beta.sequencia = bt.sequencia;
                        beta.fasta = fasta;
                        betas.add(beta);
                        bt = null;
                    }
                }
                
                //System.out.println(alfa.toString());
                

                if (alfa.quantidadeCaracteres < parametro.k) {
                    validation.addError("Erro", "A quantidade de diferenças (k) não pode ser maior que a quantidade de caracteres da sequência alvo!");
                }
                
                if(parametro.jInicio == null) parametro.jInicio = 1;
                if(parametro.jFim == null) parametro.jFim = alfa.sequencia.length();


                if (parametro.jInicio < 1) { //se o j for menor que 1 está errado
                   validation.addError("Erro", "A posição de início não pode ser menor que 1, que é a posição inicial da sequência alvo!");
                }

                if (parametro.jInicio == parametro.jFim) { //se o j for igual o fim esta errado
                   validation.addError("Erro", "A posição de início não pode ser igual à posição de fim da sequência alvo!");
                }
                if (parametro.jInicio > (alfa.quantidadeCaracteres - 1)) {
                   validation.addError("Erro", "A posição de início deve estar no intervalo entre 1 e "
                                + (alfa.quantidadeCaracteres - 1) + "!");
                }
                if (parametro.jFim > alfa.quantidadeCaracteres) {
                   validation.addError("Erro", "A posição de fim deve estar no intervalo entre 2 e "
                                + alfa.quantidadeCaracteres + ", que é o tamanho da sequência alvo!");
                }
             }
            
            if(parametro.mostrarDistancia){
            	if(parametro.distancia < 1){
            		validation.addError("Erro", "A distância entre as ocorrências deve ser maior que 0!");
            	}
            	if(parametro.distancia > alfa.quantidadeCaracteres){
            		validation.addError("Erro", "A distância entre as ocorrências não deve ser menor ou igual ao tamanho da sequência alvo!");
            	}
            }
            
            if(parametro.mostrarLimiteCaracteres){
            	if(parametro.limiteCaracteres < 1){
            		validation.addError("Erro", "O limite de caracteres na ocorrência deve ser maior que 0!");
            	}
            	if(parametro.limiteCaracteres >= alfa.quantidadeCaracteres){
            		validation.addError("Erro", "O limite de caracteres na ocorrência deve ser menor ou igual ao tamanho da sequência alvo!");
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
    
    public static void posicao(Long processamentoId, Integer j){
        System.out.println(processamentoId + ", " + j);
        Resultado resultado = Resultado.getResultadoPorProcessamento(processamentoId, j);
        render(resultado);
    }

    public static void ocorrencia(Long id){
        ArquivoBeta beta = ArquivoBeta.findById(id);
        if(beta != null) {
            Processamento processamento = beta.processamento;
            render(processamento, beta);
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um arquivo beta com esse ID no banco de dados!");
            validation.keep();
            erro();
        }
    }

    //lista de ocorrencias JSON
    public static void listaOcorrencia(Long betaId, Integer pagina){

        List<Ocorrencia> ocorrencias = Ocorrencia.todasOcorrenciasPorBetaOrdem(betaId, pagina);
        int total = Ocorrencia.paginasOcorrenciasPorBeta(betaId);
        render(ocorrencias, pagina, total);
    }

    //lista de processamentos JSON
    public static void listaProcessamento(String busca, Integer pagina){

        List<ViewProcessamento> processamentos = ViewProcessamento.listaProcessamentosSalvos(busca, pagina);
        int total = Processamento.paginasProcessamentos(busca);
        //System.out.println("===================================");
        //System.out.println(busca);
        //System.out.println(pagina);
        //System.out.println(processamentos.size());
        render(processamentos, pagina, total);
    }

    public static void ocorrenciaPosicao(Long id, Integer posicao){
        ArquivoBeta beta = ArquivoBeta.buscarPorProcessamentoPosicao(id, posicao);
        if(beta != null) {
            Processamento processamento = beta.processamento;
            List<Ocorrencia> ocorrencias = beta.ocorrencias;
            render(processamento, beta, ocorrencias);
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um arquivo beta com a posição "
                    + posicao
                    + " no processamento "
                    + id + "  no banco de dados!");
            validation.keep();
            erro();
        }
    }

    //mostrar todos os resultados em forma de tabela
    public static void resultado(Long id){
        Processamento processamento = Processamento.findById(id);
        int total = Resultado.paginasResultadosPorProcessamento(id);
        if(processamento != null) {
            List<Resultado> resultados = Resultado.todosResultadosPorProcessamentoOrdem(id, 1);
            render(processamento, resultados, total);
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um processamento no banco de dados com id " + id + "!");
            validation.keep();
            erro();
        }
    }

    //json para os resultados
    public static void listaResultado(Long processamentoId, Integer pagina){
        int total = Resultado.paginasResultadosPorProcessamento(processamentoId);
        List<Resultado> resultados = Resultado.todosResultadosPorProcessamentoOrdem(processamentoId, pagina);
//        System.out.println("===================================");
//        System.out.println(pagina);
//        System.out.println(resultados.size());
        
        render(resultados, pagina, total);
    }

    //mostar a lista de processamentos salvos
    public static void processamento(){
        render();
    }

    public static void salvar_processamento(Long id){
        Processamento processamento = Processamento.findById(id);
        if(processamento != null) {
            if(processamento.salvo){
                validation.addError("Erro", "Este processamento já foi salvo!");
                validation.keep();
                erro();
            } else {
                render(processamento);
            }
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um processamento no banco de dados com id " + id + "!");
            validation.keep();
            erro();
        }
    }

    public static void gerarFileResultado(Long resultadoId){
        Resultado rs = Resultado.findById(resultadoId);
        Ocorrencia ocr = rs.ocorrencia;

        String nomeArquivo = "segmento_" + ocr.j;
        response.setHeader("Content-type", "application/text; charset=utf-8");
        response.setHeader("Content-disposition", "inline;  filename=\"" + nomeArquivo+".txt\"");
        renderText("> posição: " + ocr.j + "; tamanho: " + ocr.r + ": " + ocr.segmento);
    }

    public static void gerarFileOcorrencia(Long ocorrenciaId){
        Ocorrencia ocr = Ocorrencia.findById(ocorrenciaId);
        String nomeArquivo = "segmento_" + ocr.j;
        response.setHeader("Content-type", "application/text; charset=utf-8");
        response.setHeader("Content-disposition", "inline;  filename=\"" + nomeArquivo+".txt\"");
        renderText("> posição: " + ocr.j + "; tamanho: " + ocr.r + ": " + ocr.segmento);
    }

    public static void excluir(Long id){
        Processamento processamento = Processamento.findById(id);
        if(processamento != null) {
            processamento.delete();
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um processamento no banco de dados com id " + id + "!");
            validation.keep();
            erro();
        }
    }

    public static void salvar(Long id, String nome, String informacao){
        Processamento processamento = Processamento.findById(id);
        if(processamento != null) {
            processamento.nome = nome;
            processamento.informacao = informacao;
            processamento.salvo = Boolean.TRUE;
            processamento.save();

            result(id);//mostrar novamente o resultado
        } else {
            validation.addError("Parâmetros incorretos", "Não existe um processamento no banco de dados com id " + id + "!");
            validation.keep();
            erro();
        }
    }
    
    public static void config(){    	
    	List<Programa> programas = Programa.getLista();	
    	String entrada = Configuracao.getValor("diretorio.entrada");
    	String saida   = Configuracao.getValor("diretorio.saida");
    	render(entrada, saida, programas);
    }
    
    public static void configSalvar(String entrada, String saida){
    	Configuracao.setValor("diretorio.entrada", entrada);
    	Configuracao.setValor("diretorio.saida", saida);
    	config();
    }
    
    public static void computarLCE(){
    	//  ProcessamentoBean.computarLCE();  
    }
}