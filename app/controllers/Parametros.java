package controllers;

import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.*;
import java.io.File;
import java.util.List;


public class Parametros{
   @Required
   public File alfa;

   @Required
   public List<File> beta;

   @Required
   @Min(1)
   public Integer k;

   public Boolean tipoProcessamento;

   @Min(0)
   public Integer j;
   public Integer distancia;

   public Integer tipoKdiference; //1-parcedias, 2 - diferentes

   public Boolean maiorMenor;
   public Boolean regioesTamanhoFixo;

   @java.lang.Override
   public java.lang.String toString() {
      return "Parametros{" +
              "alfa=" + alfa +
              ", beta=" + beta +
              ", k=" + k +
              ", tipoProcessamento=" + tipoProcessamento +
              ", j=" + j +
              ", distancia=" + distancia +
              ", tipoKdiference=" + tipoKdiference +
              '}';
   }
}
