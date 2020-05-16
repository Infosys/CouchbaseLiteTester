package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final Log logger = LogFactory.getLog(Main.class);
    public ComboBox sgScheme;
    public TextField sgURL;
    public TextField sgPort;
    public TextField sgDB;
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
    @FXML
    void environmentAction(ActionEvent event) {
        switch (environment.getValue()){
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
                sgPort.setText("4984");
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
        String sgURLValue = sgScheme.getValue() + sgURL.getText();
        System.setProperty("sgURL",sgURLValue);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        environment.setItems(FXCollections.observableArrayList("Dev","QA","Perf","POC"));
        settingsAccordion.setExpandedPane(sgPane);
        sgScheme.setItems(FXCollections.observableArrayList("ws://","wss://"));
    }
}
