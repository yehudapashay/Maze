package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;


import algorithms.search.AState;
import algorithms.search.Solution;
import com.sun.org.apache.xpath.internal.operations.String;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MyModel extends Observable implements IModel {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    private java.util.Properties gameProperties;

    private static Server forGenerate;
    private static Server forSolve;

    //private boolean printSol = false;
    private Maze m_Maze = null;
    private ArrayList<AState> m_solutionPath = null;
    private Position goalPosition;

    private int characterPositionRow = 1;
    private int characterPositionColumn = 1;


    public MyModel() {
        //Raise the servers
        //IServerStrategy generateStrategy = new ServerStrategyGenerateMaze();
        //IServerStrategy solveStrategy = new ServerStrategySolveSearchProblem();
        startServers();
        try {
            gameProperties = new java.util.Properties();
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "\\config.properties");
            gameProperties.setProperty("SearchingAlgorithm", "DepthFirstSearch");
            gameProperties.setProperty("MazeGenerator", "MyMazeGenerator");
            gameProperties.setProperty("ThreadPoolCapacity", "15");
            gameProperties.store(outputStream, null);
        } catch (IOException e) {
            System.out.println("IOException because of gameProperties in MyModel");
        }
    }


    public void startServers() {
        forGenerate = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        forSolve = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        forGenerate.start();
        forSolve.start();
    }


    public static void stopServers() {
        forGenerate.stop();
        forSolve.stop();
        threadPool.shutdown();
    }

