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
package dk.dtu.compute.se.pisd.roborally.model;

import com.google.gson.JsonArray;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import javafx.util.Pair;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;

import java.util.ArrayList;
import java.util.List;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.*;

/**
 * Space class of the Roborally game
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Space extends Subject {

    public final Board board;

    public final int x;
    public final int y;

    private boolean hasGear;


    /**
     * Here it checks if the player has landed on a checkpoint
     *
     * @author Martin Dahl Lund, s235454
     */

    private boolean isCheckpoint;
    public boolean isCheckpoint(){
        return isCheckpoint;
    }
    public void setCheckpoint(boolean isCheckpoint){
        this.isCheckpoint = isCheckpoint;
    }

    private List<Heading> walls = new ArrayList<>();
    private List<FieldAction> actions = new ArrayList<>();

    /**
     * @author Anton Fu Hou Dong, s235460
     * @param hasWall boolean
     */
    public void setHasWall(boolean hasWall) {
        this.hasWall = hasWall;
    }

    /**
     * @author Anton Fu Hou Dong, s235460
     * @return boolean
     */
    public boolean hasCurrentWall(Player player) {
        var heading = player.getHeading();
        if(EAST.equals(heading)){
            return walls.contains(EAST);
        }
        if(WEST.equals(heading)){
            return walls.contains(WEST);
        }
        if(NORTH.equals(heading)){
            return walls.contains(NORTH);
        }
        if(SOUTH.equals(heading)){
            return walls.contains(SOUTH);
        }
        return false;
    }

    public boolean hasTargetWall(Player player) {
        var heading = player.getHeading();
        if(EAST.equals(heading)){
            return walls.contains(WEST);
        }
        if(WEST.equals(heading)){
            return walls.contains(EAST);
        }
        if(NORTH.equals(heading)){
            return walls.contains(SOUTH);
        }
        if(SOUTH.equals(heading)){
            return walls.contains(NORTH);
        }
        return false;
    }


    private boolean hasWall;

    private Player player;


    /**
     * Constructor of Space
     * @param board Board
     * @param x int
     * @param y int
     */
    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
    }

    /**
     * Get player
     * @return Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Set player
     * @param player Player
     */
    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                // this should actually not happen
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }

    /**
     * notify PlayerChanged
     */
    void playerChanged() {
        // This is a minor hack; since some views that are registered with the space
        // also need to update when some player attributes change, the player can
        // notify the space of these changes by calling this method.
        notifyChange();
    }

    public void setHasGear(boolean hasGear) {
        this.hasGear = hasGear;
    }
    public boolean hasGear() {
        return hasGear;
    }


    public List<Heading> getWalls() {
        return walls;
    }

    public List<FieldAction> getActions() {
        return actions;
    }

}