package persistence;

import com.sun.org.apache.xpath.internal.operations.Bool;
import controllers.Arquivo;

import java.io.*;

/**
 * Created by jeandobre on 14/10/2016.
 */
public class ValidCharDNA {
    public static Arquivo validar(String file){
        Arquivo novo = new Arquivo();
        novo.local = file;
        novo.quantidadeCaracteres = 0;
        novo.sequencia = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            int value = 0;
            while((value = br.read()) != -1){
                char c = (char)value;            
                novo.sequencia += c;
                novo.quantidadeCaracteres++;
            }
            br.close();
            // return True;
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        
        return novo;
    }


}
