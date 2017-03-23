package uk.ac.kcl.testservices;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl;
import org.junit.Ignore;

import java.io.IOException;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Ignore
public class HsqlTestUtils {
    private static Server server1;
    private static Server server2;    

    public static Server getServer1() {
        return server1;
    }

    public static void setServer1(Server server1) {
        HsqlTestUtils.server1 = server1;
    }

    public static Server getServer2() {
        return server2;
    }

    public static void setServer2(Server server2) {
        HsqlTestUtils.server2 = server2;
    }
    public static void initHSQLDBs() throws IOException, ServerAcl.AclFormatException {
 
        HsqlProperties p1 = new HsqlProperties();
        p1.setProperty("server.database.0", "mem:hsqldb");
        p1.setProperty("server.dbname.0", "minicogs");
        p1.setProperty("server.port", "9001");
        p1.setProperty("server.remote_open", "true");
        server1 = new Server();
        server1.setProperties(p1);
        server1.setLogWriter(null);
        server1.setErrWriter(null);
        server1.start();

        HsqlProperties p2 = new HsqlProperties();
        p2.setProperty("server.database.0", "mem:hsqldb");
        p2.setProperty("server.dbname.0", "minicogs");
        p2.setProperty("server.port", "9002");
        p2.setProperty("server.remote_open", "true");
        server2 = new Server();
        server2.setProperties(p2);
        server2.setLogWriter(null);
        server2.setErrWriter(null);
        server2.start();

        //yodieconfig
        //Properties prop = System.getProperties();
        //prop.setProperty("at.ofai.gate.modularpipelines.configFile", "/home/rich/gate-apps/yodie/yodie-pipeline/main-bio/main-bio.config.yaml");        
    }
    public static void destroyHSQLDBs() {
        server1.stop();
        server2.stop();
    }
    
}
