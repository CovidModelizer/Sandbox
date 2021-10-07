package fr.covidmodelizer.linear;

import com.opencsv.CSVWriter;
import fr.covidmodelizer.utils.ConsoleColors;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class LinearModel {

    protected static void predict(final List<String[]> originalData, final ArrayList<Attribute> datasetAttributes,
                           final String filePath) throws Exception {
        final LocalTime START = LocalTime.now();

        Instances dataSet = new Instances("-- DATASET --", datasetAttributes, 0);
        dataSet.setClassIndex(datasetAttributes.size() - 1);

        for (int i = 1; i < originalData.size(); i++) {
            double[] instanceValue = new double[dataSet.numAttributes()];
            instanceValue[0] = dataSet.attribute("date").parseDate(originalData.get(i)[0]);
            instanceValue[1] = originalData.get(i)[1].isEmpty() ? Double.NaN :
                    Double.parseDouble(originalData.get(i)[1]);
            instanceValue[2] = (i + 1 >= originalData.size()) ? Double.NaN
                    : originalData.get(i + 1)[1].isEmpty() ? Double.NaN :
                    Double.parseDouble(originalData.get(i + 1)[1]);
            dataSet.add(new DenseInstance(1.0, instanceValue));
        }

        // Variables utiles à l'entraînement du modèle de prédiction
        int expanse = 21;

        double predictiveValue;

        Instances trainSet;
        Instances testSet;

        LinearRegression lrClassifier;

        Evaluation eval;

        Instance predictiveData;

        LocalDate nextDate;

        // Entraînement du modèle de régression linéaire à une variable
        trainSet = dataSet.trainCV(5, 0, new Random());
        testSet = dataSet.testCV(5, 0);

        lrClassifier = new LinearRegression();
        lrClassifier.setOptions(new String[]{"-R", "1"});
        lrClassifier.buildClassifier(trainSet);

        eval = new Evaluation(trainSet);
        eval.evaluateModel(lrClassifier, testSet);

        // Prédiction
        System.out.println("** Linear Regression Evaluation **");
        System.out.println(eval.toSummaryString());
        System.out.print("=> The expression of the classifier is : ");
        System.out.println(lrClassifier);

        predictiveData = dataSet.get(dataSet.size() - 2);
        System.out.println(ConsoleColors.BLUE + "\nPrediction on " + predictiveData.stringValue(0) + " : "
                + lrClassifier.classifyInstance(predictiveData)
                + ConsoleColors.RESET + " (value in dataset : "
                + predictiveData.value(dataSet.numAttributes() - 1) + ")");
        System.out.println("\nReal value for " + dataSet.lastInstance().stringValue(0) + " : "
                + dataSet.lastInstance().value(1) + "\n");

        System.out.println(ConsoleColors.PURPLE + "\nTemps de calcul : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});
        for (int i = 0; i < (2 * expanse); i++) {
            predictiveData = dataSet.instance(dataSet.size() - 1 - (2 * expanse) + i);
            nextDate = LocalDate.parse(predictiveData.stringValue(0),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveValue = lrClassifier.classifyInstance(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(),
                    String.valueOf((int) predictiveData.value(dataSet.numAttributes() - 1)),
                    predictiveValue < 0 ? "0" : String.valueOf((int) predictiveValue)});
        }
        predictiveData = dataSet.lastInstance();
        for (int i = 0; i < expanse; i++) {
            nextDate = LocalDate.parse(predictiveData.stringValue(0),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveValue = lrClassifier.classifyInstance(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveValue < 0 ? "0" : String.valueOf((int) predictiveValue)});
            predictiveData.setValue(0, dataSet.attribute("date").parseDate(nextDate.toString()));
            predictiveData.setValue(1, (int) predictiveValue);
        }
        csvWriter.close();
    }
}
