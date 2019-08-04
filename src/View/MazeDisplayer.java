package View;

import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class MazeDisplayer extends Canvas {

    private int[][] maze;
    private int characterPositionRow = 1;
    private int characterPositionColumn = 1;
    private ArrayList<AState> m_solutionPath;
    private Position maze_goalPosition;

    private double xPixel = 0;
    private double yPixel = 0;
    private double cellHeight;
    private double cellWidth;

    private Position goalPosition;

    private StringProperty ImageWall = new SimpleStringProperty();
    private StringProperty ImageCharacter = new SimpleStringProperty();
    private StringProperty ImageSolution = new SimpleStringProperty();
    private StringProperty ImageGoalPosition = new SimpleStringProperty();

    public void setMaze(int[][] maze, ArrayList<AState> solutionPath, double width, double height) {
        this.maze = maze;
        m_solutionPath = solutionPath;
        redraw(width, height);
    }

    public void setCharacterPosition(int row, int column, double width, double height) {
        characterPositionRow = row;
        characterPositionColumn = column;
        redraw(width, height);
    }

    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    public boolean getMaze() {
        if (maze != null)
            return true;
        return false;
    }

    public GraphicsContext initDisplayer() {
        GraphicsContext graphicsContext2D = getGraphicsContext2D();
        graphicsContext2D.clearRect(0, 0, getWidth(), getHeight());
        return graphicsContext2D;
    }

    public void redraw(double width, double height) {
        if (maze != null) {
            double canvasHeight = height - 50;//getHeight();//;getHeight();//height;
            double canvasWidth = width - 150;//getWidth();//getWidth();//width
            double cellHeight = canvasHeight / maze[0].length;
            double cellWidth = canvasWidth / maze.length;//maze[0].length;
            this.cellWidth = cellWidth;
            this.cellHeight = cellHeight;
            // try {
            //GraphicsContext graphicsContext2D = getGraphicsContext2D();
            //graphicsContext2D.clearRect(0, 0, getWidth(), getHeight());
            GraphicsContext graphicsContext2D = initDisplayer();
            //Image wallImage = new Image(new FileInputStream(ImageWall.get()));
            Image wallImage = new Image(this.getClass().getResourceAsStream("/Images/wall.jpg"));//ImageWall.get()));
            for (int i = 0; i < maze.length; i++) {
                for (int j = 0; j < maze[i].length; j++) {
                    if (maze[i][j] == 1) {
                        //graphicsContext2D.fillRect(i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                        //graphicsContext2D.drawImage(wallImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                        graphicsContext2D.drawImage(wallImage, j * cellHeight, i * cellWidth, cellHeight, cellWidth);
                    }
                }
            }
            boolean printSolution = m_solutionPath != null;
            if (printSolution) {
                //printSolPath(graphicsContext2D, cellHeight, cellWidth);
                //Image solutionImage = new Image(new FileInputStream(ImageSolution.get()));
                Image solutionImage = new Image(this.getClass().getResourceAsStream("/Images/solution.jpg"));
                String currState;
                String[] splitted;
                int iRow, iCol;
                int index = 0;
                while (index < m_solutionPath.size()) {
                    currState = m_solutionPath.get(index).toString();
                    splitted = currState.split(",");
                    iRow = Integer.valueOf(splitted[0]);
                    iCol = Integer.valueOf(splitted[1]);
                    index++;
                    graphicsContext2D.drawImage(solutionImage, iCol * cellHeight, iRow * cellWidth, cellHeight, cellWidth);
                }
            }
            //gc.setFill(Color.RED);
            //gc.fillOval(characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
            if (goalPosition != null) {
                //Image goalPositionImage = new Image(new FileInputStream(ImageGoalPosition.get()));
                Image goalPositionImage = new Image(this.getClass().getResourceAsStream("/Images/goal.jpg"));
                graphicsContext2D.drawImage(goalPositionImage, goalPosition.getColumnIndex() * cellHeight, goalPosition.getRowIndex() * cellWidth, cellHeight, cellWidth);
            }
            /////////////////////////////////////////////////////////////////////////
            //Image characterImage = new Image(new FileInputStream(ImageCharacter.get()));
            Image characterImage = new Image(this.getClass().getResourceAsStream("/Images/character.jpg"));
            graphicsContext2D.drawImage(characterImage, characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
            ////////////////////////////////////////
            xPixel = characterPositionColumn * cellHeight;
            yPixel = characterPositionRow * cellWidth;
            //System.out.println("yPixel " + yPixel);
            //System.out.println("xPixel " + xPixel);

            // } catch (FileNotFoundException e) {
            //     System.out.println("FileNotFoundException in redraw in MazeDisplayer");
            // }
        }
    }
/*
    private void printSolPath(GraphicsContext graphicsContext2D, double cellHeight, double cellWidth) {
        int index = 0;
        Image solutionImage = null;
        try {
            solutionImage = new Image(new FileInputStream(ImageSolution.get()));
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException in printSolPath in MazeDisplayer");
        }
        String currState;
        String[] splitted;
        int iRow, iCol;

        while (index < m_solutionPath.size()) {
            currState = m_solutionPath.get(index).toString();
            splitted = currState.split(",");
            iRow = Integer.valueOf(splitted[0]);
            iCol = Integer.valueOf(splitted[1]);
            //System.out.println("iRow "+iRow);
            //System.out.println("iCol "+ iCol);
            //System.out.println("currState "+currState );
            index++;
            graphicsContext2D.drawImage(solutionImage, iCol * cellHeight, iRow * cellWidth, cellHeight, cellWidth);
        }
    }
*/

    public String getImageWall() {
        return ImageWall.get();
    }

    public void setImageWall(String imageOfWall) {
        this.ImageWall.set(imageOfWall);
    }

    public String getImageCharacter() {
        return ImageCharacter.get();
    }

    public void setImageCharacter(String imageOfCharacter) {
        this.ImageCharacter.set(imageOfCharacter);
    }


    public String getImageSolution() {
        return ImageSolution.get();
    }


    public void setImageSolution(String imageOfSol) {
        this.ImageSolution.set(imageOfSol);
    }

    public void setImageGoalPosition(String imageOfGoal) {
        this.ImageGoalPosition.set(imageOfGoal);
    }

    public String getImageGoalPosition() {
        return ImageGoalPosition.get();
    }

    public void setGoalPosition(Position goalPosition) {
        this.goalPosition = goalPosition;
    }

    public double getxPixel() {
        return xPixel;
    }

    public double getyPixel() {
        return yPixel;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }
}
