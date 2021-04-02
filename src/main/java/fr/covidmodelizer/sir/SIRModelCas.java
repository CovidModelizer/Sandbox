package fr.covidmodelizer.sir;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class SIRModelCas {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_SIR_CAS_CSV = "src/main/resources/sir-data-cas.csv";
	private final static String SIR_CAS_PREDICTION = "sir-cas-prediction.csv";

	public static void main(String[] args) throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_SIR_CAS_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		// Details des données
		// N -> Population totale de France
		// initialS -> Population initialement susceptible d'etre infecte
		// initialI -> Nombre initial de personnes infectées
		// initialR -> Nombre de personnes remises
		// D -> durée moyenne d'infection avant guérison ou décès
		// Durée d'isolement après une positivité au covid :
		// https://www.ameli.fr/assure/covid-19/isolement-principes-et-regles-respecter/isolement-principes-generaux
		// r0 -> taux de reproduction du virus
		// gamma -> 1 / D -> taux de guérison
		// beta -> r0 * gamma -> taux de transmission

		int firstDay = data.size() - 22;

		// Initialisation
		double N = 67000000.0;
		double initialS = Double.parseDouble(data.get(firstDay)[1]);
		double initialI = Double.parseDouble(data.get(firstDay)[2]);
		double initialR = Double.parseDouble(data.get(firstDay)[3]);
		double r0 = Double.parseDouble(data.get(firstDay)[4]);
		double D = 10.0;
		double gamma = 1.0 / D;
		double beta = r0 * gamma;
		System.out.println("gamma : " + gamma);
		System.out.println("beta : " + beta);

		/********* Parameters **********/
		// S' = -beta * S * I
		// I' = beta * S * I - nu * I
		// R' = nu * I

		int expanse = 21;

		// Calcul du SIR
		double[] predictiveI = SIRCalculation(N, initialS, initialI, initialR, gamma, beta, expanse);

		for (int i = 0; i < predictiveI.length; i++) {
			System.out.println("\nPrediction on " + data.get(firstDay)[0] + " : " + predictiveI[i] + " (value in dataset : "
					+ data.get(firstDay + i)[2] + ")");
			System.out.println(
					"\nReal value for " + data.get(firstDay + i)[0] + " : " + data.get(firstDay + i)[2] + "\n");
		}

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		LocalDate nextDate = null;

		CSVWriter csvWriter = new CSVWriter(new FileWriter(SIR_CAS_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });

		initialS = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1)[1]);
		initialI = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1)[2]);
		initialR = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1)[3]);
		r0 = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1)[4]);
		beta = r0 * gamma;
		predictiveI = SIRCalculation(N, initialS, initialI, initialR, gamma, beta, expanse);
		for (int i = 0; i < expanse; i++) {
			nextDate = LocalDate
					.parse(data.get(data.size() - (2 * expanse) - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), data.get(data.size() - (2 * expanse) + i)[2],
					String.valueOf((int) predictiveI[i + 1]) });
		}
		initialS = Double.parseDouble(data.get(data.size() - expanse - 1)[1]);
		initialI = Double.parseDouble(data.get(data.size() - expanse - 1)[2]);
		initialR = Double.parseDouble(data.get(data.size() - expanse - 1)[3]);
		r0 = Double.parseDouble(data.get(data.size() - expanse - 1)[4]);
		beta = r0 * gamma;
		predictiveI = SIRCalculation(N, initialS, initialI, initialR, gamma, beta, expanse);
		for (int i = 0; i < expanse; i++) {
			nextDate = LocalDate
					.parse(data.get(data.size() - expanse - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), data.get(data.size() - expanse + i)[2],
					String.valueOf((int) predictiveI[i + 1]) });
		}
		initialS = Double.parseDouble(data.get(data.size() - 1)[1]);
		initialI = Double.parseDouble(data.get(data.size() - 1)[2]);
		initialR = Double.parseDouble(data.get(data.size() - 1)[3]);
		r0 = Double.parseDouble(data.get(data.size() - 1)[4]);
		beta = r0 * gamma;
		predictiveI = SIRCalculation(N, initialS, initialI, initialR, gamma, beta, expanse);
		for (int i = 0; i < expanse; i++) {
			nextDate = LocalDate.parse(data.get(data.size() - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), "", String.valueOf((int) predictiveI[i + 1]) });
		}
		csvWriter.close();
	}

	public static double[] SIRCalculation(double N, double S0, double I0, double R0, double gamma, double beta,
			int nbDays) {
		double[] S = new double[nbDays + 1];
		double[] I = new double[nbDays + 1];
		double[] R = new double[nbDays + 1];
		I[0] = I0;
		R[0] = R0;
		S[0] = S0;
		for (int i = 1; i < S.length; i++) {
			S[i] = S[i - 1] - (beta / N) * I[i - 1] * S[i - 1];
			I[i] = I[i - 1] + (beta / N) * I[i - 1] * S[i - 1] - gamma * I[i - 1];
			R[i] = N - S[i] - I[i];
		}
		return I;
	}
}
