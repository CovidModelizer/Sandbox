package fr.covidmodelizer;

import fr.covidmodelizer.biologic.sir.InfectionSIRModel;
import fr.covidmodelizer.biologic.svir.VaccinationSVIRModel;
import fr.covidmodelizer.machinelearning.multivariate.InfectionMultivariateModel;
import fr.covidmodelizer.machinelearning.multivariate.VaccinationMultivariateModel;
import fr.covidmodelizer.machinelearning.univariate.InfectionUnivariateModel;
import fr.covidmodelizer.machinelearning.univariate.VaccinationUnivariateModel;
import fr.covidmodelizer.utils.ConsoleColors;
import fr.covidmodelizer.utils.DataMapper;

public class App {

    public static void main(String[] args) throws Exception {
        // Data preparation for prediction models
        DataMapper.main(args);
        // Predictions of infections
        InfectionUnivariateModel.main(args);
        InfectionMultivariateModel.main(args);
        InfectionSIRModel.main(args);
        // Predictions of vaccinations
        VaccinationUnivariateModel.main(args);
        VaccinationMultivariateModel.main(args);
        VaccinationSVIRModel.main(args);
        // End of predictions
        System.out.println(ConsoleColors.GREEN
                + "\n>> All predictions have been done."
                + ConsoleColors.RESET);
    }
}