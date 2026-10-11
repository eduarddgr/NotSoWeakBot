
/**

$$$License$$$

**/

package org.bamboomy.c44.bot.player;

import java.util.ArrayList;

import lombok.Getter;

public class Alliance {

	@Getter
	private Color one, two;

	public Alliance(Color one, Color two) {

		this.one = one;
		this.two = two;
	}

	public Color getOtherColor(Color color) {

		if (color.equals(one)) {

			return two;

		} else if (color.equals(two)) {

			return one;
		}

		throw new RuntimeException(color.getName() + " not part of this alliance!");
	}

	public boolean isInAlliance(Color color) {

		return color.equals(one) || color.equals(two);
	}

	public Alliance getOtherAlliance() {

		ArrayList<Color> otherColors = new ArrayList<Color>();

		for (Color color : Color.getAll()) {

			if (!isInAlliance(color)) {

				otherColors.add(color);
			}
		}

		return new Alliance(otherColors.get(0), otherColors.get(1));
	}

}
