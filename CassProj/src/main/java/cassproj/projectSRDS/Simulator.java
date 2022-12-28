package cassproj.projectSRDS;

import lombok.Data;
import lombok.SneakyThrows;

@Data
public class Simulator {

    private SpaceBase mySB;
    public void InitMySB(int NFloors, int NStaff){
        mySB = new SpaceBase(NFloors, NStaff);
    }

    // Generating int == 150%
    @SneakyThrows
    public void GenerateAir(int GeneratingInt) {
        for (Floor floor:mySB.getFloorsVector()) {
            if(floor.CanGenerateAir()) mySB.TransferAirToStorage.put(GeneratingInt);
        }
    }

    @SneakyThrows
    public void RequestAir(int n){
        Floor nFloor = mySB.getFloorsVector().get(n);

        //Notify monitor about AG status
        //if(nFloor.getMyAG().isWorking()) // Request air;


        // Request air;
        nFloor.setMyALS(new AirLevelSensor(mySB.RequestAir.take()));
    }

}
