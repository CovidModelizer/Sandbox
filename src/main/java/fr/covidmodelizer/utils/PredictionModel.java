package fr.covidmodelizer.utils;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface PredictionModel {

    static void details(final LinearRegression classifier,
                        final Evaluation evaluation,
                        final Instance predictiveData,
                        final int classIndex,
                        final int nbDays) throws Exception {
        final LocalDate nextDate = LocalDate.parse(predictiveData.stringValue(0),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(nbDays + 1);
        System.out.println("\n** Linear Regression Evaluation **\n" + evaluation.toSummaryString());
        System.out.print(ConsoleColors.CYAN + "** Details of the classifier **\n"
                + classifier + ConsoleColors.RESET + "\n");
        System.out.println(ConsoleColors.BLUE + "\nPrediction on " + predictiveData.stringValue(0) + " : "
                + classifier.classifyInstance(predictiveData) + ConsoleColors.RESET);
        System.out.println("\nReal value for " + nextDate + " : " + predictiveData.value(classIndex) + "\n");
    }

    static void details(final double[] predictiveModel,
                        final String[] predictiveData,
                        final String[] comparativeData) {
        System.out.println(ConsoleColors.BLUE + "\nPrediction on " + predictiveData[0] + " : "
                + predictiveModel[1] + ConsoleColors.RESET);
        System.out.println("\nReal value for " + comparativeData[0] + " : " + comparativeData[2] + "\n");
    }

    void calculate() throws Exception;
}