/*
    private int[][] maze = { // a stub...
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1},
            {0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1},
            {1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1},
            {1, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 0, 1},
            {1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1},
            {1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1}
    };
*/

    private void setCharacterPosition() {
        int start_row, start_col, goal_row, goal_col;
        Position start, goal;
        start = m_Maze.getStartPosition();
        goal = m_Maze.getGoalPosition();
        start_row = start.getRowIndex();
        start_col = start.getColumnIndex();
        goal_row = goal.getRowIndex();
        goal_col = goal.getColumnIndex();
        characterPositionRow = start_row;
        characterPositionColumn = start_col;
        goalPosition = new Position(goal_row, goal_col);
    }


    @Override
    public void generateMaze(int width, int height) {
        threadPool.execute(() -> {
            try {
                m_solutionPath = null;
                Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            int[] mazeDimensions = new int[]{width, height};
                            toServer.writeObject(mazeDimensions);
                            toServer.flush();
                            byte[] compressedMaze = (byte[]) fromServer.readObject();
                            InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                            byte[] decompressedMaze = new byte[width * height + 12];
                            is.read(decompressedMaze);
                            Maze m = new Maze(decompressedMaze);
                            m_Maze = m;
                            for(int i=0;i<10 &&m_Maze == null;i++) {
                                System.out.println("i am trying to generate it1!");
                                clientStrategy(inFromServer,outToServer);
                            }
                            //setCharacterPosition();
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.out.println("clientStrategy in generateMaze Exception");
                        }
                    }
                });
                client.communicateWithServer();
                for(int i=0;i<10 &&m_Maze == null;i++){
                    System.out.println("i am trying to generate it2!");
                    client.communicateWithServer();
                }
                setCharacterPosition();
                setChanged();
                notifyObservers();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    System.out.println("InterruptedException in generateMaze Exception");
                }
                //setChanged();
                //notifyObservers();
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                System.out.println("UnknownHostException in generateMaze Exception");
            }
        });
    }

    public void solveTheGeneratedMaze() {
        threadPool.execute(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            toServer.writeObject(m_Maze);
                            toServer.flush();
                            m_solutionPath = ((Solution) fromServer.readObject()).getSolutionPath();
                            //printSol=true;
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.out.println("UnknownHostException in ClientStrategy in solveTheGeneratedMaze Exception");
                        }
                    }
                });
                client.communicateWithServer();
                setChanged();
                notifyObservers();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                    System.out.println("InterruptedException in solveTheGeneratedMaze Exception");
                }
                //setChanged();
                //notifyObservers();
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                System.out.println("UnknownHostException in solveTheGeneratedMaze Exception");
            }
        });

    }

    @Override
    public void searchingAlgo(java.lang.String resSolving) {
        try {
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "\\config.properties");
            gameProperties.setProperty("SearchingAlgorithm", resSolving);
            gameProperties.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("IOException because of gameProperties in searchingAlgo");
        }
    }

    @Override
    public void creationAlgo(java.lang.String resCreation) {
        try {
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + "\\config.properties");
            gameProperties.setProperty("MazeGenerator", resCreation);
            gameProperties.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("IOException because of gameProperties in creationAlgo");
        }
    }

    @Override
    public ArrayList<AState> getSolutionPath() {
        return m_solutionPath;
    }

    @Override
    public void saveTheMaze(File selectedFile) {
        try {
            //FileOutputStream file = ;
            ObjectOutputStream ToFile = new ObjectOutputStream(new FileOutputStream(selectedFile));
            ToFile.writeObject(m_Maze);
            ToFile.writeObject(characterPositionRow);
            ToFile.writeObject(characterPositionColumn);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("saveTheMaze in MyModel");
        }
    }

    @Override
    public void loadTheMaze(File selectedFile) {
        InputStream is;
        ObjectInputStream in;
        Object o1;
        Object o2;
        try {
            is = new FileInputStream(selectedFile);
            in = new ObjectInputStream(is);
            m_Maze = (Maze) in.readObject();
            o1 = in.readObject();
            o2 = in.readObject();
            goalPosition = m_Maze.getGoalPosition();                 //goal Position parameter
            characterPositionRow = (int) o1;
            characterPositionColumn = (int) o2;
            //is.close();
            changeAndNotify();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            System.out.println("ClassNotFoundException in loadTheMaze");
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("IOException in loadTheMaze");
        }
    }

    private void changeAndNotify() {
        setChanged();
        notifyObservers();
    }

    @Override
    public int[][] getMaze() {
        return m_Maze.getMazeData();
        //return maze;
    }

    private boolean isLegalPosition(int row, int column) {
        boolean isLegal = false;
        if (row >= 0 && column >= 0 && row < m_Maze.getRows() && column < m_Maze.getColumns()) {
            if (m_Maze.getCellValue(row, column) == 0) {
                isLegal = true;
            }
        }
        return isLegal;
    }

    @Override
    public void moveCharacter(KeyCode movement) {
        boolean left = false, right = false, up = false, down = false;
        left = isLegalPosition(characterPositionRow, characterPositionColumn - 1);
        right = isLegalPosition(characterPositionRow, characterPositionColumn + 1);
        down = isLegalPosition(characterPositionRow + 1, characterPositionColumn);
        up = isLegalPosition(characterPositionRow - 1, characterPositionColumn);
        switch (movement) {
            case NUMPAD8:
                if (up)
                    characterPositionRow--;
                break;
            case NUMPAD2:
                if (down)
                    characterPositionRow++;
                break;
            case NUMPAD6:
                if (right)
                    characterPositionColumn++;
                break;
            case NUMPAD4:
                if (left)
                    characterPositionColumn--;
                break;
            case NUMPAD7:
                boolean numpad7 = isLegalPosition(characterPositionRow - 1, characterPositionColumn - 1);
                if ((left && numpad7) || (up && numpad7)) {
                    characterPositionRow--;
                    characterPositionColumn--;
                }
                break;
            case NUMPAD1:
                boolean numpad1 = isLegalPosition(characterPositionRow + 1, characterPositionColumn - 1);
                if ((left && numpad1) || (down && numpad1)) {
                    characterPositionColumn--;
                    characterPositionRow++;
                }
                break;
            case NUMPAD9:
                boolean numpad9 = isLegalPosition(characterPositionRow - 1, characterPositionColumn + 1);
                if ((right && numpad9) || (up && numpad9)) {
                    characterPositionRow--;
                    characterPositionColumn++;
                }
                break;
            case NUMPAD3:
                boolean numpad3 = isLegalPosition(characterPositionRow + 1, characterPositionColumn + 1);
                if ((right && numpad3) || (down && numpad3)) {
                    characterPositionRow++;
                    characterPositionColumn++;
                }
                break;
            case UP:
                if (up)
                    characterPositionRow--;
                break;
            case DOWN:
                if (down)
                    characterPositionRow++;
                break;
            case RIGHT:
                if (right)
                    characterPositionColumn++;
                break;
            case LEFT:
                if (left)
                    characterPositionColumn--;
                break;
        }
        setChanged();
        notifyObservers();
    }

    @Override
    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    @Override
    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    public Position getGoalPos() {
        return this.goalPosition;
    }

    @Override
    public void setMove(int move) {
        boolean left = false, right = false, up = false, down = false;
        left = isLegalPosition(characterPositionRow, characterPositionColumn - 1);
        right = isLegalPosition(characterPositionRow, characterPositionColumn + 1);
        down = isLegalPosition(characterPositionRow + 1, characterPositionColumn);
        up = isLegalPosition(characterPositionRow - 1, characterPositionColumn);
        switch (move) {
            case 8:
                if (up)
                    characterPositionRow--;
                break;
            case 2:
                if (down)
                    characterPositionRow++;
                break;
            case 6:
                if (right)
                    characterPositionColumn++;
                break;
            case 4:
                if (left)
                    characterPositionColumn--;
                break;
            case 7:
                boolean numpad7 = isLegalPosition(characterPositionRow - 1, characterPositionColumn - 1);
                if ((left && numpad7) || (up && numpad7)) {
                    characterPositionRow--;
                    characterPositionColumn--;
                }
                break;
            case 1:
                boolean numpad1 = isLegalPosition(characterPositionRow + 1, characterPositionColumn - 1);
                if ((left && numpad1) || (down && numpad1)) {
                    characterPositionColumn--;
                    characterPositionRow++;
                }
                break;
            case 9:
                boolean numpad9 = isLegalPosition(characterPositionRow - 1, characterPositionColumn + 1);
                if ((right && numpad9) || (up && numpad9)) {
                    characterPositionRow--;
                    characterPositionColumn++;
                }
                break;
            case 3:
                boolean numpad3 = isLegalPosition(characterPositionRow + 1, characterPositionColumn + 1);
                if ((right && numpad3) || (down && numpad3)) {
                    characterPositionRow++;
                    characterPositionColumn++;
                }
                break;
        }
        setChanged();
        notifyObservers();
    }
}
