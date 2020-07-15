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

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public ComboBox sgScheme;
    public TextField sgURL;
    public TextField sgPort;
    public TextField sgDB;
    public TitledPane cbLitePane;
    public Button cbLiteSettingsSave;
    public TextField cbLitePath;
    public Label sgSettingsErrLabel;
    public Button cancelButton;
    public AnchorPane settingsPane;
    public Button chooseCert;
    public TextField sgAdminText;
    Properties properties = new Properties();
    Properties defaults = new Properties();
    @FXML
    private Button sgSave;
    @FXML
    private ComboBox<String> environment;
    @FXML
    private TitledPane sgPane;
    @FXML
    private TextField sgCertText;

    @FXML
    void environmentAction(ActionEvent event) {
        sgURL.setText(defaults.getProperty(environment.getValue() + ".sgURL", "undefined"));
        sgPort.setText(defaults.getProperty(environment.getValue() + ".sgPort", "undefined"));
        sgDB.setText(defaults.getProperty(environment.getValue() + ".sgDB", "undefined"));
        sgScheme.setValue(defaults.getProperty(environment.getValue() + ".sgScheme", "undefined"));
        sgAdminText.setText(defaults.getProperty(environment.getValue() + ".sgAdminURL", "undefined"));
        sgCertText.setText("");
    }

    @FXML
    void certBox(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Certificate");
        File cert = fileChooser.showOpenDialog(settingsPane.getScene().getWindow());
        sgCertText.setText(cert.getPath());
    }

    @FXML
    void cbLiteBox(ActionEvent event) {
//        FileChooser fileChooser = new FileChooser();
//        File cert = fileChooser.showOpenDialog(settingsPane.getScene().getWindow());
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select CBLite Directory");
//        File defaultDirectory = new File("C:\\couchbaselite");
//        directoryChooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = directoryChooser.showDialog(settingsPane.getScene().getWindow());
        cbLitePath.setText(selectedDirectory.getPath());
    }

    @FXML
    void sgSaveAction(ActionEvent event) {
        String sgURLValue;
        sgSettingsErrLabel.setText("");
        if (sgURL.getText().isBlank()) {
            sgSettingsErrLabel.setText("URL cannot be blank");
            return;
        }
        if (sgPort.getText().isBlank())
            sgURLValue = sgScheme.getValue() + sgURL.getText() + "/" + sgDB.getText();
        else
            sgURLValue = sgScheme.getValue() + sgURL.getText() + ":" + sgPort.getText() + "/" + sgDB.getText();
        properties.setProperty("sgURL", sgURLValue);
        properties.setProperty("sgDB", sgDB.getText());
        properties.setProperty("cblite-loc", cbLitePath.getText());
        properties.setProperty("sgAdminURL", sgAdminText.getText());
        if (sgScheme.getValue().toString().contains("wss")) {
            if (sgCertText.getText().isBlank()) {
                sgSettingsErrLabel.setText("Certificate is required for wss scheme. Hint: Browse to SG URL and download cert");
                return;
            } else {
                properties.setProperty("sgCert", sgCertText.getText());
            }
        }
        try {
            FileOutputStream out = new FileOutputStream("config.xml");
            properties.storeToXML(out, "Configuration");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing config file", e);
        }
        Stage stage = (Stage) sgSave.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            properties.loadFromXML(new FileInputStream("config.xml"));
            defaults.loadFromXML(new FileInputStream("defaults.xml"));
        } catch (IOException e) {
            logger.error("Error reading config file", e);
        }
        String environments[] = defaults.getProperty("environments").split(",");
        environment.setItems(FXCollections.observableArrayList(environments));
        sgScheme.setItems(FXCollections.observableArrayList("ws://", "wss://"));
//      Setup previous settings from config.xml file
        loadDefaultsFromConfigXml();
    }

    private void loadDefaultsFromConfigXml() {
        if (!properties.getProperty("sgDB").equals("none")) {
            String syncScheme = properties.getProperty("sgURL").split("://")[0];
            String syncURL = properties.getProperty("sgURL").split("://")[1].split(":")[0];
            String syncPort = properties.getProperty("sgURL").split("://")[1].split(":")[1].split("/")[0];
            sgURL.setText(syncURL);
            sgPort.setText(syncPort);
            sgDB.setText(properties.getProperty("sgDB"));
            sgScheme.setValue(syncScheme + "://");
            sgAdminText.setText(properties.getProperty("sgAdminURL"));
            sgCertText.setText(properties.getProperty("sgCert"));
            cbLitePath.setText(properties.getProperty("cblite-loc"));
        }
    }
}
