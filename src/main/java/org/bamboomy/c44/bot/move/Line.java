
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.move;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.LinePiece;

public class Line {

	private final int xDelta, yDelta;

	private final Board board;

	private final LinePiece linePiece;

	private final ArrayList<Place> placez = new ArrayList<Place>();

	public Line(int xDelta, int yDelta, Board board, LinePiece linePiece) {

		this.xDelta = xDelta;
		this.yDelta = yDelta;

		this.board = board;

		this.linePiece = linePiece;
	}

	public void calculateRange(Place to) {

		placez.clear();

		Place currentPlace = linePiece.getCurrentPlace();
		Place nextPlace;

		int nextX = currentPlace.getX();
		int nextY = currentPlace.getY();

		boolean stopped = false;

		while (!stopped) {

			nextX = nextX + xDelta;
			nextY = nextY + yDelta;

			if (nextX > 11 || nextX < 0 || nextY > 11 || nextY < 0) {

				break;
			}

			nextPlace = board.getPlacez()[nextX][nextY];

			if (nextPlace != null && nextPlace != to) {

				placez.add(nextPlace);

				stopped = nextPlace.getPiece() != null;

			} else {

				stopped = true;
			}
		}

	}

	public boolean includes(Place to) {

		return placez.contains(to);
	}

}
