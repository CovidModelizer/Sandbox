package fr.covidmodelizer.machinelearning.univariate;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.machinelearning.MachineLearningModel;
import fr.covidmodelizer.utils.ProjectFiles;
import fr.covidmodelizer.utils.WekaDataset;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class VaccinationUnivariateModel extends MachineLearningModel {

    public static void main(String[] args) throws Exception {
        VaccinationUnivariateModel model = new VaccinationUnivariateModel();
        model.calculate();
    }

    @Override
    public void calculate() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.UNIVARIATE_VACCINATION_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();
        // Initializing dataset
        Instances dataset = initDataset(data);
        // Calculating prediction
        univariateLinearRegression(dataset, ProjectFiles.UNIVARIATE_VACCINATION_PREDICTION_CSV);
    }

    @Override
    public Instances initDataset(List<String[]> data) throws ParseException {
        // Defining dataset attributes
        ArrayList<Attribute> atts = WekaDataset.parseAttributes(data);
        atts.add(new Attribute("cumul_vaccinations_J+1"));

        Instances dataset = new Instances("VaccinationLinearModel dataset", atts, 0);
        dataset.setClassIndex(atts.size() - 1);

        // Filling the dataset
        dataset.addAll(WekaDataset.parseDataToInstances(data, atts));

        return dataset;
    }
}