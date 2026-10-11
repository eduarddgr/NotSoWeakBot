
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.piecez;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.player.Player;

public class Bisshop extends LinePiece {

	private static final boolean DEBUG = false;

	public Bisshop(Place place, int color, Player player, String identifier, boolean moved, Board board, String md5, int oldColor) {

		super(place, color, player, identifier, PieceValue.BISSHOP, moved, board, md5, oldColor);
		
		addBisshopLinez(board);
		
		createBisshopCheckLinez(board);
	}

	@Override
	public void calculateMovez() {

		super.calculateMovez();

		addBisshopMovez();

		if (DEBUG) {

			System.out.println(movez.size());
		}

		inited = true;

		// setChecks();
	}

	@Override
	public void propagateValues() {

		propagateValuesForBissop();
	}

}
