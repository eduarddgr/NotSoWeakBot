package org.bamboomy.c44.bot.player;

import lombok.Data;

@Data
public class RecursiveMateState {

	private int counter = 0;

	private boolean oneMate = false, result = true, mateFound = false;

}
