package cassproj;

import cassproj.backend.BackendException;
import cassproj.backend.BackendSession;
import cassproj.projectSRDS.Simulator;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {

	private static int id = 1;

	private static final String PROPERTIES_FILENAME = "config.properties";
	public static void printAppMenu(String[] menu){
		for (String item : menu){
			System.out.println(item);
		}
		System.out.print("Enter your choice [number]: ");
	}
	private static final String[] menu = {
			"0- Exit",
			"1- INIT SpaceBase Keyspace and connect to cassandra cluster",
			"2- Connect to cassandra cluster (if database was previously initialized)",
			"3- Drop Spacebase database",
			"4- Disconnect",
			"5- Show floors",
			"6- Upsert one floor",
			"7- Upsert N floors",
			"8- Show this menu",
			"9- run simulation"
	};

	public static void main(String[] args) {

		// Get properties
		String contactPoint = null;
		String keyspace = null;
		Properties properties = new Properties();
		try {
			properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));
			contactPoint = properties.getProperty("contact_point");
			keyspace = properties.getProperty("keyspace");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Scanner scanner = new Scanner(System.in);
		BackendSession session = null;
		int item;
		boolean session_connected = false;
		printAppMenu(menu);
		while (true){
			System.out.print("~>>");
			try {
				item = scanner.nextInt();
				switch (item){
					case 0:
						scanner.close();
						System.out.println("System exit");
						System.exit(0);
					case 1:
						if(!session_connected){
							// Init database
							session = new BackendSession(contactPoint, keyspace, true);
							session_connected  = true;
							// Cassandra connected;
						}
						else
							System.out.println("Already connected to cluster");
						break;
					case 2:
						if(!session_connected){
							// Connect to database
							session = new BackendSession(contactPoint, keyspace, false);
							session_connected  = true;
							// Cassandra connected;
						}
						else
							System.out.println("Already connected to cluster");
						break;
					case 3:
						if(session != null){
							session.dropSpaceBase();
						}
						else
							System.out.println("First connect to cluster");
						break;
					case 4:
						assert session != null;
						session.disconnect();
						session_connected = false;
						break;
					case 5:
						assert session != null;
						System.out.print(session.selectAll());
						break;
					case 6:
						System.out.println("Creating a floor.");
						assert session != null;
						session.upsertFloor(id++);
						break;
					case 7:
						assert session != null;
						System.out.println("Please enter number [positive integer] of floors to upsert");
						int N = scanner.nextInt();
						for (int i = 0; i < N; i++) {
							session.upsertFloor(id++);
						}
						break;
					case 8:
						printAppMenu(menu);
						break;
					case 9:
						assert session != null;
						System.out.println("Choose the number of air distributors.");
						int numberOfDistributors = scanner.nextInt();
						Simulator sim = new Simulator(session,numberOfDistributors);
						System.out.println("Starting simulation");
						sim.start();
						break;
					default:
						System.out.println("Possible inputs are integer values between 0 and " + (menu.length-1));
						break;
				}
			}
			catch (BackendException ex){
				System.out.println("Backend Exception.");
				System.out.println(ex.getMessage());
				scanner.next();
			}
			catch (Exception ex){
				System.out.println(ex.getMessage());
				System.out.println("Please enter an integer value between 0 and " + (menu.length-1));
				// Why scanner.next()? Bcs java intentionally won't skip invalid input;
				//https://stackoverflow.com/questions/1794281/java-infinite-loop-using-scanner-in-hasnextint
				scanner.next();
			}
		}
	}
}
