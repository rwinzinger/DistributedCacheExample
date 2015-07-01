package winzinger.samples.distributedcache;

/**
 * Created by rwinzing on 22.06.15.
 * AppConfig including
 * - number of nodes in the cluster
 * - number of threads per node
 * - number of transactions per thread
 * - hostname of the database server
 * - flag to use MapStore
 * - flag to use NearCache
 */
public class AppConfig {
    private static AppConfig ourInstance = new AppConfig();

    public static AppConfig getInstance() {
        return ourInstance;
    }

    private int numNodes = 1;
    private int numThreads = 10;
    private int numTx = 50000;
    private String dsHost = "localhost";
    private boolean mapstore = false;
    private boolean nearcache = false;

    private AppConfig() {
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumTx() {
        return numTx;
    }

    public void setNumTx(int numTx) {
        this.numTx = numTx;
    }

    public String getDsHost() {
        return dsHost;
    }

    public void setDsHost(String dsHost) {
        this.dsHost = dsHost;
    }

    public boolean isMapstore() {
        return mapstore;
    }

    public void setMapstore(boolean mapstore) {
        this.mapstore = mapstore;
    }

    public boolean isNearcache() {
        return nearcache;
    }

    public void setNearcache(boolean nearcache) {
        this.nearcache = nearcache;
    }
}
