package fr.covidmodelizer.machinelearning;

import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LinearRegressionModelCas {

	final static LocalTime START = LocalTime.now();

	public static void main(String[] args) throws Exception {
		CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
		FileReader fileReader = new FileReader("data.csv");
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
		List<String[]> data = csvReader.readAll();

		ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
		atts.add(new Attribute("date", "yyyy-MM-dd"));
		atts.add(new Attribute("nouveaux" + data.get(0)[1].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[2].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[3].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[4].substring(5)));
		atts.add(new Attribute("nouveaux" + data.get(0)[5].substring(5)));
		atts.add(new Attribute(data.get(0)[6]));
		atts.add(new Attribute(data.get(0)[7]));
		atts.add(new Attribute("nouveaux" + data.get(0)[8].substring(5)));
		atts.add(new Attribute(data.get(0)[9]));
		atts.add(new Attribute(data.get(0)[10]));
		atts.add(new Attribute("nouveaux cas J+1"));
		atts.add(new Attribute("nouveaux cas J+7"));
		atts.add(new Attribute("nouveaux cas J+14"));
		atts.add(new Attribute("nouveaux cas J+21"));
		String dataOrigin = "https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_";

		Instances dataSet = new Instances("** Instances from " + dataOrigin + " **", atts, 0);
		Instances trainSet = null;
		Instances testSet = null;
		LinearRegression lrClassifier = new LinearRegression();
		Evaluation eval = null;

		double[] firstInstanceValue = new double[dataSet.numAttributes()];
		firstInstanceValue[0] = dataSet.attribute("date").parseDate("2020-03-01");
		for (int i = 1; i < firstInstanceValue.length - 1; i++) {
			firstInstanceValue[i] = Double.NaN;
		}
		firstInstanceValue[dataSet.numAttributes() - 1] = Double.parseDouble(data.get(1)[1]);
		dataSet.add(new DenseInstance(1.0, firstInstanceValue));

		for (int i = 1; i < data.size() - 1; i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
			for (int j = 1; j <= 5; j++) {
				instanceValue[j] = (i < 2) || data.get(i)[j].equals("") || data.get(i - 1)[j].equals("") ? Double.NaN
						: Double.parseDouble(data.get(i)[j]) - Double.parseDouble(data.get(i - 1)[j]);
				instanceValue[j] = instanceValue[j] > 0 ? instanceValue[j] : Double.NaN;
			}
			instanceValue[8] = (i < 2) || data.get(i)[8].equals("") || data.get(i - 1)[8].equals("") ? Double.NaN
					: Double.parseDouble(data.get(i)[8]) - Double.parseDouble(data.get(i - 1)[8]);
			instanceValue[8] = instanceValue[8] > 0 ? instanceValue[8] : Double.NaN;
			for (int j = 6; j < data.get(i).length; j++) {
				if (j != 8) {
					instanceValue[j] = data.get(i)[j].equals("") ? Double.NaN : Double.parseDouble(data.get(i)[j]);
				}
			}
			instanceValue[11] = Double.parseDouble(data.get(i + 1)[1]) - Double.parseDouble(data.get(i)[1]);
			instanceValue[11] = instanceValue[11] > 0 ? instanceValue[11] : Double.NaN;
			instanceValue[12] = (i + 7) < data.size()
					? Double.parseDouble(data.get(i + 1)[1]) - Double.parseDouble(data.get(i)[1])
					: Double.NaN;
			instanceValue[12] = instanceValue[12] > 0 ? instanceValue[12] : Double.NaN;
			instanceValue[13] = (i + 14) < data.size()
					? Double.parseDouble(data.get(i + 1)[1]) - Double.parseDouble(data.get(i)[1])
					: Double.NaN;
			instanceValue[13] = instanceValue[13] > 0 ? instanceValue[13] : Double.NaN;
			instanceValue[14] = (i + 21) < data.size()
					? Double.parseDouble(data.get(i + 1)[1]) - Double.parseDouble(data.get(i)[1])
					: Double.NaN;
			instanceValue[14] = instanceValue[14] > 0 ? instanceValue[14] : Double.NaN;
			dataSet.add(new DenseInstance(1.0, instanceValue));
		}

		Instances dataSetJPlus1 = new Instances(dataSet);
		dataSetJPlus1.deleteAttributeAt(12);
		dataSetJPlus1.deleteAttributeAt(12);
		dataSetJPlus1.deleteAttributeAt(12);
		dataSetJPlus1.setClassIndex(11);
		dataSetJPlus1.setRelationName("** Instances to predict J+1 **");
		Instances dataSetJPlus7 = new Instances(dataSet);
		dataSetJPlus7.deleteAttributeAt(11);
		dataSetJPlus7.deleteAttributeAt(12);
		dataSetJPlus7.deleteAttributeAt(12);
		dataSetJPlus7.setClassIndex(11);
		dataSetJPlus7.setRelationName("** Instances to predict J+7 **");
		Instances dataSetJPlus14 = new Instances(dataSet);
		dataSetJPlus14.deleteAttributeAt(11);
		dataSetJPlus14.deleteAttributeAt(11);
		dataSetJPlus14.deleteAttributeAt(12);
		dataSetJPlus14.setClassIndex(11);
		dataSetJPlus14.setRelationName("** Instances to predict J+14 **");
		Instances dataSetJPlus21 = new Instances(dataSet);
		dataSetJPlus21.deleteAttributeAt(11);
		dataSetJPlus21.deleteAttributeAt(11);
		dataSetJPlus21.deleteAttributeAt(11);
		dataSetJPlus21.setClassIndex(11);
		dataSetJPlus21.setRelationName("** Instances to predict J+21 **");

		System.out.println(dataSet.relationName());
		// Entraînement du modèle de régression linéaire
		// J+1
		Instance dataToPredictJPlus1 = dataSetJPlus1.remove(dataSet.size() - 1);
		trainSet = dataSetJPlus1.trainCV(5, 4);
		testSet = dataSetJPlus1.testCV(5, 4);

		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		System.out.println(dataSetJPlus1.relationName());
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		double predictedValueJPlus1 = lrClassifier.classifyInstance(dataToPredictJPlus1);
		System.out.println("\nPrediction for " + dataToPredictJPlus1.stringValue(0) + " : " + predictedValueJPlus1);
		System.out.println("\nReal value for " + dataToPredictJPlus1.stringValue(0) + " : "
				+ dataToPredictJPlus1.value(11) + "\n");

		// J+7
		Instance dataToPredictJPlus7 = dataSetJPlus7.remove(dataSet.size() - 7);
		trainSet = dataSetJPlus7.trainCV(5, 4);
		testSet = dataSetJPlus7.testCV(5, 4);

		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		System.out.println(dataSetJPlus7.relationName());
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		double predictedValueJPlus7 = lrClassifier.classifyInstance(dataToPredictJPlus7);
		System.out.println("\nPrediction for " + dataToPredictJPlus7.stringValue(0) + " : " + predictedValueJPlus7);
		System.out.println("\nReal value for " + dataToPredictJPlus7.stringValue(0) + " : "
				+ dataToPredictJPlus7.value(11) + "\n");

		// J+14
		Instance dataToPredictJPlus14 = dataSetJPlus14.remove(dataSet.size() - 14);
		trainSet = dataSetJPlus14.trainCV(5, 4);
		testSet = dataSetJPlus14.testCV(5, 4);

		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		System.out.println(dataSetJPlus14.relationName());
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		double predictedValueJPlus14 = lrClassifier.classifyInstance(dataToPredictJPlus14);
		System.out.println("\nPrediction for " + dataToPredictJPlus14.stringValue(0) + " : " + predictedValueJPlus14);
		System.out.println("\nReal value for " + dataToPredictJPlus14.stringValue(0) + " : "
				+ dataToPredictJPlus14.value(11) + "\n");

		// J+21
		Instance dataToPredictJPlus21 = dataSetJPlus21.remove(dataSet.size() - 21);
		trainSet = dataSetJPlus21.trainCV(5, 4);
		testSet = dataSetJPlus21.testCV(5, 4);

		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		System.out.println(dataSetJPlus21.relationName());
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		double predictedValueJPlus21 = lrClassifier.classifyInstance(dataToPredictJPlus21);
		System.out.println("\nPrediction for " + dataToPredictJPlus21.stringValue(0) + " : " + predictedValueJPlus21);
		System.out.println("\nReal value for " + dataToPredictJPlus21.stringValue(0) + " : "
				+ dataToPredictJPlus21.value(11) + "\n");

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));
	}
}
