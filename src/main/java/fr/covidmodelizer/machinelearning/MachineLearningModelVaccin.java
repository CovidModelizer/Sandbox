package fr.covidmodelizer.machinelearning;

import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class MachineLearningModelVaccin {

	private final static LocalTime START = LocalTime.now();
	private final static String DATA_ML_VACCIN_CSV = "src/main/resources/ml-data-vaccin.csv";
	private final static String ML_VACCIN_PREDICTION = "ml-vaccin-prediction.csv";

	public static void main(String[] args) throws Exception {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_ML_VACCIN_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		// Préparation du data set
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute(data.get(0)[0], "yyyy-MM-dd"));
		for (int i = 1; i < data.get(0).length; i++) {
			atts.add(new Attribute(data.get(0)[i]));
		}
		atts.add(new Attribute("nouveaux vaccines J+N"));

		Instances dataSet = new Instances("*** DATASET ***", atts, 0);
		dataSet.setClassIndex(atts.size() - 1);

		for (int i = 1; i < data.size(); i++) {
			double[] instanceValue = new double[dataSet.numAttributes()];
			instanceValue[0] = dataSet.attribute("date").parseDate(data.get(i)[0]);
			for (int j = 1; j < data.get(i).length; j++) {
				instanceValue[j] = data.get(i)[j].isEmpty() ? Double.NaN : Double.parseDouble(data.get(i)[j]);
			}
			instanceValue[instanceValue.length - 1] = Double.NaN;
			dataSet.add(new DenseInstance(1.0, instanceValue));
		}

		// TODO
	}
}
