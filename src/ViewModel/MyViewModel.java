package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


public class MyViewModel extends Observable implements Observer {

    private IModel model;

    private int characterPositionRowIndex;
    private int characterPositionColumnIndex;

    //public StringProperty characterPositionRow = new SimpleStringProperty("1");
    //public StringProperty characterPositionColumn = new SimpleStringProperty("1");

    public MyViewModel(IModel model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            //characterPositionRowIndex = model.getCharacterPositionRow();
            // characterPositionRow.set(characterPositionRowIndex + "");
            //characterPositionColumnIndex = model.getCharacterPositionColumn();
            // characterPositionColumn.set(characterPositionColumnIndex + "");
            setChanged();
            notifyObservers();
        }
    }

    public void generateMaze(int width, int height) {
        model.generateMaze(width, height);
    }

    public void solveGeneratedMaze() {
        model.solveTheGeneratedMaze();
    }

    public void moveCharacter(KeyCode movement) {
        model.moveCharacter(movement);
    }

    public int[][] getMaze() {
        return model.getMaze();
    }

    public int getCharacterPositionRow() {
        return model.getCharacterPositionRow();
    }

    public int getCharacterPositionColumn() {
        return model.getCharacterPositionColumn();
    }

    public void searchingAlgo(String resSolving) {
        model.searchingAlgo(resSolving);
    }

    public void creationAlgo(String resCreation) {
        model.creationAlgo(resCreation);
    }

    public ArrayList<AState> getSolutionPath() {
        return model.getSolutionPath();
    }

    public void saveTheMaze(File selectedFile) {
        model.saveTheMaze(selectedFile);
    }

    public void loadTheMaze(File selectedFile) {
        model.loadTheMaze(selectedFile);
    }

    public Position getGoalPos() {
        return model.getGoalPos();
    }

    public void setMove(int move) {
        model.setMove(move);
    }
}
