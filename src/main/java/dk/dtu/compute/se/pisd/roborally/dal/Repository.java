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
package dk.dtu.compute.se.pisd.roborally.dal;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
class Repository implements IRepository {
	
	private static final String GAME_GAMEID = "gameID";

	private static final String GAME_NAME = "name";

	private static final String BOARD_NAME = "boardName";
	
	private static final String GAME_CURRENTPLAYER = "currentPlayer";

	private static final String GAME_PHASE = "phase";

	private static final String GAME_STEP = "step";
	
	private static final String PLAYER_PLAYERID = "playerID";
	
	private static final String PLAYER_NAME = "name";

	private static final String PLAYER_COLOUR = "colour";
	
	private static final String PLAYER_GAMEID = "gameID";
	
	private static final String PLAYER_POSITION_X = "positionX";

	private static final String PLAYER_POSITION_Y = "positionY";

	private static final String PLAYER_HEADING = "heading";

	private static final String CHECK_POINT_TOKENs = "checkpointTokens";

	private static final int FIELD_TYPE_REGISTER = 0;

	private static final int FIELD_TYPE_HAND = 1;

	private static final String FIELD_POS = "position";

	private static final String FIELD_VISIBLE = "visible";

	private static final String FIELD_COMMAND = "command";

	private static final String FIELD_PLAYERID = "playerID";

	private static final String FIELD_TYPE = "type";

	private static final int CARDSTACK_TYPE_DECK = 0;

	private static final int CARDSTACK_TYPE_DISCARD = 1;

	private static final String CARDSTACK_POS = "position";

	private static final String CARDSTACK_COMMAND = "command";

	private static final String CARDSTACK_PLAYERID = "playerID";

	private static final String CARDSTACK_TYPE = "type";

	private Connector connector;

