package cassproj.projectSRDS;

import lombok.Data;

import java.util.concurrent.BlockingQueue;

import static java.lang.Math.round;

@Data
public class Floor{
    private int id;
    private int airLevel;
    private int population;
    private boolean needAir;
    private BlockingQueue<Integer> airRequestQueue;

    public Floor(int id, int airLevel, int population, BlockingQueue<Integer> airRequestQueue) {
        this.id = id;
        this.airLevel = airLevel;
        this.population = population;
        this.airRequestQueue = airRequestQueue;
    }
    public void consumeAir(){
        airLevel -= population;
        if(airLevel < population)
            needAir = true;
        if(airLevel < 0) airLevel=0;
    }

    public void resupplyAir(int amount){
        airLevel += amount;
        if(airLevel >= population)
            needAir = false;
    }

    //When transferring global air fails, floor's neighbors should borrow theirs according to each other's level
    public void borrowAir(Floor prev, Floor next){
        //minimum required air
        int transferAir = population - airLevel;
        //at what ratio should the neighbors lend the air
        float prevPart = (float) prev.airLevel/(prev.airLevel + next.airLevel);
        float nextPart = 1-prevPart;
        //amount of air for each neighbor to send over
        int prevAir = round(prevPart * transferAir);
        int nextAir = round(nextPart * transferAir);
        //altruistic behaviour, if the neighbor doesn't have enough air, it gives all it has
        if(prevAir > prev.airLevel) prevAir = prev.airLevel;
        if(nextAir > next.airLevel) nextAir = next.airLevel;
        //air moves from neighbors to the failed floor
        prev.airLevel -= prevAir;
        next.airLevel -= nextAir;
        this.airLevel += prevAir;
        this.airLevel += nextAir;
    }


}
