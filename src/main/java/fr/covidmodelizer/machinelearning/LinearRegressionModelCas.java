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
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).withSkipLines(1).build();
		List<String[]> data = csvReader.readAll();

		ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
		atts.add(new Attribute("date", "yyyy-MM-dd"));
		atts.add(new Attribute("cas confirmes"));
		atts.add(new Attribute("moyenne sur a jour(s)"));
		atts.add(new Attribute("moyenne sur b jour(s)"));
		atts.add(new Attribute("moyenne sur c jour(s)"));
		atts.add(new Attribute("moyenne sur d jour(s)"));
		atts.add(new Attribute("moyenne sur e jour(s)"));
		atts.add(new Attribute("moyenne sur f jour(s)"));
		atts.add(new Attribute("moyenne sur g jour(s)"));
		atts.add(new Attribute("nouveaux cas"));
		String dataOrigin = "https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_";

		int expanse = 21, bestA = 3, bestB = 7, bestC = 8, bestD = 9, bestE = 15, bestF = 21, bestG = 1;
		double bestScore = Double.MAX_VALUE;
		Instances dataSet = null;
		Instances trainSet = null;
		Instances testSet = null;
		LinearRegression lrClassifier = new LinearRegression();
		Evaluation eval = null;

		boolean searching = false;

		if (searching) {
			for (int a = 1; a < expanse; a++) {
				for (int b = a + 1; b < expanse + 1; b++) {
					for (int c = b + 1; c < expanse + 2; c++) {
						for (int d = c + 1; d < expanse + 3; d++) {
							for (int e = d + 1; e < expanse + 4; e++) {
								for (int f = e + 1; f < expanse + 5; f++) {
									for (int g = f + 1; g < expanse + 6; g++) {

										dataSet = new Instances("** Instances from : " + dataOrigin + " **", atts, 0);

										for (int i = 0; i < data.size(); i++) {
											double[] instanceValue = new double[dataSet.numAttributes()];
											instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
											instanceValue[1] = Integer.parseInt(data.get(i)[1]);
											instanceValue[2] = (i > a)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - 1 - a).value(1)) / a
													: instanceValue[1] / a;
											instanceValue[2] = instanceValue[2] > 0 ? instanceValue[2] : 0;
											instanceValue[3] = (i > b)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - b).value(1)) / b
													: instanceValue[1] / b;
											instanceValue[3] = instanceValue[3] > 0 ? instanceValue[3] : 0;
											instanceValue[4] = (i > c)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - c).value(1)) / c
													: instanceValue[1] / c;
											instanceValue[4] = instanceValue[4] > 0 ? instanceValue[4] : 0;
											instanceValue[5] = (i > d)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - d).value(1)) / d
													: instanceValue[1] / d;
											instanceValue[5] = instanceValue[5] > 0 ? instanceValue[5] : 0;
											instanceValue[6] = (i > e)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - e).value(1)) / e
													: instanceValue[1] / e;
											instanceValue[6] = instanceValue[6] > 0 ? instanceValue[6] : 0;
											instanceValue[7] = (i > f)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - f).value(1)) / f
													: instanceValue[1] / f;
											instanceValue[7] = instanceValue[7] > 0 ? instanceValue[7] : 0;
											instanceValue[8] = (i > g)
													? (dataSet.instance(i - 1).value(1)
															- dataSet.instance(i - g).value(1)) / g
													: instanceValue[1] / g;
											instanceValue[8] = instanceValue[8] > 0 ? instanceValue[8] : 0;
											instanceValue[9] = (i > 0)
													? instanceValue[1] - dataSet.lastInstance().value(1)
													: instanceValue[1];
											instanceValue[9] = instanceValue[9] > 0 ? instanceValue[9] : 0;
											dataSet.add(new DenseInstance(1.0, instanceValue));
										}

										dataSet.setClassIndex(9);

										// Entraînement du modèle de régression linéaire
										trainSet = dataSet.trainCV(5, 4);
										testSet = dataSet.testCV(5, 4);

										lrClassifier.buildClassifier(trainSet);

										eval = new Evaluation(trainSet);
										eval.evaluateModel(lrClassifier, testSet);

										if (Math.abs(eval.correlationCoefficient()) > 0.5
												&& bestScore > eval.rootMeanSquaredError()) {
											bestScore = eval.rootMeanSquaredError();
											bestA = a;
											bestB = b;
											bestC = c;
											bestD = d;
											bestE = e;
											bestF = f;
											bestG = g;
										}

										dataSet.clear();
									}
								}
							}
						}
					}
				}
			}
		}

		dataSet = new Instances("** Instances from : " + dataOrigin + " **", atts, 0);
		dataSet.renameAttribute(2, "Moyenne sur " + bestA + " jour(s)");
		dataSet.renameAttribute(3, "Moyenne sur " + bestB + " jour(s)");
		dataSet.renameAttribute(4, "Moyenne sur " + bestC + " jour(s)");
		dataSet.renameAttribute(5, "Moyenne sur " + bestD + " jour(s)");
		dataSet.renameAttribute(6, "Moyenne sur " + bestE + " jour(s)");
		dataSet.renameAttribute(7, "Moyenne sur " + bestF + " jour(s)");
		dataSet.renameAttribute(8, "Moyenne sur " + bestG + " jour(s)");

		for (int i = 0; i < data.size(); i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
			instanceValue[1] = Integer.parseInt(data.get(i)[1]);
			instanceValue[2] = (i > bestA)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - 1 - bestA).value(1)) / bestA
					: instanceValue[1] / bestA;
			instanceValue[2] = instanceValue[2] > 0 ? instanceValue[2] : 0;
			instanceValue[3] = (i > bestB)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestB).value(1)) / bestB
					: instanceValue[1] / bestB;
			instanceValue[3] = instanceValue[3] > 0 ? instanceValue[3] : 0;
			instanceValue[4] = (i > bestC)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestC).value(1)) / bestC
					: instanceValue[1] / bestC;
			instanceValue[4] = instanceValue[4] > 0 ? instanceValue[4] : 0;
			instanceValue[5] = (i > bestD)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestD).value(1)) / bestD
					: instanceValue[1] / bestD;
			instanceValue[5] = instanceValue[5] > 0 ? instanceValue[5] : 0;
			instanceValue[6] = (i > bestE)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestE).value(1)) / bestE
					: instanceValue[1] / bestE;
			instanceValue[6] = instanceValue[6] > 0 ? instanceValue[6] : 0;
			instanceValue[7] = (i > bestF)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestF).value(1)) / bestF
					: instanceValue[1] / bestF;
			instanceValue[7] = instanceValue[7] > 0 ? instanceValue[7] : 0;
			instanceValue[8] = (i > bestG)
					? (dataSet.instance(i - 1).value(1) - dataSet.instance(i - bestG).value(1)) / bestG
					: instanceValue[1] / bestG;
			instanceValue[8] = instanceValue[8] > 0 ? instanceValue[8] : 0;
			instanceValue[9] = (i > 0) ? instanceValue[1] - dataSet.lastInstance().value(1) : instanceValue[1];
			instanceValue[9] = instanceValue[9] > 0 ? instanceValue[9] : 0;
			dataSet.add(new DenseInstance(1.0, instanceValue));
		}

		dataSet.setClassIndex(9);
		dataSet.deleteAttributeAt(8);

		// Entraînement du modèle de régression linéaire
		Instance dataToPredict = dataSet.remove(dataSet.size() - 1);
		trainSet = dataSet.trainCV(5, 4);
		testSet = dataSet.testCV(5, 4);

		lrClassifier.buildClassifier(trainSet);

		eval = new Evaluation(trainSet);
		eval.evaluateModel(lrClassifier, testSet);

		System.out.println(dataSet.relationName());
		System.out.println("** Linear Regression Evaluation **");
		System.out.println(eval.toSummaryString());
		System.out.print("=> The expression for the input data as per algorithm is : ");
		System.out.println(lrClassifier);

		double predictedValue = lrClassifier.classifyInstance(dataToPredict);
		System.out.println("\nPrediction for " + dataToPredict.stringValue(0) + " : " + predictedValue);
		System.out.println("\nReal value for " + dataToPredict.stringValue(0) + " : " + dataToPredict.value(8));

		System.out.println("\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()));
	}
}
