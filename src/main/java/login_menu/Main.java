package login_menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//import java.util.prefs.Preferences;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
//        Preferences prefs =

        Parent root = FXMLLoader.load(getClass().getResource("LoginScene.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
