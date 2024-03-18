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
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 40; //  75; // 60; // 75;
    final public static int SPACE_WIDTH = 40; //75;  // 60; // 75;

    public final Space space;


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
    }

    private void updatePlayer() {
        this.getChildren().clear();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updatePlayer();
            updateCheckpoint();
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
        //@S235451
        if (space.x == 2 && space.y == 2) {

            Pane pane = new Pane();
            Rectangle rectangle =
                    new Rectangle(0.0, 0.0, SPACE_WIDTH, SPACE_HEIGHT);
            rectangle.setFill(Color.TRANSPARENT);
            pane.getChildren().add(rectangle);
            // SOUTH
            Line SOUTHline =
                    new Line(2, SPACE_HEIGHT -2,
                            SPACE_WIDTH-2, SPACE_HEIGHT-2);
            SOUTHline.setStroke(Color.RED);
            SOUTHline.setStrokeWidth(5);
            pane.getChildren().add(SOUTHline);
            // EAST
            Line eastLine = new Line(SPACE_WIDTH - 2, 2,
                    SPACE_WIDTH - 2, SPACE_HEIGHT - 2);
            eastLine.setStroke(Color.RED);
            eastLine.setStrokeWidth(5);
            pane.getChildren().add(eastLine);
            // NORTH
            Line NorthLine = new Line(2, 2,
                    SPACE_WIDTH - 2, 2);
            NorthLine.setStroke(Color.RED);
            NorthLine.setStrokeWidth(5);
            pane.getChildren().add(NorthLine);
            // WEST
            Line WESTLine = new Line(2, 2,
                   2 , SPACE_HEIGHT - 2);
            WESTLine.setStroke(Color.RED);
            WESTLine.setStrokeWidth(5);
            pane.getChildren().add(WESTLine);


            this.getChildren().add(pane);





        }
    }

}
