
/**

$$$License$$$

**/


package org.bamboomy.c44.bot.piecez;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.player.Player;

public class Queen extends LinePiece {

	public Queen(Place place, int color, Player player, String identifier, boolean moved, Board board, String md5, int oldColor) {

		super(place, color, player, identifier, PieceValue.QUEEN, moved, board, md5, oldColor);

		addBisshopLinez(board);
		addRookLinez(board);

		createBisshopCheckLinez(board);
		createRookCheckLinez(board);
	}

	@Override
	public void calculateMovez() {

		movez = new ArrayList<>();

		addBisshopMovez();

		addTowerMovez();

		inited = true;

		// setChecks();
	}

	@Override
	public void propagateValues() {

		propagateValuesForBissop();
		propagateValuesForRook();
	}

}
