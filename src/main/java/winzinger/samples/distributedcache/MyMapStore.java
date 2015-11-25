package winzinger.samples.distributedcache;

import com.hazelcast.core.MapStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.Collection;
import java.util.Map;

/**
 * Created by rwinzing on 22.06.15.
 *
 * Very simple MapStore implementation. All we do is implementing "load" to access the db.
 * The other loaders should return "null". Hazelcast will try them and die in case they throw
 * NotImplemented ...
 */
public class MyMapStore implements MapStore<Long, String> {
    private PreparedStatement query;
    private Connection conn;

    /**
     * Initialize MapStore by creating a connection to our database server
     * @param dsHost
     */
    public MyMapStore(String dsHost) {
        System.out.println("initializing mapstore");
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:tcp://" + dsHost + "/~/data", "sa", "");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void store(Long aLong, String s) {
        throw new NotImplementedException();
    }

    public void storeAll(Map<Long, String> map) {
        throw new NotImplementedException();
    }

    public void delete(Long aLong) {
        throw new NotImplementedException();
    }

    public void deleteAll(Collection<Long> collection) {
        throw new NotImplementedException();
    }

    public String load(Long id) {
        // in case we have a cache-miss, we will load the data from the server
        try {
            PreparedStatement query = conn.prepareStatement("select * from DUMMYDATA where ID=?");
            query.setLong(1, id);
            query.execute();
            ResultSet rs = query.getResultSet();
            if (rs != null) {
                rs.next();
                return rs.getString("value");
            }
            // just in case ...
            System.out.println("error");
            return "";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Long, String> loadAll(Collection<Long> collection) {
        return null;
    }

    public Iterable<Long> loadAllKeys() {
        return null;
    }
}
