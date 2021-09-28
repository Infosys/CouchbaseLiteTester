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
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
            logger.error("Error in static block while reading config file", e);
        }
    }
    public static MainController mainController;
    public static boolean isReplicatorStarted = false;

    public static void setIsReplicatorStarted(boolean isReplicatorStarted) {
        SyncController.isReplicatorStarted = isReplicatorStarted;
    }

    public static String DB_NAME = properties.getProperty("sgDB", "syncdb");
    public static String DB_PATH = properties.getProperty("cblite-loc");
    public static String SYNC_GATEWAY_URL = properties.getProperty("sgURL");
    private static Database database;
    private static Replicator replicator;
    private static ReplicatorConfiguration replicatorConfig;

    public static Database getDatabase() {
        return database;
    }

    private static final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private static int size;

    public static double getProgress() {
        return progressProperty().get();
    }

    public static ReadOnlyDoubleProperty progressProperty() {
        return progress ;
    }

    public static void createLocalCBLiteFile() {
        // Initialize Couchbase Lite
        CouchbaseLite.init();
        // Get the database (and create it if it doesn't exist).
        DatabaseConfiguration config = new DatabaseConfiguration();
        loadProperties();
        config.setDirectory(DB_PATH);
        try {
            SyncController.database = new Database(DB_NAME, config);
        } catch (CouchbaseLiteException e) {
            logger.info("Unable to create CBLite DB", e);
        }
        logger.debug("CbLite file has been created and database has been initialized");
    }

    public static void startReplicator(String user, String pwd, String sessionToken, boolean isContinuous, List<String> channels, String replicationMode, MainController controller) {
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
        replicatorConfig = new ReplicatorConfiguration(database, targetEndpoint);
        logger.debug("Replication Mode is {}", replicationMode);
        switch (replicationMode) {
            case "Push":
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH);
                break;
            case "Pull and Push":
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
                break;
            case "Pull":
            default:
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
                break;
        }
