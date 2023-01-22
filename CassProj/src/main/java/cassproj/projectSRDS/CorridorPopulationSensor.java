package cassproj.projectSRDS;
import lombok.Data;

@Data
public class CorridorPopulationSensor {
    private int Population;

    public CorridorPopulationSensor(int initCPS) {
        Population = initCPS;
    }
}
