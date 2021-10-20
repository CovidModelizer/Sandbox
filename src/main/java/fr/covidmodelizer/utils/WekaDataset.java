package fr.covidmodelizer.utils;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface WekaDataset {

    static ArrayList<Attribute> parseAttributes(final List<String[]> data) {
        // Parsing each line of data list to define attributes of dataset
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(data.get(0)[0], "yyyy-MM-dd"));
        for (int i = 1; i < data.get(0).length; i++) {
            attributes.add(new Attribute(data.get(0)[i]));
        }
        return attributes;
    }

    static List<Instance> parseDataToInstances(final List<String[]> data,
                                               ArrayList<Attribute> atts) throws ParseException {
        List<Instance> datasetInstances = new LinkedList<>();

        // Parsing each line of data list to fill the dataset with real values
        for (int i = 1; i < data.size(); i++) {
            double[] instanceValue = new double[atts.size()];
            instanceValue[0] = atts.get(0).parseDate(data.get(i)[0]);
            for (int j = 1; j < data.get(i).length; j++) {
                instanceValue[j] = data.get(i)[j].isEmpty() ? Double.NaN :
                        Double.parseDouble(data.get(i)[j]);
            }
            instanceValue[instanceValue.length - 1] = Double.NaN;
            // Changing negative values to NaN values
            for (int j = 1; j < instanceValue.length; j++) {
                instanceValue[j] = Double.isNaN(instanceValue[j]) ? instanceValue[j]
                        : instanceValue[j] < 0.0 ? Double.NaN : instanceValue[j];
            }
            datasetInstances.add(new DenseInstance(1.0, instanceValue));
        }
        return datasetInstances;
    }

    Instances initDataset(final List<String[]> data) throws ParseException;
}