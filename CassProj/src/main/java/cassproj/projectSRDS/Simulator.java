package cassproj.projectSRDS;

import cassproj.backend.BackendException;
import cassproj.backend.BackendSession;
import lombok.Data;

@Data
public class Simulator {

    private SpaceBase mySB;
    public Simulator(BackendSession bs, int numberOfDistributors) throws BackendException {
        mySB = new SpaceBase(numberOfDistributors, bs);
    }

    public void start() throws InterruptedException {
        System.out.println("Entering spacebase.");
        mySB.start();
    }

    // Generating int == 150%

}
