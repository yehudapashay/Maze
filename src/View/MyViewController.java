package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class MyViewController implements Observer, IView {

    @FXML
    private MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;

    public double lastX = 0;
    public double lastY = 0;

    public javafx.scene.control.TextField txtfld_rowsNum;
    public javafx.scene.control.TextField txtfld_columnsNum;
    public javafx.scene.control.Label lbl_rowsNum;
    public javafx.scene.control.Label lbl_columnsNum;
    public javafx.scene.control.Button btn_generateMaze;
    public javafx.scene.control.Button btn_solveMaze;
    public javafx.scene.control.MenuItem newmaze;

    public MediaPlayer mediaPlayer;

    public StringProperty characterPositionRow = new SimpleStringProperty();
    public StringProperty characterPositionColumn = new SimpleStringProperty();

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        //  bindProperties(viewModel);
        Media media = null;
        String path = "resources/music/play.mp3";
        //media = new Media(new File(path).toURI().toString());
        try {
            media = new Media(this.getClass().getResource("/music/play.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            System.out.println("FAIL LOAD");
        }
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setStartTime(Duration.seconds(0));
        mediaPlayer.setStopTime(Duration.seconds(110));
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setAutoPlay(true);
    }

    /*
        private void bindProperties(MyViewModel viewModel) {
            lbl_rowsNum.textProperty().bind(viewModel.characterPositionRow);
            lbl_columnsNum.textProperty().bind(viewModel.characterPositionColumn);
        }
    */
    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            boolean gameDone = false;
            Stage s = (Stage) btn_generateMaze.getScene().getWindow();
            double height = s.getHeight() - 90;
            double width = s.getWidth() - 280;
            int goalRow, goalCol;
            goalRow = viewModel.getGoalPos().getRowIndex();
            goalCol = viewModel.getGoalPos().getColumnIndex();
            int currRow, currCol;
            currRow = viewModel.getCharacterPositionRow();
            currCol = viewModel.getCharacterPositionColumn();
            mazeDisplayer.setGoalPosition(viewModel.getGoalPos());
            gameDone = (goalRow == currRow) && (goalCol == currCol);
            displayMaze(viewModel.getMaze(), viewModel.getSolutionPath(), height, width);
            if (gameDone) {
                Stage winnerPlayer;
                BorderPane borderPane = new BorderPane();
                mediaPlayer.stop();
                Media media=null;
                String path = "resources/music/done.mp4";
                try {
                    media = new Media(this.getClass().getResource("/music/done.mp4").toURI().toString());
                } catch (URISyntaxException e) {
                    System.out.println("FAIL LOAD");
                }
                //media = new Media(new File(path).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                //mediaPlayer.setAutoPlay(true);
                borderPane.getChildren().add(new MediaView(mediaPlayer));
                ////////////////////////////////////
                int sWidth = 750, sHeight = 460;
                winnerPlayer = new Stage();
                winnerPlayer.setTitle("You are the winner!!!");
                winnerPlayer.initModality(Modality.APPLICATION_MODAL);
                winnerPlayer.setScene(new Scene(borderPane, sWidth, sHeight, Color.TRANSPARENT));
                stableSizeForVideo(winnerPlayer, sWidth, sHeight);
                winnerPlayer.show();
                winnerPlayer.setOnCloseRequest(event -> {
                    mediaPlayer.stop();
                    //-------------------->init maze to be null and solution to be null
                    mazeDisplayer.initDisplayer();
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("The Maze Application");
                    alert.setContentText("Thank you for playing our game!\nWe hope you have enjoyed!");
                    alert.showAndWait();
                });
            }
            btn_generateMaze.setDisable(false);
            btn_solveMaze.setDisable(false);
        }
    }

    private void stableSizeForVideo(Stage winnerPlayer, int sWidth, int sHeight) {
        winnerPlayer.setMinWidth(sWidth);
        winnerPlayer.setMaxWidth(sWidth);
        winnerPlayer.setMinHeight(sHeight);
        winnerPlayer.setMaxHeight(sHeight);
    }

    @Override
    public void displayMaze(int[][] maze, ArrayList<AState> solutionPath, double width, double height) {
        mazeDisplayer.setMaze(maze, solutionPath, width, height);
        int characterPositionRow = viewModel.getCharacterPositionRow();
        int characterPositionColumn = viewModel.getCharacterPositionColumn();
        mazeDisplayer.setCharacterPosition(characterPositionRow, characterPositionColumn, width, height);
        mazeDisplayer.setGoalPosition(viewModel.getGoalPos());
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");
    }

    public void newMaze() { //-------------------------> should be action event? and a problem with txt field
        TextInputDialog dialog1 = new TextInputDialog("rows");
        dialog1.setTitle("Choose maze Dimensions");
        dialog1.setHeaderText("Enter rows");
        dialog1.setContentText("Please enter rows' size:");
        Optional<String> result1 = dialog1.showAndWait();
        TextInputDialog dialog2 = new TextInputDialog("columns");
        dialog2.setTitle("Choose maze Dimensions");
        dialog2.setHeaderText("Enter columns");
        dialog2.setContentText("Please enter columns' size:");
        Optional<String> result2 = dialog2.showAndWait();
        txtfld_rowsNum.setText(result1.get());
        txtfld_columnsNum.setText(result2.get());
        generateMaze();
        //Platform.runLater(()->mazeDisplayer.requestFocus());
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public void generateMaze() {
        //newmaze.setDisable(false);
        int height, width;
        boolean areLegalInt = isInteger(txtfld_rowsNum.getText()) && isInteger(txtfld_columnsNum.getText());
        if (!areLegalInt) {
            height = -1;
            width = -1;
        } else {
            height = Integer.valueOf(txtfld_rowsNum.getText());
            width = Integer.valueOf(txtfld_columnsNum.getText());
        }
        if (height <= 3 || width <= 3 || !areLegalInt) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("The Maze Application");
            alert.setContentText("You have to put numbers higher than 3 for the columns and the rows!!!\nKeep playing under the rules!");
            alert.showAndWait();
        } else {
            btn_generateMaze.setDisable(true);
            viewModel.generateMaze(height, width);
            btn_solveMaze.setDisable(false);
            Platform.runLater(() -> mazeDisplayer.requestFocus());
        }
    }

    public void solveMaze(ActionEvent actionEvent) {
        //showAlert("Solving maze..");
        btn_solveMaze.setDisable(true);
        viewModel.solveGeneratedMaze();
        Platform.runLater(() -> mazeDisplayer.requestFocus());
        /////////////////////////////////
    }

    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    public void KeyPressed(KeyEvent keyEvent) {
        viewModel.moveCharacter(keyEvent.getCode());
        keyEvent.consume();
    }

    public String getCharacterPositionRow() {
        return characterPositionRow.get();
    }

    public StringProperty characterPositionRowProperty() {
        return characterPositionRow;
    }

    public String getCharacterPositionColumn() {
        return characterPositionColumn.get();
    }

    public StringProperty characterPositionColumnProperty() {
        return characterPositionColumn;
    }

    public void setResizeEvent(Scene scene) {
        //long width = 0;
        //long height = 0;
        double width = scene.getWidth();
        double height = scene.getHeight();
        mazeDisplayer.widthProperty().addListener(observable -> mazeDisplayer.redraw(width, height));
        mazeDisplayer.heightProperty().addListener(observable -> mazeDisplayer.redraw(width, height));
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                //System.out.println("Width: " + newSceneWidth);
                boolean exists = mazeDisplayer != null && mazeDisplayer.getMaze() == true;
                if (exists) {
                    mazeDisplayer.setWidth(width);
                    mazeDisplayer.setHeight(height);
                    update(viewModel, new Object());
                }
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                //System.out.println("Height: " + newSceneHeight);
                boolean exists = mazeDisplayer != null && mazeDisplayer.getMaze() == true;
                if (exists) {
                    mazeDisplayer.setWidth(width);
                    mazeDisplayer.setHeight(height);
                    update(viewModel, new Object());
                }
            }
        });
    }

    public void About(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("AboutController");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public void saveTheMaze(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze File");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("saved Mazes", "*.maze"); //(*.maze)
        fileChooser.getExtensionFilters().add(extensionFilter);
        File selectedFile = fileChooser.showSaveDialog(btn_generateMaze.getScene().getWindow());
        if (selectedFile != null) {
            viewModel.saveTheMaze(selectedFile);
        }
    }

    public void loadTheMaze(ActionEvent actionEvent) {
        boolean focus = true;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Maze File");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("saved Mazes", "*.maze");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File selectedFile = fileChooser.showOpenDialog(btn_generateMaze.getScene().getWindow());
        if (selectedFile != null)
            viewModel.loadTheMaze(selectedFile);
        if (focus)
            mazeDisplayer.requestFocus();
    }

    public void propertiesEvent() {
        String data = "Current Properties data :\n\n";
        try {
            InputStream inputStream = null;
            java.util.Properties gameProperties = new java.util.Properties();
            try {
                inputStream = new FileInputStream(System.getProperty("user.dir") + "\\config.properties");
            } catch (FileNotFoundException e1) {
                System.out.println("FileNotFoundException because of gameProperties in propertiesEvent");
            }
            gameProperties.load(inputStream);
            data += "SearchingAlgorithm: " + gameProperties.getProperty("SearchingAlgorithm") + "\n";
            data += "MazeGenerator: " + gameProperties.getProperty("MazeGenerator") + "\n";
            data += "ThreadPoolCapacity: " + gameProperties.getProperty("ThreadPoolCapacity") + "\n\n";
            //try {
        } catch (IOException e3) {
            System.out.println("IOException because of gameProperties in propertiesEvent");
        }
        List<String> choiceCreation = new ArrayList<>();
        choiceCreation.add("SimpleMazeGenerator");
        choiceCreation.add("MyMazeGenerator");
        ChoiceDialog<String> dialogCreation = new ChoiceDialog<>("MyMazeGenerator", choiceCreation);

        List<String> choiceSolving = new ArrayList<>();
        choiceSolving.add("BreadthFirstSearch");
        choiceSolving.add("DepthFirstSearch");
        choiceSolving.add("BestFirstSearch");
        ChoiceDialog<String> dialogSolving = new ChoiceDialog<>("DepthFirstSearch", choiceSolving);

        dialogCreation.setTitle("Choice Creation Generator");
        dialogCreation.setHeaderText(data);
        dialogCreation.setContentText("Choose your Maze Generator:");

        dialogSolving.setTitle("Choice Solving Algorithm");
        dialogSolving.setHeaderText(data);
        dialogSolving.setContentText("Choose your Solving Algorithm:");

        Optional<String> resultCreation = dialogCreation.showAndWait();
        Optional<String> resultSolving = dialogSolving.showAndWait();
        //////////////////////////////////////
        String resCreation, resSolving;
        if (resultCreation.isPresent())
            resCreation = resultCreation.get();
        else
            resCreation = "MyMazeGenerator";
        //////////////////////////////////////
        if (resultSolving.isPresent())
            resSolving = resultSolving.get();
        else
            resSolving = "DepthFirstSearch";
        viewModel.searchingAlgo(resSolving);
        viewModel.creationAlgo(resCreation);
    }

    public void helpEvent() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help the user");
        alert.setHeaderText(null);
        alert.setContentText("Hello, Dear User!\n\n" +
                "This game is a maze game where you have to reach the exit point. You must define the dimensions of the maze as you wish.\n" + "In addition, you can choose the algorithm of the solution of the maze you have created.\n" +
                "\n" + "You have to use the numpad keys [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ] or the arrows [ up, down, left, right ] to move in the maze" + "\n" +
                "\n" + "Good luck, here's the staff!" + "\n\n\nCopyright © 2018 Lior Pizman and Yehuda Pashay");
        alert.showAndWait();
    }

    public void aboutEvent() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the project and the stuff");
        alert.setHeaderText(null);
        alert.setContentText("Hello, Dear User!\n\nWe are Students in 2nd year in SISE Department in Ben Gurion University.\n\nThe algoritms of maze creation is based on Prim algorithm.\nMoreover, the solving algortihms the game provides are: BFS, DFS and best first search." + "\n\n\nCopyright © 2018 Lior Pizman and Yehuda Pashay");
        alert.showAndWait();
    }

    public void closeEvent() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Exit Game");
        alert.setContentText("Are you sure you want to leave this amazing game?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            int exitCode = 0;
            MyModel.stopServers();
            //------------------------------------------------> thread pool shut down
            System.exit(exitCode);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        lastX = mouseEvent.getX();
        lastY = mouseEvent.getY();
        mouseEvent.consume();
    }

    private boolean isCharacterPressed() {
        //double currWidth = 0;
        ///double currHeight = 0;
        double xStart = mazeDisplayer.getxPixel(), xEnd;
        double yStart = mazeDisplayer.getyPixel(), yEnd;
        //currWidth = yStart / mazeDisplayer.getCharacterPositionRow();
        //currHeight = xStart / mazeDisplayer.getCharacterPositionColumn();
        xEnd = xStart + mazeDisplayer.getCellHeight();
        yEnd = yStart + mazeDisplayer.getCellWidth();
        if (lastX <= xEnd && lastX >= xStart && lastY <= yEnd && lastY >= yStart)
            return true;
        return false;
    }

    private int getMove(double currX, double currY) {
        //double currWidth ;
        //double currHeight ;
        double xStart = mazeDisplayer.getxPixel(), xEnd;
        double yStart = mazeDisplayer.getyPixel(), yEnd;
        //currWidth = yStart / mazeDisplayer.getCharacterPositionRow();
        //currHeight = xStart / mazeDisplayer.getCharacterPositionColumn();
        xEnd = xStart + mazeDisplayer.getCellHeight();
        yEnd = yStart + mazeDisplayer.getCellWidth();
        if (currX > xEnd && currY > yEnd)
            return 3;
        else if (currX < xStart && currY < yStart)
            return 7;
        else if (currX < xStart && currY > yEnd)
            return 1;
        else if (currX > xEnd && currY < yStart)
            return 9;
        else if (currX >= xStart && currX <= xEnd && currY < yStart)
            return 8;
        else if (currX >= xStart && currX <= xEnd && currY > yEnd)
            return 2;
        else if (currY <= yEnd && currY >= yStart && currX < xStart)
            return 4;
        else if (currY <= yEnd && currY >= yStart && currX > xEnd)
            return 6;
        return 0;
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        double currX = mouseEvent.getX();
        double currY = mouseEvent.getY();
        int move;
        if (isCharacterPressed()) {
            move = getMove(currX, currY);
            viewModel.setMove(move);
        }
    }

    /*
    public void ctrlPressed(KeyEvent keyEvent) {
        while(keyEvent.getCode()== KeyCode.CONTROL){
            System.out.println("ctrl\n");
        }
    }
*/

    public void scrollMouse(ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            double zoomFactor = 1.05;

            double deltaY = scrollEvent.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }
            //System.out.println(zoomFactor);
            //double h=mazeDisplayer.getScaleX() * zoomFactor;
            //double w=mazeDisplayer.getScaleY() * zoomFactor;
            double scaleY = mazeDisplayer.getScaleX() * zoomFactor;
            double scaleX = mazeDisplayer.getScaleY() * zoomFactor;
            double MIN_SCALE = 0.1;
            double MAX_SCALE = 1.02;
            if (scaleY < MIN_SCALE || scaleY > MAX_SCALE) {
                scaleY = scaleY < MIN_SCALE ? MIN_SCALE : MAX_SCALE;
            }
            if (scaleX < MIN_SCALE || scaleX > MAX_SCALE) {
                scaleX = scaleX < MIN_SCALE ? MIN_SCALE : MAX_SCALE;
            }
            mazeDisplayer.setScaleX(scaleX);
            mazeDisplayer.setScaleY(scaleY);
/*
            mazeDisplayer.setScaleX(mazeDisplayer.getScaleX() * zoomFactor);
            mazeDisplayer.setScaleY(mazeDisplayer.getScaleY() * zoomFactor);
  */
            //mazeDisplayer.redraw(h,w);
            scrollEvent.consume();
        }
    }


}