//        Do not replicate deleted docs!
        replicatorConfig.setPullFilter((document, flags) -> !flags.contains(DocumentFlag.DocumentFlagsDeleted));
        replicatorConfig.setPushFilter((document, flags) -> !flags.contains(DocumentFlag.DocumentFlagsDeleted));

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
        if (sessionToken.isBlank()) {
            logger.info("Password is: {}", pwd);
            replicatorConfig.setAuthenticator(new BasicAuthenticator(user, pwd));
        }
        else {
            logger.info("Session Token is: {}", sessionToken);
            replicatorConfig.setAuthenticator(new SessionAuthenticator(sessionToken));
        }

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

    public static void onDemandSync(boolean isContinuous, List<String> channels, String replicationMode) {
        logger.info(" starting onDemandSync");
        replicator.stop();
        if (isContinuous) replicatorConfig.setContinuous(true);
        else replicatorConfig.setContinuous(false);
        replicatorConfig.setChannels(channels);
        switch (replicationMode) {
            case "Push":
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH);
                break;
            case "Pull and Push":
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
                break;
            case "Pull":
            default:
                replicatorConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
                break;
        }
        replicator = new Replicator(replicatorConfig);
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
        replicator.start();
    }

    public static void stopReplication() {
        logger.info("Stopping replication");
        if (replicator != null) {
            replicator.stop();
            isReplicatorStarted = false;
        }
    }

    public static void stopAndDeleteDB() throws CouchbaseLiteException, InterruptedException {
        logger.debug("- Deleting CBLite DB file");
        if (database != null) {
            try {
                stopReplication();
                Thread.sleep(1000);
                database.delete();
                database.close();
                database = null;
                isReplicatorStarted = false;
                logger.debug("CBLite file deleted...");
                logger.info("Replication Stopped and CBLite DB Deleted");
            } catch (CouchbaseLiteException | InterruptedException ex) {
                logger.error("Unable to delete CBLite DB", ex);
                throw ex;
            }
        }
    }

    public static HashMap getCBLiteData(boolean fullDoc) throws CouchbaseLiteException {
        logger.info("Start: {}", System.currentTimeMillis());
        HashMap<String, String> cbData = new HashMap<>(10);
        Query queryAll = QueryBuilder.select(SelectResult.all(),
                SelectResult.expression(Meta.id))
                .from(DataSource.database(database));
        ResultSet resultFull = queryAll.execute();
        Gson gson = new Gson();

        int i = 0;
        for (Result result : resultFull) {
            if (fullDoc) {
                i++;
                progress.set(i * 1.0/ size);
                cbData.put(result.getString("id"), gson.toJson(result.toMap().get(DB_NAME)));
            } else {
                cbData.put(result.getString("id"), "Click to load document...");
            }
        }
        size = cbData.size();
        logger.info("total number of records are :" + size);
        logger.info("End: {}", System.currentTimeMillis());
        return cbData;
    }

    public static HashMap getCBLiteDataV2(boolean fullDoc) throws CouchbaseLiteException {
//        TODO - Performance tuning for fetching all docs
        logger.info("Start: {}", System.currentTimeMillis());
        HashMap<String, String> cbData = new HashMap<>(10);
        Document doc;
        Gson gson = new Gson();
        Query queryAll = QueryBuilder.select(SelectResult.all(),SelectResult.expression(Meta.id))
                .from(DataSource.database(database));
        ResultSet resultFull = queryAll.execute();
        logger.info("Start List: {}", System.currentTimeMillis());
        List<Result> list = resultFull.allResults();
        Spliterator<Result> sp1 = list.spliterator().trySplit();
        Spliterator<Result> sp2 = list.spliterator().trySplit();
        sp1.forEachRemaining(result -> {
            if (!fullDoc)
                cbData.put(result.getString("id"), "Click to load document...");
            else
                cbData.put(result.getString("id"), gson.toJson(result.toMap().get(DB_NAME)));
        });
        sp2.forEachRemaining(result -> {
            if (!fullDoc)
                cbData.put(result.getString("id"), "Click to load document...");
            else
                cbData.put(result.getString("id"), gson.toJson(result.toMap().get(DB_NAME)));
        });
        logger.info("End List: {}", System.currentTimeMillis());
        logger.info("total number of records are :" + cbData.size());
        logger.info("End: {}", System.currentTimeMillis());
        return cbData;
    }

    public static String getCBLiteDocument(String docId) throws CouchbaseLiteException {
        Document doc = database.getDocument(docId);
        Gson gson = new Gson();
        String json = null;
        try {
            json = gson.toJson(doc.toMap());
        } catch (IllegalArgumentException exception) {
            Query queryAll = QueryBuilder.select(SelectResult.all(),
                    SelectResult.expression(Meta.id))
                    .from(DataSource.database(database))
                    .where(Expression.property("_id").equalTo(Expression.string(docId)));
            ResultSet resultFull = queryAll.execute();

            for (Result result : resultFull) {
                json = gson.toJson(result.toMap().get(DB_NAME));
            }
        }
        return json;
    }

    public static void setCBLiteDocument(String key, String value, InputStream is, String mimeType) throws JsonSyntaxException {
        Document doc = database.getDocument(key);
        MutableDocument mutableDocument;
        if (doc != null) {
             mutableDocument = doc.toMutable();
        } else {
            mutableDocument = new MutableDocument(key);
        }
        Gson gson = new Gson();
        Map dataMap = gson.fromJson(value, Map.class);
        mutableDocument.setData(dataMap);

//        Add attachments
        if (is != null){
        Blob blob = new Blob(mimeType, is);
        mutableDocument.setBlob("docAttachment",blob);
        }

        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            logger.info("Unable to save doc {}",key);
        }
    }

    private static void loadProperties() {
        try {
            properties.loadFromXML(new FileInputStream("config.xml"));
            DB_NAME = properties.getProperty("sgDB", "syncdb");
            DB_PATH = properties.getProperty("cblite-loc");
            SYNC_GATEWAY_URL = properties.getProperty("sgURL");
        } catch (IOException e) {
            logger.error("Error loading config file", e);
        }
    }
}
