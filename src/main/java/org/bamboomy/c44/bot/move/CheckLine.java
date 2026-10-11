
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.move;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.LinePiece;
import org.bamboomy.c44.bot.player.Player;

public class CheckLine {

	private int xDelta, yDelta;
	private Board board;
	private LinePiece linePiece;

	private ArrayList<Place> oneDirection = new ArrayList<Place>(), otherDirection = new ArrayList<Place>();

	public CheckLine(int xDelta, int yDelta, Board board, LinePiece linePiece) {

		this.xDelta = xDelta;
		this.yDelta = yDelta;
		this.board = board;
		this.linePiece = linePiece;

		update();
	}

	public void update() {

		oneDirection = new ArrayList<Place>();

		Place otherPlace = linePiece.getCurrentPlace();

		while (otherPlace != null) {

			if (otherPlace.getX() + xDelta < 12 && otherPlace.getX() + xDelta > -1 && otherPlace.getY() + yDelta < 12
					&& otherPlace.getY() + yDelta > -1) {

				otherPlace = board.getPlacez()[otherPlace.getX() + xDelta][otherPlace.getY() + yDelta];

				if (otherPlace != null) {

					oneDirection.add(otherPlace);
				}

			} else {

				otherPlace = null;
			}
		}

		otherDirection = new ArrayList<Place>();

		otherPlace = linePiece.getCurrentPlace();

		while (otherPlace != null) {

			if (otherPlace.getX() - xDelta > -1 && otherPlace.getX() - xDelta < 12 && otherPlace.getY() - yDelta < 12
					&& otherPlace.getY() - yDelta > -1) {

				otherPlace = board.getPlacez()[otherPlace.getX() - xDelta][otherPlace.getY() - yDelta];

				if (otherPlace != null) {

					otherDirection.add(otherPlace);
				}

			} else {

				otherPlace = null;
			}
		}
	}

	public void checkCheck(Player player) {

		for (Place place : oneDirection) {

			if (place.getPiece() != null && place.getPiece() != player.getKing()) {

				break;
			}

			if (place.getPiece() == player.getKing()) {

				player.setCheck(linePiece);
			}
		}

		for (Place place : otherDirection) {

			if (place.getPiece() != null && place.getPiece() != player.getKing()) {

				break;
			}

			if (place.getPiece() == player.getKing()) {

				player.setCheck(linePiece);
			}
		}
	}

}
