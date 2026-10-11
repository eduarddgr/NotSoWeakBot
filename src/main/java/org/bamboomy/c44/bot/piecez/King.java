
/**

$$$License$$$

**/


package org.bamboomy.c44.bot.piecez;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.Check;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.move.Rocade;
import org.bamboomy.c44.bot.player.Player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class King extends PlaceAttackingPiece {

	@Getter
	private final int xDelta, yDelta;

	@Getter
	private ArrayList<Place> possibleMoves = new ArrayList<>();

	private ArrayList<Rocade> rocades = new ArrayList<>();

	private ArrayList<Check> checks = new ArrayList<>();

	public King(Place place, int color, int xDelta, int yDelta, Player player, String identifier, boolean moved,
			Board board, String md5, int oldColor) {

		super(place, color, player, identifier, PieceValue.KING, moved, board, md5, oldColor);

		this.xDelta = xDelta;
		this.yDelta = yDelta;

		log.debug("king created...");
	}

	public void prepareMovez() {

		possibleMoves = new ArrayList<>();

		int k = currentPlace.getX() + 1;
		int l = currentPlace.getY() + 1;

		if (k < 12 && l < 12) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX();

		if (k < 12 && l < 12) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX() - 1;

		if (k >= 0 && l < 12) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX() + 1;
		l = currentPlace.getY();

		if (k < 12 && l < 12) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX() - 1;

		if (k >= 0 && l < 12) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX() + 1;
		l = currentPlace.getY() - 1;

		if (k < 12 && l >= 0) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX();

		if (k < 12 && l >= 0) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		k = currentPlace.getX() - 1;

		if (k >= 0 && l >= 0) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[k][l];

			if (otherPlace != null) {

				if (otherPlace.getPiece() == null || otherPlace.getPiece().color != color) {

					possibleMoves.add(otherPlace);
				}
			}
		}

		// checkRocadeRight();

		/*
		 * if (!checkRocade) {
		 *
		 * return; }
		 * 
		 * /*
		 *
		 *
		 * //attackableMoves.add(rocade); }
		 *
		 * if (canRocadeLeft(addMove, board)) {
		 *
		 * //attackableMoves.add(rocade); }
		 * 
		 */

		inited = true;
	}

	@Override
	public void calculateMovez() {

		// King movez are calculated in Player
	}

	private void checkRocadeRight() {

		if (moved.peek()) {

			return;
		}

		Piece otherPiece = currentPlace.getBoard().getPlacez()[currentPlace.getX() + (yDelta * 3)][currentPlace.getY()
				- (xDelta * 3)].getPiece();

		if (!isCheck()
				&& currentPlace.getBoard().getPlacez()[currentPlace.getX() + yDelta][currentPlace.getY() - xDelta]
						.getPiece() == null
				&& currentPlace.getBoard().getPlacez()[currentPlace.getX() + (yDelta * 2)][currentPlace.getY()
						- (xDelta * 2)].getPiece() == null
				&& otherPiece instanceof Tower && otherPiece.getColor() == color && !otherPiece.moved.peek()) {

			Place rocadePlace = getRocadePlace((Tower) otherPiece);

			possibleMoves.add(rocadePlace);
		}
	}

	private Place getRocadePlace(Tower otherPiece) {
		Place otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + (yDelta * 2)][currentPlace.getY()
				- (xDelta * 2)];

		/*
		 * 
		 * if (otherPlace.getRocade() == null) {
		 * 
		 * Rocade rocade = new Rocade(currentPlace, otherPlace, this, otherPiece,
		 * currentPlace .getBoard().getPlacez()[currentPlace.getX() +
		 * yDelta][currentPlace.getY() - xDelta]);
		 * 
		 * otherPlace.setRocade(rocade);
		 * 
		 * rocades.add(rocade); }
		 */
		return otherPlace;
	}

	@Override
	public void propagateValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void calculateDefence(Place unused) {

		// no, I do nothing, I'm the freaking King!!!
	}

	@Override
	public void calculateOffence() {

		// no, I do nothing, I'm the freaking King!!!
	}

	@Override
	public boolean attacks(Place to) {

		return false;
	}

	@Override
	public boolean defends(Place to) {

		return false;
	}

	@Override
	protected void calculateReach(Place unused) {

		// TODO also for king!!!
	}

	public void addCheck(Check check) {

		checks.add(check);
	}

	public synchronized boolean isCheck() {

		return checks.size() != 0;
	}

	public void clearChecks() {

		checks = new ArrayList<Check>();
	}

	public void setMovez(ArrayList<Move> movez) {

		this.movez = movez;
	}
}
