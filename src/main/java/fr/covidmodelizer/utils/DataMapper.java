package fr.covidmodelizer.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class DataMapper {

    private final static int CONNECTION_TIMEOUT = 10 * 1000;
    private final static int READ_TIMEOUT = 5 * 1000;

    private final static String DATA_URL =
            "https://www.data.gouv.fr/fr/datasets/r/d2671c6c-c0eb-4e12-b69a-8e8f87fc224c";
    private final static String INDICATOR_URL =
            "https://www.data.gouv.fr/fr/datasets/r/381a9472-ce83-407d-9a64-1b8c23af83df";

    private final static String DATA_JSON = "src/main/resources/all-data.json";

    private final static String DATA_CSV = "src/main/resources/all-data.csv";
    private final static String INDICATOR_CSV = "src/main/resources/indicator.csv";

    private final static String DATA_LIN_INF_CSV = "src/main/resources/lin-data-infection.csv";
    private final static String DATA_ML_INF_CSV = "src/main/resources/ml-data-infection.csv";
    private final static String DATA_SIR_INF_CSV = "src/main/resources/sir-data-infection.csv";

    private final static String DATA_LIN_VACCIN_CSV = "src/main/resources/lin-data-vaccination.csv";
    private final static String DATA_ML_VACCIN_CSV = "src/main/resources/ml-data-vaccination.csv";
    private final static String DATA_SVIR_VACCIN_CSV = "src/main/resources/svir-data-vaccination.csv";

    public static void main(String[] args) throws IOException, CsvException {
        // To automatically download data and indicators
        downloadData();
        // To parse data from json format to csv
        parseAllDataToCSV();
        // To custom the data set for the class LinearModelCas
        prepareDataForLinearInfection();
        // To custom the data set for the class MachineLearningModelCas
        prepareDataForMachineLearningInfection();
        // To custom the data set for the class SIRModelCas
        prepareDataForSIRInfection();
        // To custom the data set for the class LinearModelVaccin
        prepareDataForLinearVaccination();
        // To custom the data set for the class MachineLearningModelVaccin
        prepareDataForMachineLearningVaccination();
        // To custom the data set for the class SVIRModelVaccin
        prepareDataForSVIRVaccination();
        // End of data preparation
        System.out.println(ConsoleColors.YELLOW + "\n>> All files in resources directory have been updated\n" + ConsoleColors.RESET);
    }

    public static void downloadData() throws IOException {
        FileUtils.copyURLToFile(new URL(DATA_URL), new File(DATA_JSON), CONNECTION_TIMEOUT, READ_TIMEOUT);
        FileUtils.copyURLToFile(new URL(INDICATOR_URL), new File(INDICATOR_CSV), CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    public static void parseAllDataToCSV() throws IOException, CsvException {
        JsonNode jsonTree = new ObjectMapper().readTree(new File(DATA_JSON));
        Builder csvBuilder = CsvSchema.builder();
        JsonNode[] jsonColumns = {jsonTree.get(41), jsonTree.get(326), jsonTree.get(jsonTree.size() - 1)};
        TreeSet<String> columnFields = new TreeSet<>();
        for (JsonNode j : jsonColumns) {
            j.fieldNames().forEachRemaining(columnFields::add);
        }
        List<String> columns = new ArrayList<>(columnFields);
        Comparator<String> lengthComparator = (s1, s2) -> s1.length() == s2.length() ? 1 : s1.length() - s2.length();
        columns.sort(lengthComparator);
        csvBuilder.addColumns(columns, ColumnType.STRING);

        String tmp_csv = "src/main/resources/tmp-data.csv";
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvBuilder.build().withHeader();
        csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValue(new File(tmp_csv), jsonTree);

        List<String[]> r0data = new CSVReaderBuilder(new FileReader(INDICATOR_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVReader csvReader = new CSVReaderBuilder(new FileReader(tmp_csv))
                .withCSVParser(new CSVParserBuilder().build()).build();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        int count = 0;
        String[] entries = null;
        while ((entries = csvReader.readNext()) != null) {
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(entries));
            String[] array = new String[list.size()];
            if (count == 0) {
                list.add("r0");
            } else {
                list.add((count > 0 && count < 17) || r0data.get(count - 16)[2].equals("NA") ? ""
                        : r0data.get(count - 16)[2]);
            }
            count++;
            array = list.toArray(array);
            csvWriter.writeNext(array);
        }
        csvWriter.close();

        Files.delete(Paths.get(tmp_csv).toAbsolutePath());
    }

    public static void prepareDataForLinearInfection() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_LIN_INF_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[2];
        content[0] = data.get(0)[0];
        content[1] = data.get(0)[5];
        csvWriter.writeNext(content);

        for (int i = 0; i < data.size(); i++) {
            if (i == 0) {
                content[0] = "2020-03-01";
                content[1] = "";
            } else {
                content[0] = data.get(i)[0];
                content[1] = data.get(i)[5];
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForMachineLearningInfection() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_ML_INF_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[14];
        content[0] = data.get(0)[0];
        content[1] = "nouveaux_" + data.get(0)[1];
        content[2] = "nouveaux_" + data.get(0)[2];
        content[3] = "nouveaux_" + data.get(0)[3];
        content[4] = "total_" + data.get(0)[4];
        content[5] = "nouveaux_" + data.get(0)[5];
        content[6] = "total_" + data.get(0)[6];
        content[7] = "nouveaux_" + data.get(0)[7];
        content[8] = "nouveaux_" + data.get(0)[8];
        content[9] = "nouveaux_" + data.get(0)[9];
        content[10] = data.get(0)[11];
        content[11] = data.get(0)[16];
        content[12] = data.get(0)[18];
        content[13] = "nouveau_" + data.get(0)[25];
        csvWriter.writeNext(content);

        int idx;
        for (int i = 0; i < data.size(); i++) {
            if (i == 0) {
                content[0] = "2020-03-01";
                for (int j = 1; j < content.length - 1; j++) {
                    content[j] = "";
                }
            } else {
                content[0] = data.get(i)[0];
                idx = 0;
                for (int j = 1; j < data.get(i).length; j++) {
                    if (j <= 9 || j == 11 || j == 16 || j == 18) {
                        content[++idx] = (i == 1) || !(j <= 3 || j == 5 || j == 9)
                                ? data.get(i)[j].isEmpty() ? "" : data.get(i)[j]
                                : data.get(i)[j].isEmpty() || data.get(i - 1)[j].isEmpty() ? ""
                                : String.valueOf(Integer.parseInt(data.get(i)[j])
                                - Integer.parseInt(data.get(i - 1)[j]));
                        content[idx] = content[idx].isEmpty() ? content[idx]
                                : Integer.parseInt(content[idx]) < 0 ? "" : content[idx];
                    }
                }
            }
            content[13] = "";
            if (i > 16) {
                for (int d = 0; d < 15; d++) {
                    if (!(data.get(i - d)[25].isEmpty())) {
                        content[13] = data.get(i - d)[25];
                        break;
                    }
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForSIRInfection() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_SIR_INF_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[5];
        content[0] = data.get(0)[0];
        content[1] = "S";
        content[2] = "I";
        content[3] = "R";
        content[4] = data.get(0)[25];
        csvWriter.writeNext(content);

        int N = 67000000;
        int daysToRecover = 10;

        for (int i = 92; i < data.size(); i++) {
            content[0] = data.get(i)[0];
            content[3] = data.get(i - daysToRecover)[5];
            content[2] = String.valueOf(Integer.parseInt(data.get(i)[5]) - Integer.parseInt(content[3]));
            content[1] = String.valueOf(N - Integer.parseInt(content[2]) - Integer.parseInt(content[3]));
            content[4] = "";
            for (int d = 0; d < 15; d++) {
                if (!(data.get(i - d)[25].isEmpty())) {
                    content[4] = data.get(i - d)[25];
                    break;
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForLinearVaccination() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_LIN_VACCIN_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[2];
        content[0] = data.get(0)[0];
        content[1] = data.get(0)[15];
        csvWriter.writeNext(content);

        for (int i = 300; i < data.size(); i++) {
            if (i == 300) {
                content[0] = "2020-12-26";
                content[1] = "";
            } else {
                content[0] = data.get(i)[0];
                content[1] = data.get(i)[15];
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForMachineLearningVaccination() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(DATA_ML_VACCIN_CSV));

        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[12];
        content[0] = data.get(0)[0];
        content[1] = data.get(0)[12];
        content[2] = data.get(0)[13];
        content[3] = data.get(0)[14];
        content[4] = data.get(0)[17];
        content[5] = data.get(0)[18];
        content[6] = data.get(0)[19];
        content[7] = data.get(0)[20];
        content[8] = data.get(0)[21];
        content[9] = "nouvelles_" + data.get(0)[22].substring(0, 10) + data.get(0)[22].substring(15);
        content[10] = "nouvelles_" + data.get(0)[23].substring(0, 10) + data.get(0)[23].substring(15);
        content[11] = "nouvelles_" + data.get(0)[24].substring(0, 10) + data.get(0)[24].substring(15);
        csvWriter.writeNext(content);

        int idx;
        for (int i = 300; i < data.size(); i++) {
            if (i == 300) {
                content[0] = "2020-12-26";
                for (int j = 1; j < content.length; j++) {
                    content[j] = "";
                }
            } else {
                content[0] = data.get(i)[0];
                idx = 0;
                for (int j = 1; j < data.get(i).length; j++) {
                    if ((j > 11 && j < 15) || (j > 16 && j < 22)) {
                        content[++idx] = data.get(i)[j].isEmpty() ? "" : data.get(i)[j];
                    } else if (j >= 22 && j < 25) {
                        content[++idx] = (i < 308) ? data.get(i)[j].isEmpty() ? "" : data.get(i)[j]
                                : data.get(i)[j].isEmpty() || data.get(i - 7)[j].isEmpty() ? ""
                                : String.valueOf(Integer.parseInt(data.get(i)[j])
                                - Integer.parseInt(data.get(i - 7)[j]));
                        content[idx] = content[idx].isEmpty() ? content[idx]
                                : Integer.parseInt(content[idx]) < 0 ? "" : content[idx];
                    }
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForSVIRVaccination() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_SVIR_VACCIN_CSV), CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        String[] content = new String[7];
        content[0] = data.get(0)[0];
        content[1] = "S";
        content[2] = "V";
        content[3] = "I";
        content[4] = "R";
        content[5] = data.get(0)[25];
        content[6] = "nouveau_taux_vaccination";
        csvWriter.writeNext(content);

        int N = 67000000;
        int daysToRecover = 10;

        for (int i = 301; i < data.size(); i++) {
            content[0] = data.get(i)[0];
            content[4] = data.get(i - daysToRecover)[5];
            content[3] = String.valueOf(Integer.parseInt(data.get(i)[5]) - Integer.parseInt(content[4]));
            content[2] = data.get(i)[15];
            content[1] = String.valueOf(
                    N - Integer.parseInt(content[2]) - Integer.parseInt(content[3]) - Integer.parseInt(content[4]));
            content[6] = String.valueOf(Double.parseDouble(data.get(i)[18]) / Double.parseDouble(content[1]));
            content[5] = "";
            for (int d = 0; d < 15; d++) {
                if (!(data.get(i - d)[25].isEmpty())) {
                    content[5] = data.get(i - d)[25];
                    break;
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }
}
