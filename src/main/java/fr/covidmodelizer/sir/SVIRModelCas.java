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

public class SVIRModelCas {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_SVIR_CAS_CSV = "src/main/resources/svir-data-cas.csv";
	private final static String SVIR_CAS_PREDICTION = "svir-cas-prediction.csv";

	public static void main(String[] args) throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_SVIR_CAS_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		// Details des donnÃ©es
		// N -> Population totale de France
		// initialS -> Population initialement susceptible d'etre infecte
		// initialI -> Nombre initial de personnes infectÃ©es
		// initialR -> Nombre de personnes remises
		// D -> durÃ©e moyenne d'infection avant guÃ©rison ou dÃ©cÃ¨s
		// DurÃ©e d'isolement aprÃ¨s une positivitÃ© au covid :
		// https://www.ameli.fr/assure/covid-19/isolement-principes-et-regles-respecter/isolement-principes-generaux
		// r0 -> taux de reproduction du virus
		// gamma -> 1 / D -> taux de guÃ©rison
		// beta -> r0 * gamma -> taux de transmission
		// tauxV -> taux de vaccination journalier

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

		int expanse = 21;

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		// Calcul du SIR
		double[] predictiveSVIR = null; // SIRCalculation(N, initialS, initialI, initialR, gamma, beta, tauxV);

		// Vérif
		double sum_tauxV = 0;
		for (int i = firstDay - 7; i < firstDay; i++) {
			sum_tauxV = sum_tauxV + Double.parseDouble(data.get(i)[6]);
		}
		double tauxV = sum_tauxV / 7;
		for (int i = 0; i < expanse; i++) {
			initialS = Double.parseDouble(data.get(firstDay + i)[1]);
			initialI = Double.parseDouble(data.get(firstDay + i)[2]);
			initialR = Double.parseDouble(data.get(firstDay + i)[3]);
			r0 = Double.parseDouble(data.get(firstDay + i)[4]);
			beta = r0 * gamma;
			predictiveSVIR = SVIRCalculation(N, initialS, initialI, initialR, gamma, beta, tauxV);
			double realvalueSIR = Double.parseDouble(data.get(firstDay + i + 1)[3])
					+ gamma * Double.parseDouble(data.get(firstDay + i + 1)[2])
					+ Double.parseDouble(data.get(firstDay + i + 1)[6]) * N;
			System.out.println("\nPrediction on " + data.get(firstDay + i)[0] + " : " + predictiveSVIR[2]
					+ " (value in dataset : " + realvalueSIR + ")");
		}

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		LocalDate nextDate = null;

		CSVWriter csvWriter = new CSVWriter(new FileWriter(SVIR_CAS_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });

		// Première prediction
		int debut = data.size() - (2 * expanse) - 1;
		System.out.println("Date de début première prediction" + data.get(debut)[0]);
		double sum_tauxV2 = 0;
		for (int i = debut - 7; i < debut; i++) {
			sum_tauxV2 = sum_tauxV2 + Double.parseDouble(data.get(i)[6]);
		}
		double tauxV2 = sum_tauxV2 / 7;
		System.out.println("taux V2 = " + tauxV2);
		for (int i = 0; i < (2 * expanse); i++) {
			initialS = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[1]);
			initialI = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[2]);
			initialR = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[3]);
			r0 = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i)[4]);
			beta = r0 * gamma;
			predictiveSVIR = SVIRCalculation(N, initialS, initialI, initialR, gamma, beta, tauxV2);
			double realvalueSIR = Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i + 1)[3])
					+ gamma * Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i + 1)[2])
					+ Double.parseDouble(data.get(data.size() - (2 * expanse) - 1 + i + 1)[6]) * N;
//			System.out.println("\nPrediction on " + data.get(data.size() - (2 * expanse) - 1 + i)[0] + " : "
//					+ predictiveSIR[2] + " (value in dataset : " + realvalueSIR + ")");
			nextDate = LocalDate
					.parse(data.get(data.size() - (2 * expanse) - 1 + i)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(1);
			csvWriter.writeNext(new String[] { nextDate.toString(), String.valueOf((int) realvalueSIR),
					String.valueOf((int) predictiveSVIR[2]) });
		}

		// Deuxième prediction
		debut = data.size() - 1;
		System.out.println("date de début " + data.get(debut)[0]);
		double sum_tauxV3 = 0;
		for (int i = debut - 7; i < debut; i++) {
			sum_tauxV3 = sum_tauxV3 + Double.parseDouble(data.get(i)[6]);
		}
		double tauxV3 = sum_tauxV3 / 7;
		System.out.println("taux V3 = " + tauxV3);
		predictiveSVIR[0] = Double.parseDouble(data.get(data.size() - 1)[1]);
		predictiveSVIR[1] = Double.parseDouble(data.get(data.size() - 1)[2]);
		predictiveSVIR[2] = Double.parseDouble(data.get(data.size() - 1)[3]);
		r0 = Double.parseDouble(data.get(data.size() - 1)[4]);
		beta = r0 * gamma;
		for (int i = 0; i < expanse; i++) {
			predictiveSVIR = SVIRCalculation(N, predictiveSVIR[0], predictiveSVIR[1], predictiveSVIR[2], gamma, beta,
					tauxV3);
			nextDate = LocalDate.parse(data.get(data.size() - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), "", String.valueOf((int) predictiveSVIR[2]) });
		}
		csvWriter.close();
	}

	public static double[] SVIRCalculation(double N, double S0, double I0, double R0, double gamma, double beta,
			double tauxV) {
		/********* Parameters **********/
		// S' = -beta * S * I -tauxV*N
		// I' = beta * S * I - gamma * I
		// R' = gamma * I + tauxV*N

		double S = S0 - (beta / N) * S0 * I0 - tauxV * N;
		double I = I0 + (beta / N) * S0 * I0 - gamma * I0;
		double R = R0 + gamma * I0 + tauxV * N;

		return new double[] { S, I, R };
	}

}