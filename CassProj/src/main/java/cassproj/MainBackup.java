package cassproj;

import cassproj.backend.BackendException;
import cassproj.backend.BackendSession;

import java.io.IOException;
import java.util.Properties;

public class MainBackup {

	private static final String PROPERTIES_FILENAME = "config.properties";

	public static void main(String[] args) throws IOException, BackendException {
		String contactPoint = null;
		String keyspace = null;

		Properties properties = new Properties();
		try {
			properties.load(MainBackup.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

			contactPoint = properties.getProperty("contact_point");
			keyspace = properties.getProperty("keyspace");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
			
		BackendSession session = new BackendSession(contactPoint, keyspace);

		session.upsertUser("PP", "Adam", 609, "A St");
		session.upsertUser("PP", "Ola", 509, null);
		session.upsertUser("UAM", "Ewa", 720, "B St");
		session.upsertUser("PP", "Kasia", 713, "C St");

		String output = session.selectAll();
		System.out.println("Users: \n" + output);

		session.deleteAll();

		System.exit(0);
	}
}
