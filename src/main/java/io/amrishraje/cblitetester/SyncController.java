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

package io.amrishraje.cblitetester;

import com.couchbase.lite.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import javafx.application.Platform;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SyncController {
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);
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

    public static void startReplicator(String user, String pwd, boolean isContinuous, List<String> channels, MainController controller) {
        logger.debug("calling startReplicator");
        logger.debug("syncing channels: " + channels);
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

//      Set sync channels
        replicatorConfig.setChannels(channels);
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
                            mainController.populateTable(false);
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

    public static HashMap getCBLiteData(boolean fullDoc) throws CouchbaseLiteException {
        HashMap<String, String> cbData = new HashMap<>(10);
        Query queryAll = QueryBuilder.select(SelectResult.all(),
                SelectResult.expression(Meta.id))
                .from(DataSource.database(database));
        ResultSet resultFull = queryAll.execute();
        Gson gson = new Gson();

        for (Result result: resultFull){
            if (fullDoc)
//                cbData.put(result.getString("id"), result.toList().get(0).toString());
                cbData.put(result.getString("id"), gson.toJson(result.toMap().get(DB_NAME)));
            else
                cbData.put(result.getString("id"), "Click to load document...");
        }
        logger.info("total number of records are :" + cbData.size());
        return cbData;
    }

    public static String getCBLiteDocument(String docId) throws CouchbaseLiteException{
        Document doc = database.getDocument(docId);
        Gson gson = new Gson();
        String json = gson.toJson(doc.toMap());
        return json;
    }

    public static void setCBLiteDocument(String key, String value) throws JsonSyntaxException {
//        todo fix this method to save whole doc
        Document doc = database.getDocument(key);
        MutableDocument mutableDocument = doc.toMutable();
        Gson gson = new Gson();
        Map dataMap = gson.fromJson(value, Map.class);
        mutableDocument.setData(dataMap);
        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            logger.info("Unable to save doc {}",key);
        }
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
