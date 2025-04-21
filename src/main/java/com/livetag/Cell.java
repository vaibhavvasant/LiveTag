package com.livetag;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cell extends Rectangle {
    public static final int SIZE = 40;

    private int row, col;

    public Cell(int row, int col) {
        super(SIZE, SIZE);
        this.row = row;
        this.col = col;

        setFill(Color.BLACK);
        setStroke(Color.GRAY);
    }
}
