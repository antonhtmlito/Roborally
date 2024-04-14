package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class GearsFieldAction extends FieldAction{


    public Heading heading;

    public GearsFieldAction(){
        System.out.println("GearsFieldAction");
    }


    public Heading getHeading(){
        System.out.println("getHeading");
        return heading;
    }

    public void setHeading(Heading heading){
        System.out.println("setHeading");
        this.heading = heading;
    }

    public boolean doAction(GameController gameController, Space space) {
        System.out.println("GearsFieldAction : doAction()");
        Player player = space.getPlayer();
        Heading heading = player.getHeading();

        if (heading == Heading.NORTH){
            player.setHeading(Heading.EAST);
        } else if (heading == Heading.EAST){
            player.setHeading(Heading.SOUTH);
        } else if (heading == Heading.SOUTH){
            player.setHeading(Heading.WEST);
        } else if (heading == Heading.WEST){
            player.setHeading(Heading.NORTH);
        } else {
            return false;
        }
        return true;
    }
}