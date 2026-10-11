
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.move;

import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.Piece;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Move {

	protected Place from, to;

	protected Piece piece;

	private Piece takenPiece;

	private static final boolean DEBUG = false;

	protected String identifier = "m";
	
	@Setter
	@Getter
	private Move[] movesToDepth;

	@Setter
	private Boolean attacked = null, defended = null;

	public Move(Place from, Place to, Piece piece, String identifier) {

		this.from = from;
		this.to = to;

		if (to == null) {

			throw new RuntimeException("to is null");
		}

		this.piece = piece;

		this.identifier = identifier;
	}

	public boolean execute(boolean fromGameMaster) {

		if (to.getPiece() != null) {

			takenPiece = to.getPiece();

			takenPiece.setRemoved(true);
		}

		return piece.moveTo(to, fromGameMaster);
	}

	public void rollback() {

		resetPiece();

		if (takenPiece != null) {

			to.setPiece(takenPiece);

			takenPiece.setRemoved(false);
		}

		/*
		 * if (enPassant != null) {
		 * 
		 * EnPassant result = enPassant.release();
		 * 
		 * enPassant = result;
		 * 
		 * piece.getPlayer().setEnPassant(result); }
		 */
	}

	protected void resetPiece() {

		piece.moveTo(from, false);

		piece.rollBackMoved();
	}

}
