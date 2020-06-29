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

import com.couchbase.lite.CouchbaseLiteException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public SwitchButton continuousToggleRemove;
    public Button initSync;
    public Button reloadTable;
    public Button stopSync;
    public Label tableStatusLabel;
    public TextField tableSearchText;
    @FXML
    public TextField sgURL;
    public SwitchButton loadFullDocToggle;
    //    public ComboBox channelsComboBox;
    public AnchorPane tableAnchorPane;
    public ToggleSwitch loadFullDocSwitch;
    public ToggleSwitch continuousToggle;
    public CheckComboBox channelsComboBoxList;
    Properties properties = new Properties();
    @FXML
    private Button settingsButton;
    @FXML
    private ChoiceBox<String> environment;
    private Map<String, String> cbLiteDataMap;
    private String user = "";
    private String pwd;

    public MainController() {
        cbLiteDataMap = new HashMap<>();
    }

    @FXML
    void openSettings(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            Parent settingRoot = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(settingRoot));
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading Settings.fxml", e);
        }
    }

    void openChannelEditor() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChannelEditor.fxml"));
            Parent settingRoot = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Channel Editor");
            stage.setScene(new Scene(settingRoot));
            ChannelEditorController channelEditorController = fxmlLoader.getController();
            channelEditorController.getParentInstance(this);
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading ChannelEditor.fxml", e);
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
        if (!SyncController.isReplicatorStarted) {
            SyncController.startReplicator(localUser, pwd, continuousToggle.isSelected(), this);
        } else {
            SyncController.onDemandSync();
//                TODO is populate data needed here?
            populateTable(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        readProperties();
        SyncController.createLocalCBLiteFile();
        populateTable(false);
        channelsComboBoxList.getCheckModel().getCheckedItems().addListener((ListChangeListener) change -> {
            logger.info("Selected: " + channelsComboBoxList.getCheckModel().getCheckedItems());
            if (channelsComboBoxList.getCheckModel().getCheckedItems().contains("Click to add...")) {
                channelsComboBoxList.getCheckModel().toggleCheckState(0);
                openChannelEditor();
            }
        });
    }


    @FXML
    public void populateTable(ActionEvent event) {
        populateTable(false);
    }

    public void populateTable(boolean fullDoc) {
        tableStatusLabel.setText("Loading table from CBLite DB...");
//        TODO implement platform.runlater
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    cbLiteDataMap = (SyncController.getDatabase() == null) ? new HashMap<>() : SyncController.getCBLiteData(fullDoc);
                } catch (CouchbaseLiteException e) {
                    logger.info("Unable to read CBLite DB", e);
                }
                docId.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                        return new SimpleStringProperty(p.getValue().getKey());
                    }
                });

                docValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                        return new SimpleStringProperty(p.getValue().getValue());
                    }
                });

                ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(cbLiteDataMap.entrySet());
//        TODO test out filtering
                FilteredList<Map.Entry<String, String>> filteredData = new FilteredList<>(items, p -> true);
                tableSearchText.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(tableData -> {
                        // If filter text is empty, display all persons.
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        String lowerCaseFilter = newValue.toLowerCase();

                        if (tableData.getKey().toLowerCase().contains(lowerCaseFilter)) {
                            return true; // Filter matches key.
                        }
                        return false; // Does not match.
                    });
                });
                dataTable.getColumns().setAll(docId, docValue);
                docId.setEditable(false);
                docId.prefWidthProperty().bind(dataTable.widthProperty().divide(8).multiply(3)); //w*3/8
                docValue.prefWidthProperty().bind(dataTable.widthProperty().divide(8).multiply(5)); //w*5/8
                dataTable.setItems(filteredData);
                dataTable.setRowFactory(tv -> {
                    TableRow<Map.Entry<String, String>> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (!row.isEmpty()) {
                            Map.Entry<String, String> dataValue = row.getItem();
                            String document = "Unable to load document!";
                            if (dataValue.getValue() == "Click to load document...") {
                                try {
                                    document = SyncController.getCBLiteDocument(dataValue.getKey());
                                    dataValue.setValue(document);
                                    row.setItem(dataValue);
                                    dataTable.refresh();
                                } catch (CouchbaseLiteException e) {
                                    logger.error("Error reading doc " + dataValue.getValue() + " from CBLite DB");
                                }
                                showDataPopup(dataValue.getKey(), document);
                            } else
                                showDataPopup(dataValue.getKey(), dataValue.getValue());
                        }
                    });
                    return row;
                });
                showStatusForTable("");
            }
        });
    }

    private void showStatusForTable(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tableStatusLabel.setText(message);
            }
        });
    }

    private void showDataPopup(String key, String value) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tableDataPopup.fxml"));
            Parent dataRoot = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Data Viewer");
            stage.setScene(new Scene(dataRoot, 800, 500));
            DataPopupController dataPopupController = fxmlLoader.getController();
            dataPopupController.loadDataTextArea(key, value);
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
//        TODO do not set default for SG
        properties.setProperty("sgURL", "ws://none");
        properties.setProperty("sgCert", "none");
        properties.setProperty("sgDB", "none");
    }

    public void deleteDB(ActionEvent actionEvent) {
        SyncController.stopAndDeleteDB();
        statusLabel.setText("CBLite DB Deleted");
        populateTable(false);
    }

    public void initSync(ActionEvent actionEvent) {
        SyncController.stopAndDeleteDB();
        SyncController.createLocalCBLiteFile();
        user = "";
        statusLabel.setText("Initialization Complete, you may sync again");
        populateTable(false);
    }

    @FXML
    void toggleContinuousMode(MouseEvent event) {
        if (continuousToggle.isSelected()) {
            stopSync.setDisable(false);
        } else {
            stopSync.setDisable(true);
        }
    }

    @FXML
    void stopContinuousSync(ActionEvent event) {
        SyncController.stopReplication();
    }

    public void loadFullDocument(MouseEvent mouseEvent) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                populateTable(loadFullDocSwitch.isSelected());
            }
        });
    }

    public void setUpChannels(MouseEvent mouseEvent) {
        logger.info("Combo Mouse Entered ");
        if (channelsComboBoxList.getItems().isEmpty()
                || channelsComboBoxList.getItems().get(0).equals("Set Admin URL in Settings...")) {
            readProperties();
            if (properties.getProperty("sgAdminURL", "").isBlank()) {
                if (channelsComboBoxList.getItems().isEmpty()) {
                    channelsComboBoxList.getItems().add("Set Admin URL in Settings...");
                }
            } else {
//                TODO GET channels from SG
//                First empty current item list and then set new channels
                channelsComboBoxList.getItems().removeAll();
                channelsComboBoxList.getItems().add("Click to add...");
                channelsComboBoxList.getItems().add("Channel1");
                channelsComboBoxList.getItems().add("Channel2");
                channelsComboBoxList.getItems().add("Channel3");
            }
        }
    }
}
