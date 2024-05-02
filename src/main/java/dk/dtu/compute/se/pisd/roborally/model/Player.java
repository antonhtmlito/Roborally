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

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.view.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.SOUTH;

/**
 * Player class of the Roborally game
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Player extends Subject {

    final public static int NO_REGISTERS = 5;
    final public static int NO_CARDS = 8;

    final public Board board;

    private String name;
    private String color;

    private int playerId;
    private Space space;
    private Heading heading = SOUTH;

    private CommandCardField[] program;
    private CommandCardField[] cards;

    private Stack<CommandCard> stack;

    private boolean moved;

    public void setMoved(boolean isMoved) {
        moved = isMoved;
    }

    public boolean getMoved() {
        return moved;
    }
    public CommandCardField[] getCards() {
        return cards;
    }

    public CommandCardField[] getProgram() {
        return program;
    }

    /**
     * Constructor of Player class
     *
     * @param board Board
     * @param color String
     * @param name  String
     */
    public Player(@NotNull Board board, String color, @NotNull String name) {
        this.board = board;
        this.name = name;
        this.color = color;

        this.space = null;

        program = new CommandCardField[NO_REGISTERS];
        for (int i = 0; i < program.length; i++) {
            program[i] = new CommandCardField(this);
        }

        cards = new CommandCardField[NO_CARDS];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new CommandCardField(this);
        }

        stack = new Stack<>();
    }

    /**
     * Get name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param name String
     */
    public void setName(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    /**
     * Get color
     *
     * @return String
     */
    public String getColor() {
        return color;
    }

    /**
     * Set color
     *
     * @param color String
     */
    public void setColor(String color) {
        this.color = color;
        notifyChange();
        if (space != null) {
            space.playerChanged();
        }
    }

    public int getPlayerId() {
        return playerId;
    }


    public void setPlayerId(int id) {
        playerId = id;
    }

    /**
     * Get space
     *
     * @return Space
     */
    public Space getSpace() {
        return space;
    }

    /**
     * Set space
     *
     * @param space Space
     */
    public void setSpace(Space space) {
        Space oldSpace = this.space;
        if (space != oldSpace &&
                (space == null || space.board == this.board)) {
            this.space = space;
            if (oldSpace != null) {
                oldSpace.setPlayer(null);
            }
            if (space != null) {
                space.setPlayer(this);
            }
            notifyChange();
        }
    }

    /**
     * Get Heading
     *
     * @return Heading
     */
    public Heading getHeading() {
        return heading;
    }

    /**
     * Set Heading
     *
     * @param heading Heading
     */
    public void setHeading(@NotNull Heading heading) {
        if (heading != this.heading) {
            this.heading = heading;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    /**
     * Get program field
     *
     * @param i int
     * @return CommandCardField
     */
    public CommandCardField getProgramField(int i) {
        return program[i];
    }

    /**
     * Get card field
     *
     * @param i int
     * @return CommandCardField
     */
    public CommandCardField getCardField(int i) {
        return cards[i];
    }

    public Stack<CommandCard> getCardDeck() {
        return stack;
    }
}


