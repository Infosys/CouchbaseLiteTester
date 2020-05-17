package sample;

import com.couchbase.lite.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InitiateSync {
    static Properties properties = new Properties();
    private static String SYNC_GATEWAY_URL = properties.getProperty("sgURL");
    private static final String DB_NAME = properties.getProperty("liteDBName","syncdb");
    public static final String DB_PATH = properties.getProperty("cblite-loc");
    private static String DB_PASS = properties.getProperty("dbpass","password");
    private static Database database;
    private static Replicator replicator;
    public static boolean isReplStarted = false;
    private static final Log log = LogFactory.getLog(InitiateSync.class);

    public static void createLocalCBLiteFile() throws CouchbaseLiteException {
        // Initialize Couchbase Lite
        CouchbaseLite.init();
        // Get the database (and create it if it doesn't exist).
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(DB_PATH);
        InitiateSync.database = new Database(DB_NAME, config);
        log.info("CbLite file has been created and database has been initialized");
    }

    public static void startReplicator(String gpid, char[] pwd, String gwURL) throws URISyntaxException, InterruptedException {
        SYNC_GATEWAY_URL = properties.getProperty("shURL");
        Endpoint targetEndpoint = new URLEndpoint(new URI(SYNC_GATEWAY_URL));
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(database, targetEndpoint);
        replConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
//        Do not replicate deleted docs!
        replConfig.setPullFilter((document, flags) -> !flags.contains(DocumentFlag.DocumentFlagsDeleted));

//    Amrish - Cert pinning
        if (!properties.getProperty("sgCert").equals("none")){
            InputStream is = null;
            try {
                log.info(System.getProperty("user.dir"));
                is = new FileInputStream(properties.getProperty("sgCert"));
            } catch (FileNotFoundException ex) {
                log.error("Sync Gateway Cert not found",ex);
            }
            byte[] cert = null;
            try {
                cert = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                log.error("Sync Gateway cert error",ex);
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            replConfig.setPinnedServerCertificate(cert);
        }
//end cert pinning


        // Add authentication.

        DB_PASS = System.getProperty("dbpass", "password");
        if (!(new String(pwd).isBlank()))
            DB_PASS = new String(pwd);
        log.info("password is: " + DB_PASS);

        replConfig.setAuthenticator(new BasicAuthenticator(gpid, DB_PASS));

        // Create replicator (be sure to hold a reference somewhere that will prevent the Replicator from being GCed)
        replicator = new Replicator(replConfig);

        // Listen to replicator change events.
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                log.error("Error code ::  " + change.getStatus().getError().getCode());
                // System.err.println("Error code ::  " + change.getStatus().getError().getCode());
            }
        });

        // Start replication.
        replicator.start();
        isReplStarted = true;
        // Check status of replication and wait till it is completed
        while (replicator.getStatus().getActivityLevel() != Replicator.ActivityLevel.STOPPED) {
            Thread.sleep(1000);
        }
        log.info("Replicator has been started, pulled initial data from sync gateway");
    }

    public static void pullReplicationData() {
        log.info(" check any data in case if we don't create new mapping...");
        // Start replication.
        replicator.start();
        while (replicator.getStatus().getActivityLevel() != Replicator.ActivityLevel.STOPPED) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void stopReplication() {
        log.info("Stop the replication...");
        if (replicator != null) {
            replicator.stop();
            isReplStarted = false;
        }
    }

    public static void stopAndDeletreDB() {
        log.info("- Delete the cblite database if want to reinitialize the local db");
        if (database != null) {
            try {
                stopReplication();
                database.delete();
                database.close();
                database = null;
                log.info(" local cblite file has been deleted...");
            } catch (CouchbaseLiteException ex) {
                log.error("stopAndDeletreDB - " + ex.getMessage());
            }
        }
    }

    public static HashMap getCBLiteData() throws CouchbaseLiteException {
        HashMap<String, String> cbData = new HashMap<>(10);
        Query queryAll = QueryBuilder.select(SelectResult.all(),
                SelectResult.expression(Meta.id))
                .from(DataSource.database(database));
        ResultSet resultFull = queryAll.execute();
        List<Result> results = resultFull.allResults();
        for (Iterator iterator = results.iterator(); iterator.hasNext(); ) {
            Result ts = (Result) iterator.next();

            cbData.put(ts.getString("id"), ts.toList().get(0).toString());
//        cbData.put(ts.getDictionary(0).getString("Id"), ts.toList().toString());
        }

        log.info("total number of records are :" + cbData.size());
        return cbData;
    }


//    public static void main(String[] args) throws CouchbaseLiteException, InterruptedException, URISyntaxException {
//    }

}
