package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * A factory for creating boards. The factory itself is implemented as a singleton.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
// XXX A3: might be used for creating a first slightly more interesting board.
public class BoardFactory {

    /**
     * The single instance of this class, which is lazily instantiated on demand.
     */
    static private BoardFactory instance = null;

    /**
     * Constructor for BoardFactory. It is private in order to make the factory a singleton.
     */
    private BoardFactory() {
    }

    /**
     * Returns the single instance of this factory. The instance is lazily
     * instantiated when requested for the first time.
     *
     * @return the single instance of the BoardFactory
     */
    public static BoardFactory getInstance() {
        if (instance == null) {
            instance = new BoardFactory();
        }
        return instance;
    }

    /**
     * This method creates coordinates for the spaces to have gears
     * on them and specifies which spaces should have gears as a set
     * of coordinates
     *
     * @author Jonas Woetmann Larsen, S235446
     */
    public void setGearSpaces(Board board,int[][] gearSpaceCoordinates) {

        for (int[] coordinates : gearSpaceCoordinates) {
            int x = coordinates[0];
            int y = coordinates[1];
            Space space = board.getSpace(x, y);
            if (space != null) {
                space.setHasGear(true);
            }

        }
    }

    /**
     * This code gives the placement of the checkpoint on the board
     * base on the coordinates in checkpointData
     *
     * @author Martin Dahl Lund, s235454
     */
    public void initializeCheckpoints(Board board, int[][] checkpointData) {
        for (int[] checkpoint : checkpointData) {
            int x = checkpoint[0];
            int y = checkpoint[1];

            Space checkpointSpace = board.getSpace(x, y);
            if (checkpointSpace != null) {
                CheckPointFieldAction checkPointFieldAction = new CheckPointFieldAction();
                checkPointFieldAction.setCheckPointId(checkPointFieldAction.checkPointFieldId);
                checkpointSpace.addAction((checkPointFieldAction));
                checkpointSpace.setCheckpoint(true);
            }
        }
    }


    /**
     * Creates a new board of given name of a board, which indicates
     * which type of board should be created. For now the name is ignored.
     *
     * @param name the given name board
     * @return the new board corresponding to that name
     *
     * @author William Wegener Kofoed, S235451
     */
    public Board createBoard(String name) {
        Board board;
        if (name == null) {
            board = new Board(8,8, "<none>");
        } else {
            board = new Board(8,8, name);
        }

        // add some walls, actions and checkpoints to some spaces
        Space space = board.getSpace(0,0);
        ConveyorBelt action  = new ConveyorBelt();
        /*
        space.getWalls().add(Heading.SOUTH);
        action.setHeading(Heading.WEST);
        space.getActions().add(action);


        space = board.getSpace(1,0);
        space.getWalls().add(Heading.NORTH);
        action  = new ConveyorBelt();
        action.setHeading(Heading.WEST);
        space.getActions().add(action);

        space = board.getSpace(1,1);
        space.getWalls().add(Heading.WEST);
        action  = new ConveyorBelt();
        action.setHeading(Heading.NORTH);
        space.getActions().add(action);

        space = board.getSpace(5,5);
        space.getWalls().add(Heading.SOUTH);
        action  = new ConveyorBelt();
        action.setHeading(Heading.WEST);
        space.getActions().add(action);
        */
        space = board.getSpace(6,5);
        action  = new ConveyorBelt();
        action.setHeading(Heading.WEST);
        space.getActions().add(action);

        space = board.getSpace(7,7);
        space.getWalls().add(Heading.SOUTH);
        space.getWalls().add(Heading.NORTH);
        space.getWalls().add(Heading.EAST);
        space.getWalls().add(Heading.WEST);

        int[][] gearSpaceCoordinates = {
                {5, 2},
                {2, 4},
        };
        setGearSpaces(board,gearSpaceCoordinates);

        /**
         * this code gives the coordinates used in the public void initializeCheckpoints
         *
         * @author Martin Dahl Lund, s235454
         */
        int [][] checkpointData ={
                {3,5},
                {4,1},
                {7,3},
        };
        initializeCheckpoints(board, checkpointData);

        return board;
    }

}
