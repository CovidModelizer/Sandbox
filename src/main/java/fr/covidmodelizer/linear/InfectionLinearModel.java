package fr.covidmodelizer.linear;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.utils.DataUtils;
import weka.core.Attribute;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class InfectionLinearModel extends LinearModel {

    private final static String DATA_LIN_INF_CSV = "resources/data/lin-data-infection.csv";
    private final static String LIN_INF_PREDICTION = "resources/predictions/lin-infection-prediction.csv";

    public static void main(String[] args) throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_LIN_INF_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Préparation du dataset
        ArrayList<Attribute> atts = DataUtils.prepare(data);
        atts.add(new Attribute("cumul infections J+1"));

        predict(data, atts, LIN_INF_PREDICTION);
    }
}
