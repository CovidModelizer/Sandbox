package fr.covidmodelizer.machinelearning;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.utils.DataUtils;
import weka.core.Attribute;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class VaccinationMachineLearningModel extends MachineLearningModel {

    private final static String DATA_ML_VACCIN_CSV = "resources/data/ml-data-vaccination.csv";
    private final static String ML_VACCIN_PREDICTION = "resources/predictions/ml-vaccination-prediction.csv";

    public static void main(String[] args) throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_ML_VACCIN_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Préparation du dataset
        ArrayList<Attribute> atts = DataUtils.prepare(data);
        atts.add(new Attribute("nouveaux vaccines J+N"));

        predict(data, atts, ML_VACCIN_PREDICTION);
    }
}
