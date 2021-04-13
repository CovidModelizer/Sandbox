package fr.covidmodelizer.linear;

import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LinearModelVaccin {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_LIN_VAC_CSV = "src/main/resources/lin-data-vaccination.csv";
	private final static String LIN_VACCIN_PREDICTION = "lin-vaccination-prediction.csv";

	public static void main(String[] args) throws Exception {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_LIN_VAC_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		// Préparation du data set
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute(data.get(0)[0], "yyyy-MM-dd"));
		atts.add(new Attribute(data.get(0)[1]));
		atts.add(new Attribute("cumul vaccines J+1"));

		Instances dataSet = new Instances("*** DATASET ***", atts, 0);
		dataSet.setClassIndex(atts.size() - 1);

		for (int i = 1; i < data.size(); i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
			instanceValue[1] = data.get(i)[1].isEmpty() ? Double.NaN : Double.parseDouble(data.get(i)[1]);
			instanceValue[2] = (i + 1 >= data.size()) ? Double.NaN
					: data.get(i + 1)[1].isEmpty() ? Double.NaN : Double.parseDouble(data.get(i + 1)[1]);
			dataSet.add(new DenseInstance(1.0, instanceValue));
		}

		// Variables utiles à l'entraînement du modèle de prédiction
		int expanse = 21;

		double calculationValue = Double.NaN;

		Instances trainSet = null;
		Instances testSet = null;

		LinearRegression lrClassifier = null;

		Evaluation eval = null;

		Instance predictiveData = null;

		LocalDate nextDate = null;

		// Entraînement du modèle de régression linéaire à une variable
		trainSet = dataSet.trainCV(5, 0, new Random());
		testSet = dataSet.testCV(5, 0);

		lrClassifier = new LinearRegression();
		lrClassifier.setOptions(new String[] { "-R", "1" });
		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		// Prédiction
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		predictiveData = dataSet.get(dataSet.size() - 2);
		System.out.println("\nPrediction on " + predictiveData.stringValue(0) + " : "
				+ lrClassifier.classifyInstance(predictiveData) + " (value in dataset : "
				+ predictiveData.value(dataSet.numAttributes() - 1) + ")");
		System.out.println("\nReal value for " + dataSet.lastInstance().stringValue(0) + " : "
				+ dataSet.lastInstance().value(1) + "\n");

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		CSVWriter csvWriter = new CSVWriter(new FileWriter(LIN_VACCIN_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });
		for (int i = 0; i < (2 * expanse); i++) {
			predictiveData = dataSet.instance(dataSet.size() - (2 * expanse) - 1 + i);
			nextDate = LocalDate.parse(predictiveData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(1);
			csvWriter.writeNext(new String[] { nextDate.toString(),
					String.valueOf((int) predictiveData.value(dataSet.numAttributes() - 1)),
					lrClassifier.classifyInstance(predictiveData) < 0 ? "0"
							: String.valueOf((int) lrClassifier.classifyInstance(predictiveData)) });
		}
		predictiveData = dataSet.lastInstance();
		calculationValue = predictiveData.value(1);
		for (int i = 0; i < expanse; i++) {
			nextDate = LocalDate.parse(predictiveData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			calculationValue = lrClassifier.coefficients()[1] * calculationValue + lrClassifier.coefficients()[2];
			csvWriter.writeNext(new String[] { nextDate.toString(), "",
					calculationValue < 0 ? "0" : String.valueOf((int) calculationValue) });
		}
		csvWriter.close();
	}
}
