package com.livetag;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Stack;
import javafx.geometry.Insets;


public class GameUI extends Application {
    //field declarations
    private static final int GRID_SIZE = 12;
    private static final int CELL_SIZE = 30;

    private Rectangle[][] gridState = new Rectangle[GRID_SIZE][GRID_SIZE];
    private Stack<Color[][]> history = new Stack<>();

    private Color currentPlayerColor = Color.RED;
    private int player1Turns = 0;
    private int player2Turns = 0;
    private Label statusLabel = new Label("Player RED's turn");
    private Label scoreLabel = new Label("Red: 0   Blue: 0");

    private Timeline simulation;

    @Override
    public void start(Stage stage) {
        //setting up UI
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: black;");
        // grid.setHgap(1);
        // grid.setVgap(1);
        

        //painting cells black
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.BLACK);
                cell.setStroke(Color.GRAY);
                final int r = row;
                final int c = col;
                //change color on mouse click
                cell.setOnMouseClicked(e -> {
                    if (!simulationRunning() && cell.getFill().equals(Color.BLACK)) {
                        if ((currentPlayerColor == Color.RED && player1Turns < 8) ||
                            (currentPlayerColor == Color.BLUE && player2Turns < 8)) {
                            
                            saveGridState(); // Save before modifying

                            cell.setFill(currentPlayerColor);
                            if (currentPlayerColor == Color.RED) {
                                player1Turns++;
                            } else {
                                player2Turns++;
                            }

                            switchPlayer();
                        }
                    }
                });

