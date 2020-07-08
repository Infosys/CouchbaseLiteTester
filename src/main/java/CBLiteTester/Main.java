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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main extends Application {

    private static final Log logger = LogFactory.getLog(Main.class);
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("=============================================================================");
        System.out.println("Couchbase CBLite Tester");
        System.out.println("Author: Amrish Raje");
        System.out.println("Repo: https://github.com/amrishraje/CBLiteTester");
        System.out.println("License: https://github.com/amrishraje/CBLiteTester/blob/master/LICENSE");
        System.out.println("=============================================================================");
        Parent root = FXMLLoader.load(getClass().getResource("CBLiteScreen.fxml"));
        primaryStage.setTitle("Couchbase Lite Sync");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                SyncController.stopReplication();
                Platform.exit();
                System.exit(0);
            }
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
