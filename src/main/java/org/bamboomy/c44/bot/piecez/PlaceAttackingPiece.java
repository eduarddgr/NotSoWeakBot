
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.piecez;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.player.Player;

public abstract class PlaceAttackingPiece extends Piece {

	protected final ArrayList<Place> reach = new ArrayList<Place>();

	public PlaceAttackingPiece(Place place, int color, Player player, String identifier, PieceValue value,
			boolean moved, Board board, String md5, int oldColor) {

		super(place, color, player, identifier, value, moved, board, md5, oldColor);
	}

	@Override
	public void calculateDefence(Place to) {

		calculateReach(to);
	}

	@Override
	public void calculateOffence() {

		calculateReach(null);
	}

	protected abstract void calculateReach(Place to);

	@Override
	public boolean attacks(Place to) {

		return reach.contains(to);
	}

	@Override
	public boolean defends(Place to) {

		return reach.contains(to);
	}

	@Override
	public void updateChecks(Player kingPlayer) {

		calculateReach(null);

		for (Place place : reach) {

			if (place != null && place.getPiece() == kingPlayer.getKing()) {

				kingPlayer.setCheck(this);
			}
		}
	}

}
