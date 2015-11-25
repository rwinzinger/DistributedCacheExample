package winzinger.samples.distributedcache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by rwinzing on 18.06.15.
 *
 * "business logic" that accesses the MapStore (which might read from DB if necessary)
 */
public class BusinessLogicMapStore implements Callable<Double> {
    private Logger logger = Logger.getLogger("BL");

    private IMap<Long, String> mymap = null;

    public BusinessLogicMapStore(HazelcastInstance hazelcastInstance) {
        // get MapStore-handle (see configuration in hazelcast.xml)
        mymap = hazelcastInstance.getMap("MyMapStore");
    }

    public Double call() throws Exception {
        logger.info("starting BL thread (MS) "+Thread.currentThread().getName());

        // randomly choose an ID (1000-5999), read data and track response time
        Random rand = new Random();
        long start = System.currentTimeMillis();
        for (int i=0; i<AppConfig.getInstance().getNumTx(); i++) {
            long id = 1000+rand.nextInt(5000);
            mymap.get(id);
        }
        long end = System.currentTimeMillis();

        // thread returns average number of milliseconds per data read
        return (double)(end-start)/AppConfig.getInstance().getNumTx();
    }
}
