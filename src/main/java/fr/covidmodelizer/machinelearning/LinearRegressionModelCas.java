package fr.covidmodelizer.machinelearning;

import java.io.FileReader;
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LinearRegressionModelCas {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_ORIGIN = "https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_";
	private final static String DATA_ML_CAS_CSV = "src/main/resources/ml-data-cas.csv";

	public static void main(String[] args) throws Exception {
		CSVParser csvParser = new CSVParserBuilder().build();
		FileReader fileReader = new FileReader(DATA_ML_CAS_CSV);
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
		List<String[]> data = csvReader.readAll();

		// Préparation du data set
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute(data.get(0)[0], "yyyy-MM-dd"));
		for (int i = 1; i < data.get(0).length; i++) {
			atts.add(new Attribute(data.get(0)[i]));
		}
		atts.add(new Attribute("nouveaux cas J+N"));

		Instances dataSet = new Instances("** Instances from " + DATA_ORIGIN + " **", atts, 0);
		dataSet.setClassIndex(atts.size() - 1);

		for (int i = 1; i < data.size(); i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
			for (int j = 1; j < data.get(i).length; j++) {
				instanceValue[j] = data.get(i)[j].isEmpty() ? Double.NaN : Double.parseDouble(data.get(i)[j]);
			}
			instanceValue[instanceValue.length - 1] = Double.NaN;
			dataSet.add(new DenseInstance(1.0, instanceValue));
		}

		// Variables utiles à l'entraînement du modèle de prédiction
		double realValue = Double.NaN;
		int expanse = 21;

		Instances[] allDataSet = new Instances[expanse];

		Instances trainSet = null;
		Instances testSet = null;
		LinearRegression[] lrClassifier = new LinearRegression[expanse];
		Evaluation[] eval = new Evaluation[expanse];

		Instance dataToPredict = null;
		Instance predictionData = null;
		LocalDate nextDate = null;

		// Entraînement du modèle de régression linéaire
		for (int n = 1; n <= expanse; n++) {
			for (int i = 0; i < dataSet.size(); i++) {
				realValue = (i + n) >= dataSet.size() ? Double.NaN : dataSet.instance(i + n).value(5);
				dataSet.instance(i).setValue(dataSet.numAttributes() - 1, realValue);
			}

			dataSet.renameAttribute(dataSet.numAttributes() - 1, "nouveaux cas J+" + n);

			trainSet = dataSet.trainCV(5, 0, new Random());
			testSet = dataSet.testCV(5, 0);

			lrClassifier[n - 1] = new LinearRegression();
			lrClassifier[n - 1].buildClassifier(trainSet);

			eval[n - 1] = new Evaluation(trainSet);
			eval[n - 1].evaluateModel(lrClassifier[n - 1], testSet);

			predictionData = dataSet.get(dataSet.size() - 1 - n);

			allDataSet[n - 1] = new Instances(dataSet);
		}

		// Prédictions sur 21 (= expanse) jours
		for (int i = 0; i < allDataSet.length; i++) {
			dataToPredict = allDataSet[i].get(allDataSet[i].size() - expanse + i);

			System.out.println(allDataSet[i].relationName());
			System.out.println("** Linear Regression Evaluation **");
			System.out.println(eval[i].toSummaryString());
			System.out.print("=> The expression for the input data as per algorithm is : ");
			System.out.println(lrClassifier[i]);

			predictionData = allDataSet[i].get(allDataSet[i].size() - 1 - expanse);
			System.out.println("\nPrediction on " + predictionData.stringValue(0) + " : "
					+ lrClassifier[i].classifyInstance(predictionData) + " (value in dataset : "
					+ predictionData.value(dataSet.numAttributes() - 1) + ")");
			System.out.println(
					"\nReal value for " + dataToPredict.stringValue(0) + " : " + dataToPredict.value(5) + "\n");

//			System.out.println("Coefficients :\n");
//			for (int j = 0; j < allDataSet[i].numAttributes(); j++) {
//				System.out.println(lrClassifier[i].coefficients()[j] + " | " + allDataSet[i].attribute(j).name() + "\n");
//			}
//			for (int j = allDataSet[i].numAttributes(); j < lrClassifier[i].coefficients().length; j++) {
//				System.out.println(lrClassifier[i].coefficients()[j] + "\n");
//			}
		}

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		Writer writer = Files.newBufferedWriter(Paths.get("predictions.csv"));

		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });
		for (int i = 0; i < expanse; i++) {
			predictionData = allDataSet[i].instance(dataSet.size() - (2 * expanse) - 1);
			nextDate = LocalDate.parse(predictionData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(),
					String.valueOf((int) predictionData.value(dataSet.numAttributes() - 1)),
					String.valueOf((int) lrClassifier[i].classifyInstance(predictionData)) });
		}
		for (int i = 0; i < expanse; i++) {
			predictionData = allDataSet[i].instance(dataSet.size() - expanse - 1);
			nextDate = LocalDate.parse(predictionData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(),
					String.valueOf((int) predictionData.value(dataSet.numAttributes() - 1)),
					String.valueOf((int) lrClassifier[i].classifyInstance(predictionData)) });
		}
		for (int i = 0; i < expanse; i++) {
			predictionData = allDataSet[i].lastInstance();
			nextDate = LocalDate.parse(predictionData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.plusDays(i + 1);
			csvWriter.writeNext(new String[] { nextDate.toString(), "",
					String.valueOf((int) lrClassifier[i].classifyInstance(predictionData)) });
		}
		csvWriter.close();
	}
}
