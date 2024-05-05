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
/**
 * dk.dtu.compute.se.pisd.roborally.controller is group of controller classes
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * ...
 * This is the class that controls the game
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class GameController {

    public static Board board;

    /**
     * Set of all the checkpoint that need to be collected to win.
     */
    private Set<Integer> allCheckpoints;

    /**
     * List of players
     */
    private List<Player> players;

    /**
     * Static set the checkpoints that has been collected.
     */
    private static Set<Integer> collectedCheckpoints = new HashSet<>();

    /**
     * Retrieves the set of the collected checkpoints.
     *
     * @return
     */
    public static Set<Integer> getCollectedCheckpoints() {
        return collectedCheckpoints;
    }

    /**
     * Constructs a new GameController.
     *
     * @param board
     */
    public GameController(@NotNull Board board) {
        this.board = board;
        board.setGameController(this);
        collectedCheckpoints.add(0);
        collectedCheckpoints.add(1);
        collectedCheckpoints.add(2);
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space) {
        Player current = board.getCurrentPlayer();
        if (current != null && space.getPlayer() == null) {
            current.setSpace(space);
            int currentNumber = board.getPlayerNumber(current);
            int nextPlayerNumber = (currentNumber + 1) % board.getPlayersNumber();
            Player next = board.getPlayer(nextPlayerNumber);
            board.setCurrentPlayer(next);
        }
        board.setCounter(board.getCounter() + 1);
    }

    /**
     * This method starts the programming phase
     */
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    // XXX: V2

    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * This concludes the programming phase
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }


    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }


    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }



    /**
     * This method executes the programs.
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * This method executes the next step.
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * This is executing program step, if the board is in the activation phase.
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }


    /**
     * This method executes the command option and continues.
     *
     * @param option Command
     * @author William Wegener Kofoed, S235451
     */
    public void executeCommandOptionAndContinue(@NotNull Command option) {
        Player currentPlayer = board.getCurrentPlayer();
        if (currentPlayer != null &&
                board.getPhase() == Phase.PLAYER_INTERACTION &&
                option != null) {
            board.setPhase(Phase.ACTIVATION);
            executeCommand(currentPlayer, option);

            int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
            if (nextPlayerNumber < board.getPlayersNumber()) ;
            {
                board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
            }
        } else {
            int step = board.getStep() + 1;

            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                startProgrammingPhase();
            }

        }

    }

    /**
     * This method checks whether there is a card and what card it is
     * and then executes the step based on the card
     *
     * @author Jonas Woetmann Larsen, S235446
     */
    void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if (command.isInteractive()) {
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    }

                    executeCommand(currentPlayer, command);
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    for (Player player : board.getPlayers()) {
                        Space space = player.getSpace();
                        if (!space.getActions().isEmpty()) {
                            for (FieldAction fieldAction : space.getActions()) {
                                fieldAction.doAction(board.getGameController(), space);
                            }
                        }
                    }
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    /**
     * Execute the command
     *
     * @param player  Player
     * @param command Command
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    public static boolean hasCollectedAllCheckpoints(Set<Integer> checkpoints) {
        return collectedCheckpoints.containsAll(checkpoints);
    }


    /**
     * This method makes the player move forward when a "FWD" card has been played.
     * It also checks to see if the target has a gear and rotates the player accordingly.
     * It also checks if there is a wall, if there is a wall the player connot move thourgh it.
     *
     * @param player Player
     * @author Anton Fu Hou Dong, s235460, Jonas Woetmann Larsen, S235446, @author William Wegener Kofoed, S235451
     */
    public void moveForward(@NotNull Player player) {

        Space current = player.getSpace();
        player.setMoved(true);
        Space target = board.getNeighbour(current, player.getHeading());
        Player targetPlayer;
        targetPlayer = target.getPlayer();
        if (current.hasCurrentWall(player)) {
            player.setMoved(false);
            //System.out.println("Cannot move forward: Wall detected in the target space for player " + player.getName());
            return;
        } else {
            //System.out.println("no wall ahead for player " + player.getName());
        }

        if (targetPlayer != null) {
            targetPlayer.setMoved(true);
            //System.out.println("there is player " + targetPlayer.getName() + " on the space, push the player!");
            Heading tempHeading = target.getPlayer().getHeading();
            targetPlayer.setHeading(player.getHeading());
            if (!target.hasCurrentWall(targetPlayer)) {
                // System.out.println("no wall ahead for player " + targetPlayer.getName());
                moveForward(targetPlayer);
                targetPlayer.setHeading(tempHeading);
            } else {
                // System.out.println("There is wall ahead, no move for player " + targetPlayer.getName());
                targetPlayer.setHeading(tempHeading);
                targetPlayer.setMoved(false);
            }
            if (!targetPlayer.getMoved()) {
                targetPlayer.setSpace(target);
                player.setSpace(current);
                player.setMoved(false);
                // System.out.println("Player " + player.getName() + "cannot move as target cannot move");
                return;
            }
        }
        if (target.hasGear()) {
            switch (player.getHeading()) {
                case NORTH:
                    player.setHeading(Heading.EAST);
                    break;
                case EAST:
                    player.setHeading(Heading.SOUTH);
                    break;
                case SOUTH:
                    player.setHeading(Heading.WEST);
                    break;
                case WEST:
                    player.setHeading(Heading.NORTH);
                    break;
            }
        }
        // nothing prevent move
        if (player.getMoved()) {
            player.setSpace(target);
            //System.out.println("++++++++  moveForward");
        }
    }

    /**
     * This method is similar to the Forward method but moves the player
     * two spaces forward when playing a "Fast FWD" card. And again it
     * checks whether the target is a gear or not
     *
     * @author Jonas Woetmann Larsen, S235446
     */
    public void fastForward(@NotNull Player player) {
        //System.out.println("++++++++  fastForward");
        moveForward(player);
        moveForward(player);
    }

    /**
     * This makes player turn to right (Clockwise)
     *
     * @param player Player
     */
    public void turnRight(@NotNull Player player) {
        //System.out.println("++++++++  turnRight");
        if (player.getHeading() == Heading.SOUTH)
            player.setHeading(Heading.WEST);
        else if (player.getHeading() == Heading.NORTH)
            player.setHeading(Heading.EAST);
        else if (player.getHeading() == Heading.WEST)
            player.setHeading(Heading.NORTH);
        else
            player.setHeading(Heading.SOUTH);
    }

    /**
     * This makes player turn to left (Counter-Clockwise)
     *
     * @param player Player
     */
    public void turnLeft(@NotNull Player player) {
        //System.out.println("++++++++  turnLeft");
        if (player.getHeading() == Heading.SOUTH)
            player.setHeading(Heading.EAST);
        else if (player.getHeading() == Heading.NORTH)
            player.setHeading(Heading.WEST);
        else if (player.getHeading() == Heading.WEST)
            player.setHeading(Heading.SOUTH);
        else
            player.setHeading(Heading.NORTH);
    }

    /**
     * Moving the cards from the source to the target in the programming phase.
     *
     * @param source The set of cards the player has available
     * @param target The current player
     * @return
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            //System.out.println("++++++++  233");
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }
}
