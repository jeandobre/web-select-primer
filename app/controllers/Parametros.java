package controllers;

import com.sun.istack.internal.NotNull;
import models.TipoSequencia;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;


public class Parametros{

   public File alfa;

   public File[] beta = null;

   @Required
   @Min(1)
   @NotNull
   public Integer k;

   //public Boolean tipoProcessamento;

   @Min(1)
   public Integer jInicio;
   @Min(2)
   public Integer jFim;
   
   
   @Min(1)
   public Integer distancia;
   public Integer limiteCaracteres; 

   public TipoSequencia tipoSequencia;

   /*
   public Boolean maiorMenor = false; */
   public Boolean mostrarDistancia = false;
   public boolean mostrarLimiteCaracteres = false;
 
   /*
   public Integer tipoSequenciaAlfa; //1 - texto e 2 - Arquivo
   public Integer tipoSequenciaBeta;
   */
   
   public String textoAlfa;
   public String textoBeta;

   @Override
	public String toString() {
		return "Parametros [alfa=" + alfa + ", beta=" + Arrays.toString(beta) + ", k=" + k + ", jInicio=" + jInicio
				+ ", jFim=" + jFim + ", distancia=" + distancia + ", limiteCaracteres=" + limiteCaracteres
				+ ", tipoSequencia=" + tipoSequencia + ", mostrarDistancia=" + mostrarDistancia
				+ ", mostrarLimiteCaracteres=" + mostrarLimiteCaracteres + ", textoAlfa=" + textoAlfa + ", textoBeta="
				+ textoBeta + "]";
	}
}
