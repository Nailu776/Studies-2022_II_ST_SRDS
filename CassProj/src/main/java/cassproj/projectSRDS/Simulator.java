package cassproj.projectSRDS;

import lombok.Data;

@Data
public class Simulator {

    private SpaceBase mySB;
    public void InitMySB(int NFloors, int NStaff){
        mySB = new SpaceBase(NFloors, NStaff);
    }


}
