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
	private static final String[] menu = {"0- Init bazy danych 0",
			"1- Item 1",
			"2- Item 2",
			"3- Exit",
	};

	public static void main(String[] args) throws IOException, BackendException {

		// Connect to cassandra cluster
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
		BackendSession session = new BackendSession(contactPoint, keyspace);
		// Cassandra connected;

		Scanner scanner = new Scanner(System.in);
		int item = 1;
		while (true){
			printAppMenu(menu);
			try {
				item = scanner.nextInt();
				switch (item){
					case 0:
						System.out.println("Wybrano init");
						break;
					case 1:
						System.out.println("Wybrano 1");
						break;
					case 2:
						System.out.println("Wybrano 2");
						break;
					case 3:
						System.out.println("Wybrano exit");
						System.exit(0);
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
