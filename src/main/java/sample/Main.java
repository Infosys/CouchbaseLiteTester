package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main extends Application {

    private static final Log logger = LogFactory.getLog(Main.class);
    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("settings.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("CBLiteScreen.fxml"));
        primaryStage.setTitle("Couchbase Lite Sync");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
