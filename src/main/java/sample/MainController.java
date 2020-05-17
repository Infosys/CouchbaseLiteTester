package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Log logger = LogFactory.getLog(Main.class);
    @FXML
    private Button settingsButton;
    @FXML
    private ChoiceBox<String> environment;
    @FXML
    public TextField sgURL;
    Properties properties = new Properties();

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
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        readProperties();
    }

    private void readProperties() {
        FileInputStream in;
        try {
            in = new FileInputStream("config.xml");
            properties.loadFromXML(in);
            logger.info("Author :" + properties.getProperty("author"));
        } catch (IOException e) {
            logger.error("IO Exception on config file", e);
            setupProperties();
        }
    }

    private void setupProperties() {
        logger.info("Bad properties file, reloading defaults");
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
    }
}
