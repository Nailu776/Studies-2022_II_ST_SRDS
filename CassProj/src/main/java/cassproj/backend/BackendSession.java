package cassproj.backend;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * For error handling done right see: 
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 * 
 * Performing stress tests often results in numerous WriteTimeoutExceptions, 
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and 
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {
	// Private
	private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);
	private Session session;
	private static final String FLOOR_FORMAT = "- %-10s %-20s %-25s %-20s\n";
	private static final String AIR_FORMAT = "- %-10s %-20s \n";
	private static PreparedStatement INIT_KEYSPACE;
	private static PreparedStatement INIT_FLOORS;
	private static PreparedStatement INIT_AIRSTORAGE;
	private void prepareInitStatements() throws BackendException {
		try {
			INIT_KEYSPACE = session.prepare("CREATE KEYSPACE IF NOT EXISTS SpaceBase\n" +
					"  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };");
			INIT_FLOORS = session
					.prepare("CREATE TABLE IF NOT EXISTS SpaceBase.Floors (\n" +
							"  id int,\n" +
							"  airLevelSensor int,\n" +
							"  corridorPopulationSensor int,\n" +
							"  airGenerator boolean,\n" +
							"  PRIMARY KEY (id)\n" +
							");");
			INIT_AIRSTORAGE = session.prepare("CREATE TABLE IF NOT EXISTS SpaceBase.AirStorage (\n" +
					"  id int PRIMARY KEY,\n" +
					"  airStored counter\n" +
					");");
		} catch (Exception e) {
			throw new BackendException("Could not prepare init statements. " + e.getMessage() + ".", e);
		}
		logger.info("Init Statements prepared");
	}
	public void InitDataBaseSpaceBase(Cluster cluster) throws BackendException {
		// Connect to cassandra cluster without keyspace
		try {
			session = cluster.connect();
		} catch (Exception e) {
			throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
		}
		// Prepare database init statements
		prepareInitStatements();
		// Create Keyspace if not exists
		BoundStatement bs = new BoundStatement(INIT_KEYSPACE);
		try {
			logger.info("Creating SpaceBase Keyspace...");
			session.execute(bs);
			logger.info("Keyspace created.");
		} catch (Exception e) {
			throw new BackendException("Could not create SpaceBase Keyspace. " + e.getMessage() + ".", e);
		}
		// Create table floors if not exists
		bs = new BoundStatement(INIT_FLOORS);
		try {
			logger.info("Creating floors table...");
			session.execute(bs);
			logger.info("Floors table created.");
		} catch (Exception e) {
			throw new BackendException("Could not create floors table. " + e.getMessage() + ".", e);
		}
		// Create table air storage if not exists
		bs = new BoundStatement(INIT_AIRSTORAGE);
		try {
			logger.info("Creating airStorage table...");
			session.execute(bs);
			logger.info("airStorage table created.");
		} catch (Exception e) {
			throw new BackendException("Could not create airStorage table. " + e.getMessage() + ".", e);
		}
	}
	public BackendSession(String contactPoint, String keyspace, boolean init) throws BackendException {
		// Cassandra cluster
		Cluster cluster = Cluster.builder().addContactPoint(contactPoint).withCredentials("cassandra","cassandra").build();
		// Should Initialize database?
		if(init) {
			logger.info("Init database SpaceBase...");
			InitDataBaseSpaceBase(cluster);
			logger.info("Init database SpaceBase completed.");
		} else {
			logger.info("Omitting initialization of SpaceBase database.");
		}
		// Connect to SpaceBase database
		try {
			logger.info("Trying to connect to SpaceBase...");
			session = cluster.connect(keyspace);
			logger.info("Connected.");
		} catch (Exception e) {
			throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
		}
		// Prepare other statements
		prepareStatements();
	}

	// Other statements
	private static PreparedStatement SELECT_ALL_FROM_FLOORS;
	private static PreparedStatement DROP_SPACEBASE;
	private static PreparedStatement INSERT_INTO_FLOORS;
	private static PreparedStatement INCREMENT_100_AIRSTORAGECOUNTER;
	private static PreparedStatement SELECT_AIR_STORAGE;
	private void prepareStatements() throws BackendException {
		try {
			DROP_SPACEBASE = session.prepare("DROP KEYSPACE SpaceBase;");
			SELECT_ALL_FROM_FLOORS = session.prepare("SELECT * FROM floors;");
			INSERT_INTO_FLOORS = session.prepare("INSERT INTO floors" +
					" (id, airLevelSensor, corridorPopulationSensor, airGenerator)" +
					" VALUES (?, ?, ?, ?);");
			INCREMENT_100_AIRSTORAGECOUNTER = session.prepare("UPDATE AirStorage\n" +
					" SET airStored = airStored + 100\n" +
					" WHERE id = 0;");
			SELECT_AIR_STORAGE = session.prepare("SELECT * FROM AirStorage;");
		} catch (Exception e) {
			throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
		}
		logger.info("Statements prepared");
	}

	public void upsertFloor(int id, int airLevelSensor,
							int corridorPopulationSensor, boolean airGenerator) throws BackendException{
		BoundStatement bs = new BoundStatement(INSERT_INTO_FLOORS);
		bs.bind(id, airLevelSensor, corridorPopulationSensor, airGenerator);
		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
		}
		logger.info("Floor " + id + " upserted");
	}
	public void dropSpaceBase() throws BackendException{
		BoundStatement bs = new BoundStatement(DROP_SPACEBASE);
		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform a drop keyspace operation. " + e.getMessage() + ".", e);
		}
		logger.info("Keyspace Spacebase was dropped.");
	}

	public String selectAll() throws BackendException {
		StringBuilder builder = new StringBuilder();
		BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_FLOORS);

		ResultSet rs = null;
		try {
			rs = session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}

		builder.append(String.format(FLOOR_FORMAT, "ID", "AirLevelSensor", "CorridorPopulationSensor", "AirGenerator"));

		for (Row row : rs) {
			int rid = row.getInt("id");
			int rairLevelSensor = row.getInt("airLevelSensor");
			int rcorridorPopulationSensor = row.getInt("corridorPopulationSensor");
			boolean rairGenerator = row.getBool("airGenerator");
			builder.append(String.format(FLOOR_FORMAT, rid, rairLevelSensor, rcorridorPopulationSensor, rairGenerator));
		}

		return builder.toString();
	}


	public String selectAirStorage() throws BackendException {
		StringBuilder builder = new StringBuilder();
		BoundStatement bs = new BoundStatement(SELECT_AIR_STORAGE);

		ResultSet rs = null;
		try {
//			logger.info("Trying to select AirStorage.");
			rs = session.execute(bs);
//			logger.info("Got response.");
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}

		builder.append(String.format(AIR_FORMAT, "ID", "AirLevelSensor"));

//		logger.info("Trying to build string from it.");
		for (Row row : rs) {
			int rid = row.getInt("id");
			Long rairStored = row.getLong("airStored");
			builder.append(String.format(AIR_FORMAT, rid, rairStored));
		}

//		logger.info("Returning output.");
		return builder.toString();
	}


	public void upsertAirStorage() throws BackendException {
		BoundStatement bs = new BoundStatement(INCREMENT_100_AIRSTORAGECOUNTER);
		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
		}
		logger.info("Air with value " + 100 + " updated");
	}

	// TODO inspect deprecated finalize method
	public void dissconnect(){
		finalize();
	}
	protected void finalize() {
		try {
			if (session != null) {
				logger.info("Closing cluster...");
				session.getCluster().close();
			}
		} catch (Exception e) {
			logger.error("Could not close existing cluster", e);
		}
	}

}
