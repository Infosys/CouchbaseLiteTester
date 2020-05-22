/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 */

package CBLiteTester;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final Log logger = LogFactory.getLog(Main.class);
    public ComboBox sgScheme;
    public TextField sgURL;
    public TextField sgPort;
    public TextField sgDB;
    public TitledPane cbLitePane;
    public Button cbLiteSettingsSave;
    public TextField cbLitePath;
    public Label sgSettingsErrLabel;
    @FXML
    private Button sgSave;
    public Button chooseCert;
    @FXML
    private ComboBox<String> environment;
    @FXML
    private Accordion settingsAccordion;
    @FXML
    private TitledPane sgPane;
    @FXML
    private TextField sgCertText;
    Properties properties = new Properties();

    @FXML
    void environmentAction(ActionEvent event) {
        switch (environment.getValue()) {
            case "QA":
                sgURL.setText("peplap04997.corp.pep.pvt");
                sgPort.setText("4984");
                sgDB.setText("syncdb");
                sgScheme.setValue("ws://");
                break;
            case "Perf":
                sgURL.setText("undefined");
                sgPort.setText("4984");
                sgDB.setText("syncdb");
                sgScheme.setValue("ws://");
                break;
            case "POC":
                sgURL.setText("52.153.112.176");
                sgPort.setText("443");
                sgDB.setText("syncdb");
                sgScheme.setValue("wss://");
                break;
            case "Dev":
            default:
                sgURL.setText("peplap04996.corp.pep.pvt");
                sgPort.setText("4984");
                sgDB.setText("syncdb");
                sgScheme.setValue("ws://");
                break;
        }
    }

    @FXML
    void certBox(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File cert = fileChooser.showOpenDialog(sgPane.getScene().getWindow());
        sgCertText.setText(cert.getPath());
    }

    @FXML
    void sgSaveAction(ActionEvent event) {
        String sgURLValue;
        sgSettingsErrLabel.setText("");
        if (sgPort.getText().isBlank())
            sgURLValue = sgScheme.getValue() + sgURL.getText() + "/" + sgDB.getText();
        else
            sgURLValue = sgScheme.getValue() + sgURL.getText() + ":" + sgPort.getText() + "/" + sgDB.getText();
        properties.setProperty("sgURL", sgURLValue);
        properties.setProperty("sgDB", sgDB.getText());
        if (sgScheme.getValue().toString().contains("wss")) {
            if (sgCertText.getText().isBlank()) {
                sgSettingsErrLabel.setText("Certificate is required for wss scheme");
            } else {
                properties.setProperty("sgCert",sgCertText.getText());
            }
        }
        try {
            FileOutputStream out = new FileOutputStream("config.xml");
            properties.storeToXML(out, "Configuration");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing config file", e);
        }
    }

    @FXML
    void cbLiteSave(ActionEvent event) {
        properties.setProperty("cblite-loc", cbLitePath.getText());
        try {
            FileOutputStream out = new FileOutputStream("config.xml");
            properties.storeToXML(out, "Configuration");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing config file", e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        environment.setItems(FXCollections.observableArrayList("Dev", "QA", "Perf", "POC"));
        settingsAccordion.setExpandedPane(sgPane);
        sgScheme.setItems(FXCollections.observableArrayList("ws://", "wss://"));
        try {
            properties.loadFromXML(new FileInputStream("config.xml"));
        } catch (IOException e) {
            logger.error("Error reading config file", e);
        }
    }
}
