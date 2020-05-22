/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 */

package CBLiteTester;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class DataPopupController {

    private static final Log logger = LogFactory.getLog(DataPopupController.class);
    public TextField searchField;
    public Button searchButton;
    public Label docIdLabel;
    public Button editCancelButton;
    int fromIndex = 0;
    @FXML
    private TextArea dataTextArea;
    private String holdSearchText;

    @FXML
    void initialize() {

    }

    public void loadDataTextArea(String docId, String data) {
        dataTextArea.setText(data);
        docIdLabel.setText(docId);
    }

    private String formatData(String data) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson("data");
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
}
