package fr.covidmodelizer.machinelearning;

import com.opencsv.CSVWriter;
import fr.covidmodelizer.utils.ConsoleColors;
import fr.covidmodelizer.utils.PredictionModel;
import fr.covidmodelizer.utils.WekaDataset;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public abstract class MachineLearningModel implements PredictionModel, WekaDataset {

    protected static void univariateLinearRegression(final Instances dataset,
                                                     final String predictionFilePath) throws Exception {
        // Initialization
        final int
                expanse = 21,
                expanseX2 = expanse * 2,
                datasetSize = dataset.size(),
                lastIndex = datasetSize - 1,
                lastAttribute = dataset.numAttributes() - 1,
                expanseX2Index = lastIndex - expanseX2;
        double
                realValue,
                predictiveValue;
        Instances
                trainSet,
                testSet;
        LinearRegression
                lrClassifier;
        Evaluation
                evaluation;
        Instance
                predictiveData;
        LocalDate
                nextDate;

        // Training of the univariate linear regression model
        for (int i = 0; i < datasetSize; i++) {
            realValue = (i + 1) >= datasetSize ? Double.NaN : dataset.instance(i + 1).value(1);
            dataset.instance(i).setValue(lastAttribute, realValue);
        }

        trainSet = dataset.trainCV(5, 0, new Random());
        testSet = dataset.testCV(5, 0);

        lrClassifier = new LinearRegression();
        lrClassifier.setOptions(new String[]{"-R", "1"});
        lrClassifier.buildClassifier(trainSet);

        evaluation = new Evaluation(trainSet);
        evaluation.evaluateModel(lrClassifier, testSet);

        // Displaying model details
        predictiveData = dataset.get(lastIndex - 1);
        PredictionModel.details(lrClassifier, evaluation, predictiveData, lastAttribute, 1);

        // Writing predictions in the dedicated file
        Writer writer = Files.newBufferedWriter(Paths.get(predictionFilePath));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});

        final LocalTime START = LocalTime.now();

        // Predictions
        for (int i = 0; i < expanseX2; i++) {
            predictiveData = dataset.instance(expanseX2Index + i);
            nextDate = LocalDate.parse(predictiveData.stringValue(0),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveValue = lrClassifier.classifyInstance(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(),
                    String.valueOf((int) predictiveData.value(lastAttribute)),
                    predictiveValue < 0 ? "0" : String.valueOf((int) predictiveValue)});
        }
        predictiveData = dataset.lastInstance();
        for (int i = 0; i < expanse; i++) {
            nextDate = LocalDate.parse(predictiveData.stringValue(0),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveValue = lrClassifier.classifyInstance(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveValue < 0 ? "0" : String.valueOf((int) predictiveValue)});
            predictiveData.setValue(0, dataset.attribute("date").parseDate(nextDate.toString()));
            predictiveData.setValue(1, (int) predictiveValue);
        }

        System.out.println(ConsoleColors.PURPLE + "\nComputation time : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        csvWriter.close();
    }

    protected static void multivariateLinearRegression(final Instances dataset,
                                                       final String predictionFilePath) throws Exception {
        // Initialization
        final int
                expanse = 21,
                datasetSize = dataset.size(),
                lastIndex = datasetSize - 1,
                lastAttribute = dataset.numAttributes() - 1,
                expanseIndex = lastIndex - expanse;
        final String
                lastAttributeName = dataset.attribute(lastAttribute).name();
        int
                currentIndex;
        double
                realValue,
                predictiveValue;
        Instances[]
                allDataSet = new Instances[expanse];
        Instances
                trainSet,
                testSet;
        LinearRegression[]
                lrClassifier = new LinearRegression[expanse];
        Evaluation[]
                evaluation = new Evaluation[expanse];
        Instance
                predictiveData;
        LocalDate
                nextDate;

        // Training of the multivariate linear regression model
        for (int n = 1; n <= expanse; n++) {
            for (int i = 0; i < datasetSize; i++) {
                realValue = (i + n) >= datasetSize ? Double.NaN : dataset.instance(i + n).value(5);
                dataset.instance(i).setValue(lastAttribute, realValue);
            }

            dataset.renameAttribute(lastAttribute,
                    lastAttributeName.substring(0, lastAttributeName.length() - 1) + n);

            trainSet = dataset.trainCV(5, 0, new Random());
            testSet = dataset.testCV(5, 0);

            lrClassifier[n - 1] = new LinearRegression();
            lrClassifier[n - 1].setOptions(new String[]{"-R", "1"});
            lrClassifier[n - 1].buildClassifier(trainSet);

            evaluation[n - 1] = new Evaluation(trainSet);
            evaluation[n - 1].evaluateModel(lrClassifier[n - 1], testSet);

            allDataSet[n - 1] = new Instances(dataset);
        }

        // Displaying model details
        for (int i = 0; i < expanse; i++) {
            predictiveData = allDataSet[i].get(expanseIndex);
            PredictionModel.details(lrClassifier[i], evaluation[i], predictiveData, lastAttribute, i);
        }

        // Writing predictions in the dedicated file
        Writer writer = Files.newBufferedWriter(Paths.get(predictionFilePath));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});

        final LocalTime START = LocalTime.now();

        // Predictions
        for (int x = 2; x >= 0; x--) {
            currentIndex = lastIndex - (x * expanse);
            for (int i = 0; i < expanse; i++) {
                predictiveData = allDataSet[i].instance(currentIndex);
                nextDate = LocalDate.parse(predictiveData.stringValue(0),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(i + 1);
                realValue = predictiveData.value(lastAttribute);
                predictiveValue = lrClassifier[i].classifyInstance(predictiveData);
                csvWriter.writeNext(new String[]{nextDate.toString(),
                        Double.isNaN(realValue) ? "" : String.valueOf((int) realValue),
                        predictiveValue < 0 ? "0" : String.valueOf((int) predictiveValue)});
            }
        }

        System.out.println(ConsoleColors.PURPLE + "\nComputation time : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        csvWriter.close();
    }
}