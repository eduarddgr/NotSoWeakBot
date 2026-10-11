
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.board.gui;

import org.bamboomy.c44.bot.piecez.Piece;
import org.bamboomy.c44.bot.player.Color;

import lombok.Getter;

@Getter
public class GuiPiece {

	private final String identifier;

	private final int color;

	private String md5 = "md5";

	private boolean pinned = false;

	private boolean moved = false;

	public GuiPiece(Piece piece, String color, boolean currentPlayer) {

		identifier = piece.getIdentifier();
		this.color = piece.getColor();

		this.pinned = piece.isPinned();

		moved = piece.getMoved().get(0);

		if (currentPlayer && Color.getBySeq(piece.getColor()).getName().equalsIgnoreCase(color)) {

			this.md5 = piece.getMd5();
		}
	}
}
