
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.board;

import org.bamboomy.c44.bot.board.gui.GuiPlace;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.piecez.Piece;
import org.bamboomy.c44.bot.player.Alliance;
import org.bamboomy.c44.bot.player.Color;
import org.bamboomy.c44.bot.player.Player;
import org.bamboomy.c44.rest.Poller;
import org.json.JSONArray;

import lombok.Getter;

public class Board {

	@Getter
	private Place[][] placez = new Place[12][12];

	@Getter
	private Player[] players = new Player[4];

	@Getter
	private final Alliance alliance;

	@Getter
	private final Color color;

	@Getter
	private final Poller poller;

	@Getter
	private Player ownPlayer;

	private static final boolean DEBUG = true;

	@Getter
	private String numberOfMove;

	public Board(String json, Alliance alliance, String ownColor, Poller poller, String numberOfMove) {

		this.numberOfMove = numberOfMove;

		this.alliance = alliance;

		color = Color.getByName(ownColor);

		this.poller = poller;

		for (int i = 0; i < 4; i++) {

			players[i] = new Player(i, this);
		}

		ownPlayer = players[color.getSeq()];

		JSONArray columns = new JSONArray(json);

		for (int i = 0; i < columns.length(); i++) {

			JSONArray row = columns.getJSONArray(i);

			for (int j = 0; j < row.length(); j++) {

				if (!row.isNull(j)) {

					placez[i][j] = new Place(row.getJSONObject(j), this, i, j);
				}
			}
		}

		for (int i = 0; i < columns.length(); i++) {

			JSONArray row = columns.getJSONArray(i);

			for (int j = 0; j < row.length(); j++) {

				if (!row.isNull(j)) {

					placez[i][j].parsePiece(row.getJSONObject(j));
				}
			}
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getPiecez().size() == 0) {

				players[i].setDead(true);
			}
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getKing() == null && !players[i].isDead()) {

				throw new RuntimeException("I don't have a king (" + i + ")");
			}
		}
	}

	public Board(Board board) {

		this.numberOfMove = board.numberOfMove;

		alliance = board.alliance;
		color = board.color;
		poller = board.poller;

		for (int i = 0; i < 4; i++) {

			players[i] = new Player(i, this);
		}

		for (int i = 0; i < placez.length; i++) {

			System.out.print(i + ":");

			for (int j = 0; j < placez.length; j++) {

				System.out.print(j + "|");

				if (board.getPlacez()[i][j] != null) {

					placez[i][j] = new Place(board.getPlacez()[i][j], this, i, j, players);
				}
			}

			System.out.println();
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getPiecez().size() == 0) {

				players[i].setDead(true);
			}
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getKing() == null && !players[i].isDead()) {

				throw new RuntimeException("I don't have a king (" + i + ")");
			}
		}
	}

	public void execute(Move move) {

		System.out.println("Move executed :-D :-D :-D");

		Place from = placez[move.getFrom().getX()][move.getFrom().getY()];
		Place to = placez[move.getTo().getX()][move.getTo().getY()];
		Piece piece = players[color.getSeq()].getPieceByIdentifier(move.getPiece().getIdentifier());

		(new Move(from, to, piece, "m")).execute(false);
	}

	public void clear() {

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null) {

					placez[i][j].clear();
				}
			}
		}
	}

	public double value(Alliance alliance) {

		double result = 0;

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null) {

					result += placez[i][j].value(alliance);
				}
			}
		}

		result *= 1000;

		result = Math.round(result);

		result /= 1000;

		return result;
	}

	public void output() {

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null && placez[i][j].getAttached().size() > 0) {

					System.out.print("(" + i + "," + j + ") -> ");

					for (Piece attached : placez[i][j].getAttached()) {

						System.out.print(attached.getValue().getName() + "(" + attached.getColor() + "), ");
					}

					System.out.println();
				}
			}
		}
	}

	public GuiPlace[][] getGuiArray(String color, boolean currentPlayer) {

		GuiPlace[][] result = new GuiPlace[12][12];

		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 12; j++) {

				if (placez[i][j] != null) {

					result[i][j] = placez[i][j].toGuiPlace(color, currentPlayer);
				}
			}
		}

		return result;
	}

}
