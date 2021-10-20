package fr.covidmodelizer.utils;

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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataMapper {

    // Links to download documents containing data about COVID-19
    private final static String DATA_URL =
            "https://www.data.gouv.fr/fr/datasets/r/d2671c6c-c0eb-4e12-b69a-8e8f87fc224c";
    private final static String INDICATOR_URL =
            "https://www.data.gouv.fr/fr/datasets/r/381a9472-ce83-407d-9a64-1b8c23af83df";

    // Download settings
    private final static int CONNECTION_TIMEOUT = 10 * 1000;
    private final static int READ_TIMEOUT = 5 * 1000;

    public static void main(String[] args) throws IOException, CsvException {
        // Downloading data and indicators
        downloadData();
        // Parsing data from json format to csv
        parseAllDataToCSV();
        // Processing data for the InfectionLinearModel class
        prepareDataForInfectionUnivariateModel();
        // Processing data for the InfectionMachineLearningModel class
        prepareDataForInfectionMultivariateModel();
        // Processing data for the class InfectionSIRModel class
        prepareDataForInfectionSIRModel();
        // Processing data for the VaccinationLinearModel class
        prepareDataForVaccinationUnivariateModel();
        // Processing data for the VaccinationMachineLearningModel class
        prepareDataForVaccinationMultivariateModel();
        // Processing data for the VaccinationSVIRModel class
        prepareDataForVaccinationSVIRModel();
        // End of data preparation
        System.out.println(ConsoleColors.GREEN
                + "\n>> All files in resources directory have been updated.\n"
                + ConsoleColors.RESET);
    }

    public static void downloadData() throws IOException {
        // Download and store documents
        FileUtils.copyURLToFile(new URL(DATA_URL),
                new File(ProjectFiles.DATA_JSON), CONNECTION_TIMEOUT, READ_TIMEOUT);
        FileUtils.copyURLToFile(new URL(INDICATOR_URL),
                new File(ProjectFiles.INDICATORS_CSV), CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    public static void parseAllDataToCSV() throws IOException, CsvException {
        JsonNode jsonTree = new ObjectMapper().readTree(new File(ProjectFiles.DATA_JSON));
        Builder csvBuilder = CsvSchema.builder();
        JsonNode[] jsonColumns = {jsonTree.get(41), jsonTree.get(326), jsonTree.get(jsonTree.size() - 1)};
        TreeSet<String> columnFields = new TreeSet<>();
        for (JsonNode j : jsonColumns) {
            j.fieldNames().forEachRemaining(columnFields::add);
        }
        // Keeping only uniques columns
        List<String> columns = new ArrayList<>(columnFields);
        Comparator<String> lengthComparator = (s1, s2) -> s1.length() == s2.length() ? 0 : s1.length() - s2.length();
        columns.sort(lengthComparator);
        csvBuilder.addColumns(columns, ColumnType.STRING);

        // Combining data columns and r0 column from indicators file
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvBuilder.build().withHeader();
        csvMapper.writerFor(JsonNode.class).with(csvSchema)
                .writeValue(new File(ProjectFiles.TEMPORARY_DATA_CSV), jsonTree);

        List<String[]> indicators = new CSVReaderBuilder(new FileReader(ProjectFiles.INDICATORS_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        CSVReader csvReader = new CSVReaderBuilder(new FileReader(ProjectFiles.TEMPORARY_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.ALL_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        int count = 0;
        String[] entries;
        while ((entries = csvReader.readNext()) != null) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(entries));
            String[] array = new String[list.size()];
            // Adding the r0 indicator when it exists
            list.add(count == 0 ? "r0" :
                    (count > 0 && count < 17) || indicators.get(count - 16)[2].equals("NA") ? ""
                            : indicators.get(count - 16)[2]);
            count++;
            array = list.toArray(array);
            csvWriter.writeNext(array);
        }
        csvWriter.close();

        Files.delete(Paths.get(ProjectFiles.TEMPORARY_DATA_CSV).toAbsolutePath());
    }

    public static void prepareDataForInfectionUnivariateModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.UNIVARIATE_INFECTION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[2];
        content[0] = data.get(0)[0];            // date
        content[1] = "cumul_" + data.get(0)[5]; // cumul_casConfirmes
        csvWriter.writeNext(content);

        for (int i = 0; i < data.size(); i++) {
            if (i == 0) {
                // Adding a first line of empty values to let the model starts learning from the real first values
                content[0] = "2020-03-01";
                content[1] = "";
            } else {
                // Processing the real values to prepare the dataset for the model
                content[0] = data.get(i)[0];
                content[1] = data.get(i)[5];
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForInfectionMultivariateModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.MULTIVARIATE_INFECTION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[14];
        content[0] = data.get(0)[0];                // date
        content[1] = "nouveaux_" + data.get(0)[1];  // nouveaux_deces
        content[2] = "nouveaux_" + data.get(0)[2];  // nouveaux_gueris
        content[3] = "nouveaux_" + data.get(0)[3];  // nouveaux_decesEhpad
        content[4] = "total_" + data.get(0)[4];     // total_reanimation
        content[5] = "nouveaux_" + data.get(0)[5];  // nouveaux_casConfirmes
        content[6] = "total_" + data.get(0)[6];     // total_hospitalises
        content[7] = "nouveaux_" + data.get(0)[7];  // nouveaux_testsPositifs
        content[8] = "nouveaux_" + data.get(0)[8];  // nouveaux_testsRealises
        content[9] = "nouveaux_" + data.get(0)[9];  // nouveaux_casConfirmesEhpad
        content[10] = data.get(0)[11];              // nouvellesReanimations
        content[11] = data.get(0)[16];              // nouvellesHospitalisations
        content[12] = data.get(0)[18];              // nouvellesPremieresInjections
        content[13] = "nouveau_" + data.get(0)[25]; // nouveau_r0
        csvWriter.writeNext(content);

        int idx;
        for (int i = 0; i < data.size(); i++) {
            if (i == 0) {
                // Adding a first line of empty values to let the model starts learning from the real first values
                content[0] = "2020-03-01";
                for (int j = 1; j < content.length - 1; j++) {
                    content[j] = "";
                }
            } else {
                // Processing the real values to prepare the dataset for the model
                content[0] = data.get(i)[0];
                idx = 0;
                for (int j = 1; j < data.get(i).length; j++) {
                    if (j <= 9 || j == 11 || j == 16 || j == 18) {
                        content[++idx] = (i == 1) || !(j <= 3 || j == 5 || j == 9)
                                ? data.get(i)[j].isEmpty() ? "" : data.get(i)[j]
                                : data.get(i)[j].isEmpty() || data.get(i - 1)[j].isEmpty() ? ""
                                : String.valueOf(Integer.parseInt(data.get(i)[j])
                                - Integer.parseInt(data.get(i - 1)[j]));
                        // Negative values are not expected, so they are change to empty values
                        content[idx] = content[idx].isEmpty() ? content[idx]
                                : Integer.parseInt(content[idx]) < 0 ? "" : content[idx];
                    }
                }
            }
            content[13] = "";
            // Adding the r0 indicator when it exists
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

    public static void prepareDataForInfectionSIRModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.SIR_INFECTION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[5];
        content[0] = data.get(0)[0];    // date
        content[1] = "S";               // S : stands for healthy people
        content[2] = "I";               // I : stands for infected people
        content[3] = "R";               // R : stands for cured people
        content[4] = data.get(0)[25];   // r0 : virus reproduction rate
        csvWriter.writeNext(content);

        int N = 67000000;
        int daysToRecover = 10;

        for (int i = 92; i < data.size(); i++) {
            // Processing the real values to prepare the dataset for the model
            content[0] = data.get(i)[0];
            content[3] = data.get(i - daysToRecover)[5];
            content[2] = String.valueOf(Integer.parseInt(data.get(i)[5]) - Integer.parseInt(content[3]));
            content[1] = String.valueOf(N - Integer.parseInt(content[2]) - Integer.parseInt(content[3]));
            content[4] = "";
            // Adding the r0 indicator when it exists
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

    public static void prepareDataForVaccinationUnivariateModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.UNIVARIATE_VACCINATION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[2];
        content[0] = data.get(0)[0];    // date
        content[1] = data.get(0)[15];   // cumulPremieresInjections
        csvWriter.writeNext(content);

        for (int i = 300; i < data.size(); i++) {
            if (i == 300) {
                // Adding a first line of empty values to let the model starts learning from the real first values
                content[0] = "2020-12-26";
                content[1] = "";
            } else {
                // Processing the real values to prepare the dataset for the model
                content[0] = data.get(i)[0];
                content[1] = data.get(i)[15];
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForVaccinationMultivariateModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.MULTIVARIATE_VACCINATION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[12];
        content[0] = data.get(0)[0];                // date
        content[1] = data.get(0)[12];               // stockNombreTotalDoses
        content[2] = data.get(0)[13];               // stockNombreDosesPfizer
        content[3] = data.get(0)[14];               // stockNombreDosesModerna
        content[4] = data.get(0)[17];               // stockEhpadNombreDosesPfizer
        content[5] = data.get(0)[18];               // nouvellesPremieresInjections
        content[6] = data.get(0)[19];               // prisesRendezVousSemaineRang1
        content[7] = data.get(0)[20];               // prisesRendezVousSemaineRang2
        content[8] = data.get(0)[21];               // totalPrisesRendezVousSemaine
        content[9] = "nouvelles_" + data.get(0)[22].substring(0, 10)
                + data.get(0)[22].substring(15);    // nouvelles_livraisonsNombreTotalDoses
        content[10] = "nouvelles_" + data.get(0)[23].substring(0, 10)
                + data.get(0)[23].substring(15);    // nouvelles_livraisonsNombreDosesPfizer
        content[11] = "nouvelles_" + data.get(0)[24].substring(0, 10)
                + data.get(0)[24].substring(15);    // nouvelles_livraisonsNombreDosesModerna
        csvWriter.writeNext(content);

        int idx;
        for (int i = 300; i < data.size(); i++) {
            if (i == 300) {
                // Adding a first line of empty values to let the model starts learning from the real first values
                content[0] = "2020-12-26";
                for (int j = 1; j < content.length; j++) {
                    content[j] = "";
                }
            } else {
                // Processing the real values to prepare the dataset for the model
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
                        // Negative values are not expected, so they are change to empty values
                        content[idx] = content[idx].isEmpty() ? content[idx]
                                : Integer.parseInt(content[idx]) < 0 ? "" : content[idx];
                    }
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }

    public static void prepareDataForVaccinationSVIRModel() throws IOException, CsvException {
        List<String[]> data = new CSVReaderBuilder(new FileReader(ProjectFiles.ALL_DATA_CSV))
                .withCSVParser(new CSVParserBuilder().build()).build().readAll();

        Writer writer = Files.newBufferedWriter(Paths.get(ProjectFiles.SVIR_VACCINATION_DATA_CSV));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Keeping only needed columns for this model
        String[] content = new String[7];
        content[0] = data.get(0)[0];                // date
        content[1] = "S";                           // S : stands for healthy people
        content[2] = "V";                           // V : stands for vaccinated people
        content[3] = "I";                           // I : stands for infected people
        content[4] = "R";                           // R : stands for cured people
        content[5] = "nouveau_taux_vaccination";    // daily vaccination rate
        content[6] = data.get(0)[25];               // r0 : virus reproduction rate
        csvWriter.writeNext(content);

        int N = 67000000;
        int daysToRecover = 10;

        for (int i = 301; i < data.size(); i++) {
            // Processing the real values to prepare the dataset for the model
            content[0] = data.get(i)[0];
            content[4] = data.get(i - daysToRecover)[5];
            content[3] = String.valueOf(Integer.parseInt(data.get(i)[5]) - Integer.parseInt(content[4]));
            content[2] = data.get(i)[15];
            content[1] = String.valueOf(N - Integer.parseInt(content[2])
                    - Integer.parseInt(content[3]) - Integer.parseInt(content[4]));
            content[5] = String.valueOf(Double.parseDouble(data.get(i)[18]) / Double.parseDouble(content[1]));
            content[6] = "";
            // Adding the r0 indicator when it exists
            for (int d = 0; d < 15; d++) {
                if (!(data.get(i - d)[25].isEmpty())) {
                    content[6] = data.get(i - d)[25];
                    break;
                }
            }
            csvWriter.writeNext(content);
        }
        csvWriter.close();
    }
}