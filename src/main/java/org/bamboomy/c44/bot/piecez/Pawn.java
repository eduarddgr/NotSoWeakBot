
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.piecez;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.player.Player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Pawn extends PlaceAttackingPiece {

	@Getter
	private final int xDelta, yDelta;

	private static final boolean DEBUG = false;

	public Pawn(Place place, int color, int xDelta, int yDelta, Player player, String identifier, boolean moved,
			Board board, String md5, int oldColor) {

		super(place, color, player, identifier, PieceValue.PAWN, moved, board, md5, oldColor);

		this.xDelta = xDelta;
		this.yDelta = yDelta;
	}

	@Override
	public void calculateMovez() {

		super.calculateMovez();

		if (pinned || removed) {

			return;
		}

		// go forward moves

		Place otherPlace = null;

		otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() + yDelta];

		if (otherPlace != null) {

			if (otherPlace.getPiece() == null) {

				if ((currentPlace.getX() + xDelta > 0 && currentPlace.getX() + xDelta < 12)
						&& (currentPlace.getY() + yDelta > 0 && currentPlace.getY() + yDelta < 12)) {

					movez.add(new Move(currentPlace, otherPlace, this, "m"));

				} else {

					// Promotion

					/*
					 * attackableMoves.add(new Promotion(currentPlace, otherPlace, this, addMove,
					 * Color.getBySeq(color), player, board));
					 * 
					 */
				}

				if (!moved.peek()) {

					Place secondPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX()
							+ (xDelta * 2)][currentPlace.getY() + (yDelta * 2)];

					if (secondPlace != null && secondPlace.getPiece() == null) {

						Move move = new Move(currentPlace, secondPlace, this, "m");

						movez.add(move);
					}
				}
			}
		}

		// attacking moves

		if (xDelta != 0 && currentPlace.getY() + 1 < 12 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() + 1];

			pawnAttack(otherPlace);
		}

		if (xDelta != 0 && currentPlace.getY() - 1 >= 0 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() - 1];

			pawnAttack(otherPlace);
		}

		if (yDelta != 0 && currentPlace.getX() + 1 < 12 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + 1][currentPlace.getY() + yDelta];

			pawnAttack(otherPlace);
		}

		if (yDelta != 0 && currentPlace.getX() - 1 >= 0 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() - 1][currentPlace.getY() + yDelta];

			pawnAttack(otherPlace);
		}

		inited = true;
	}

	private void pawnAttack(Place otherPlace) {

		if (otherPlace != null && otherPlace.getPiece() != null && otherPlace.getPiece().color != color) {

			movez.add(new Move(currentPlace, otherPlace, this, "m"));
		}
	}

	@Override
	public void propagateValues() {

		Place otherPlace;

		if (xDelta != 0 && currentPlace.getY() + 1 < 12 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() + 1];

			attachMe(otherPlace, this);
		}

		if (xDelta != 0 && currentPlace.getY() - 1 >= 0 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() - 1];

			attachMe(otherPlace, this);
		}

		if (yDelta != 0 && currentPlace.getX() + 1 < 12 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + 1][currentPlace.getY() + yDelta];

			attachMe(otherPlace, this);
		}

		if (yDelta != 0 && currentPlace.getX() - 1 >= 0 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() - 1][currentPlace.getY() + yDelta];

			attachMe(otherPlace, this);
		}
	}

	@Override
	protected void calculateReach(Place unused) {

		reach.clear();

		if (xDelta != 0 && currentPlace.getY() + 1 < 12 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			reach.add(currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() + 1]);
		}

		if (xDelta != 0 && currentPlace.getY() - 1 >= 0 && currentPlace.getX() + xDelta > 0
				&& currentPlace.getX() + xDelta < 12) {

			reach.add(currentPlace.getBoard().getPlacez()[currentPlace.getX() + xDelta][currentPlace.getY() - 1]);
		}

		if (yDelta != 0 && currentPlace.getX() + 1 < 12 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			reach.add(currentPlace.getBoard().getPlacez()[currentPlace.getX() + 1][currentPlace.getY() + yDelta]);
		}

		if (yDelta != 0 && currentPlace.getX() - 1 >= 0 && currentPlace.getY() + yDelta > 0
				&& currentPlace.getY() + yDelta < 12) {

			reach.add(currentPlace.getBoard().getPlacez()[currentPlace.getX() - 1][currentPlace.getY() + yDelta]);
		}
	}

}
