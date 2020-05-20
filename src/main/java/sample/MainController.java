/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 */

package sample;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Replicator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Log logger = LogFactory.getLog(MainController.class);
    public Button syncButton;
    public TextField userText;
    public PasswordField pwdText;
    public Label statusLabel;
    public Button deleteSync;
    public TableView dataTable;
    public TableColumn<Map.Entry<String, String>, String> docId;
    public TableColumn<Map.Entry<String, String>, String> docValue;
    public SwitchButton continuousToggle;
    public Button initSync;
    @FXML
    private Button settingsButton;
    @FXML
    private ChoiceBox<String> environment;
    @FXML
    public TextField sgURL;
    Properties properties = new Properties();
    private Map<String, String> cbLiteDataMap;
    private String user = "";
    private String pwd;

    public MainController() {
        cbLiteDataMap = new HashMap<>();
    }

    @FXML
    void openSettings(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
            Parent settingRoot = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(settingRoot));
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading settings.fxml", e);
        }
    }

    @FXML
    void startSync(ActionEvent event) {
        statusLabel.setText("");
        logger.info("Starting sync");
        String localUser = userText.getText();
        pwd = pwdText.getText();
        if (localUser.isBlank() || pwd.isBlank()) {
            statusLabel.setText("Username and password cannot be blank!");
            return;
        }
        if (!localUser.equals(user) && !user.equals("")) {
            statusLabel.setText("User changed. Initialize DB first and then Sync");
            return;
        }
        user = localUser;
        try {
            if (!InitiateSync.isReplStarted) {
                InitiateSync.startReplicator(localUser, pwd, continuousToggle.isContinuous());
                statusLabel.setText("Syncing...");
                if (InitiateSync.isIsReplError()) {
                    statusLabel.setText("Error syncing data: " + InitiateSync.getReplErrorMsg());
                }
//                TODO populate table should be done after sync is stopped based on a change listener
                populateTable();
            } else {
                InitiateSync.onDemandSync();
//                TODO populate table should be done after sync is stopped based on a change listener
                populateTable();
            }
        } catch (URISyntaxException | CouchbaseLiteException e) {
            logger.info(" Error is" + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        readProperties();
        try {
            InitiateSync.createLocalCBLiteFile();
        } catch (CouchbaseLiteException ex) {
            logger.error(" Error is :" + ex.getMessage());
        }
//        Populate Table
        try {
            populateTable();
        } catch (CouchbaseLiteException e) {
            logger.error("Error populating table from CBLite DB", e);
        }
    }


    private void populateTable() throws CouchbaseLiteException {

        cbLiteDataMap = (InitiateSync.getDatabase() == null) ? new HashMap<>() : InitiateSync.getCBLiteData();
        docId = new TableColumn<>("Key");
        docId.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                return new SimpleStringProperty(p.getValue().getKey());
            }
        });

//        TableColumn<Map.Entry<String, String>, String> docValue = new TableColumn<>("Value");
        docValue = new TableColumn<>("Value");
        docValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });

        ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(cbLiteDataMap.entrySet());
        dataTable.getColumns().setAll(docId, docValue);
        docId.setEditable(false);
//        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        docId.prefWidthProperty().bind(dataTable.widthProperty().divide(4).multiply(1)); //w*1/4
        docValue.prefWidthProperty().bind(dataTable.widthProperty().divide(4).multiply(3)); //w*3/4
        dataTable.setItems(items);
//        ------- Set click on table
        dataTable.setRowFactory(tv -> {
            TableRow<Map.Entry<String, String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Map.Entry<String, String> dataValue = row.getItem();
                    showDataPopup(dataValue.getKey(), dataValue.getValue());
                }
            });
            return row;
        });
    }

    private void showDataPopup(String key, String value) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tableDataPopup.fxml"));
            Parent dataRoot = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Data Viewer");
            stage.setScene(new Scene(dataRoot,800,500));
            DataPopupController dataPopupController = fxmlLoader.getController();
            dataPopupController.loadDataTextArea(value);
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading tableDataPopup.fxml", e);
        }
    }

    private void readProperties() {
        FileInputStream in;
        try {
            in = new FileInputStream("config.xml");
            properties.loadFromXML(in);
//            logger.info("Author :" + properties.getProperty("author"));
//            logger.info("sgURL: " + properties.getProperty("sgURL"));
//            logger.info("cblite-loc: " + properties.getProperty("cblite-loc"));
        } catch (IOException e) {
            logger.error("IO Exception on config file", e);
            setupProperties();
        }
    }

    private void setupProperties() {
        logger.error("Bad properties file, reloading defaults");
        try {
            File configFile = new File("config.xml");
            FileOutputStream out = new FileOutputStream(configFile);
            setDefaultProperties();
            properties.storeToXML(out, "Configuration");
        } catch (IOException e) {
            logger.error("Exception writing default config file", e);
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("cblite-loc", "C:\\couchbaselight/resources");
        properties.setProperty("author", "amrishraje@gmail.com");
        properties.setProperty("sgURL", "ws://peplap04996.corp.pep.pvt:4984/syncdb");
        properties.setProperty("sgCert", "none");
    }

    public void deleteDB(ActionEvent actionEvent) throws CouchbaseLiteException {
        InitiateSync.stopAndDeleteDB();
        statusLabel.setText("CBLite DB Deleted");
        populateTable();
    }

    public void initSync(ActionEvent actionEvent) throws CouchbaseLiteException {
        InitiateSync.stopAndDeleteDB();
        InitiateSync.createLocalCBLiteFile();
        user = "";
        statusLabel.setText("Initialization Complete, you may sync again");
        populateTable();
    }
}
