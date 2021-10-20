package fr.covidmodelizer.machinelearning;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import fr.covidmodelizer.utils.ProjectFiles;
import fr.covidmodelizer.utils.WekaDataset;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class InfectionMachineLearningModel extends MachineLearningModel {

    public static void main(String[] args) throws Exception {
        InfectionMachineLearningModel model = new InfectionMachineLearningModel();
        model.predict();
    }

    @Override
    public void predict() throws Exception {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.MACHINE_LEARNING_INFECTION_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();
        // Initializing dataset
        Instances dataset = initDataset(data);
        // Calculating prediction
        machineLearningPrediction(dataset, ProjectFiles.MACHINE_LEARNING_INFECTION_PREDICTION_CSV);
    }

    @Override
    public Instances initDataset(List<String[]> data) throws ParseException {
        // Defining dataset attributes
        ArrayList<Attribute> atts = WekaDataset.parseAttributes(data);
        atts.add(new Attribute("nouvelles_infections_J+N"));

        Instances dataset = new Instances("InfectionMachineLearningModel dataset", atts, 0);
        dataset.setClassIndex(atts.size() - 1);

        // Filling the dataset
        dataset.addAll(WekaDataset.parseDataToInstances(data, atts));

        return dataset;
    }
}
