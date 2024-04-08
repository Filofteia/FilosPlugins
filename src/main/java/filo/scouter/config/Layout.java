package filo.scouter.config;

import lombok.Getter;

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
	L_4c2P(4, 2)
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
}
