package fr.covidmodelizer.biologic;

import com.opencsv.CSVWriter;
import fr.covidmodelizer.utils.ConsoleColors;
import fr.covidmodelizer.utils.PredictionModel;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class BiologicalModel implements PredictionModel {
    // Data details
    // N → Approximate population of France
    // d → Average duration of infection before recovery or death
    // Isolation time after COVID-19 positivity :
    // https://www.ameli.fr/assure/covid-19/isolement-principes-et-regles-respecter/isolement-principes-generaux
    // gamma → 1 / d → Recovery rate
    // initialS → Number of people susceptible to be infected
    // initialV → Number of vaccinated people
    // initialI → Number of infected people
    // initialR → Number of cured people
    // vaccinRate → Daily vaccination rate
    // r0 → Virus reproduction rate
    // beta → r0 * gamma → Transmission rate

    protected final static double
            N = 67000000.0,
            d = 10.0,
            gamma = 1.0 / d;
    protected static double
            beta;

    protected static void biologicalCalculation(final List<String[]> data,
                                                final String model,
                                                final String predictionFilePath) throws IOException {
        // Initializing
        final boolean
                isModelSIR = model.equals("SIR");
        final int
                expanse = 21,
                expanseX2 = expanse * 2,
                lastIndex = data.size() - 1,
                expanseIndex = lastIndex - expanse,
                expanseX2Index = lastIndex - expanseX2,
                modelSize = isModelSIR ? 3 : 5;
        final CalculationInterface
                calculationInterface = isModelSIR ? BiologicalModel::SIRCalculation
                : BiologicalModel::SVIRCalculation;
        int
                currentIndex;
        double[]
                predictiveModel;
        String[]
                predictiveData;
        LocalDate
                nextDate;

        // Displaying model details and prediction
        for (int i = 0; i < expanse; i++) {
            currentIndex = expanseIndex + i;
            predictiveData = data.get(currentIndex);
            predictiveModel = calculationInterface.doCalculation(predictiveData);
            PredictionModel.details(predictiveModel, predictiveData, data.get(currentIndex + 1));
        }

        // Writing predictions in the dedicated file
        Writer writer = Files.newBufferedWriter(Paths.get(predictionFilePath));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});

        final LocalTime START = LocalTime.now();

        // Predicting
        for (int i = 0; i < expanseX2; i++) {
            currentIndex = expanseX2Index + i;
            predictiveData = data.get(currentIndex);
            nextDate = LocalDate.parse(predictiveData[0],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveModel = calculationInterface.doCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), data.get(currentIndex + 1)[2],
                    predictiveModel[1] < 0 ? "0" : String.valueOf((int) predictiveModel[1])});
        }
        predictiveData = data.get(lastIndex);
        for (int i = 0; i < expanse; i++) {
            nextDate = LocalDate.parse(predictiveData[0],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(i + 1);
            predictiveModel = calculationInterface.doCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveModel[1] < 0 ? "0" : String.valueOf((int) predictiveModel[1])});
            // Updating predictiveData with calculated values from predictiveModel
            for (int j = 0; j < modelSize; j++) {
                predictiveData[j + 1] = String.valueOf(predictiveModel[j]);
            }
        }

        System.out.println(ConsoleColors.PURPLE + "\nComputation time : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        csvWriter.close();
    }

    protected static double[] SIRCalculation(final String[] data) {
        // Initializing
        double
                initialS = Double.parseDouble(data[1]),
                initialI = Double.parseDouble(data[2]),
                initialR = Double.parseDouble(data[3]),
                r0 = Double.parseDouble(data[4]);

        /* ******* Calculation ******** */
        // dS = - beta * S * I
        // dI = beta * S * I - gamma * I
        // dR = gamma * I

        beta = r0 * gamma;
        double S = initialS - (beta / N) * initialS * initialI;
        double I = initialI + (beta / N) * initialS * initialI - gamma * initialI;
        double R = initialR + gamma * initialI;

        return new double[]{S, I, R};
    }

    protected static double[] SVIRCalculation(final String[] data) {
        // Initializing
        double
                initialS = Double.parseDouble(data[1]),
                initialV = Double.parseDouble(data[2]),
                initialI = Double.parseDouble(data[3]),
                initialR = Double.parseDouble(data[4]),
                initialVaccinRate = Double.parseDouble(data[5]),
                r0 = Double.parseDouble(data[6]);

        /* ******* Calculation ******** */
        // dS = - beta * S * I - vaccinRate * S
        // dV = vaccinRate * S
        // dI = beta * S * I - gamma * I
        // dR = gamma * I

        beta = r0 * gamma;
        double S = initialS - (beta / N) * initialS * initialI - initialVaccinRate * initialS;
        double V = initialV + initialVaccinRate * initialS;
        double I = initialI + (beta / N) * initialS * initialI - gamma * initialI;
        double R = initialR + gamma * initialI;
        double vaccinRate = initialVaccinRate * initialS / S;

        return new double[]{S, V, I, R, vaccinRate};
    }

    private interface CalculationInterface {
        // abstract for SIRCalculation and SVIRCalculation
        double[] doCalculation(final String[] data);
    }
}