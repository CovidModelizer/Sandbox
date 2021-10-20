package fr.covidmodelizer.biologic.svir;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.biologic.BiologicalModel;
import fr.covidmodelizer.utils.ProjectFiles;

import java.io.FileReader;
import java.util.List;

public class VaccinationSVIRModel extends BiologicalModel {

    public static void main(String[] args) throws Exception {
        VaccinationSVIRModel model = new VaccinationSVIRModel();
        model.calculate();
    }

    @Override
    public void calculate() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.SVIR_VACCINATION_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Predicting
        biologicalCalculation(data, "SVIR", ProjectFiles.SVIR_VACCINATION_PREDICTION_CSV);
    }
}