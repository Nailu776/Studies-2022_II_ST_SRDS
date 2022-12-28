package cassproj.projectSRDS;
import lombok.Data;

@Data
public class CorridorPopulationSensor {
    private double Population;

    public CorridorPopulationSensor(double initCPS) {
        Population = initCPS;
    }
}
