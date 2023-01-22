package cassproj.projectSRDS;

import cassproj.backend.BackendSession;

import java.util.concurrent.BlockingQueue;

public class FloorThread extends Thread{

    int floorNumber;
    int time;
    BlockingQueue<Integer> outOfAir;
    BackendSession bs;

    FloorThread(int floorNumber, int time, BlockingQueue<Integer> outOfAir, BackendSession bs){
        this.outOfAir = outOfAir;
        this.floorNumber = floorNumber;
        this.time = time;
        this.bs = bs;
        this.setName("Floor " + floorNumber);
    }

    @Override
    public void run(){
        int airConsumed = 0;
        int numberOfConsumptions = 0;
        while(true){
            int level = (int) bs.readAirLevel(floorNumber);
            try {
                sleep(time);
            } catch (InterruptedException e) {
                e.getMessage();
            }
            if(level > 0) {
                bs.decrementAirLevel(floorNumber, level);
                airConsumed+=level;
                numberOfConsumptions++;
                if (outOfAir.isEmpty())
                    break;
            }
        }
        bs.setTotalAirConsumed(floorNumber, airConsumed, numberOfConsumptions);
        System.out.println("Floor "+floorNumber+" thread finished.");
    }
}