                grid.add(cell, col, row);
                gridState[row][col] = cell;
            }
        }

        //UI elements
        Button startBtn = new Button("Start");
        Button undoBtn = new Button("Undo");
        Button stopBtn = new Button("Stop");
        Button restartBtn = new Button("Restart");
        Button rulesButton = new Button("Rules");
        //Button functionality
        rulesButton.setOnAction(e -> showRules());
        restartBtn.setOnAction(e -> resetGame());
        startBtn.setOnAction(e -> startSimulation());
        stopBtn.setOnAction(e -> stopSimulation());
        undoBtn.setOnAction(e -> undoLastMove());

        //wrapping nodes
        VBox controls = new VBox(20, statusLabel, scoreLabel, startBtn, undoBtn, stopBtn, restartBtn, rulesButton);
        controls.setAlignment(Pos.CENTER);

        StackPane gridWrapper = new StackPane(grid);
        gridWrapper.setPrefSize(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        
        HBox content = new HBox(30, gridWrapper, controls);
        content.setAlignment(Pos.CENTER); // align items inside HBox
        
        BorderPane root = new BorderPane();
        root.setCenter(content); // center the whole thing
        
        Scene scene = new Scene(root);

        content.setPadding(new Insets(20));

        stage.setScene(scene);
        stage.setTitle("LiveTag");
        stage.show();
    }

    private void switchPlayer() {
        if (player1Turns + player2Turns < 16) {
            currentPlayerColor = currentPlayerColor.equals(Color.RED) ? Color.BLUE : Color.RED;
            statusLabel.setText("Player " + (currentPlayerColor.equals(Color.RED) ? "RED" : "BLUE") + "'s turn");
        } else {
            statusLabel.setText("Simulation ready");
        }
    }

    private void saveGridState() {
        Color[][] snapshot = new Color[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                snapshot[row][col] = (Color) gridState[row][col].getFill();
            }
        }
        history.push(snapshot);
    }

    private void undoLastMove() {
        if (!history.isEmpty()) {
            Color[][] last = history.pop();
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    gridState[row][col].setFill(last[row][col]);
                }
            }

            if (currentPlayerColor == Color.RED) {
                player2Turns--;
            } else {
                player1Turns--;
            }
            currentPlayerColor = currentPlayerColor.equals(Color.RED) ? Color.BLUE : Color.RED;
            statusLabel.setText("Player " + (currentPlayerColor.equals(Color.RED) ? "RED" : "BLUE") + "'s turn");
        }
    }

    private boolean simulationRunning() {
        return simulation != null && simulation.getStatus() == Timeline.Status.RUNNING;
    }

    private void startSimulation() {
        if (player1Turns == 8 && player2Turns == 8) {
            statusLabel.setText("Simulation running...");
            simulation = new Timeline(new KeyFrame(Duration.seconds(1), e -> simulateNextGeneration()));
            simulation.setCycleCount(Timeline.INDEFINITE);
            simulation.play();
        } else {
            statusLabel.setText("Each player must place 8 cells");
        }
    }

    private void stopSimulation() {
        if (simulation != null) {
            simulation.stop();
            statusLabel.setText("Simulation stopped");
        }
    }

    private void simulateNextGeneration() {
        Color[][] next = new Color[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Color current = (Color) gridState[row][col].getFill();
                int red = 0, blue = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        int nr = row + i, nc = col + j;
                        if (nr >= 0 && nr < GRID_SIZE && nc >= 0 && nc < GRID_SIZE) {
                            Color neighbor = (Color) gridState[nr][nc].getFill();
                            if (neighbor.equals(Color.RED)) red++;
                            else if (neighbor.equals(Color.BLUE)) blue++;
                        }
                    }
                }

                // Apply rules
                if (current.equals(Color.RED) || current.equals(Color.BLUE)) {
                    int same = current.equals(Color.RED) ? red : blue;
                    int diff = current.equals(Color.RED) ? blue : red;

                    if (same <= 1 || same >= 4 || diff > same) {
                        next[row][col] = Color.BLACK;
                    } else {
                        next[row][col] = current;
                    }
                } else {
                    if (red == 3 && blue != 3) next[row][col] = Color.RED;
                    else if (blue == 3 && red != 3) next[row][col] = Color.BLUE;
                    else if (red == 2 && blue == 2) next[row][col] = Math.random() < 0.5 ? Color.RED : Color.BLUE;
                    else next[row][col] = Color.BLACK;
                }
            }
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                gridState[row][col].setFill(next[row][col]);
            }
        }

        updateScores();

        // Winner check:
        int red = 0, blue = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Color color = (Color) gridState[r][c].getFill();
                if (color.equals(Color.RED)) red++;
                else if (color.equals(Color.BLUE)) blue++;
            }
        }
        
        if (red == 0 || blue == 0) {
            simulation.stop();
            String winner = (red > 0) ? "RED" : "BLUE";
            statusLabel.setText("Player " + winner + " wins!");
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("Winner: " + winner);
            alert.setContentText("All " + (winner.equals("RED") ? "BLUE" : "RED") + " cells are gone.");
            alert.showAndWait();
        }
        
    }

    private void updateScores() {
        int red = 0, blue = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Color color = (Color) gridState[r][c].getFill();
                if (color.equals(Color.RED)) red++;
                else if (color.equals(Color.BLUE)) blue++;
            }
        }
        scoreLabel.setText("Red: " + red + "   Blue: " + blue);
    }

    private void resetGame() {
        if (simulation != null) simulation.stop();
    
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                gridState[row][col].setFill(Color.BLACK);
            }
        }
    
        player1Turns = 0;
        player2Turns = 0;
        currentPlayerColor = Color.RED;
        history.clear();
    
        statusLabel.setText("Player RED's turn");
        scoreLabel.setText("Red: 0   Blue: 0");
    }
    
    private void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Rules");
        alert.setHeaderText("Two-Player Conway's Game of Life");
        alert.setContentText("""
            1. Each player gets 8 moves to place cells (RED and BLUE).
            2. After setup, the grid evolves automatically.
            3. Rules per generation:
               - A cell with 2 or 3 neighbors survives.
               - An empty cell with exactly 3 neighbors becomes a new cell (takes majority color).
               - Any other cell dies.
            4. You cannot overwrite opponent's or your own cell.
            5. First player to eliminate all opponent cells wins.
            6. Undo allows taking back last move before simulation starts.
            7. Restart resets the game.
            """);
        alert.showAndWait();
    }
    
}
