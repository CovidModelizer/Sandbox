package fr.covidmodelizer.utils;

public class ProjectFiles {

    // Project directories
    private final static String DATA_DIR = "resources/data/";
    // Raw data files
    public final static String DATA_JSON = DATA_DIR + "data.json";
    public final static String INDICATORS_CSV = DATA_DIR + "indicators.csv";
    public final static String TEMPORARY_DATA_CSV = DATA_DIR + "tmp-data.csv";
    // Combined raw data files
    public final static String ALL_DATA_CSV = DATA_DIR + "all-data.csv";
    private final static String PREDICTION_DIR = "resources/predictions/";
    // Data files suffix
    private final static String DATA_INFECTION_CSV = "-infection-data.csv";
    private final static String DATA_VACCINATION_CSV = "-vaccination-data.csv";
    // Prediction files suffix
    private final static String PREDICTION_INFECTION_CSV = "-infection-prediction.csv";
    private final static String PREDICTION_VACCINATION_CSV = "-vaccination-prediction.csv";
    // Model identifiers
    private final static String UNIVARIATE = "univariate";
    // Prepared data files
    public final static String UNIVARIATE_INFECTION_DATA_CSV =
            DATA_DIR + UNIVARIATE + DATA_INFECTION_CSV;
    public final static String UNIVARIATE_VACCINATION_DATA_CSV =
            DATA_DIR + UNIVARIATE + DATA_VACCINATION_CSV;
    // Prediction files
    public final static String UNIVARIATE_INFECTION_PREDICTION_CSV =
            PREDICTION_DIR + UNIVARIATE + PREDICTION_INFECTION_CSV;
    public final static String UNIVARIATE_VACCINATION_PREDICTION_CSV =
            PREDICTION_DIR + UNIVARIATE + PREDICTION_VACCINATION_CSV;
    private final static String MULTIVARIATE = "multivariate";
    public final static String MULTIVARIATE_INFECTION_DATA_CSV =
            DATA_DIR + MULTIVARIATE + DATA_INFECTION_CSV;
    public final static String MULTIVARIATE_VACCINATION_DATA_CSV =
            DATA_DIR + MULTIVARIATE + DATA_VACCINATION_CSV;
    public final static String MULTIVARIATE_INFECTION_PREDICTION_CSV =
            PREDICTION_DIR + MULTIVARIATE + PREDICTION_INFECTION_CSV;
    public final static String MULTIVARIATE_VACCINATION_PREDICTION_CSV =
            PREDICTION_DIR + MULTIVARIATE + PREDICTION_VACCINATION_CSV;
    private final static String SIR = "sir";
    public final static String SIR_INFECTION_DATA_CSV =
            DATA_DIR + SIR + DATA_INFECTION_CSV;
    public final static String SIR_INFECTION_PREDICTION_CSV =
            PREDICTION_DIR + SIR + PREDICTION_INFECTION_CSV;
    private final static String SVIR = "svir";
    public final static String SVIR_VACCINATION_DATA_CSV =
            DATA_DIR + SVIR + DATA_VACCINATION_CSV;
    public final static String SVIR_VACCINATION_PREDICTION_CSV =
            PREDICTION_DIR + SVIR + PREDICTION_VACCINATION_CSV;
}