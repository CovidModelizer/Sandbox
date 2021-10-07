package fr.covidmodelizer.utils;

import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;

public class DataUtils {

    public static ArrayList<Attribute> prepare(final List<String[]> originalData) {
        ArrayList<Attribute> atts = new ArrayList<>();
        atts.add(new Attribute(originalData.get(0)[0], "yyyy-MM-dd"));
        for (int i = 1; i < originalData.get(0).length; i++) {
            atts.add(new Attribute(originalData.get(0)[i]));
        }
        return atts;
    }
}
