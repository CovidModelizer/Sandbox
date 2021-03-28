package fr.covidmodelizer.sir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;


public class EpidemiologicalModel {

	final static LocalTime START = LocalTime.now();

	// Méthode qui nous permet de lire le fichier CSV
	public static String[][] readCSV(String nomFichier, char c, Charset charset) throws IOException {
		return Files.lines(Paths.get(nomFichier), charset).map(ligne -> ligne.split(String.valueOf(c)))
				.toArray(String[][]::new);
	}
	
	
	/*********Paramètres**********/
	/*S' = -beta*S*I
	I' = beta*S*I - nu*I
	R' =  nu*I*/
	
	
	//Méthode calcul SIR
	public static double[] calculSIR(int duree, double beta, double gamma, double N, double S0, double I0, double R0) {
		double[] S = new double[duree+1];
		double[] I = new double[duree+1];
		double[] R = new double[duree+1];
		I[0] = I0;
		R[0] = R0;
		S[0] = S0;
		for(int i=1;i<S.length;i++) {
			double dS = -(beta/N)*I[i-1]*S[i-1];
			double dI = ((beta/N)*I[i-1]*S[i-1])-gamma*I[i-1];
			//double dR = gamma*I[i-1];
			S[i] = S[i-1] + dS;
			I[i] = I[i-1] + dI;
			//R[i] = R[i-1] + dR;
			R[i] = N - S[i] - I[i];
			System.out.println("*************" + BigDecimal.valueOf(S[i]+I[i]+R[i]));
		}
		for(int i=1;i<I.length;i++) {
			I[i] = I[i];
		}
		I[0] = I0;
		return I;
	}

	public static void main(String[] args) throws IOException {
		String[][] tableau_data = readCSV("data.csv", ';', StandardCharsets.UTF_8); // semi-column separated values

		for (String[] ligne : tableau_data) {
			for (int colonne = 0; colonne < ligne.length; colonne++) {
				String col = ligne[colonne];
			}
		}
		
		int nb_lignes = tableau_data.length;
		int nb_colonnes = tableau_data[0].length;
		System.out.println(tableau_data[0][0]);
		System.out.println("Nombre de personnes infectées initialement : " + tableau_data[1][1]);
		System.out.println(tableau_data[tableau_data.length-1][tableau_data[0].length-1]);
		System.out.println("Nombre de lignes " + nb_lignes);
		System.out.println("Nombre de colonnes " + nb_colonnes);

		
		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));
		
		//Initialisation
//		double N = 60000000; //Population française
//		double I0 = Integer.parseInt(tableau_data[1][1]); //Nb initial de personnes infectées
//		double R0 = 0; //Nombre de personnes remises
//		double S0 = N - I0 - R0; //population initialement susceptible d'être infecté
//		int D = 8; //d est la durée moyenne d'infection avant guérison ou décès
//		double r0 = 1.02; //Diff de R0, r0 est le taux de reproduction initial
//		double beta = r0/(N*D);
//		double gamma = 1/D;
		
		
		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));
		
		//Initialisation
		double N = 67000000.0;
		double I0 = Integer.parseInt(tableau_data[1][1]); //4252022
		double R0 = 0; //(280571+66735)
		double S0 = N - I0 - R0; //(N-4252022-(280571+66735))
		double D = 8.0; //d est la durée moyenne (en jours) d'infection avant guérison ou décès
		double r0 = 1.16; //Diff de R0, r0 est le taux de reproduction initial
		//double beta = (r0/(N*D));
		//System.out.println("beta : " + beta);
		double gamma = 1/D;
		double beta = r0*gamma;
		System.out.println("beta : " + beta);
		System.out.println("gamma : " + gamma);
		System.out.println("Population " + BigDecimal.valueOf(S0+I0+R0));
		
		double infectes[] = calculSIR(21, beta, gamma, N, (N-3904716.0-280571.0-66735.0) , 3904716.0 , (280571.0+66735.0));
		
		for (double e : infectes) {
			System.out.println(e);
		}
		
		//R = guéris + décès
		//Pb : trouver la bonne valeur de beta
		
	}

}
