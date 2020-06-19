/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CBLiteTester;

import com.couchbase.lite.*;
import javafx.application.Platform;
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

public class SyncController {
    private static final Log logger = LogFactory.getLog(SyncController.class);
    private static Properties properties;
    static {
        try {
            properties = new Properties();
            properties.loadFromXML(new FileInputStream("config.xml"));
        } catch (IOException e) {
            logger.error("Error in static block", e);
        }
    }
    public static MainController mainController;
    public static boolean isReplicatorStarted = false;
    public static final String DB_NAME = properties.getProperty("sgDB", "syncdb");
    public static final String DB_PATH = properties.getProperty("cblite-loc");
    public static String SYNC_GATEWAY_URL = properties.getProperty("sgURL");
    private static Database database;
    private static Replicator replicator;


    public static Database getDatabase() {
        return database;
    }

    public static void createLocalCBLiteFile() {
        // Initialize Couchbase Lite
        CouchbaseLite.init();
        // Get the database (and create it if it doesn't exist).
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(DB_PATH);
        try {
            SyncController.database = new Database(DB_NAME, config);
        } catch (CouchbaseLiteException e) {
            logger.info("Unable to create CBLite DB", e);
        }
        logger.info("CbLite file has been created and database has been initialized");
    }

    public static void startReplicator(String user, String pwd, boolean isContinuous, MainController controller) {
        logger.info("calling startReplicator");
        mainController = controller;
        if (database == null) createLocalCBLiteFile();
        loadProperties();
        SYNC_GATEWAY_URL = properties.getProperty("sgURL");
        Endpoint targetEndpoint = null;
        try {
            targetEndpoint = new URLEndpoint(new URI(SYNC_GATEWAY_URL));
        } catch (URISyntaxException e) {
            logger.info("Bad Sync URL", e);
        }
        ReplicatorConfiguration replicatorConfig = new ReplicatorConfiguration(database, targetEndpoint);
        replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
//        Do not replicate deleted docs!
        replicatorConfig.setPullFilter((document, flags) -> !flags.contains(DocumentFlag.DocumentFlagsDeleted));

//    Amrish - Cert pinning
        if (!properties.getProperty("sgCert", "none").equals("none")) {
            InputStream is = null;
            try {
                is = new FileInputStream(properties.getProperty("sgCert"));
            } catch (FileNotFoundException ex) {
                logger.error("Sync Gateway Cert not found", ex);
            }
            byte[] cert = null;
            try {
                cert = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                logger.error("Sync Gateway cert error", ex);
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            replicatorConfig.setPinnedServerCertificate(cert);
            if (isContinuous) replicatorConfig.setContinuous(true);
            else replicatorConfig.setContinuous(false);
        }
//end cert pinning
        // Add authentication.
        replicatorConfig.setAuthenticator(new BasicAuthenticator(user, pwd));
//        TODO support session based auth in future
//        replicatorConfig.setAuthenticator(new SessionAuthenticator("00ee4a2fca27d65061f509f89c758e00a4ca83cf"));
        replicator = new Replicator(replicatorConfig);
        //Add Change listener to check for errors
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                logger.error("Error replicating from Sync GW, error:  " + change.getStatus().getError().getMessage()
                        + " " + change.getStatus().getError().getCode());
            }
            logger.debug("Replication Status: " + change.getStatus());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (change.getStatus().getError() != null) {
                        mainController.statusLabel.setText(change.getStatus().getError().getMessage());
                        isReplicatorStarted = false;
                    } else {
                        mainController.statusLabel.setText("Sync Status: " + change.getStatus().getActivityLevel() +
                                "\n" + "Synced  " + change.getStatus().getProgress().getCompleted() + "  of   " +
                                change.getStatus().getProgress().getTotal());
                        if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED) ||
                                change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.IDLE)) {
                            mainController.populateTable();
                        }
                    }
                }
            });
        });

        // Start replication.
        replicator.start();
        isReplicatorStarted = true;
    }

    public static String getSyncStatus() {
        return replicator.getStatus().getActivityLevel().toString();
    }

    public static void onDemandSync() {
        logger.info(" starting onDemandSync");
        replicator.start();
    }

    public static void stopReplication() {
        logger.info("Stopping replication");
        if (replicator != null) {
            replicator.stop();
            isReplicatorStarted = false;
        }
    }

    public static void stopAndDeleteDB() {
        logger.info("- Deleting CBLite DB file");
        if (database != null) {
            try {
                stopReplication();
                Thread.sleep(1000);
                database.delete();
                database.close();
                database = null;
                isReplicatorStarted = false;
                logger.info("CBLite file deleted...");
            } catch (CouchbaseLiteException | InterruptedException ex) {
                logger.error("stopAndDeleteDB", ex);
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

        logger.info("total number of records are :" + cbData.size());
        return cbData;
    }

    private static void loadProperties() {
        try {
//            properties = new Properties();
            properties.loadFromXML(new FileInputStream("config.xml"));
        } catch (IOException e) {
            logger.error("Error in static block", e);
        }
    }
}
