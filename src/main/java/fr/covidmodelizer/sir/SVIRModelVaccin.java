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

public class SVIRModelVaccin {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_SVIR_VAC_CSV = "src/main/resources/svir-data-vaccination.csv";
	private final static String SVIR_VACCIN_PREDICTION = "svir-vaccination-prediction.csv";

	public static void main(String[] args) throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_SVIR_VAC_CSV))
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
		// tauxV -> taux de vaccination journalier

		int firstDay = data.size() - 22;

		// Initialisation
		final double N = 67000000.0;
		double initialS = Double.parseDouble(data.get(firstDay)[1]);
		double initialV = Double.parseDouble(data.get(firstDay)[2]);
		double initialI = Double.parseDouble(data.get(firstDay)[3]);
		double initialR = Double.parseDouble(data.get(firstDay)[4]);
		double r0 = Double.parseDouble(data.get(firstDay)[5]);
		double txV = Double.parseDouble(data.get(firstDay)[6]);
		double d = 10.0;
		double gamma = 1.0 / d;
		double beta = r0 * gamma;

		int expanse = 21;

		// Calcul du SVIR
		double[] predictiveSVIR = null;

		for (int i = 0; i < expanse; i++) {
			initialS = Double.parseDouble(data.get(firstDay + i)[1]);
			initialV = Double.parseDouble(data.get(firstDay + i)[2]);
			initialI = Double.parseDouble(data.get(firstDay + i)[3]);
			initialR = Double.parseDouble(data.get(firstDay + i)[4]);
			r0 = Double.parseDouble(data.get(firstDay + i)[5]);
			txV = Double.parseDouble(data.get(firstDay + i)[6]);
			beta = r0 * gamma;
			predictiveSVIR = SVIRCalculation(N, initialS, initialV, initialI, initialR, gamma, beta, txV);
			System.out.println("\nPrediction on " + data.get(firstDay + i)[0] + " : " + predictiveSVIR[1]
					+ " (value in dataset : " + data.get(firstDay + i + 1)[2] + ")");
			System.out.println(
					"\nReal value for " + data.get(firstDay + i + 1)[0] + " : " + data.get(firstDay + i + 1)[2] + "\n");
		}

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		LocalDate nextDate = null;

		CSVWriter csvWriter = new CSVWriter(new FileWriter(SVIR_VACCIN_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });

		for (int i = 0; i < (2 * expanse); i++) {
			initialS = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[1]);
			initialV = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[2]);
			initialI = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[3]);
			initialR = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[4]);
			r0 = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[5]);
			txV = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[6]);
			beta = r0 * gamma;
			predictiveSVIR = SVIRCalculation(N, initialS, initialV, initialI, initialR, gamma, beta, txV);
			nextDate = LocalDate
					.parse(data.get(data.size() - (2 * expanse) - 1 + i)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(1);
			csvWriter.writeNext(new String[] { nextDate.toString(), data.get(data.size() - (2 * expanse) + i)[2],
					predictiveSVIR[1] < 0 ? "0" : String.valueOf((int) predictiveSVIR[1]) });
		}
		predictiveSVIR[0] = Double.parseDouble(data.get(data.size() - 1)[1]);
		predictiveSVIR[1] = Double.parseDouble(data.get(data.size() - 1)[2]);
		predictiveSVIR[2] = Double.parseDouble(data.get(data.size() - 1)[3]);
		predictiveSVIR[3] = Double.parseDouble(data.get(data.size() - 1)[4]);
		r0 = Double.parseDouble(data.get(data.size() - 1)[5]);
		predictiveSVIR[4] = Double.parseDouble(data.get(data.size() - 1)[6]);
		beta = r0 * gamma;
		for (int i = 0; i < expanse; i++) {
			predictiveSVIR = SVIRCalculation(N, predictiveSVIR[0], predictiveSVIR[1], predictiveSVIR[2],
					predictiveSVIR[3], gamma, beta, predictiveSVIR[4]);
			nextDate = LocalDate.parse(data.get(data.size() - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), "",
					predictiveSVIR[1] < 0 ? "0" : String.valueOf((int) predictiveSVIR[1]) });
		}
		csvWriter.close();
	}

	public static double[] SVIRCalculation(double N, double S0, double V0, double I0, double R0, double gamma,
			double beta, double txV0) {
		/********* Parameters **********/
		// dS = - beta * S * I - txV * S
		// dV = txV * S
		// dI = beta * S * I - gamma * I
		// dR = gamma * I

		double S = S0 - (beta / N) * S0 * I0 - txV0 * S0;
		double V = V0 + txV0 * S0;
		double I = I0 + (beta / N) * S0 * I0 - gamma * I0;
		double R = R0 + gamma * I0;
		double txV = txV0 * S0 / S;

		return new double[] { S, V, I, R, txV };
	}

}