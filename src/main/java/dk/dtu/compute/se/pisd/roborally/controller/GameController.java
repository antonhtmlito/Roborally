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

/**
 * ...
 * This is the class that controls the game
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;


    public GameController(@NotNull Board board) {this.board = board;}

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space)  {
        Player current = board.getCurrentPlayer();
        if(current != null && space.getPlayer() == null) {
            current.setSpace(space);
            int currentNumber = board.getPlayerNumber(current);
            int nextPlayerNumber = (currentNumber + 1) % board.getPlayersNumber();
            Player next = board.getPlayer(nextPlayerNumber);
            board.setCurrentPlayer(next);
        }
        board.setCounter(board.getCounter() + 1);
    }

    // XXX: V2

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
    // XXX: V2
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    // XXX: V2

    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    // XXX: V2

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

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }


    /**
     * @S235451
     * This method executes the command option and continues.
     * @param option Command
     */
    public void executeCommandOptionAndContinue(@NotNull Command option) {
        Player currentPlayer = board.getCurrentPlayer();
        if (currentPlayer != null &&
                board.getPhase() == Phase.PLAYER_INTERACTION &&
                option != null){
                board.setPhase(Phase.ACTIVATION);
                executeCommand(currentPlayer, option);

                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()); {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
            }
        } else {
           int step = board.getStep() +1;

           if (step < Player.NO_REGISTERS){
               makeProgramFieldsVisible(step);
               board.setStep(step);
               board.setCurrentPlayer(board.getPlayer(0));
           } else {
               startProgrammingPhase();
           }

        }

    }
    /**
     *
     * This method checks whether there is a card and what card it is
     * and then executes the step based on the card
     *
     * @author Jonas Woetmann Larsen,
     *
     */
    private void executeNextStep() {
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
     * @param player Player
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

    /**
     * This makes player move forward
     *
     * @param player Player
     * @author Anton Fu Hou Dong, s235460
     */
    public void moveForward(@NotNull Player player) {
        System.out.println("++++++++  moveForward");
        Space current = player.getSpace();
        Space target = board.getNeighbour(current, player.getHeading());
        Space nextTarget = board.getNeighbour(target, player.getHeading());
        Space T3Target  = board.getNeighbour(nextTarget, player.getHeading());
        Space T4Target  = board.getNeighbour(T3Target, player.getHeading());
        Space T5Target  = board.getNeighbour(T4Target, player.getHeading());
        Space T6Target  = board.getNeighbour(T5Target, player.getHeading());

        if(target.isHasWall()) {
            System.out.println("there is wall on the space, cannot push the player!");
            //return;
        }
        if(target.getPlayer() != null) {
            System.out.println("there is player " + target.getPlayer().getName() + " on the space, push the player!");
            if(nextTarget.getPlayer() == null) {
                if(nextTarget.isHasWall()) {
                    System.out.println("there is wall on the space, cannot push the player!");
                    return;
                }

                // move both players
                nextTarget.setPlayer(target.getPlayer());
                player.setSpace(target);
            } else {
                System.out.println("there is player " + nextTarget.getPlayer().getName() + " on the next space!");
                if(T3Target.getPlayer() == null) {
                    if(T3Target.isHasWall()) {
                        System.out.println("there is wall on the space, cannot push the player!");
                        return;
                    }
                    T3Target.setPlayer(nextTarget.getPlayer());
                    nextTarget.setPlayer(target.getPlayer());
                    player.setSpace(target);
                } else {
                    if(T4Target.getPlayer() == null) {
                        if(T4Target.isHasWall()) {
                            System.out.println("there is wall on the space, cannot push the player!");
                            return;
                        }
                        T4Target.setPlayer(T3Target.getPlayer());
                        T3Target.setPlayer(nextTarget.getPlayer());
                        nextTarget.setPlayer(target.getPlayer());
                        player.setSpace(target);
                    } else {
                        if(T5Target.getPlayer() == null) {
                            if(T5Target.isHasWall()) {
                                System.out.println("there is wall on the space, cannot push the player!");
                                return;
                            }
                            T5Target.setPlayer(T4Target.getPlayer());
                            T4Target.setPlayer(T3Target.getPlayer());
                            T3Target.setPlayer(nextTarget.getPlayer());
                            nextTarget.setPlayer(target.getPlayer());
                            player.setSpace(target);
                        } else {
                            if(T6Target.isHasWall()) {
                                System.out.println("there is wall on the space, cannot push the player!");
                                return;
                            }
                            T6Target.setPlayer(T5Target.getPlayer());
                            T5Target.setPlayer(T4Target.getPlayer());
                            T4Target.setPlayer(T3Target.getPlayer());
                            T3Target.setPlayer(nextTarget.getPlayer());
                            nextTarget.setPlayer(target.getPlayer());
                            player.setSpace(target);
                        }
                    }

                }
            }
        } else {
            player.setSpace(target);
        }
    }

    /**
     * This makes player move forward twice
     * @param player Player
     */
    public void fastForward(@NotNull Player player) {
        System.out.println("++++++++  fastForward");
        moveForward(player);
        moveForward(player);
    }

    /**
     * This makes player turn to right (Clockwise)
     * @param player Player
     */
    public void turnRight(@NotNull Player player) {
        System.out.println("++++++++  turnRight");
        if(player.getHeading() == Heading.SOUTH)
            player.setHeading(Heading.WEST);
        else if(player.getHeading() == Heading.NORTH)
            player.setHeading(Heading.EAST);
        else if(player.getHeading() == Heading.WEST)
            player.setHeading(Heading.NORTH);
        else
            player.setHeading(Heading.SOUTH);
    }

    /**
     * This makes player turn to left (Counter-Clockwise)
     * @param player Player
     */
    public void turnLeft(@NotNull Player player) {
        System.out.println("++++++++  turnLeft");
        if(player.getHeading() == Heading.SOUTH)
            player.setHeading(Heading.EAST);
        else if(player.getHeading() == Heading.NORTH)
            player.setHeading(Heading.WEST);
        else if(player.getHeading() == Heading.WEST)
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
            System.out.println("++++++++  233");
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }

}
