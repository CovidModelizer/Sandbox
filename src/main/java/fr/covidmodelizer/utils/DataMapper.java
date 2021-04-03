package fr.covidmodelizer.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
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

	private final static String DATA_URL = "https://www.data.gouv.fr/fr/datasets/r/d2671c6c-c0eb-4e12-b69a-8e8f87fc224c";
	private final static String INDICATOR_URL = "https://www.data.gouv.fr/fr/datasets/r/381a9472-ce83-407d-9a64-1b8c23af83df";

	private final static String DATA_JSON = "src/main/resources/all-data.json";

	private final static String DATA_CSV = "src/main/resources/all-data.csv";
	private final static String INDICATOR_CSV = "src/main/resources/indicator.csv";

	private final static String DATA_ML_CAS_CSV = "src/main/resources/ml-data-cas.csv";
	private final static String DATA_SIR_CAS_CSV = "src/main/resources/sir-data-cas.csv";
	private final static String DATA_LIN_CAS_CSV = "src/main/resources/lin-data-cas.csv";

	private final static String DATA_ML_VACCIN_CSV = "src/main/resources/ml-data-vaccin.csv";
	private final static String DATA_LIN_VACCIN_CSV = "src/main/resources/lin-data-vaccin.csv";

	public static void main(String[] args) throws MalformedURLException, IOException, CsvException {
		// To automatically download data and indicators
		// downloadData();
		// To parse data from json format to csv
		parseAllDataToCSV();
		// To custom the data set for the class MachineLearningModelCas
		prepareDataForMachineLearningCas();
		// To custom the data set for the class SIRModelCas
		prepareDataForSIRCas();
		// To custom the data set for the class LinearModelCas
		prepareDataForLinearCas();
		// To custom the data set for the class MachineLearningModelVaccin
		prepareDataForMachineLearningVaccin();
		// To custom the data set for the class LinearModelVaccin
		prepareDataForLinearVaccin();
	}

	public static void downloadData() throws MalformedURLException, IOException {
		FileUtils.copyURLToFile(new URL(DATA_URL), new File(DATA_JSON), CONNECTION_TIMEOUT, READ_TIMEOUT);
		FileUtils.copyURLToFile(new URL(INDICATOR_URL), new File(INDICATOR_CSV), CONNECTION_TIMEOUT, READ_TIMEOUT);
	}

	public static void parseAllDataToCSV() throws JsonProcessingException, IOException, CsvException {
		JsonNode jsonTree = new ObjectMapper().readTree(new File(DATA_JSON));
		Builder csvBuilder = CsvSchema.builder();
		JsonNode[] jsonColumns = { jsonTree.get(41), jsonTree.get(326) };
		TreeSet<String> columnFields = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return (!s1.equals(s2) && s1.length() == s2.length()) ? 1 : s1.length() - s2.length();
			}
		});
		for (JsonNode j : jsonColumns) {
			j.fieldNames().forEachRemaining(fieldName -> {
				columnFields.add(fieldName);
			});
		}
		csvBuilder.addColumns(columnFields, ColumnType.STRING);

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

		new File(tmp_csv).delete();
	}

	public static void prepareDataForMachineLearningCas() throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_ML_CAS_CSV), CSVWriter.DEFAULT_SEPARATOR,
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

	public static void prepareDataForSIRCas() throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_SIR_CAS_CSV), CSVWriter.DEFAULT_SEPARATOR,
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

	public static void prepareDataForLinearCas() throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		CSVWriter csvWriter = new CSVWriter(new FileWriter(DATA_LIN_CAS_CSV), CSVWriter.DEFAULT_SEPARATOR,
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

	public static void prepareDataForMachineLearningVaccin() throws IOException, CsvException {
		List<String[]> data = new CSVReaderBuilder(new FileReader(DATA_CSV))
				.withCSVParser(new CSVParserBuilder().build()).build().readAll();

		Writer writer = Files.newBufferedWriter(Paths.get(DATA_ML_VACCIN_CSV));

		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		String[] content = new String[9];
		content[0] = data.get(0)[0];
		content[1] = data.get(0)[12];
		content[2] = data.get(0)[13];
		content[3] = data.get(0)[14];
		content[4] = data.get(0)[17];
		content[5] = data.get(0)[18];
		content[6] = data.get(0)[19];
		content[7] = data.get(0)[20];
		content[8] = data.get(0)[21];
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
					}
				}
			}
			csvWriter.writeNext(content);
		}
		csvWriter.close();
	}

	public static void prepareDataForLinearVaccin() throws IOException, CsvException {
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
}
