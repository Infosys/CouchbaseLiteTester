/*
 * Copyright (c) 2020.  amrishraje@gmail.com
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
//        Parent root = FXMLLoader.load(getClass().getResource("Settings.fxml"));
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
