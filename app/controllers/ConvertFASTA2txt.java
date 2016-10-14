package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConvertFASTA2txt {
	
	public static void converter(File file, String localArquivoTxt){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter wr = new BufferedWriter(new FileWriter(localArquivoTxt));
		
		while(br.ready()){
			String linha = br.readLine();
			if(linha.length() > 0 && linha.charAt(0) == '>') continue;
			wr.write(linha);
		}
		br.close();
		wr.close();
		
		}catch(IOException ioe){
			ioe.printStackTrace();
		} 
	}

}
