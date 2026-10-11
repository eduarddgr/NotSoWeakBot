
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.piecez;

import lombok.Getter;

@Getter
public enum PieceValue {

	KING(0, "king", "k", 1, King.class), PAWN(5, "pawn", "p", 1, Pawn.class),
	BISSHOP(3, "bisshop", "b", 3, Bisshop.class), TOWER(2, "tower", "t", 5, Tower.class),
	HORSE(4, "horse", "h", 3, Horse.class), QUEEN(1, "queen", "q", 10, Queen.class);

	private final int ordinal, value;
	private final String name, letter;
	private final Class pieceClass;

	private PieceValue(int ordinal, String name, String letter, int value, Class pieceClass) {
		this.ordinal = ordinal;
		this.name = name;
		this.letter = letter;
		this.value = value;
		this.pieceClass = pieceClass;
	}

	public static PieceValue getByName(String name) {

		for (PieceValue pv : PieceValue.values()) {

			if (pv.getName().equalsIgnoreCase(name)) {

				return pv;
			}
		}

		return null;
	}
}
