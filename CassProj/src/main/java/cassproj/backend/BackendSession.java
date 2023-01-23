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
	private static final String FLOOR_FORMAT = "- %-10s %-20s %-20s %-20s\n";
	private static PreparedStatement INIT_KEYSPACE;
	private static PreparedStatement INIT_FLOORS;
	private static PreparedStatement INIT_FLOORSAIR;
	private void prepareInitStatements() throws BackendException {
		int i=1;
		try {
			INIT_KEYSPACE = session.prepare("CREATE KEYSPACE IF NOT EXISTS SpaceBase " +
					"  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };");
			i++;
			// Create Keyspace if not exists
			BoundStatement bs = new BoundStatement(INIT_KEYSPACE);
			try {
				logger.info("Creating SpaceBase Keyspace...");
				session.execute(bs);
				logger.info("Keyspace created.");
			} catch (Exception e) {
				throw new BackendException("Could not create SpaceBase Keyspace. " + e.getMessage() + ".", e);
			}
			INIT_FLOORS = session
					.prepare("CREATE TABLE IF NOT EXISTS SpaceBase.Floors (\n" +
							"  id int,\n" +
							"  airLevel counter,\n" +
							"  PRIMARY KEY (id)\n" +
							");");
			i++;
			INIT_FLOORSAIR = session
					.prepare("CREATE TABLE IF NOT EXISTS SpaceBase.FloorsAir (\n" +
							" id int,\n"+
							" totalAirConsumed int,\n " +
							" totalAirConsumptions int, \n " +
							" PRIMARY KEY (id)\n" +
							");");
		} catch (Exception e) {
			throw new BackendException("Could not prepare init statement " + i + ". " + e.getMessage() + ".", e);
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
		// Create table floors if not exists
		BoundStatement bs = new BoundStatement(INIT_FLOORS);
		try {
			logger.info("Creating floors table...");
			session.execute(bs);
			logger.info("Floors table created.");
		} catch (Exception e) {
			throw new BackendException("Could not create floors table. " + e.getMessage() + ".", e);
		}
		bs = new BoundStatement(INIT_FLOORSAIR);
		try {
			logger.info("Creating floorsAir table...");
			session.execute(bs);
			logger.info("FloorsAir table created.");
		} catch (Exception e) {
			throw new BackendException("Could not create floorsAir table. " + e.getMessage() + ".", e);
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
	private static PreparedStatement SELECT_AIRLEVEL_FROM_FLOOR;
	private static PreparedStatement INCREMENT_AIR_LEVEL;
	private static PreparedStatement DECREMENT_AIR_LEVEL;
	private static PreparedStatement SET_AIRCONSUMED;
	private static PreparedStatement SELECT_FLOORSAIR;

	private void prepareStatements() throws BackendException {
		int i=1;
		try {
			DROP_SPACEBASE = session.prepare("DROP KEYSPACE SpaceBase;");
			i++;
			SELECT_ALL_FROM_FLOORS = session.prepare("SELECT * FROM floors;");
			i++;
			SELECT_AIRLEVEL_FROM_FLOOR = session.prepare("SELECT airLevel FROM floors WHERE id = ?;");
			i++;
			INCREMENT_AIR_LEVEL = session.prepare("UPDATE floors SET airLevel = airLevel + 1 WHERE id = ?");
			i++;
			DECREMENT_AIR_LEVEL = session.prepare("UPDATE floors SET airLevel = airLevel - ? WHERE id = ?;");
			i++;
			SET_AIRCONSUMED = session.prepare("UPDATE floorsAir SET totalAirConsumed = ?, totalAirConsumptions = ? WHERE id = ?;");
			i++;
			SELECT_FLOORSAIR = session.prepare("SELECT * FROM floorsAir;");
		} catch (Exception e) {
			throw new BackendException("Could not prepare statement " + i + ". " + e.getMessage() + ".", e);
		}
		logger.info("Statements prepared");
	}

	public long readAirLevel(int id){
		BoundStatement bs = new BoundStatement(SELECT_AIRLEVEL_FROM_FLOOR);
		StringBuilder builder = new StringBuilder();
		ResultSet rs;

		bs.bind(id);
		rs = session.execute(bs);
		return rs.one().getLong("airLevel");
	}

	public void incrementAirLevel(int id){
		BoundStatement bs = new BoundStatement(INCREMENT_AIR_LEVEL);
		bs.bind(id);
		session.execute(bs);
		logger.info("Floor " + id + " air level incremented.");
	}
	public void setTotalAirConsumed(int id, int air, int consumptions){
		BoundStatement bs = new BoundStatement(SET_AIRCONSUMED);
		bs.bind(air, consumptions, id);
		session.execute(bs);
		logger.info("Total air consumed updated.");
	}
	public void decrementAirLevel(int id, int level){
		BoundStatement bs = new BoundStatement(DECREMENT_AIR_LEVEL);
		bs.bind((long) level, id);
		try {
//		 	session.execute(bs); // Error: Async execution - Can read before decrement.
			ResultSetFuture future = session.executeAsync(bs);
			while (!future.isDone()) {
				logger.debug("Floor " + id + " is decrementing...");
			}
			logger.info("Floor " + id + " air level decremented by " + level +".");
		} catch (Exception e) {
			logger.error("Could not perform a decrement operation. " + e.getMessage() + ".");
		}
	}

	public void upsertFloor(int id) {
		BoundStatement bs = new BoundStatement(DECREMENT_AIR_LEVEL);
		long level = 0;
		bs.bind(level, id);
		session.execute(bs);
		bs = new BoundStatement(SET_AIRCONSUMED);
		bs.bind( 0,0, id);
		session.execute(bs);
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

	public int getNumberOfFloors() throws BackendException{
		BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_FLOORS);

		ResultSet rs;
		try {
			rs = session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}
		return rs.all().size();
	}
	public String selectAll() throws BackendException {
		StringBuilder builder = new StringBuilder();
		BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_FLOORS);
		BoundStatement bs2 = new BoundStatement(SELECT_FLOORSAIR);

		ResultSet rs, rs2;
		try {
			rs = session.execute(bs);
			rs2 = session.execute(bs2);
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}

		builder.append(String.format(FLOOR_FORMAT, "ID", "airLevel", "totalAirConsumed", "totalAirConsumptions"));

		for (Row row : rs) {
			int rid = row.getInt("id");
			long airLevel = row.getLong("airLevel");
			Row row2 = rs2.one();
			int totalAirConsumed = row2.getInt("totalAirConsumed");
			int totalAirConsumptions = row2.getInt("totalAirConsumptions");
			builder.append(String.format(FLOOR_FORMAT, rid, airLevel, totalAirConsumed, totalAirConsumptions));
		}

		return builder.toString();
	}


//	public String selectAirStorage() throws BackendException {
//		StringBuilder builder = new StringBuilder();
//		BoundStatement bs = new BoundStatement(SELECT_AIR_STORAGE);
//
//		ResultSet rs = null;
//		try {
////			logger.info("Trying to select AirStorage.");
//			rs = session.execute(bs);
////			logger.info("Got response.");
//		} catch (Exception e) {
//			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
//		}
//
//		builder.append(String.format(AIR_FORMAT, "ID", "AirLevelSensor"));
//
////		logger.info("Trying to build string from it.");
//		for (Row row : rs) {
//			int rid = row.getInt("id");
//			Long rairStored = row.getLong("airStored");
//			builder.append(String.format(AIR_FORMAT, rid, rairStored));
//		}
//
////		logger.info("Returning output.");
//		return builder.toString();
//	}


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
	public void disconnect(){
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
