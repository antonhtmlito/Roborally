package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.dal.RepositoryAccess;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class CheckPointFieldAction extends FieldAction{

    public int checkPointFieldId;
    public CheckPointFieldAction() {
        System.out.println("CheckPointFieldAction");
    }

    public int getCheckPointFieldId() {
        return checkPointFieldId;
    }

    public void setCheckPointId(int checkPointFieldId) {
        this.checkPointFieldId = checkPointFieldId;
    }


    public boolean doAction(GameController gameController, Space space) {
        System.out.println("CheckPointFieldAction doAction()");
        Player player = space.getPlayer();
        if (player != null) {
            System.out.println("CheckPointFieldAction player " + player.getName() + " reach check point " + checkPointFieldId);
            player.collectedCheckpoints(checkPointFieldId);
            if(player.hasCollectedAllCheckpoints(GameController.getCollectedCheckpoints())) {
                System.out.println("player " + player.getName() + " has collected all checkpoints");
                //System.exit(0);
            }
        }
        return false;
    }

}
