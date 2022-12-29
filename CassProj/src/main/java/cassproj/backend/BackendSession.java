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
	private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);
	private Session session;

	//private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	// private static final SimpleDateFormat df = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


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
							"  airLevelsensor int,\n" +
							"  corridorPopulationSensor int,\n" +
							"  airGenerator boolean,\n" +
							"  PRIMARY KEY (id)\n" +
							");");
			INIT_AIRSTORAGE = session.prepare("CREATE TABLE IF NOT EXISTS SpaceBase.AirStorage (\n" +
					"  id UUID PRIMARY KEY,\n" +
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

	private static PreparedStatement SELECT_ALL_FROM_USERS;
	private static PreparedStatement INSERT_INTO_USERS;
	private static PreparedStatement DROP_SPACEBASE;


	private void prepareStatements() throws BackendException {
		try {
			DROP_SPACEBASE = session.prepare("DROP KEYSPACE SpaceBase;");
		} catch (Exception e) {
			throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
		}
		logger.info("Statements prepared");
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
		BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_USERS);

		ResultSet rs = null;

		try {
			rs = session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}

		for (Row row : rs) {
			String rcompanyName = row.getString("companyName");
			String rname = row.getString("name");
			int rphone = row.getInt("phone");
			String rstreet = row.getString("street");

		//	builder.append(String.format(USER_FORMAT, rcompanyName, rname, rphone, rstreet));
		}

		return builder.toString();
	}

	public void upsertUser(String companyName, String name, int phone, String street) throws BackendException {
		BoundStatement bs = new BoundStatement(INSERT_INTO_USERS);
		bs.bind(companyName, name, phone, street);

		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
		}

		logger.info("User " + name + " upserted");
	}


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
