package controllers;

import play.templates.JavaExtensions;

public class Extensions extends JavaExtensions {

	  public static String scapeJson(String texto) {

	    try {

	      if (texto.isEmpty()) {
	        return "";
	      }

          String textoR = texto
            		.replaceAll("\"", "'")
            		.replaceAll("\\n", "")
            		.replaceAll("\\r", "")
            		.replaceAll("\\t","");

	      return textoR.replace("\\", "/");
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    return "";

	  }
}
