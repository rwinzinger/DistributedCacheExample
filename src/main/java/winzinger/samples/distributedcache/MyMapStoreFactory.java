package winzinger.samples.distributedcache;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

import java.util.Properties;

/**
 * Created by rwinzing on 22.06.15.
 *
 * Since we need a database server to initialize the MapStore (and we get this piece of
 * information via commandline) we have to use a MapStoreFactory instead of just configuring
 * the MapStore in the configuration file
 */
public class MyMapStoreFactory implements MapStoreFactory<Long, String> {
    public MapLoader<Long, String> newMapStore(String name, Properties properties) {
        // that's actually one factory for all our (potential) MapStores, so we might
        // have to check the MapStore name ... irrelevant in this simple example
        if ("MyMapStore".equalsIgnoreCase(name)) {
            return new MyMapStore(AppConfig.getInstance().getDsHost());
        }
        return null;
    }
}
