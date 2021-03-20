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

	final static LocalTime START = LocalTime.now();

	public static void main(String[] args) throws Exception {
		CSVParser csvParser = new CSVParserBuilder().build();
		FileReader fileReader = new FileReader("data.csv");
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
		List<String[]> data = csvReader.readAll();

		// Préparation du data set
		ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
		atts.add(new Attribute("date", "yyyy-MM-dd"));
		atts.add(new Attribute("nouveaux" + data.get(0)[1].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[2].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[3].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[4].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[5].substring(5)));
		atts.add(new Attribute("total_" + data.get(0)[6]));
		atts.add(new Attribute("total_" + data.get(0)[7]));
		atts.add(new Attribute("nouveaux" + data.get(0)[8].substring(5)));
		atts.add(new Attribute(data.get(0)[9]));
		atts.add(new Attribute(data.get(0)[10]));
		atts.add(new Attribute("nouveaux cas J+N"));

		String dataOrigin = "https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_";
		Instances dataSet = new Instances("** Instances from " + dataOrigin + " **", atts, 0);
		dataSet.setClassIndex(11);

		for (int i = 0; i < data.size(); i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			if (i == 0) {
				instanceValue[0] = dataSet.attribute("date").parseDate("2020-03-01");
				for (int j = 1; j < instanceValue.length - 1; j++) {
					instanceValue[j] = Double.NaN;
				}
			} else {
				instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
				for (int j = 1; j < data.get(i).length; j++) {
					if (j == 6 || j == 7 || j > 8) {
						instanceValue[j] = data.get(i)[j].equals("") ? Double.NaN : Double.parseDouble(data.get(i)[j]);
					} else {
						instanceValue[j] = (i < 2) || data.get(i)[j].equals("") || data.get(i - 1)[j].equals("")
								? Double.NaN
								: Double.parseDouble(data.get(i)[j]) - Double.parseDouble(data.get(i - 1)[j]);
						instanceValue[j] = instanceValue[j] > 0 ? instanceValue[j] : Double.NaN;
					}
				}
				for (int j = data.get(i).length; j < instanceValue.length; j++) {
					instanceValue[j] = Double.NaN;
				}
			}
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
		double[] predictedValue = new double[expanse];
		LocalDate nextDate = null;

		// Entraînement du modèle de régression linéaire
		for (int n = 1; n <= expanse; n++) {
			for (int i = 0; i < data.size(); i++) {
				realValue = (i == 0 && n == 1) ? Double.parseDouble(data.get(n)[1])
						: (i + n) < data.size()
								? Double.parseDouble(data.get(i + n)[1]) - Double.parseDouble(data.get(i + n - 1)[1])
								: Double.NaN;
				dataSet.instance(i).setValue(11, realValue);
			}

			dataSet.renameAttribute(11, "nouveaux cas J+" + n);

			dataToPredict = dataSet.lastInstance();

			trainSet = dataSet.trainCV(5, 4, new Random());
			testSet = dataSet.testCV(5, 4);

			lrClassifier[n - 1] = new LinearRegression();
			lrClassifier[n - 1].buildClassifier(trainSet);

			eval[n - 1] = new Evaluation(trainSet);
			eval[n - 1].evaluateModel(lrClassifier[n - 1], testSet);

			predictionData = dataSet.get(dataSet.size() - 1 - n);
			predictedValue[n - 1] = lrClassifier[n - 1].classifyInstance(predictionData);

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
			predictedValue[i] = lrClassifier[i].classifyInstance(predictionData);
			System.out.println("\nPrediction on " + predictionData.stringValue(0) + " : " + predictedValue[i]
					+ " (value in dataset : " + predictionData.value(11) + ")");
			System.out.println(
					"\nReal value for " + dataToPredict.stringValue(0) + " : " + dataToPredict.value(1) + "\n");
		}

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));

		Writer writer = Files.newBufferedWriter(Paths.get("predictions.csv"));

		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		csvWriter.writeNext(new String[] { "date", "real value", "prediction value" });
		for (int i = 0; i < expanse; i++) {
			csvWriter.writeNext(new String[] { data.get(data.size() - expanse + i)[0],
					String.valueOf((int) dataSet.instance(dataSet.size() - expanse + i).value(1)),
					String.valueOf((int) Math.round(predictedValue[i])) });
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
