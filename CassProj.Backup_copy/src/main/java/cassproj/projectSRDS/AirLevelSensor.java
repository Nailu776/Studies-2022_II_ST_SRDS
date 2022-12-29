package cassproj.projectSRDS;
import lombok.Data;
@Data
public class AirLevelSensor {
    private int AirLevel;

    public AirLevelSensor(int initALS) {
        AirLevel = initALS;
    }
}
