package cassproj.projectSRDS;

import cassproj.backend.BackendException;
import cassproj.backend.BackendSession;
import lombok.Data;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Data
public class SpaceBase {
    private int numberOfFloors;
    private int numberOfDistributors;
    private BackendSession bs;
    private int staff;
    // Procent personelu oznaczajacy zapotrzebowanie na 1x100% paczke powietrza
    private int storageAir;
    private List<Integer> airRequestList;
    private Vector<FloorThread> floorThreadVector = new Vector<FloorThread>();
    private Vector<AirDistributorThread> distributorThreadVector = new Vector<AirDistributorThread>();


    // TODO private or public?
    public BlockingQueue<Integer> TransferAirToStorage = new LinkedBlockingDeque<>();
    public BlockingQueue<Integer> outOfAir = new LinkedBlockingDeque<>();


    SpaceBase(int numberOfDistributors, BackendSession bs) throws BackendException {
        this.numberOfDistributors = numberOfDistributors;
        this.numberOfFloors = bs.getNumberOfFloors();
        this.bs = bs;
    }

    public void start() throws InterruptedException {
        for(int i = 0; i < numberOfDistributors; ++i){
            System.out.println("Creating distributor thread");
            outOfAir.put(1);
            distributorThreadVector.add(new AirDistributorThread(numberOfFloors,50, outOfAir, bs, i+1));
        }
        for(int i = 0; i < numberOfFloors;++i){
            System.out.println("Creating floor thread.");
            floorThreadVector.add(new FloorThread(i+1, new Random().nextInt(1000)+2000, outOfAir, bs));
        }
        for(FloorThread thread : floorThreadVector){
            thread.start();
        }
        for(AirDistributorThread thread : distributorThreadVector){
            thread.start();
        }
    }
}