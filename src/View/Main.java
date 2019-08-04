package View;

import Model.*;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MyModel myModel = new MyModel();
        myModel.startServers();
        MyViewModel myViewModel = new MyViewModel(myModel);
        myModel.addObserver(myViewModel);
        //--------------
        primaryStage.setTitle("Solve The Maze Application!");
        primaryStage.setMaxHeight(900);
        primaryStage.setMaxWidth(1135);
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MyView.fxml").openStream());
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        //--------------
        MyViewController myViewController = fxmlLoader.getController();
        myViewController.setResizeEvent(scene);
        myViewController.setViewModel(myViewModel);
        myViewModel.addObserver(myViewController);
        //--------------
        SetStageCloseEvent(primaryStage);
        primaryStage.show();
        TextInputDialog dialog = new TextInputDialog("New Guest");
        dialog.setTitle("The Maze Application");
        dialog.setHeaderText("Welcome !!!");
        dialog.setContentText("Hi, what is your name?");
        Optional<java.lang.String> result = dialog.showAndWait();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("The Maze Application");
        if ((Object)result==Optional.empty()) {
            int exitCode = 0;
            MyModel.stopServers();
            //------------------------------------------------> thread pool shut down
            System.exit(exitCode);
        }
        String name = result.get();



        if (result.get() == "" || result.get() == null)
            name = "Guest";
        alert.setContentText("Hi, " + name + ".\nWe are happy to see you in our game.\nWe wish you luck,have fun!");
        Optional<ButtonType> result2 = alert.showAndWait();
        if (result2.get() == ButtonType.CANCEL) {
            int exitCode = 0;
            MyModel.stopServers();
            //------------------------------------------------> thread pool shut down
            System.exit(exitCode);
        }
    }


    private void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Exit Game");
                alert.setContentText("Are you sure you want to leave this amazing game?");
                //Image image = new Image(getClass().getResource("resources/Images/character.jpg").toExternalForm());
                //ImageView imageView = new ImageView(image);
                //alert.setGraphic(imageView);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    int exitCode = 0;
                    MyModel.stopServers();
                    //Platform.exit();
                    System.exit(exitCode);
                } else {
                    windowEvent.consume();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
