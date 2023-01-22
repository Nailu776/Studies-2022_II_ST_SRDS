package cassproj.projectSRDS;

import cassproj.backend.BackendSession;

import java.util.concurrent.BlockingQueue;

public class AirDistributorThread extends Thread{
    int floorCount;
    int airUnits;
    BlockingQueue<Integer> outOfAir;
    BackendSession bs;

    AirDistributorThread(int floorCount, int airUnits, BlockingQueue<Integer> outOfAir, BackendSession bs, int threadId){
        this.outOfAir = outOfAir;
        this.airUnits = airUnits;
        this.bs = bs;
        this.floorCount = floorCount;
        this.setName("Distributor " + threadId);
    }


    @Override
    public void run(){
        while(airUnits > 0){
            for(int i = 1; i<=floorCount; ++i){
                int level = (int) bs.readAirLevel(i);
                if(level == 0) {
                    bs.incrementAirLevel(i);
                    --airUnits;
                }
            }
        }
        try {
            outOfAir.take();
        } catch (InterruptedException e) {
            e.getMessage();
        }
        System.out.println("Air Distributor has run out of air");
    }
}
