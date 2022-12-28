package cassproj.projectSRDS;

import lombok.Data;

import java.util.Random;

@Data
public class AirGenerator {

    private boolean isWorking;
    private Random rand = new Random();
    public AirGenerator(boolean initAG) {
        isWorking = initAG;
    }
    // Losowy moment awarii, jezeli wylosowana liczba bedzie wieksza niz WP,
    // to nastepuje awaria (dla WP == 100, nigdy nie wystapi awaria)
    public void randWorking(int intWProbability){
        isWorking = (rand.nextInt(100) <= intWProbability);
    }
}
