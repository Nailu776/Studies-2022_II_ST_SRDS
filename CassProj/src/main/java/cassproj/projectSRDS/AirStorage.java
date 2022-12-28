package cassproj.projectSRDS;

import lombok.Data;

@Data
public class AirStorage {
    // Ilosc dostepnego/ zmagazynowanego powietrza
    //
    private int AirAvailable;
    private int MaxAirAvailable;
    public AirStorage(int InitAA){
        AirAvailable = InitAA;
        MaxAirAvailable = InitAA;
    }
    public AirStorage(int InitAA, int MaxAA){
        AirAvailable = InitAA;
        MaxAirAvailable = MaxAA;
    }
}
