package fr.covidmodelizer.biology.sir;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import fr.covidmodelizer.biology.BiologicalModel;
import fr.covidmodelizer.utils.ConsoleColors;
import fr.covidmodelizer.utils.PredictionModel;
import fr.covidmodelizer.utils.ProjectFiles;

import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InfectionSIRModel extends BiologicalModel {

    public static void main(String[] args) throws Exception {
        InfectionSIRModel model = new InfectionSIRModel();
        model.predict();
    }

    @Override
    public void predict() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.SIR_INFECTION_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Initialization
        final int
                expanse = 21,
                expanseX2 = expanse * 2,
                lastIndex = data.size() - 1,
                expanseIndex = lastIndex - expanse,
                expanseX2Index = lastIndex - expanseX2;
        int
                currentIndex;
        double[]
                predictiveSIR;
        String[]
                predictiveData;
        LocalDate
                nextDate;

        // Displaying model details and prediction
        for (int i = 0; i < expanse; i++) {
            currentIndex = expanseIndex + i;
            predictiveData = data.get(currentIndex);
            predictiveSIR = SIRCalculation(predictiveData);
            PredictionModel.details(predictiveSIR, predictiveData, data.get(currentIndex + 1));
        }

        // Writing predictions in the dedicated file
        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.SIR_INFECTION_PREDICTION_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        csvWriter.writeNext(new String[]{"date", "real value", "prediction value"});

        final LocalTime START = LocalTime.now();

        // Predictions
        for (int i = 0; i < expanseX2; i++) {
            currentIndex = expanseX2Index + i;
            predictiveData = data.get(currentIndex);
            nextDate = LocalDate.parse(predictiveData[0],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
            predictiveSIR = SIRCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), data.get(currentIndex + 1)[2],
                    predictiveSIR[1] < 0 ? "0" : String.valueOf((int) predictiveSIR[1])});
        }
        predictiveData = data.get(lastIndex);
        for (int i = 0; i < expanse; i++) {
            nextDate = LocalDate.parse(predictiveData[0],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(i + 1);
            predictiveSIR = SIRCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveSIR[1] < 0 ? "0" : String.valueOf((int) predictiveSIR[1])});
            for (int j = 0; j < 3; j++) {
                predictiveData[j + 1] = String.valueOf(predictiveSIR[j]);
            }
        }

        System.out.println(ConsoleColors.PURPLE + "\nComputation time : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        csvWriter.close();
    }
}
