package cassproj.projectSRDS;

import lombok.Data;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Data
public class SpaceBase {
    private Vector<Floor> FloorsVector;
    private AirStorage myAS;
    private AirMonitoring myAM;
    private AirDistributor myAD;
    private int Staff;
    // Procent personelu oznaczajacy zapotrzebowanie na 1x100% paczke powietrza
    private double StaffPerFloors;


    // TODO private or public?
    public BlockingQueue<String> TransferAirToStorage = new LinkedBlockingDeque<>();
    public BlockingQueue<String> RequestAir = new LinkedBlockingDeque<>();


    SpaceBase(int NFloors, int NStaff){
        Staff = NStaff;
        // Init 1 Paczka 100% powietrza;
        // 100% powietrza == rownomierne rozlozenie personelu/pietra;
        StaffPerFloors = 1/(double)NFloors;
        // Init N pieter, kazde majace 100% powietrza i dzialajace generatory powietrza
        for (int i = 0; i < NFloors; i++) {
            FloorsVector.add(new Floor(100,StaffPerFloors,true));
        }
        // Init Dostepnego powietrza == 100% na kazdym pietrze;
        // == Maximum dostepnego powietrza;
        myAS = new AirStorage(100*NFloors);
        // Monitoring init
        myAM = new AirMonitoring();
        // Distributor init
        myAD = new AirDistributor();
    }


}
