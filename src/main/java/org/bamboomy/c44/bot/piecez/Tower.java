
/**

$$$License$$$

**/


package org.bamboomy.c44.bot.piecez;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.player.Player;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tower extends LinePiece {

	private static final boolean DEBUG = false;

	public Tower(Place place, int color, Player player, String identifier, boolean moved, Board board, String md5, int oldColor) {

		super(place, color, player, identifier, PieceValue.TOWER, moved, board, md5, oldColor);
		
		addRookLinez(board);
		
		createRookCheckLinez(board);
	}

	@Override
	public void calculateMovez() {

		super.calculateMovez();

		addTowerMovez();
		
		if (DEBUG) {

			System.out.println(movez.size());
		}

		inited = true;

		// setChecks();
	}


	@Override
	public void propagateValues() {

		propagateValuesForRook();		
	}


}
