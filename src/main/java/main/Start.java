package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Start extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
	    String start = "AdminScene.fxml", mainMenu = "MainMenu.fxml", createGame = "CreateGame.fxml", game = "Game.fxml", roundWinner = "RoundWinner.fxml";
	    Parent root = FXMLLoader.load(getClass().getResource(start));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
	    primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}