package io.amrishraje.cblitetester;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

public class NewDocController {

    private static final Logger logger = LoggerFactory.getLogger(NewDocController.class);
    public Label saveStatusLabel;
    public Button formatDataButton;
    public TextField docIdText;
    public Button attachmentButton;
    @FXML
    private TextArea dataTextArea;

    @FXML
    private Button editCancelButton;

    @FXML
    private Button newDocSaveButton;
    private InputStream is = null;
    private String mimeType = null;

    @FXML
    void cancelEdit(ActionEvent event) {
        Stage stage = (Stage) editCancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void saveNewDocument(ActionEvent event) {
        try {
            saveStatusLabel.setText("");
            if (dataTextArea.getText().isBlank() || docIdText.getText().isBlank()) {
                saveStatusLabel.setText("Document ID and Document Body cannot be blank");
                return;
            }
            String savedData = minifyData(dataTextArea.getText());
            String docId = docIdText.getText();
            SyncController.setCBLiteDocument(docId, savedData, is, mimeType);
            Stage stage = (Stage) editCancelButton.getScene().getWindow();
            stage.close();
        } catch (JsonSyntaxException exception) {
            saveStatusLabel.setText("Malformed JSON, please correct");
            logger.error("Malformed JSON - {}", exception.getMessage());
        }
    }

    private String minifyData(String data) {
        Gson gson = new Gson();
        return gson.toJson(JsonParser.parseString(data));
    }

    public void formatData(ActionEvent event) {
        try {
            saveStatusLabel.setText("");
            dataTextArea.setText(formatInput(dataTextArea.getText()));
        } catch (JsonSyntaxException exception) {
            saveStatusLabel.setText("Malformed JSON, please correct");
            logger.error("Malformed JSON - {}", exception.getMessage());
        }
    }

    private String formatInput(String data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(JsonParser.parseString(data));
    }

    public void chooseAttachment(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        File attachment = fileChooser.showOpenDialog(attachmentButton.getScene().getWindow());
        logger.debug("Attachment path is: {}", (attachment.getPath()));
        try {
            is = new FileInputStream(attachment);
            mimeType = Files.probeContentType(attachment.toPath());
            logger.debug("Attachment is of type: {}", mimeType);
        } catch (FileNotFoundException e) {
            logger.error("attachment not found!!");
        } catch (IOException e) {
            logger.error("Cannot figure out attachment MIME");
        }
        logger.debug("Input Stream is: {}", is);
    }
}
