package fr.covidmodelizer.machinelearning;

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

import fr.covidmodelizer.utils.ConsoleColors;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InfectionMachineLearningModel {

    private final static LocalTime START = LocalTime.now();
    private final static String DATA_ML_INF_CSV = "resources/data/ml-data-infection.csv";
    private final static String ML_INF_PREDICTION = "resources/predictions/ml-infection-prediction.csv";

    public static void main(String[] args) throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_ML_INF_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Préparation du dataset
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        atts.add(new Attribute(data.get(0)[0], "yyyy-MM-dd"));
        for (int i = 1; i < data.get(0).length; i++) {
            atts.add(new Attribute(data.get(0)[i]));
        }
        atts.add(new Attribute("nouveaux cas J+N"));

        Instances dataSet = new Instances("*** DATASET ***", atts, 0);
        dataSet.setClassIndex(atts.size() - 1);

        for (int i = 1; i < data.size(); i++) {
            double[] instanceValue = new double[dataSet.numAttributes()];
            instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
            for (int j = 1; j < data.get(i).length; j++) {
                instanceValue[j] = data.get(i)[j].isEmpty() ? Double.NaN : Double.parseDouble(data.get(i)[j]);
            }
            instanceValue[instanceValue.length - 1] = Double.NaN;
            for (int j = 1; j < instanceValue.length; j++) {
                instanceValue[j] = Double.isNaN(instanceValue[j]) ? instanceValue[j]
                        : instanceValue[j] < 0.0 ? Double.NaN : instanceValue[j];
            }
            dataSet.add(new DenseInstance(1.0, instanceValue));
        }

        // Variables utiles à l'entraînement du modèle de prédiction
        int expanse = 21;

        double realValue;

        Instances[] allDataSet = new Instances[expanse];

        Instances trainSet;
        Instances testSet;

        LinearRegression[] lrClassifier = new LinearRegression[expanse];

        Evaluation[] eval = new Evaluation[expanse];

        Instance dataToPredict;
        Instance predictiveData;

        LocalDate nextDate;

        // Entraînement du modèle de régression linéaire à plusieurs variables
        for (int n = 1; n <= expanse; n++) {
            for (int i = 0; i < dataSet.size(); i++) {
                realValue = (i + n) >= dataSet.size() ? Double.NaN : dataSet.instance(i + n).value(5);
                dataSet.instance(i).setValue(dataSet.numAttributes() - 1, realValue);
            }

            dataSet.renameAttribute(dataSet.numAttributes() - 1, "nouveaux cas J+" + n);

            trainSet = dataSet.trainCV(5, 0, new Random());
            testSet = dataSet.testCV(5, 0);

            lrClassifier[n - 1] = new LinearRegression();
            lrClassifier[n - 1].setOptions(new String[]{"-R", "1"});
            lrClassifier[n - 1].buildClassifier(trainSet);

            eval[n - 1] = new Evaluation(trainSet);
            eval[n - 1].evaluateModel(lrClassifier[n - 1], testSet);

            allDataSet[n - 1] = new Instances(dataSet);
        }

        // Prédictions sur 21 (= expanse) jours
        for (int i = 0; i < allDataSet.length; i++) {
            dataToPredict = allDataSet[i].get(allDataSet[i].size() - expanse + i);

            System.out.println("\n** Linear Regression Evaluation **");
            System.out.println(eval[i].toSummaryString());
            System.out.print("=> The expression for the input data as per algorithm is : ");
            System.out.println(lrClassifier[i]);

            predictiveData = allDataSet[i].get(allDataSet[i].size() - 1 - expanse);
            System.out.println(ConsoleColors.BLUE + "\nPrediction on " + predictiveData.stringValue(0) + " : "
                    + lrClassifier[i].classifyInstance(predictiveData) + ConsoleColors.RESET + " (value in dataset : "
                    + predictiveData.value(dataSet.numAttributes() - 1) + ")");
            System.out.println(
                    "\nReal value for " + dataToPredict.stringValue(0) + " : " + dataToPredict.value(5) + "\n");

            for (int j = 1; j < dataSet.numAttributes(); j++) {
                if (lrClassifier[i].coefficients()[j] != 0.0) {
                    System.out.println(dataSet.attribute(j).name() + " : " + lrClassifier[i].coefficients()[j]);
                }
            }
        }

        System.out.println(ConsoleColors.PURPLE + "\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        CSVWriter csvWriter = new CSVWriter(new FileWriter(ML_INF_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});
        for (int i = 0; i < expanse; i++) {
            predictiveData = allDataSet[i].instance(dataSet.size() - 1 - (2 * expanse));
            nextDate = LocalDate.parse(predictiveData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .plusDays(i + 1);
            csvWriter.writeNext(new String[]{nextDate.toString(),
                    String.valueOf((int) Math.ceil(predictiveData.value(dataSet.numAttributes() - 1))),
                    lrClassifier[i].classifyInstance(predictiveData) < 0 ? "0"
                            : String.valueOf((int) Math.ceil(lrClassifier[i].classifyInstance(predictiveData)))});
        }
        for (int i = 0; i < expanse; i++) {
            predictiveData = allDataSet[i].instance(dataSet.size() - 1 - expanse);
            nextDate = LocalDate.parse(predictiveData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .plusDays(i + 1);
            csvWriter.writeNext(new String[]{nextDate.toString(),
                    String.valueOf((int) Math.ceil(predictiveData.value(dataSet.numAttributes() - 1))),
                    lrClassifier[i].classifyInstance(predictiveData) < 0 ? "0"
                            : String.valueOf((int) Math.ceil(lrClassifier[i].classifyInstance(predictiveData)))});
        }
        for (int i = 0; i < expanse; i++) {
            predictiveData = allDataSet[i].lastInstance();
            nextDate = LocalDate.parse(predictiveData.stringValue(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .plusDays(i + 1);
            csvWriter.writeNext(
                    new String[]{nextDate.toString(), "", lrClassifier[i].classifyInstance(predictiveData) < 0 ? "0"
                            : String.valueOf((int) Math.ceil(lrClassifier[i].classifyInstance(predictiveData)))});
        }
        csvWriter.close();
    }
}
