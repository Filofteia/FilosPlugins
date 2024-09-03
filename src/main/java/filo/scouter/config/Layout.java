package filo.scouter.config;

import lombok.Getter;
import net.runelite.client.party.PartyMember;

@Getter
public enum Layout
{
	L_3C2P(3, 2)
		{
			@Override
			public String toString()
			{
				return "3C2P";
			}
		},
	L_4C1P(4, 1)
		{
			@Override
			public String toString()
			{
				return "4C1P";
			}
		},
	L_4C2P(4, 2)
		{
			@Override
			public String toString()
			{
				return "4C2P";
			}
		};

	private final int maxCombat;
	private final int maxPuzzles;

	Layout(int combat, int puzzles)
	{
		this.maxCombat = combat;
		this.maxPuzzles = puzzles;
	}

	/**
	 * Return the accositated layout to your request
	 * @param combatQty The amount of Combat Rooms in the raid
	 * @param puzzleQty The amount of Puzzle Rooms in the raid
	 * @return That Layout or null
	 */
	public static Layout findLayout(int combatQty, int puzzleQty)
	{
		for (Layout layout : Layout.values())
		{
			if (layout.maxCombat == combatQty && layout.maxPuzzles == puzzleQty)
				return layout;
		}

		return null;
	}
}
