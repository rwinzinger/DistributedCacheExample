package winzinger.samples.distributedcache;

import org.h2.tools.Server;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by rwinzing on 22.06.15.
 *
 * H2 based database server
 */
public class DummyDatabaseServer {
    public static void main(String[] args) {
        DummyDatabaseServer dds = new DummyDatabaseServer();

        try {
            dds.startServerAndLoadData();
            System.out.println("database server up and running ...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startServerAndLoadData() throws Exception {
        // start tcp server (for accepting JDBC requests)
        Server.createTcpServer("-tcpAllowOthers").start();
        // start http server (management console)
        Server.createWebServer().start();

        // load driver (file based data-store)
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:~/data", "sa", "");

        // drop everything that might already be there
        Statement dropAll = conn.createStatement();
        dropAll.executeUpdate("drop all objects delete files");
        dropAll.close();
        conn.commit();
        conn.close();

        // re-connect to database ... using the connection we used with "drop all"
        // sometimes results in hickups (table not found)
        conn = DriverManager.getConnection("jdbc:h2:~/data", "sa", "");
        Statement createTable = conn.createStatement();
        createTable.executeUpdate("create table DUMMYDATA (ID integer, VALUE varchar(100))");
        createTable.close();

        // push data into db
        PreparedStatement insertStatement = conn.prepareStatement("insert into DUMMYDATA values (?, ?)");
        for (long i=1000; i<6000; i++) {
            insertStatement.setLong(1, i);
            insertStatement.setString(2, "This is entry #"+i+".");
            int retval = insertStatement.executeUpdate();
            if (retval != 1) {
                System.out.println("failed to create entry #"+i);
            }
        }
        insertStatement.close();

        // done. Commit & close connection
        // servers will remain online and accept JDBC/web-connections
        conn.commit();
        conn.close();
    }
}
