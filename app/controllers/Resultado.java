package controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeandobre on 14/10/2016.
 */
public class Resultado {
    public Integer j;
    public Integer tamanho;
    public String sequencia;

    static public List<Resultado> computaResultado(String file){
        List<Resultado> resultados = new ArrayList<Resultado>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            int count = 0;
            while(br.ready()){
                String linha = br.readLine();
                if(count == 0){
                    count++;
                    continue;
                }
                Resultado r = new Resultado();
                String gg[] = linha.split(";");
                r.j = Integer.valueOf( gg[0] );
                r.tamanho = Integer.valueOf( gg[1] );
                r.sequencia = gg[2];
                resultados.add(r);
            }
            br.close();
            // return True;
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return resultados;
    }
}
