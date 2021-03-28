package fr.covidmodelizer.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import java.net.MalformedURLException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;

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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class DataMapper {

	private final static int CONNECTION_TIMEOUT = 10 * 1000;
	private final static int READ_TIMEOUT = 5 * 1000;
	private final static String DATA_URL = "https://www.data.gouv.fr/fr/datasets/r/d2671c6c-c0eb-4e12-b69a-8e8f87fc224c";
	private final static String DATA_JSON = "src/main/resources/all-data.json";
	private final static String DATA_CSV = "src/main/resources/all-data.csv";
	private final static String DATA_ML_CAS_CSV = "src/main/resources/ml-data-cas.csv";

	public static void main(String[] args) throws MalformedURLException, IOException, CsvException {
		// To automatically download the data json file
		// downloadData();
		// To parse data from json format to csv
		// parseDataFromJsonToCSV();
		// To custom the data set for the class LinearRegressionModelCas
		// prepareDataForMachineLearningCas();
	}

	public static void downloadData() throws MalformedURLException, IOException {
		FileUtils.copyURLToFile(new URL(DATA_URL), new File(DATA_JSON), CONNECTION_TIMEOUT, READ_TIMEOUT);
	}

	public static void parseDataFromJsonToCSV() throws JsonProcessingException, IOException {
		JsonNode jsonTree = new ObjectMapper().readTree(new File(DATA_JSON));
		Builder csvSchemaBuilder = CsvSchema.builder();
		JsonNode[] objectToInitColumns = { jsonTree.get(41), jsonTree.get(326) };
		TreeSet<String> columnFields = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return (!s1.equals(s2) && s1.length() == s2.length()) ? 1 : s1.length() - s2.length();
			}
		});
		for (JsonNode j : objectToInitColumns) {
			j.fieldNames().forEachRemaining(fieldName -> {
				columnFields.add(fieldName);
			});
		}
		csvSchemaBuilder.addColumns(columnFields, ColumnType.STRING);
		CsvMapper csvMapper = new CsvMapper();
		csvMapper.writerFor(JsonNode.class).with(csvSchemaBuilder.build().withHeader()).writeValue(new File(DATA_CSV),
				jsonTree);
	}

	public static void prepareDataForMachineLearningCas() throws IOException, CsvException {
		CSVParser csvParser = new CSVParserBuilder().build();
		FileReader fileReader = new FileReader(DATA_CSV);
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
		List<String[]> data = csvReader.readAll();

		Writer writer = Files.newBufferedWriter(Paths.get(DATA_ML_CAS_CSV));

		CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		String[] header = new String[13];
		header[0] = data.get(0)[0];
		header[1] = "nouveaux_" + data.get(0)[1];
		header[2] = "nouveaux_" + data.get(0)[2];
		header[3] = "nouveaux_" + data.get(0)[3];
		header[4] = "total_" + data.get(0)[4];
		header[5] = "nouveaux_" + data.get(0)[5];
		header[6] = "total_" + data.get(0)[6];
		header[7] = "nouveaux_" + data.get(0)[7];
		header[8] = "nouveaux_" + data.get(0)[8];
		header[9] = "nouveaux_" + data.get(0)[9];
		header[10] = data.get(0)[11];
		header[11] = data.get(0)[16];
		header[12] = data.get(0)[18];
		csvWriter.writeNext(header);

		String[] content = new String[13];
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
								? data.get(i)[j].equals("") ? "" : data.get(i)[j]
								: data.get(i)[j].equals("") || data.get(i - 1)[j].equals("") ? ""
										: String.valueOf(Integer.parseInt(data.get(i)[j])
												- Integer.parseInt(data.get(i - 1)[j]));
						content[idx] = content[idx].equals("") ? content[idx]
								: Integer.parseInt(content[idx]) < 0 ? "" : content[idx];
					}
				}
			}
			csvWriter.writeNext(content);
		}
		csvWriter.close();
	}
}
