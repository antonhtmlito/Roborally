/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 40; //  75; // 60; // 75;
    final public static int SPACE_WIDTH = 40; //75;  // 60; // 75;

    public final Space space;

    public Polygon gearSymbol;

    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);

        if (space.hasGear()) {
            addGearSymbol();
        }
    }

    /**
     *
     * This method updates the player on the board so as when a move
     * is made the arrow indicating the players robot will turn or move
     * and the prior arrow will disappear. It also updates the gear so
     * it doesn't disappear when a player lands on it
     *
     * @author Jonas Woetmann Larsen, S235446
     *
     */
    private void updatePlayer() {
        boolean gearSymbolExists = gearSymbol != null && this.getChildren().contains(gearSymbol);

        this.getChildren().clear();

        if (gearSymbolExists) {
            this.getChildren().add(gearSymbol);
        }

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0 );
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*player.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        /*} else if(space.isHasWall()) {
            // show a wall here, need optimize later
            this.setStyle("-fx-background-color: brown;");
        */}
    }

    @Override
    public void updateView(Subject subject) {



        if (subject == this.space) {
            updatePlayer();
            updateCheckpoint();
            updateWall();
        }


    }
    /**
     * This method create a wall on the board
     * @author William Wegener Kofoed, S235451
     */
    private void updateWall(){
        if(space.isHasWall()){
            Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.RED);
            gc.setLineWidth(5);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.strokeLine(2, SPACE_HEIGHT - 2, SPACE_WIDTH - 2, SPACE_HEIGHT - 2);
            this.getChildren().add(canvas);
        }
    }

    /**
     * This code is how the checkpoint token should look like
     * we chose the color Gold in right now
     *
     * @author Martin Dahl Lund, s235454
     */
    private void updateCheckpoint(){
        if (space.isCheckpoint()){
            Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setFill(Color.GOLD);
            gc.fillOval(SPACE_WIDTH * 0.2, SPACE_HEIGHT * 0.2, SPACE_WIDTH * 0.6, SPACE_HEIGHT * 0.6);

            this.getChildren().add(canvas);
        }
    }

    /**
     *
     * This method adds the gear symbol when called upon.
     * Its shape is also defined here.
     *
     * @author Jonas Woetmann Larsen, S235446
     *
     */
    public void addGearSymbol() {
        if (gearSymbol == null) {
            gearSymbol = new Polygon(
                    0.0, 0.0,
                    5.0, 5.0,
                    6.0, 10.0,
                    -6.0, 10.0,
                    -5.0, 5.0
            );
            gearSymbol.setFill(Color.RED);
            this.getChildren().add(gearSymbol);
        }
    }



}
