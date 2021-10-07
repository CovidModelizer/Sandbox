package fr.covidmodelizer.linear;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.utils.DataUtils;
import weka.core.Attribute;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class VaccinationLinearModel extends LinearModel {

    private final static String DATA_LIN_VACCIN_CSV = "resources/data/lin-data-vaccination.csv";
    private final static String LIN_VACCIN_PREDICTION = "resources/predictions/lin-vaccination-prediction.csv";

    public static void main(String[] args) throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_LIN_VACCIN_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        // Préparation du dataset
        ArrayList<Attribute> atts = DataUtils.prepare(data);
        atts.add(new Attribute("cumul vaccinations J+1"));

        predict(data, atts, LIN_VACCIN_PREDICTION);
    }
}
