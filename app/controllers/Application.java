package controllers;

import play.*;
import play.mvc.*;
//import play.data.Upload;
//import java.io.File;
//import java.io.FileInputStream;

//import models.*;


public class Application extends Controller {

    public static final String sequenciaParecida =  Play.configuration.getProperty("sequencia.parecida");
    public static final String sequenciaDiferente = Play.configuration.getProperty("sequencia.diferente");

    public static void index() {
        render();
    }

    public static void process(Parametros parametro){
    /*    Logger.info(alfa.getContentType());
    	Logger.info(alfa.getFieldName());
    	Logger.info(alfa.getFileName());       
        */



        Logger.info(parametro.toString());
        render();
    }

}