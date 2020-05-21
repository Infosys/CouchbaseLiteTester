/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 */

package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataPopupController {

    private static final Log logger = LogFactory.getLog(DataPopupController.class);
    public TextField searchField;
    public Button searchButton;
    int fromIndex = 0;
    @FXML
    private TextArea dataTextArea;
    private String holdSrchTxtx;

    @FXML
    void initialize() {

    }

    public void loadDataTextArea(String data) {
        dataTextArea.setText(data);
    }

    @FXML
    void searchHandler(ActionEvent event) {
        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            holdSrchTxtx = searchField.getText();
            if (!holdSrchTxtx.equals(searchField.getText())) fromIndex = 0;
            int index = dataTextArea.getText().toLowerCase().indexOf(searchField.getText(), fromIndex);
//            TODO fix wrap around search
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


}
