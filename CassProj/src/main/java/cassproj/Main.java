package cassproj;

import cassproj.backend.BackendException;
import cassproj.backend.BackendSession;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {

	private static final String PROPERTIES_FILENAME = "config.properties";
	public static void printAppMenu(String[] menu){
		for (String item : menu){
			System.out.println(item);
		}
		System.out.print("Enter your choice [number]: ");
	}
	private static final String[] menu = {
			"0- INIT SpaceBase Keyspace and connect to cassandra cluster",
			"1- Connect to cassandra cluster (if database was previously initialized)",
			"2- Drop Spacebase database",
			"3- Exit",
			"4- Dissconnect",
	};

	public static void main(String[] args) throws IOException, BackendException {

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
		// Got contact_point and keyspace

		Scanner scanner = new Scanner(System.in);
		BackendSession session = null;
		int item = 1;
		boolean session_connected = false;
		while (true){
			printAppMenu(menu);
			try {
				item = scanner.nextInt();
				switch (item){
					case 0:
						if(!session_connected){
							// Init database
							session = new BackendSession(contactPoint, keyspace, true);
							session_connected  = true;
							// Cassandra connected;
						}
						else
							System.out.println("Already connected to cluster");
						break;
					case 1:
						if(!session_connected){
							// Init database
							session = new BackendSession(contactPoint, keyspace, false);
							session_connected  = true;
							// Cassandra connected;
						}
						else
							System.out.println("Already connected to cluster");
						break;
					case 2:
						if(session != null){
							session.dropSpaceBase();
						}
						else
							System.out.println("First connect to cluster");
						break;
					case 3:
						System.out.println("Wybrano exit");
						System.exit(0);
					case 4:
						session.dissconnect();
						session_connected = false;
						break;
					default:
						System.out.println("Possible inputs are integer values between 0 and " + (menu.length-1));
				}
			}
			catch (Exception ex){
				System.out.println("Please enter an integer value between 0 and " + (menu.length-1));
				// Why scanner.next()? Bcs java intentionally won't skip invalid input;
				//https://stackoverflow.com/questions/1794281/java-infinite-loop-using-scanner-in-hasnextint
				scanner.next();
			}
		}
	}
}
