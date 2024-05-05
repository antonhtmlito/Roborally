package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class CheckPointFieldAction extends FieldAction{

    public int checkPointFieldId;
    public CheckPointFieldAction() {
       // System.out.println("CheckPointFieldAction");
    }

    public int getCheckPointFieldId() {
        return checkPointFieldId;
    }

    public void setCheckPointId(int checkPointFieldId) {
        this.checkPointFieldId = checkPointFieldId;
    }


    public boolean doAction(GameController gameController, Space space) {
        //System.out.println("CheckPointFieldAction doAction()");
        Player player = space.getPlayer();
        if (player != null) {
           // System.out.println("CheckPointFieldAction player " + player.getName() + " reach check point " + checkPointFieldId);
            if((player.getCollectedTokens()) == checkPointFieldId) {
                player.collectedCheckpoints(checkPointFieldId);
                if (player.hasCollectedAllCheckpoints(GameController.getCollectedCheckpoints())) {
                   // System.out.println("player " + player.getName() + " has collected all checkpoints");
                    AppController.saveGame();
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(player.getName() + " has won the game");
                    alert.setContentText(player.getName() +  " has won the game" + "\n Start new game by going to file -> new game");
                    Optional<ButtonType> result = alert.showAndWait();

                    if (!result.isPresent() || result.get() != ButtonType.OK) {

                    }
                    //System.exit(0);
                }
            }
        }
        return false;
    }

}
