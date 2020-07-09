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


import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ChannelEditorController implements Initializable {

    public Button setChannelsButton;
    public AnchorPane editChannelPane;
    public TextField channelsList;
    public String[] channels;
    public MainController mainController;

    //    TODO clean up this code
    public void saveChannelEditor(ActionEvent event) {
        channels = channelsList.getText().split(",");
        Arrays.stream(channels).forEach(s -> mainController.channelsComboBoxList.getItems().add(s));
        mainController.channelsComboBoxList.getCheckModel().clearChecks();
        closeWindow(event);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                channelsList.requestFocus();
//            }
//        });
    }

    public void getParentInstance(MainController mainController) {
        this.mainController = mainController;
    }

    public void closeWindow(ActionEvent event) {
        Stage stage = (Stage) editChannelPane.getScene().getWindow();
        stage.close();
    }
}
