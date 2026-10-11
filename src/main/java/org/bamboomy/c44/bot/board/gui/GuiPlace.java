
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.board.gui;

import org.bamboomy.c44.bot.piecez.Piece;

import lombok.Getter;

@Getter
public class GuiPlace {

    private GuiPiece guiPiece = null;

    private int x, y;

    private String md5 = "md5";

    public GuiPlace(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public GuiPlace(Piece piece, String color,
                    boolean currentPlayer, int x, int y) {

        this(x, y);

        if (piece != null) {

            guiPiece = new GuiPiece(piece, color, currentPlayer);
        }
    }

    public GuiPlace(int x, int y, String md5) {

        this.x = x;
        this.y = y;

        this.md5 = md5;
    }
}
