package persistence;

import java.io.*;

public class ConvertFASTA2txt {

	public static String converter(String texto){
		BufferedReader br = new BufferedReader(new StringReader(texto));
		String retorno = "";
		try{

			while(br.ready()){
				String linha = br.readLine();
				if(linha == null) break;
				if(linha.length() > 0 && linha.charAt(0) == '>') continue;
				retorno += linha;
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return retorno;
	}
	
	public static Boolean converter(File file, String localArquivoTxt){
        Boolean fasta = false;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter wr = new BufferedWriter(new FileWriter(localArquivoTxt));
		
			while(br.ready()){
				String linha = br.readLine();
				if(linha.length() > 0 && linha.charAt(0) == '>'){
                    fasta = true;
                    continue;
                }
				wr.write(linha);
			}
			br.close();
			wr.close();

		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return fasta;
	}

}
