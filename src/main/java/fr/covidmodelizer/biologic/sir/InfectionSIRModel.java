package fr.covidmodelizer.biologic.sir;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.biologic.BiologicalModel;
import fr.covidmodelizer.utils.ProjectFiles;

import java.io.FileReader;
import java.util.List;

public class InfectionSIRModel extends BiologicalModel {

    public static void main(String[] args) throws Exception {
        InfectionSIRModel model = new InfectionSIRModel();
        model.calculate();
    }

    @Override
    public void calculate() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.SIR_INFECTION_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Predicting
        biologicalCalculation(data, "SIR", ProjectFiles.SIR_INFECTION_PREDICTION_CSV);
    }
}