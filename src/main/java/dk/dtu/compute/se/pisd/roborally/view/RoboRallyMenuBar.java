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

import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

/**
 *  * This is the RoboRallyMenuBar class of the RoboRally Game
 *
 * @author Ekkart Kindler, ekki@dtu.dk, @author Anton Fu Hou Dong, @s235460
 *
 */

public class RoboRallyMenuBar extends MenuBar {

    private final AppController appController;

    private final MenuItem newGame;
    private final MenuItem saveGame;

    private final MenuItem loadGame;

    private final MenuItem stopGame;

    /**
     *
     * This code constructs a menu  bar for the Roborally application.
     * This menu bar includes a "File" menu with options to start a new game, stop the current game,
     * save the current game, load a saved game, and exit the application.
     * @author Anton Fu Hou Dong, @s235460
     *
     */

    public RoboRallyMenuBar(AppController appController) {
        this.appController = appController;

        Menu controlMenu = new Menu("File");
        this.getMenus().add(controlMenu);

        newGame = new MenuItem("New Game");
        newGame.setOnAction( e -> this.appController.newGame());
        controlMenu.getItems().add(newGame);

        stopGame = new MenuItem("Stop Game");
        stopGame.setOnAction( e -> this.appController.stopGame());
        controlMenu.getItems().add(stopGame);

        saveGame = new MenuItem("Save Game");
        saveGame.setOnAction( e -> this.appController.saveGame());
        controlMenu.getItems().add(saveGame);

        loadGame = new MenuItem("Load Game");
        loadGame.setOnAction( e -> this.appController.loadGame());
        controlMenu.getItems().add(loadGame);

        MenuItem exitApp = new MenuItem("Exit");
        exitApp.setOnAction(e -> this.appController.exit());
        controlMenu.getItems().add(exitApp);

        controlMenu.setOnShowing(e -> update());
        controlMenu.setOnShown(e -> this.updateBounds());
        update();
    }

    public void update() {
        if (appController.isGameRunning()) {
            newGame.setVisible(true);
            stopGame.setVisible(true);
            saveGame.setVisible(true);
            loadGame.setVisible(true);
        } else {
            newGame.setVisible(true);
            stopGame.setVisible(true);
            saveGame.setVisible(true);
            loadGame.setVisible(true);
        }
    }

}
