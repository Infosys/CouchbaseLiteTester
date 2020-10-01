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

public class DataPopupController {

    private static final Logger logger = LoggerFactory.getLogger(DataPopupController.class);
    public TextField searchField;
    public Button searchButton;
    public TextField docIdLabel;
    public Button editCancelButton;
    public Button documentSaveButton;
    public Label saveStatusLabel;
    public Button attachmentButton;
    int fromIndex = 0;
    MainController mainController;
    int tableIndex;
    @FXML
    private TextArea dataTextArea;
    private String holdSearchText;
    private FileInputStream is = null;
    private String mimeType = null;

    @FXML
    void initialize() {

    }

    public void loadDataTextArea(String docId, String data, MainController mainController1, int index) {
        dataTextArea.setText(formatData(data));
        docIdLabel.setText(docId);
        this.mainController = mainController1;
        tableIndex = index;
    }

    private String formatData(String data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(JsonParser.parseString(data));
    }

    private String minifyData(String data) {
        Gson gson = new Gson();
        return gson.toJson(JsonParser.parseString(data));
    }

    @FXML
    void searchHandler(ActionEvent event) {
        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            holdSearchText = searchField.getText();
            if (!holdSearchText.equals(searchField.getText())) fromIndex = 0;
            int index = dataTextArea.getText().toLowerCase().indexOf(searchField.getText(), fromIndex);
            if (index == -1) {
                if (fromIndex > 0) {
//                  string was previously found, so wrap around and search
                    fromIndex = 0;
                    index = dataTextArea.getText().toLowerCase().indexOf(searchField.getText(), fromIndex);
                    highlightSearchText(index);
                }
            } else {
                highlightSearchText(index);
            }
        }
    }

    private void highlightSearchText(int index) {
        fromIndex = index + searchField.getLength();
        dataTextArea.selectRange(index, index + searchField.getLength());
        dataTextArea.setStyle("-fx-highlight-fill: lightgray; -fx-highlight-text-fill: firebrick;");
    }

    @FXML
    void cancelEdit(ActionEvent event) {
        Stage stage = (Stage) editCancelButton.getScene().getWindow();
        stage.close();
    }

    public void saveEditedDocument(ActionEvent event) {
//        Todo - refresh table after save
        try {
            SyncController.setCBLiteDocument(docIdLabel.getText(), dataTextArea.getText(), is, mimeType);
            mainController.refreshTable(minifyData(dataTextArea.getText()), tableIndex);
            Stage stage = (Stage) editCancelButton.getScene().getWindow();
            stage.close();
        } catch (JsonSyntaxException exception) {
            saveStatusLabel.setText("Malformed JSON, please correct");
            logger.error("Malformed JSON - {}", exception.getMessage());
        }
    }

    public void chooseAttachment(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        File attachment = fileChooser.showOpenDialog(attachmentButton.getScene().getWindow());
        if (attachment == null) return;
        logger.debug("Attachment path is: {}", (attachment.getPath()));
        try {
            is = new FileInputStream(attachment);
            mimeType = Files.probeContentType(attachment.toPath());
            saveStatusLabel.setText("Attachment Added. Please save document");
            logger.debug("Attachment is of type: {}", mimeType);
        } catch (FileNotFoundException e) {
            logger.info("attachment not found!!");
        } catch (IOException e) {
            logger.info("Cannot figure out attachment MIME");
        }
        logger.debug("Input Stream is: {}", is);
    }
}
