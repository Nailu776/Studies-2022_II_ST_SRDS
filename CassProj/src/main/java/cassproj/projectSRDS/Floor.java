package cassproj.projectSRDS;

import lombok.Data;

@Data
public class Floor {

    private AirLevelSensor myALS;
    private CorridorPopulationSensor myCPS;
    private AirGenerator myAG;

    public Floor(int initALS, double initCPS, boolean initAG) {
        myALS = new AirLevelSensor(initALS);
        myCPS = new CorridorPopulationSensor(initCPS);
        myAG = new AirGenerator(initAG);
    }
}
