package fr.covidmodelizer.sir;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import fr.covidmodelizer.utils.ConsoleColors;

public class InfectionSIRModel {

    private final static LocalTime START = LocalTime.now();
    private final static String DATA_SIR_INF_CSV = "src/main/resources/sir-data-infection.csv";
    private final static String SIR_INF_PREDICTION = "sir-infection-prediction.csv";

    public static void main(String[] args) throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_SIR_INF_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Details des données
        // N → Population totale de France
        // initialS → Population initialement susceptible d'etre infecte
        // initialI → Nombre initial de personnes infectées
        // initialR → Nombre de personnes remises
        // D → durée moyenne d'infection avant guérison ou décès
        // Durée d'isolement après une positivité à la covid :
        // https://www.ameli.fr/assure/covid-19/isolement-principes-et-regles-respecter/isolement-principes-generaux
        // r0 → taux de reproduction du virus
        // gamma → 1 / D → taux de guérison
        // beta → r0 * gamma → taux de transmission

        int firstDay = data.size() - 22;

        // Initialisation
        final double N = 67000000.0;
        double initialS;
        double initialI;
        double initialR;
        double r0;
        double d = 10.0;
        double gamma = 1.0 / d;
        double beta;

        int expanse = 21;

        LocalDate nextDate;

        // Calcul du SIR
        double[] predictiveSIR = null;

        for (int i = 0; i < expanse; i++) {
            initialS = Double.parseDouble(data.get(firstDay + i)[1]);
            initialI = Double.parseDouble(data.get(firstDay + i)[2]);
            initialR = Double.parseDouble(data.get(firstDay + i)[3]);
            r0 = Double.parseDouble(data.get(firstDay + i)[4]);
            beta = r0 * gamma;
            predictiveSIR = SIRCalculation(N, initialS, initialI, initialR, gamma, beta);
            System.out.println("\nPrediction on " + data.get(firstDay + i)[0] + " : " + predictiveSIR[1]
                    + " (value in dataset : " + data.get(firstDay + 1 + i)[2] + ")");
            System.out.println(
                    "\nReal value for " + data.get(firstDay + 1 + i)[0] + " : " + data.get(firstDay + 1 + i)[2] + "\n");
        }

        System.out.println(ConsoleColors.PURPLE + "\nTemps de calcul : " + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        CSVWriter csvWriter = new CSVWriter(new FileWriter(SIR_INF_PREDICTION), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});

        for (int i = 0; i < (2 * expanse); i++) {
            initialS = Double.parseDouble(data.get(data.size() - 1 - (2 * expanse) + i)[1]);
            initialI = Double.parseDouble(data.get(data.size() - 1 - (2 * expanse) + i)[2]);
            initialR = Double.parseDouble(data.get(data.size() - 1 - (2 * expanse) + i)[3]);
            r0 = Double.parseDouble(data.get(data.size() - 1 - (2 * expanse) + i)[4]);
            beta = r0 * gamma;
            predictiveSIR = SIRCalculation(N, initialS, initialI, initialR, gamma, beta);
            nextDate = LocalDate
                    .parse(data.get(data.size() - 1 - (2 * expanse) + i)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .plusDays(1);
            csvWriter.writeNext(new String[]{nextDate.toString(), data.get(data.size() - (2 * expanse) + i)[2],
                    predictiveSIR[1] < 0 ? "0" : String.valueOf((int) predictiveSIR[1])});
        }
        predictiveSIR[0] = Double.parseDouble(data.get(data.size() - 1)[1]);
        predictiveSIR[1] = Double.parseDouble(data.get(data.size() - 1)[2]);
        predictiveSIR[2] = Double.parseDouble(data.get(data.size() - 1)[3]);
        r0 = Double.parseDouble(data.get(data.size() - 1)[4]);
        beta = r0 * gamma;
        for (int i = 0; i < expanse; i++) {
            predictiveSIR = SIRCalculation(N, predictiveSIR[0], predictiveSIR[1], predictiveSIR[2], gamma, beta);
            nextDate = LocalDate.parse(data.get(data.size() - 1)[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .plusDays(i + 1);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveSIR[1] < 0 ? "0" : String.valueOf((int) predictiveSIR[1])});
        }
        csvWriter.close();
    }

    public static double[] SIRCalculation(double N, double S0, double I0, double R0, double gamma, double beta) {
        /* ******* Parameters ******** */
        // dS = - beta * S * I
        // dI = beta * S * I - gamma * I
        // dR = gamma * I

        double S = S0 - (beta / N) * S0 * I0;
        double I = I0 + (beta / N) * S0 * I0 - gamma * I0;
        double R = R0 + gamma * I0;

        return new double[]{S, I, R};
    }
}
