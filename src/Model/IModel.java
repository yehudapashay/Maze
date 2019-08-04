package Model;

import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;


public interface IModel {
    void generateMaze(int width, int height);
    void moveCharacter(KeyCode movement);
    int[][] getMaze();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    void solveTheGeneratedMaze();

    void searchingAlgo(String resSolving);
    void creationAlgo(String resCreation);

    ArrayList<AState> getSolutionPath();

    void saveTheMaze(File selectedFile);

    void loadTheMaze(File selectedFile);

    Position getGoalPos();

    void setMove(int move);
}
