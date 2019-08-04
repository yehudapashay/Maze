package View;


import algorithms.search.AState;

import java.util.ArrayList;

public interface IView {
    void displayMaze(int[][] maze, ArrayList<AState> solutionPath,double width, double height);
}
