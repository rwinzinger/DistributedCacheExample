package winzinger.samples.distributedcache;

import com.hazelcast.core.HazelcastInstance;

import java.sql.*;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by rwinzing on 18.06.15.
 *
 * "business logic" that directly accesses the datastore via JDBC
 */
public class BusinessLogicDatastore implements Callable<Double> {
    private Logger logger = Logger.getLogger("BL");

    private PreparedStatement query = null;

    public BusinessLogicDatastore(HazelcastInstance hazelcastInstance) {
    }

    @Override
    public Double call() throws Exception {
        logger.info("starting BL thread (DS) "+Thread.currentThread().getName());

        // load driver
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:tcp://"+AppConfig.getInstance().getDsHost()+"/~/data", "sa", "");
        // use a prepared statement for queries
        query = conn.prepareStatement("select * from DUMMYDATA where ID=?");

        // randomly choose an ID (1000-5999), read data and track response time
        Random rand = new Random();
        long start = System.currentTimeMillis();
        for (int i=0; i<AppConfig.getInstance().getNumTx(); i++) {
            long id = 1000+rand.nextInt(5000);
            requestData(id);
        }
        long end = System.currentTimeMillis();

        // thread returns average number of milliseconds per data read
        return (double)(end-start)/AppConfig.getInstance().getNumTx();
    }

    private String requestData(long id) throws Exception {
        // set parameter and execute query
        query.setLong(1, id);
        query.execute();
        ResultSet rs = query.getResultSet();
        rs.next();
        return rs.getString("value");
    }
}
