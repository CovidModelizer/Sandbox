package fr.covidmodelizer.machinelearning;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.utils.DataUtils;
import weka.core.Attribute;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class InfectionMachineLearningModel extends MachineLearningModel {

    private final static String DATA_ML_INF_CSV = "resources/data/ml-data-infection.csv";
    private final static String ML_INF_PREDICTION = "resources/predictions/ml-infection-prediction.csv";

    public static void main(String[] args) throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_ML_INF_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Préparation du dataset
        ArrayList<Attribute> atts = DataUtils.prepare(data);
        atts.add(new Attribute("nouveaux cas J+N"));

        predict(data, atts, ML_INF_PREDICTION);
    }
}
