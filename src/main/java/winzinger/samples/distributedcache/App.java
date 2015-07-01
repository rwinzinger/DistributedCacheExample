package winzinger.samples.distributedcache;

import com.hazelcast.core.*;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import static java.util.concurrent.Executors.*;

/**
 * as soon as n members are in the cluster, the first member (master) sends out an event to start execution
 *
 * this is not safe, as the master might go down before it triggers the event
 */
public class App implements MessageListener<String> {
    private Logger logger = Logger.getLogger("APP");
    private HazelcastInstance hazelcastInstance = null;

    // We are using a master-node in this example. Not clever, but ok - see below
    private boolean masternode = false;

    // topic to send/receive message to kick off processing
    private ITopic<String> topic = null;
    // latch to wait for specific number of initialized nodes
    private ICountDownLatch latch = null;

    public static void main(String[] args) {
        // create app-config from commandline parameters
        initAppConfig(args);

        // there are two hazelcast-configurations in this project: one with near-cache, one without
        // choose the right one depending on the commandline-parameter
        // TODO: check if we could turn near-cache on at runtime ...
        if (AppConfig.getInstance().isNearcache()) {
            System.out.println("switching to near-cache ...");
            System.setProperty("hazelcast.config", "classpath:hazelcast_nc.xml");
        }

        // start the node
        App app = new App();
        app.startNode();
    }

    // create a hazelcast instance and retrieve the countdown-latch
    public App() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
        latch = hazelcastInstance.getCountDownLatch("countDownLatch");
    }

    private void startNode() {
        logger.info("starting node");

        // first node will prepare to be master
        //
        // by the way ... usually, there is no "master" in a hazelcast cluster - it would be a single point of
        // failure - just an oldest member, which is the first one in the members list or set:
        // boolean imAmSpecial = (hazelcastInstance.getCluster().getLocalMember().equals(hazelcastInstance.getCluster().getMembers().iterator().next()));
        // but since we need the "master" just to kick off processing on all the nodes at the same time, this
        // should be fine for this simple example
        //
        // we don't want concurrent threads here ...
        Lock lock = hazelcastInstance.getLock("startup");
        lock.lock();
        try {
            // the first node to set this (distributed) long will be our master
            IAtomicLong master = hazelcastInstance.getAtomicLong("master");
            if (master.compareAndSet(0, 1)) {
                // I'm the master
                logger.info("I seem to be the master - counting other nodes now ...");
                masternode = true;

                // set latch to the number of nodes we would like to have in our cluster
                // - every slave decreases latch when it is initialized
                // - if master detects enough slaves, it kicks off processing
                latch.trySetCount(AppConfig.getInstance().getNumNodes()-1);
            } else {
                // I'm a slave
                logger.info("I'm not the master - waiting for start event ...");
            }

            // every node registers for command events
            topic = hazelcastInstance.getTopic("command");
            topic.addMessageListener(this);
        } finally {
            // and don't forget the delete the lock ... ;-)
            lock.unlock();
        }

        if (masternode) {
            // masternode waits one minute for slave nodes to countdown the latch ...
            try {
                latch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.severe("not enough nodes joined the cluster");
                hazelcastInstance.shutdown();
                System.exit(1);
            }
            logger.info("ok - found "+(AppConfig.getInstance().getNumNodes()-1)+" additional nodes");
            topic.publish("go");
        } else {
            // slavenode is ready now, decreases latch
            logger.info("node initialized - reducing latch ...");
            latch.countDown();
        }
    }

    /**
     * spawn n threads and execute either direct database reads or mapstore reads
     */
    private void process() {
        logger.info("processing ...");
        ExecutorService executorService = newFixedThreadPool(AppConfig.getInstance().getNumThreads());

        // start threads. they will return their average read-time in a future
        List<Future<Double>> blResults = new ArrayList<Future<Double>>();
        for (int i=0; i<AppConfig.getInstance().getNumThreads(); i++) {
            blResults.add(executorService.submit(
                    AppConfig.getInstance().isMapstore()?
                            new BusinessLogicMapStore(hazelcastInstance):
                            new BusinessLogicDatastore(hazelcastInstance)));
        }

        // now wait for futures to complete
        try {
            for (Future<Double> blResult : blResults) {
                System.out.println("time to retrieve data: "+blResult.get()+" ms");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("finished ...");

        // the magic is over ... shut down!
        executorService.shutdown();
    }

    // MessageListener

    public void onMessage(Message<String> message) {
        // if the master sends "go", start processing ...
        if ("go".equalsIgnoreCase(message.getMessageObject())) {
            process();
        }
    }

    /**
     * read/parse commandline (apache-cli)
     *
     * @param args
     */
    private static void initAppConfig(String[] args) {
        Options options = new Options();

        options.addOption("ms", false, "use MapStore");
        options.addOption("nc", false, "use MapStore with near cache");
        options.addOption(OptionBuilder.withArgName("number of nodes").hasArg().withDescription("number of nodes to wait for").create("nodes"));
        options.addOption(OptionBuilder.withArgName("number of threads per node").hasArg().withDescription("number of nodes to wait for").create("threads"));
        options.addOption(OptionBuilder.withArgName("number of transactions per thread").hasArg().withDescription("number of nodes to wait for").create("tx"));
        options.addOption(OptionBuilder.withArgName("datastore host").hasArg().withDescription("number of nodes to wait for").create("host"));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (cmd.hasOption("nodes")) {
            AppConfig.getInstance().setNumNodes(Integer.parseInt(cmd.getOptionValue("nodes")));
        }
        if (cmd.hasOption("threads")) {
            AppConfig.getInstance().setNumThreads(Integer.parseInt(cmd.getOptionValue("threads")));
        }
        if (cmd.hasOption("tx")) {
            AppConfig.getInstance().setNumTx(Integer.parseInt(cmd.getOptionValue("tx")));
        }
        if (cmd.hasOption("ms")) {
            AppConfig.getInstance().setMapstore(true);
        }
        if (cmd.hasOption("nc")) {
            AppConfig.getInstance().setMapstore(true);
            AppConfig.getInstance().setNearcache(true);
        }
        if (cmd.hasOption("host")) {
            AppConfig.getInstance().setDsHost(cmd.getOptionValue("host"));
        }
    }
}
