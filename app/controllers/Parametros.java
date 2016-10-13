package controllers;

import play.mvc.*;
import java.io.File;


public class Parametros extends Controller {
    File alfa;
    File beta;

    Integer k;

    Boolean tipoProcessamento;

    Integer j;
    Integer distancia;

    Integer tipoKdiference; //1-parcedias, 2 - diferentes

    public java.lang.String toString() {
        return "Parametros{" +
                "arquivoAlfa='" + alfa + '\'' +
                ", arquivoBeta='" + beta + '\'' +
                ", k=" + k +
                ", tipoProcessamento=" + tipoProcessamento +
                ", j=" + j +
                ", distancia=" + distancia +
                ", tipoKdiference=" + tipoKdiference +
                '}';
    }
}
