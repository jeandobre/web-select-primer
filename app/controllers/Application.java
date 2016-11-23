package controllers;

import models.*;
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

        if(validation.hasErrors()) {
            validation.keep();
            erro();
        } else {
            Boolean fasta = (ConvertFASTA2txt.converter(parametro.alfa, localEntrada + "/" + parametro.alfa.getName()));
            Arquivo alfa = ValidCharDNA.validar(localEntrada + "/" + parametro.alfa.getName());
            alfa.nome = parametro.alfa.getName();
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
                    beta.nome = f.getName();
                    beta.local = bt.local;
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

    public static void posicao(Long processamentoId, Integer j){
        System.out.println(processamentoId + ", " + j);
        Resultado resultado = Resultado.getResultadoPorProcessamento(processamentoId, j);
        render(resultado);
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

        List<Processamento> processamentos = Processamento.listaProcessamentosSalvos(busca, pagina);
        int total = Processamento.paginasProcessamentos(busca);
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



}