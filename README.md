# SRDS-2022S9

Repozytorium do przedmiotu SRDS

## TODO
CassProj/src/main/java/cassproj/projectSRDS/* prawdopodobnie bez uzytku...
BAZA DANYCH:
-  import cassproj.backend.BackendException; dodac w trycathu throw BackendException!!!  do maina by wiedziec jaki blad rzuca cassandra

LOGIKA:
- Konfiguracja symulacji: 
    - liczba wezlow cassandry / replikacja w create keyspace zeby byla edytowalna
    - liczba pieter w symulacji 
    - liczba ludzi - personelu 
    - Zamiana liczby ludzi na pietrach na procentowe zapotrzebowanie na powietrze?   (zakladamy ze )?
    - predkosc symulacji - ilosc czasu pomiedzy kolejnymi zadaniami pieter o powietrze 
    - pietra z dzialajacym generatorem powietrza daja +100%? / 150%? do AirStorage 
        (counter set as = as + 100) co request powietrza 
        ! https://www.baeldung.com/java-blocking-queue
    - Watki pieter moga dawac powietrze za pomoca:
        > public BlockingQueue<Integer> TransferAirToStorage = new LinkedBlockingDeque<>();  
        Stworzenie watku pobierajacego w nieskonczonej petli take() - czekaja na cokolwiek w kolejce  
        i incrementuja licznik powietrza,  
        a watki pieter uzywalyby put by dodac powietrze do licznika (przez blockingque, ktory oprozni inny watek)  
    - Watki moga requestowac o powietrze za pomoca:
        >  public BlockingQueue<Integer> RequestAir = new LinkedBlockingDeque<>();  
          public BlockingQueue<Integer> GetAir = new LinkedBlockingDeque<>();  
        Pietro chce powietrze? RequestAir.put(new Request(ID_pietra, IsWorking?))  
        A nastepnie oczekuje watek w swojej kolejce GetAir   
- Uruchamianie pietra jako watek osobny 
- Uruchamianie wielu pieter jako osobny watek 
- Implementacja obserwatora pieter (monitoring) 
- Implementacja dystrybutora powietrza automat,   
dostaje requesty i jak jeden bedzie z bledem (gen not working) a ma jeszcze troche pieter do obsluzenia to daje mu 75% i nastepnemu 75% ?
nwm jak to rozwiazac 