	/**
	 * Constructs a Repository instance which acts as a bridge between the game's data model and the database.
	 * It uses a given Connector instance to establish a connection to the database for executing SQL queries.
	 * @param connector The Connector instance used to connect to the database.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	Repository(Connector connector){
		this.connector = connector;
	}

	/**
	 * Creates a new game in the database with the current state of the Board game.
	 * It saves the game's basic information along with the state of each player.
	 * * @param game The game state to be saved into the database.
	 * * @return true if the game was successfully created in the database, false otherwise.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	@Override
	public boolean createGameInDB(Board game) {
		if (game.getGameId() == null) {
			Connection connection = connector.getConnection();
			try {
				connection.setAutoCommit(false);
				PreparedStatement ps = getInsertGameStatementRGK();
				// TODO: the name should eventually set by the user
				//       for the game and should be then used 
				//       game.getName();
				ps.setString(1, "Date: " +  new Date()); // instead of name
				ps.setNull(2, Types.TINYINT); // game.getPlayerNumber(game.getCurrentPlayer())); is inserted after players!
				ps.setInt(3, game.getPhase().ordinal());
				ps.setInt(4, game.getStep());
				// If you have a foreign key constraint for current players,
				// the check would need to be temporarily disabled, since
				// MySQL does not have a per transaction validation, but
				// validates on a per row basis.
				// Statement statement = connection.createStatement();
				// statement.execute("SET foreign_key_checks = 0");
				int affectedRows = ps.executeUpdate();
				ResultSet generatedKeys = ps.getGeneratedKeys();
				if (affectedRows == 1 && generatedKeys.next()) {
					game.setGameId(generatedKeys.getInt(1));
				}
				generatedKeys.close();
				// Enable foreign key constraint check again:
				// statement.execute("SET foreign_key_checks = 1");
				// statement.close();
				createPlayersInDB(game);
				// create card stack
				createCardStackInDB(game);
				// create card field
				createCardFieldsInDB(game);
				// since current player is a foreign key, it can oly be
				// inserted after the players are created, since MySQL does
				// not have a per transaction validation, but validates on
				// a per row basis.
				ps = getSelectGameStatementU();
				ps.setInt(1, game.getGameId());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
					rs.updateRow();
				} else {
					// TODO error handling
				}
				rs.close();
				connection.commit();
				connection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Some DB error");
				
				try {
					connection.rollback();
					connection.setAutoCommit(true);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			System.err.println("Game cannot be created in DB, since it has a game id already!");
		}
		return false;
	}

	/**
	 * Updates the state of the game in the database to reflect the current status of the Board instance.
	 * This includes updating the current player, game phase, and step.
	 * @param game The current state of the game to update in the database.
	 * @return true if the update was successful, false otherwise.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	@Override
	public boolean updateGameInDB(Board game) {
		assert game.getGameId() != null;
		
		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);

			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, game.getGameId());
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
				rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
				rs.updateInt(GAME_STEP, game.getStep());
				rs.updateString(BOARD_NAME, LoadBoard.getGameBoard());
				rs.updateRow();
			} else {
				// TODO error handling
			}
			rs.close();
			updatePlayersInDB(game);
			// update card stack
			updateCardStacksInDB(game);
			// update card field
			updateCardFieldsInDB(game);
            connection.commit();
            connection.setAutoCommit(true);
			return true;
		} catch (SQLException e) {
			// TODO error handling
			e.printStackTrace();
			System.err.println("Some DB error");
			
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				// TODO error handling
				e1.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Loads a game from the database using the specified game ID.
	 * It reconstructs the game state, including the board, players, and their positions.
	 * @param id The unique identifier of the game to load from the database.
	 * @return The loaded game as a Board instance, or null if the game could not be loaded.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	@Override
	public Board loadGameFromDB(int id) {
		Board game;
		try {
			// TODO here, we could actually use a simpler statement
			//      which is not updatable, but reuse the one from
			//      above for the pupose
			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			int playerNo = -1;
			if (rs.next()) {
				// TODO the width and height could eventually come from the database
				// int width = AppController.BOARD_WIDTH;
				// int height = AppController.BOARD_HEIGHT;
				// game = new Board(width,height);
				// TODO and we should also store the used game board in the database
				//      for now, we use the default game board
				game = LoadBoard.loadBoard(rs.getString(BOARD_NAME));
				if (game == null) {
					game = LoadBoard.loadBoard(null);
				}
				playerNo = rs.getInt(GAME_CURRENTPLAYER);
				// TODO currently we do not set the games name (needs to be added)
				game.setPhase(Phase.values()[rs.getInt(GAME_PHASE)]);
				game.setStep(rs.getInt(GAME_STEP));
			} else {
				// TODO error handling
				return null;
			}
			rs.close();
			game.setGameId(id);			
			loadPlayersFromDB(game);
			if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
				game.setCurrentPlayer(game.getPlayer(playerNo));
			} else {
				// TODO  error handling
				return null;
			}
			// load card stack
			loadCardStackFromDB(game);
			// load card field
			loadCardFieldsFromDB(game);
			if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
				game.setCurrentPlayer(game.getPlayer(playerNo));
			} else {
				return null;
			}

			/* TOODO this method needs to be implemented first
			loadCardFieldsFromDB(game);
			*/
			return game;
		} catch (SQLException e) {
			// TODO error handling
			e.printStackTrace();
			System.err.println("Some DB error");
		}
		return null;
	}

	/**
	 * Retrieves a list of all games stored in the database.
	 * Each game is represented by a GameInDB instance, which includes its ID.
	 * @author Anton Fu Hou Dong, @s235460
	 * @return A List of GameInDB instances representing all games available in the database.
	 */
	@Override
	public List<GameInDB> getGames() {
		int id;
		String name;
		ResultSet rs;
		// TODO when there many games in the DB, fetching all available games
		//      from the DB is a bit extreme; eventually there should a
		//      methods that can filter the returned games in order to
		//      reduce the number of the returned games.
		List<GameInDB> result = new ArrayList<>();
		try {
			PreparedStatement ps = getSelectGameIdsStatement();
			rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt(GAME_GAMEID);
				name = rs.getString(GAME_NAME);
				result.add(new GameInDB(id,name));
			}
			rs.close();
		} catch (SQLException e) {
			// TODO proper error handling
			e.printStackTrace();
		}
		return result;		
	}

	/**
	 * Saves the state of all players in the game into the database.
	 * This includes their position, color, and other relevant attributes.
	 * @param game The game whose players' states are to be saved.
	 * @throws SQLException If there is an error during the database update process.
	 * @author Anton Fu Hou Dong, @s235460
	 */

	private void createPlayersInDB(Board game) throws SQLException {
		Player player;
		// TODO code should be more defensive
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			player = game.getPlayer(i);
			rs.moveToInsertRow();
			rs.updateInt(PLAYER_GAMEID, game.getGameId());
			rs.updateInt(PLAYER_PLAYERID, i);
			rs.updateString(PLAYER_NAME, player.getName());
			rs.updateString(PLAYER_COLOUR, player.getColor());
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.insertRow();
		}
		rs.close();
	}

	/**
	 * create card fields in database
	 * @param game Board
	 * @throws SQLException
	 */
	private void createCardFieldsInDB(Board game) throws SQLException {
		Player player;
		CommandCardField[] cards;
		CommandCardField[] program;
		PreparedStatement ps = getSelectCardFieldStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			player = game.getPlayer(i);
			cards = player.getCards();
			program = player.getProgram();
			for (int j = 0; j < cards.length; j++) {
				rs.moveToInsertRow();
				rs.updateInt("gameID", game.getGameId());
				rs.updateInt(FIELD_PLAYERID, player.getPlayerId());
				rs.updateInt(FIELD_TYPE, FIELD_TYPE_HAND);
				rs.updateBoolean(FIELD_VISIBLE, cards[j].isVisible());
				rs.updateInt(FIELD_POS, j);
				if (cards[j].getCard() != null)
					rs.updateObject(FIELD_COMMAND, cards[j].getCard().getCommand().ordinal());
				else
					rs.updateObject(FIELD_COMMAND, null);
				rs.insertRow();
			}
			for (int j = 0; j < program.length; j++) {
				rs.moveToInsertRow();
				rs.updateInt("gameID", game.getGameId());
				rs.updateInt(FIELD_PLAYERID, player.getPlayerId());
				rs.updateInt(FIELD_TYPE, FIELD_TYPE_REGISTER);
				rs.updateInt(FIELD_POS, j);
				rs.updateBoolean(FIELD_VISIBLE, program[j].isVisible());
				if (program[j].getCard() != null)
					rs.updateObject(FIELD_COMMAND, program[j].getCard().getCommand().ordinal());
				else
					rs.updateObject(FIELD_COMMAND, null);
				rs.insertRow();
			}
		}
		rs.close();
	}

	/**
	 * Loads player data from the database for the specified game and populates the game's player list.
	 * This method retrieves player information such as name, color, position, and heading from the database
	 * and creates Player objects accordingly, adding them to the game.
	 * @param game The game object for which players are being loaded from the database.
	 * @throws SQLException If a database access error occurs or this method is called on a closed connection.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	private void loadPlayersFromDB(Board game) throws SQLException {
		String name, colour;
		Player player;
		int playerId;
		int x, y, heading;
		PreparedStatement ps = getSelectPlayersASCStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			playerId = rs.getInt(PLAYER_PLAYERID);
			if (i++ == playerId) {
				// TODO this should be more defensive
				name = rs.getString(PLAYER_NAME);
				colour = rs.getString(PLAYER_COLOUR);
				player = new Player(game, colour ,name);
				game.addPlayer(player);
				x = rs.getInt(PLAYER_POSITION_X);
				y = rs.getInt(PLAYER_POSITION_Y);
				player.setSpace(game.getSpace(x,y));
				heading = rs.getInt(PLAYER_HEADING);
				player.setHeading(Heading.values()[heading]);
				// TODO  should also load players program and hand here
			} else {
				// TODO error handling
				System.err.println("Game in DB does not have a player with id " + i +"!");
			}
		}
		rs.close();
	}

	/**
	 * Updates the database with the current state of all players in the game.
	 * This method should be called to save the latest changes made to players' states.
	 * @param game The game whose players' states are to be updated in the database.
	 * @throws SQLException If there is an error during the database update process.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	private void updatePlayersInDB(Board game) throws SQLException {
		Player player;
		int playerId;
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			playerId = rs.getInt(PLAYER_PLAYERID);
			// TODO should be more defensive
			player = game.getPlayer(playerId);
			// rs.updateString(PLAYER_NAME, player.getName()); // not needed: player's names does not change
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(CHECK_POINT_TOKENs, player.getCollectedTokens());
			// TODO error handling
			// TODO take care of case when number of players changes, etc
			rs.updateRow();
		}
		rs.close();
		
		// TODO error handling/consistency check: check whether all players were updated
	}

	/**
	 * SQL statement used to insert a new game into the database with specified attributes.
	 * The statement inserts a new row into the 'Game' table with values for name, current player,
	 * phase, and step.
	 * @author Anton Fu Hou Dong, @s235460
	 */
	private static final String SQL_INSERT_GAME = "INSERT INTO Game(name, currentPlayer, phase, step) VALUES (?, ?, ?, ?)";

	private PreparedStatement insert_game_stmt = null;

	private PreparedStatement getInsertGameStatementRGK() {
		if (insert_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				insert_game_stmt = connection.prepareStatement(
						SQL_INSERT_GAME,
						Statement.RETURN_GENERATED_KEYS);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return insert_game_stmt;
	}

	private static final String SQL_SELECT_GAME = "SELECT * FROM Game WHERE gameID = ?";
	private PreparedStatement select_game_stmt = null;
	
	private PreparedStatement getSelectGameStatementU() {
		if (select_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_game_stmt = connection.prepareStatement(
						SQL_SELECT_GAME,
						ResultSet.TYPE_FORWARD_ONLY,
					    ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_game_stmt;
	}
		
	private static final String SQL_SELECT_PLAYERS = "SELECT * FROM Player WHERE gameID = ?";
	private PreparedStatement select_players_stmt = null;

	private PreparedStatement getSelectPlayersStatementU() {
		if (select_players_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_players_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_stmt;
	}

	private static final String SQL_SELECT_PLAYERS_ASC =
			"SELECT * FROM Player WHERE gameID = ? ORDER BY playerID ASC";
	
	private PreparedStatement select_players_asc_stmt = null;
	
	private PreparedStatement getSelectPlayersASCStatement() {
		if (select_players_asc_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				// This statement does not need to be updatable
				select_players_asc_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS_ASC);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_asc_stmt;
	}
	
	private static final String SQL_SELECT_GAMES =
			"SELECT gameID, name FROM Game";
	
	private PreparedStatement select_games_stmt = null;
	
	private PreparedStatement getSelectGameIdsStatement() {
		if (select_games_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_games_stmt = connection.prepareStatement(
						SQL_SELECT_GAMES);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_games_stmt;
	}



	public ArrayList<Integer> getGameIds() {
		ArrayList<Integer> gameIds = new ArrayList<>();
		Connection connection = connector.getConnection();
		int id = 0;
		try {
			connection.setAutoCommit(false);
			PreparedStatement ps = getSelectGameIdsStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt("gameId");
				gameIds.add(id);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return gameIds;
	}

	/**
	 *  Update the card stack
	 * @param game Board
	 */
	private void updateCardStacksInDB(Board game) {
		//System.out.println("  updateCardStacksInDB start. ");
		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);
			PreparedStatement ps = getSelectCardStackStatement();
			ps.setInt(1, game.getGameId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rs.deleteRow();
			}
			rs.close();
			createCardStackInDB(game);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("  updateCardStacksInDB end. ");
	}

	/**
	 * get selected card stack statement
	 * @return PreparedStatement
	 */
	private PreparedStatement getSelectCardStackStatement() {
		if (select_cardstack_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cardstack_stmt = connection.prepareStatement(
						SQL_SELECT_CARDSTACK, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
		return select_cardstack_stmt;
	}

	// variable used by getSelectCardStackStatement
	private static final String SQL_SELECT_CARDSTACK = "SELECT * FROM CardStack WHERE gameID = ?";
	private PreparedStatement select_cardstack_stmt = null;

	/**
	 * create card stack in database
	 * @param game
	 * @throws SQLException
	 */
	private void createCardStackInDB(Board game) throws SQLException {
		Player player;
		Stack<CommandCard> stack;
		int deckPosition;
		PreparedStatement ps = getSelectCardStackStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			player = game.getPlayer(i);
			stack = player.getCardDeck();
			deckPosition = 0;
			for (CommandCard command : stack) {
				rs.moveToInsertRow();
				rs.updateInt(PLAYER_GAMEID, game.getGameId());
				rs.updateInt(CARDSTACK_PLAYERID, player.getPlayerId());
				rs.updateInt(CARDSTACK_TYPE, CARDSTACK_TYPE_DECK);
				rs.updateInt(CARDSTACK_POS, deckPosition);
				rs.updateObject(CARDSTACK_COMMAND, command.getCommand().ordinal());
				rs.insertRow();
				deckPosition++;
			}
		}
		rs.close();
	}

	/**
	 * update card field in database
	 * @param game Board
	 */
	private void updateCardFieldsInDB(Board game) {
		int playerId;
		Player player;
		int pos;
		int type;
		boolean visible;
		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);
			PreparedStatement ps = getSelectCardFieldStatement();
			ps.setInt(1, game.getGameId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				playerId = rs.getInt(FIELD_PLAYERID);
				player = game.getPlayer(playerId);
				pos = rs.getInt(FIELD_POS);
				type = rs.getInt(FIELD_TYPE);
				visible = false;
				if (type == FIELD_TYPE_REGISTER) {
					visible = player.getProgram()[pos].isVisible();
					rs.updateBoolean(FIELD_VISIBLE, visible);
					if (player.getProgram()[pos].getCard() != null)
						rs.updateObject(FIELD_COMMAND, player.getProgram()[pos].getCard().getCommand().ordinal());
					else
						rs.updateObject(FIELD_COMMAND, null);
				} else if (type == FIELD_TYPE_HAND) {
					visible = player.getCards()[pos].isVisible();
					rs.updateBoolean(FIELD_VISIBLE, visible);
					if (player.getCards()[pos].getCard() != null)
						rs.updateObject(FIELD_COMMAND, player.getCards()[pos].getCard().getCommand().ordinal());
					else
						rs.updateObject(FIELD_COMMAND, null);
				}
				rs.updateRow();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get select card field statement
	 * @return PreparedStatement
	 */
	private PreparedStatement getSelectCardFieldStatement() {
		if (select_card_field_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_card_field_stmt = connection.prepareStatement(
						SQL_SELECT_CARD_FIELDS, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
		return select_card_field_stmt;
	}

	// The variables used by getSelectCardFieldStatement()
	private static final String SQL_SELECT_CARD_FIELDS = "SELECT * FROM CardField WHERE gameID = ?";
	private PreparedStatement select_card_field_stmt = null;

	/**
	 * load card stack from database
	 * @param game Board
	 * @throws SQLException
	 */
	private void loadCardStackFromDB(Board game) throws SQLException {
		int playerId;
		Player pPlayer;
		int type;
		Object cardStackCommand;
		// clear card deck
		for (Player player : game.getPlayers()) {
			player.getCardDeck().clear();
		}
		PreparedStatement ps = getSelectCardStackStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			playerId = rs.getInt(CARDSTACK_PLAYERID);
			pPlayer = game.getPlayer(playerId);
			type = rs.getInt(CARDSTACK_TYPE);
			cardStackCommand = rs.getObject(CARDSTACK_COMMAND);
			if (cardStackCommand != null) {
				Command command = Command.getCommand(rs.getInt(CARDSTACK_COMMAND));
				if (type == CARDSTACK_TYPE_DECK) {
					pPlayer.getCardDeck().push(new CommandCard(command));
				}
			}
		}
		rs.close();
	}

	/**
	 * load card fields from database
	 * @param game Board
	 * @throws SQLException
	 */
	private void loadCardFieldsFromDB(Board game) throws SQLException {
		int playerId;
		Player player;
		int type;
		int pos;
		CommandCardField field;
		PreparedStatement ps = getSelectCardFieldStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			playerId = rs.getInt(FIELD_PLAYERID);
			player = game.getPlayer(playerId);
			type = rs.getInt(FIELD_TYPE);
			pos = rs.getInt(FIELD_POS);
			if (type == FIELD_TYPE_REGISTER) {
				field = player.getProgramField(pos);
			} else if (type == FIELD_TYPE_HAND) {
				field = player.getCardField(pos);
			} else {
				field = null;
			}
			if (field != null) {
				field.setVisible(rs.getBoolean(FIELD_VISIBLE));
				Object fieldCommand = rs.getObject(FIELD_COMMAND);
				if (fieldCommand != null) {
					Command card = Command.values()[rs.getInt(FIELD_COMMAND)];
					field.setCard(new CommandCard(card));
				}
			}
		}
		rs.close();
	}
}


