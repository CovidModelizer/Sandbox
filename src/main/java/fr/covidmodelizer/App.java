package fr.covidmodelizer;

import fr.covidmodelizer.linear.InfectionLinearModel;
import fr.covidmodelizer.linear.VaccinationLinearModel;
import fr.covidmodelizer.machinelearning.InfectionMachineLearningModel;
import fr.covidmodelizer.machinelearning.VaccinationMachineLearningModel;
import fr.covidmodelizer.sir.InfectionSIRModel;
import fr.covidmodelizer.sir.VaccinationSVIRModel;
import fr.covidmodelizer.utils.ConsoleColors;
import fr.covidmodelizer.utils.DataMapper;

public class App {

    public static void main(String[] args) throws Exception {
        // Data preparation for prediction models
        DataMapper.main(args);
        // Predictions of infections
        InfectionLinearModel.main(args);
        InfectionMachineLearningModel.main(args);
        InfectionSIRModel.main(args);
        // Predictions of vaccinations
        VaccinationLinearModel.main(args);
        VaccinationMachineLearningModel.main(args);
        VaccinationSVIRModel.main(args);
        // End of predictions
        System.out.println(ConsoleColors.GREEN + "\n>> All predictions have been done\n" + ConsoleColors.RESET);
    }
}
