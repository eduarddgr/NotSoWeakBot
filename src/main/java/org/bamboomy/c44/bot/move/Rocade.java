
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.move;

import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.Piece;
import org.bamboomy.c44.bot.piecez.Tower;

import lombok.Getter;
import lombok.Setter;

public class Rocade extends Move {

	@Getter
	@Setter
	private Tower tower;

	private final Place towerToPlace;

	public Rocade(Place from, Place to, Piece piece, Tower tower, Place towerToPlace) {

		super(from, to, piece, "r");

		this.tower = tower;
		this.towerToPlace = towerToPlace;
	}
}