package fr.covidmodelizer.biology.svir;

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

public class VaccinationSVIRModel extends BiologicalModel {

    public static void main(String[] args) throws Exception {
        VaccinationSVIRModel model = new VaccinationSVIRModel();
        model.predict();
    }

    @Override
    public void predict() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.SVIR_VACCINATION_DATA_CSV))
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
                predictiveSVIR;
        String[]
                predictiveData;
        LocalDate
                nextDate;

        // Displaying model details and prediction
        for (int i = 0; i < expanse; i++) {
            currentIndex = expanseIndex + i;
            predictiveData = data.get(currentIndex);
            predictiveSVIR = SVIRCalculation(predictiveData);
            PredictionModel.details(predictiveSVIR, predictiveData, data.get(currentIndex + 1));
        }

        // Writing predictions in the dedicated file
        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.SVIR_VACCINATION_PREDICTION_CSV));
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
            predictiveSVIR = SVIRCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), data.get(currentIndex + 1)[2],
                    predictiveSVIR[1] < 0 ? "0" : String.valueOf((int) predictiveSVIR[1])});
        }
        predictiveData = data.get(lastIndex);
        for (int i = 0; i < expanse; i++) {
            nextDate = LocalDate.parse(predictiveData[0],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(i + 1);
            predictiveSVIR = SVIRCalculation(predictiveData);
            csvWriter.writeNext(new String[]{nextDate.toString(), "",
                    predictiveSVIR[1] < 0 ? "0" : String.valueOf((int) predictiveSVIR[1])});
            for (int j = 0; j < 5; j++) {
                predictiveData[j + 1] = String.valueOf(predictiveSVIR[j]);
            }
        }

        System.out.println(ConsoleColors.PURPLE + "\nComputation time : "
                + LocalTime.now().minusNanos(START.toNanoOfDay()) + ConsoleColors.RESET);

        csvWriter.close();
    }
}