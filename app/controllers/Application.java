package controllers;

import play.*;
import play.mvc.*;
//import play.data.Upload;
import java.io.File;
//import java.io.FileInputStream;

//import models.*;


public class Application extends Controller {

    public static final String sequenciaParecida =  Play.configuration.getProperty("sequencia.parecida");
    public static final String sequenciaDiferente = Play.configuration.getProperty("sequencia.diferente");

    public static final String localEntrada = Play.configuration.getProperty("diretorio.entrada");

    public static void index() {
        render();
    }

    public static void process(Parametros parametro){
        //validation.required(parametro);

        //TODO validar o arquivo alfa
        //tamanho
        //caracteres ACGT
        //FASTA
        //System.out.println(parametro.alfa.getAbsolutePath());
       // System.out.println(parametro.alfa.getCanonicalPath());
        String parametroAlfa = localEntrada + parametro.alfa.getName();
        ConvertFASTA2txt.converter(parametro.alfa, parametroAlfa);


      //  arquivo.getFilename();


       // System.out.println(parametro.alfa.getFileName());
        //Logger.info(parametro.toString());

        render();
    }

}